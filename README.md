# SimpleNotes

**SimpleNotes** (`com.shumail.simplenotes`) is a modern, local-first Markdown, `todo.txt`, CSV, and Plaintext note-taking studio for Android phones, foldables, and **16:10 tablets** — built from the ground up with **Kotlin** and **Jetpack Compose**.

---

## Key Features

- **Material You 3 + Fluent Design System**
  - Supports **Android 12+ Dynamic Wallpaper Accents (Material You)** out of the box.
  - Includes **6 curated architectural Fluent palettes** (*Obsidian Sage*, *Fluent Cobalt*, *Warm Terracotta*, *Nordic Frost*, *Amber Ink*, and *Rose Quartz*).
  - Supports **System**, **Light**, **Dark**, and **AMOLED Pitch-Black** themes.
- **16:10 Tablet & Foldable Multi-Pane Studio**
  - Adaptive canonical layout featuring a dedicated **Navigation Rail**, **Document Explorer List**, and **Side-by-Side Live Editor + Rendered Markdown Preview**.
- **First-Class Local Device File Saving**
  - **One-Tap Save to Device Documents**: Directly writes notes to `/storage/emulated/0/Documents/SimpleNotes/`.
  - **One-Tap Save to Downloads**: Exports notes directly to `/storage/emulated/0/Downloads/SimpleNotes/`.
  - **System File Picker (`Save File As...`)**: Choose any custom folder on your device or SD card.
  - **Standalone HTML Export**: Render and save any note as a self-contained `.html` document.
- **Multi-Format Plaintext Engine**
  - **Markdown (`.md`)** with live syntax highlighting, interactive checklists, GFM tables, fenced code blocks with copy button, and callout blocks (`[!TIP]`, `[!NOTE]`, `[!WARNING]`).
  - **`todo.txt` Task Suite** with `(A)`–`(C)` priority badges, `+project` and `@context` filtering, completion progress bar, and raw `.txt` sync.
  - **QuickNote Scratchpad (`QuickNote.md`)** for zero-friction capture with one-tap timestamps.
  - **CSV Data Tables (`.csv`)**, **Org-Mode (`.org`)**, **AsciiDoc (`.adoc`)**, and **Plain Text (`.txt`)**.

---

## Building the Debug APK

### Automatic GitHub Actions Build
Every push, pull request, or manual trigger on GitHub Actions runs `.github/workflows/build-android-project.yml` and publishes the installable **`SimpleNotes-debug-apk`** artifact (`app-debug.apk` / `SimpleNotes-debug.apk`).

### Local CLI Build
```bash
chmod +x gradlew scripts/build-debug-apk.sh
./scripts/build-debug-apk.sh
```
The compiled debug APK will be available at:
- `app/build/outputs/apk/debug/app-debug.apk`
- `dist/SimpleNotes-debug.apk`

---

## Tech Stack
- **Language**: 100% Kotlin
- **UI Framework**: Jetpack Compose + Material Design 3 (`androidx.compose.material3`)
- **Typography**: *Instrument Serif*, *Plus Jakarta Sans*, and *JetBrains Mono*
- **Package ID**: `com.shumail.simplenotes`
