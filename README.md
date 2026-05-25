# PDF Toolkit

Privacy-first Android app for everyday PDF tasks. Merge, split, sign, and convert documents entirely on device — your files never leave your phone or tablet.

**Package:** `com.michael.pdftoolkit`

## Features

- **Home hub** — Recent documents, search, and quick access to every tool
- **Merge PDFs** — Queue multiple files, reorder them, and combine into one document
- **Split PDFs** — Extract custom page ranges into separate PDFs
- **Sign PDFs** — Draw vector signatures, adjust stroke color and width, save reusable signatures, and stamp them onto selected pages
- **Markdown to PDF** — Write or paste Markdown and generate a formatted PDF locally
- **Share & save** — Export completed documents from the app

All processing runs offline. PDF Toolkit does not upload your documents to external servers.

## Tech stack

- Kotlin · Jetpack Compose · Material 3
- Navigation Compose · ViewModel · Room
- PDFBox Android for PDF operations
- Roborazzi for Play Store screenshot generation

## Requirements

- [Android Studio](https://developer.android.com/studio) (latest stable)
- JDK 17
- Android SDK 36 (compile/target), min SDK 24

## Getting started

1. Clone the repository and open the project root in Android Studio.
2. Let Gradle sync finish.
3. Run the **app** configuration on an emulator or physical device.

Debug builds use the bundled `debug.keystore`. No API keys or cloud setup are required for local development.

## Release build

Release signing reads `key.properties` at the project root when present:

```properties
storeFile=my-upload-key.jks
storePassword=...
keyPassword=...
keyAlias=upload
```

Place the keystore file beside `key.properties`. These files are gitignored — store release keys in your secure key archive, not in this repository.

Build a signed App Bundle:

```bash
export JAVA_HOME="$(/usr/libexec/java_home -v 17)"
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

If `key.properties` is missing, release signing falls back to `KEYSTORE_PATH`, `STORE_PASSWORD`, and `KEY_PASSWORD` environment variables.

## Play Store assets

Store graphics and listing copy live in [`play-store/`](play-store/). Regenerate screenshots and icons from the project root:

```bash
./gradlew generatePlayStoreAssets
```

See [`play-store/README.md`](play-store/README.md) for asset sizes and verification steps. Listing text is in [`play-store/listing-descriptions.md`](play-store/listing-descriptions.md).

## Project layout

```
├── app/                 Android application module
├── gradle/              Version catalog and wrapper
├── play-store/          Play Console graphics and listing copy
├── build.gradle.kts     Root Gradle config
└── settings.gradle.kts
```

## License

See repository license file if present.
