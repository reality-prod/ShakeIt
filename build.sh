#!/usr/bin/env bash
set -euo pipefail

APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FOSS_SDK_TOOLS_DIR="${FOSS_SDK_TOOLS_DIR:-$APP_DIR/third_party/android-sdk-tools}"
ANDROID_SDK_DIR="${ANDROID_SDK_DIR:-}"
IMAGE="${SHAKEIT_BUILD_IMAGE:-shakeit-foss-build}"

if [[ ! -d "$FOSS_SDK_TOOLS_DIR" ]]; then
  echo "FOSS SDK-tools source not found: $FOSS_SDK_TOOLS_DIR" >&2
  echo "Set FOSS_SDK_TOOLS_DIR to the android-sdk-tools checkout." >&2
  exit 1
fi

if [[ -z "$ANDROID_SDK_DIR" ]]; then
  if [[ -f "$APP_DIR/local.properties" ]]; then
    ANDROID_SDK_DIR="$(sed -n 's/^sdk\.dir=//p' "$APP_DIR/local.properties" | head -n 1)"
  fi
fi

if [[ -z "$ANDROID_SDK_DIR" || ! -d "$ANDROID_SDK_DIR" ]]; then
  echo "Android SDK not found. Set ANDROID_SDK_DIR to an SDK containing" >&2
  echo "platforms/android-35 and build-tools/35.0.0." >&2
  exit 1
fi

# Colima/Docker Desktop may not share /opt or other host-native SDK paths into
# the Linux VM. Stage the installed platform metadata and Java tools under the
# shared project directory once, then mount that copy into the container.
SDK_STAGE="$APP_DIR/work/android-sdk"
if [[ ! -f "$SDK_STAGE/platforms/android-35/package.xml" ]]; then
  mkdir -p "$SDK_STAGE"
  cp -R "$ANDROID_SDK_DIR"/. "$SDK_STAGE"/
fi

case "$(uname -m)" in
  arm64|aarch64) SDK_ARCH=aarch64 ;;
  x86_64|amd64) SDK_ARCH=x86_64 ;;
  *) echo "Unsupported host architecture: $(uname -m)" >&2; exit 1 ;;
esac

echo "Building FOSS aapt2 image from $FOSS_SDK_TOOLS_DIR"
docker build \
  -f "$APP_DIR/Dockerfile" \
  -t "$IMAGE" \
  "$APP_DIR"

GRADLE_CACHE="${GRADLE_USER_HOME:-$HOME/.gradle}"
mkdir -p "$GRADLE_CACHE" "$APP_DIR/outputs"

LOCAL_SDK_PATH="/opt/android-sdk"
if [[ -f "$APP_DIR/local.properties" ]]; then
  LOCAL_SDK_PATH="$(sed -n 's/^sdk\.dir=//p' "$APP_DIR/local.properties" | head -n 1)"
fi

docker run --rm \
  -v "$APP_DIR:/app" \
  -v "$FOSS_SDK_TOOLS_DIR:/work" \
  -v "$GRADLE_CACHE:/root/.gradle" \
  -v "$SDK_STAGE:$LOCAL_SDK_PATH:ro" \
  -w /app \
  "$IMAGE" \
  -lc '
    set -euo pipefail
    python3 /work/build.py --toolchain host --arch "'"$SDK_ARCH"'" \
      --build "/work/build/host-static-'"$SDK_ARCH"'" --target aapt2 -- \
      -DHAVE_ARC4RANDOM_BUF=0 \
      -DHAVE_ARC4RANDOM=0 \
      -DXML_DEV_URANDOM=1
    export ANDROID_AAPT2_OVERRIDE="/work/build/host-static-'"$SDK_ARCH"'/bin/aapt2"
    ./gradlew clean lintDebug testDebugUnitTest assembleDebug \
      -Pandroid.aapt2FromMavenOverride="$ANDROID_AAPT2_OVERRIDE" \
      -Pandroid.builder.sdkDownload=false \
      --no-daemon
    cp app/build/outputs/apk/debug/app-debug.apk outputs/ShakeIt-debug-foss.apk
    sha256sum outputs/ShakeIt-debug-foss.apk > outputs/ShakeIt-debug-foss.apk.sha256
  '

echo
echo "Built: $APP_DIR/outputs/ShakeIt-debug-foss.apk"
cat "$APP_DIR/outputs/ShakeIt-debug-foss.apk.sha256"
