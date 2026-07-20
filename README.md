# xmod

[![Build](https://github.com/Seao3oiio/xmod/actions/workflows/build.yml/badge.svg)](https://github.com/Seao3oiio/xmod/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A small personal Xposed module that groups narrowly scoped Android hooks in one
APK.

## Features

| Feature | Scope | Behavior |
| --- | --- | --- |
| GuitarTuna helpers | `com.ovelin.guitartuna`, `com.android.launcher` | Suppresses the recurring trial prompt. On version 7.96.1 and newer, also recovers the known repeatedly matched stuck splash after 15 seconds, reopens the app through the system launcher, and completes the clean Guitar 6-string setup (at most once per 24 hours). |
| Global Search to Google | `com.heytap.quicksearchbox` | Keeps local suggestions while typing, then opens the submitted query as a Google search in Chrome. |
| OEM browser to Chrome | `com.heytap.browser` | Redirects incoming `http`/`https` links from the ColorOS browser to Chrome; non-web links and failures stay in the OEM browser. |

Compatibility scopes for equivalent OPlus/Oppo package names are included but
the verified device is a PLZ110 on ColorOS 16. Global Search was verified with
`com.heytap.quicksearchbox` 11.59.5.20 and GuitarTuna with 7.96.1.
The Global Search submission hook is intentionally version-specific and fails
closed if the verified OEM search-bar method changes.
GuitarTuna recovery deliberately does not restore accounts, history, consent,
or purchase state. Android runtime permissions are kept.

## Build

Requires JDK 26 and Android SDK/Build Tools 37.

```sh
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Install

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Enable `xmod` in Vector/LSPosed and select only the packages for the features
you use. Keep Chrome as Android's default browser. Disable the old standalone
GuitarTuna and Global Search modules after enabling this combined package.

The application ID is `io.github.seao3oiio.xmod`.

## Adding a feature

Implement `XmodFeature`, register it in `XmodEntryPoint`, add its package to
`META-INF/xposed/scope.list`, and add focused tests for pure logic.

## License

MIT. Product names and trademarks belong to their respective owners.
