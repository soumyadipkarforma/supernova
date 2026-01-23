# Supernova: Mobile Coding environment

Supernova is an open-source Android IDE designed for students and developers who don't have access to a traditional PC. It provides a native UI for file management, code editing, and web testing, while offloading all execution to the powerful **Termux** environment.

## Features

- **Termux Integration**: Uses the Terminal as the backend for runtime execution (Python, Node, Bash, C++, etc.).
- **Workspace Manager**: Create, move, and edit files in a dedicated app workspace.
- **Code Editor**: Lightweight editor with syntax highlighting for multiple languages.
- **Project Execution**: Run scripts directly from the editor into a Termux terminal window.
- **In-App Browser**: Test web apps running on localhost (127.0.0.1) with an integrated port scanner.

## Enhanced File Manager Features

The file manager has been enhanced with the following features:

### Floating Action Button (+)
- A floating plus button is always visible on the file manager screen
- When clicked, it shows different options depending on the current location:
  - In the main workspace: Create Project, Create File, Import File, Import Folder
  - Inside a project: Create Folder, Create File, Import File, Import Folder

### Context Menu
- When a file or folder is selected, a context menu appears with options:
  - Rename: Allows renaming the selected file or folder
  - Compress: Creates a ZIP archive of the selected item
  - Delete: Removes the selected item permanently

### Top Menu
- A menu button on the top-left corner provides access to:
  - Settings
  - Theme control (Light, Dark, System)
  - Storage information
  - About information

### Enhanced File Operations
- The file manager now supports full navigation through the file system
- Files and folders are displayed with appropriate icons
- Selected items are highlighted for better UX

## Prerequisites

1. **Termux**: Install it from [GitHub Releases](https://github.com/termux/termux-app/releases) or F-Droid.
2. **Setup Termux**:
    Open Termux and run:
    
