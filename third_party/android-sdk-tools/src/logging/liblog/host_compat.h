#pragma once

#if defined(__APPLE__)
#include <pthread.h>
#include <stdint.h>
#ifndef SOCK_CLOEXEC
#define SOCK_CLOEXEC 0
#endif
#ifndef SOCK_NONBLOCK
#define SOCK_NONBLOCK 0
#endif
static inline uint64_t android_host_gettid() { uint64_t tid = 0; pthread_threadid_np(nullptr, &tid); return tid; }
#else
#define android_host_gettid gettid
#endif
