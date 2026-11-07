FROM ubuntu:24.04

ARG DEBIAN_FRONTEND=noninteractive
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
       clang cmake ninja-build bison flex python3 ca-certificates git make openjdk-21-jdk \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
ENTRYPOINT ["bash"]
