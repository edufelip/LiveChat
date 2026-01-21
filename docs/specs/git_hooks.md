# Git Hooks

The project includes git hooks to maintain code quality and prevent broken builds from being committed.

## Hook Locations
The hooks are stored in the `.githooks/` directory and must be manually configured by the developer during [setup](setup.md).

## Available Hooks

### `pre-commit`
- **Actions**: Runs `./gradlew spotlessCheck`.
- **Purpose**: Ensures that all staged code adheres to the project's formatting and style guidelines (managed by Spotless).
- **Result**: If the code is not properly formatted, the commit will fail, and the developer should run `./gradlew spotlessApply`.

### `pre-push`
- **Actions**: Runs unit tests for shared modules.
- **Purpose**: Prevents pushing code that breaks existing business logic.
- **Result**: If any unit test fails, the push is aborted.

## Installation
To enable these hooks, run the following command from the project root:
```bash
chmod +x .githooks/*
git config core.hooksPath .githooks
```
