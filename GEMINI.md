# Supernova – Gemini CLI Context

## What this project is
Supernova is an open-source Android application written entirely in Kotlin whose goal is to provide a VS Code–like development environment on mobile devices for students who do not have access to laptops or PCs. It is not a demo, mock, or sandbox experiment; it is intended to run on real Android devices within Android’s security model.

## Core goals
- Professional-grade file editor with syntax highlighting
- Workspace-based project structure (folders, subfolders, multiple files)
- Real device storage access with user permission
- Integrated shell experience similar to VS Code’s terminal
- Ability to run or preview simple projects (e.g., HTML) without always opening the shell
- Advanced shell panel for package installation (Node, npm, git, etc.) where allowed
- Clean, modular, production-ready Kotlin architecture

## Technology constraints
- Kotlin only (no Flutter, no React Native)
- Modern Android APIs and Jetpack Compose
- Gradle Kotlin DSL
- Must respect Android sandbox and security restrictions
- No illegal cross-app service starts
- Termux integration only via allowed mechanisms (explicit intents, user action, or API)

## What Gemini CLI should do
- Generate real, compilable Kotlin code
- Modify existing files instead of rewriting everything blindly
- Keep architecture clean and explain assumptions in comments
- Prioritize correctness, stability, and clarity over visual polish
- Treat this as a long-term evolving IDE project, not a tutorial app

## What Gemini CLI should NOT do
- Do not assume root access
- Do not bypass Android security
- Do not use placeholder pseudo-code where real code is required
- Do not switch languages or frameworks

## Versioning
Current release: 0.1.0 (early alpha)

## Philosophy
This project exists to bridge the resource gap in tech education. AI is used as a productivity tool, but architectural decisions, goals, and validation are human-driven.
