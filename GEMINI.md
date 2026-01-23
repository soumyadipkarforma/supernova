# Supernova - Standalone Android IDE Architecture

## Overview
Supernova is a self-contained IDE for Android that provides a full development environment without relying on external applications like Termux. It achieves this by embedding a minimal Linux root filesystem (bootstrap) directly into the APK (or downloading it on first launch) and executing binaries under its own UID.

## Architecture

### 1. Embedded Linux Environment
- **Core Utilities**: BusyBox (sh, ls, cp, mv, etc.) provides the foundational shell tools.
- **Package Management**: Termux's `apt` and `dpkg` are used to manage packages.
- **Path Structure**:
    - **ROOT**: `/data/data/com.supernova.app/files/usr` (The `PREFIX`)
    - **HOME**: `/storage/emulated/0/workspace` (Shared storage for easy access)
    - **TMP**: `/data/data/com.supernova.app/files/usr/tmp`

### 2. Execution Model
- **W^X Bypass**: The app targets **Android SDK 28 (Pie)**. This allows the execution of binaries located in the application's private data directory (`/data/data/com.supernova.app/files/...`), which is blocked in SDK 29+ due to W^X security policies.
- **Process Spawning**: Kotlin's `ProcessBuilder` is used to spawn `sh` or `bash` processes.
- **Environment Injection**:
    - `PATH`: Prepended with `/data/data/com.supernova.app/files/usr/bin`
    - `LD_LIBRARY_PATH`: Pointed to `/data/data/com.supernova.app/files/usr/lib`
    - `HOME`: Set to `/storage/emulated/0/workspace`

### 3. Compilation & Integration Steps

To recreate the `bootstrap.zip` used by Supernova:

1.  **Source Repositories**:
    - BusyBox: https://github.com/mirror/busybox
    - Termux Packages: https://github.com/termux/termux-packages

2.  **Cross-Compilation (x86_64 Host)**:
    - Use the Android NDK (r26b or later).
    - Build BusyBox for `aarch64-linux-android`:
      ```bash
      make defconfig
      make CROSS_COMPILE=aarch64-linux-android- install
      ```
    - Alternatively, download pre-built artifacts from the Termux bootstrap repository.

3.  **Bootstrap Creation**:
    - Create a directory structure: `usr/bin`, `usr/lib`, `usr/etc`.
    - Place compiled binaries in `usr/bin`.
    - Create a `symlinks.txt` script to link busybox applets (ls, cp, cat -> busybox).
    - Zip the contents: `zip -r bootstrap.zip usr/ symlinks.txt`.

4.  **Integration into Supernova**:
    - Place `bootstrap.zip` into `code/app/src/main/assets/`.
    - `BootstrapManager.kt` extracts this zip to `filesDir` on first launch.
    - `ShellSession.kt` sets the environment variables to point to this extracted location.

## Project Structure
- `core/shell/`: Handles process creation and I/O streaming.
- `core/fs/`: Manages the workspace filesystem.
- `feature/terminal/`: UI for the terminal emulator.
- `feature/editor/`: Code editor with syntax highlighting.
- `feature/browser/`: Embedded web view for localhost preview.

## Security & Permissions
- **Storage**: Requires `MANAGE_EXTERNAL_STORAGE` (Android 11+) or `WRITE_EXTERNAL_STORAGE` (Legacy) to access the workspace.
- **Internet**: Required for package downloads and local server binding.