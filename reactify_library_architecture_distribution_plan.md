# Reactify: Architecture & Distribution Plan

This document outlines the structural design, KMP refactoring strategy, and publication plan for Reactify, a cross-platform visual effects library bridging AGSL (Android Native) and SKSL (Kotlin Multiplatform).

## 1. Architectural Design

The architecture is designed to separate low-level graphics execution from UI bindings, ensuring a scalable and developer-friendly library.

### A. Multi-Module Structure

To maintain clean separation of concerns, the project will be divided into the following modules:

* **`reactify` (Core Module)**: The main library module containing the low-level abstraction layer, shader logic, and Jetpack Compose bindings/wrappers.

* **`app` (Sample App)**: An interactive "shader playground" application for demonstration and testing, featuring real-time sliders and color pickers.

### B. Directory Refactoring for KMM (Source Sets)

To transition the existing Android-only codebase into a Kotlin Multiplatform library, the `reactify` module's directory structure must be refactored from the standard `src/main/java` format into KMP source sets:

* **`src/commonMain/kotlin/`**:

  * Hosts all platform-agnostic logic.

  * Contains `expect` classes/functions (e.g., base shader renderers).

  * Contains state management (e.g., `GooeyTransferState.kt`).

  * Contains the Kotlin DSL API for Compose modifiers.

* **`src/androidMain/kotlin/`**:

  * Hosts Android-specific implementations.

  * Contains `actual` classes (e.g., `AgslGooeyRenderer.kt`) that utilize Android's native `RuntimeShader` and `RenderEffect`.

* **`src/iosMain/kotlin/` (and other KMP targets)**:

  * Hosts iOS-specific implementations.

  * Contains `actual` classes that utilize Skiko's `RuntimeEffect` to compile SKSL for iOS and Desktop.

### C. Cross-Platform Execution Abstraction

Because AGSL is specific to Android (API 33+), the library will utilize Kotlin's `expect/actual` mechanism to manage rendering engine differences seamlessly across the aforementioned source sets.

### D. Developer API Design (Kotlin DSL)

The library will expose a declarative, Compose-first API using a Kotlin DSL approach. This allows developers to seamlessly inject uniforms (such as `time`, `resolution`, and `color`) directly into standard Compose modifiers.

## 2. Distribution & Publication Strategy

### A. Single Dependency Strategy (Variant-Aware Resolution)

Reactify will be published as a **single** Kotlin Multiplatform library. Developers do not need to choose between an Android version and a KMP version. Thanks to Gradle's Variant-Aware Dependency Resolution:

* **KMM Developers** will automatically resolve and download the full multiplatform artifact.

* **Pure Android Native Developers** will automatically resolve and download *only* the Android `.aar` artifact.

* **App Size Impact**: Zero overhead for Android Native apps. iOS/Desktop binaries and dependencies are entirely ignored during an Android build.

### B. Phase 1: Early Iteration (JitPack)

* **Goal**: Fast iteration, community testing, and feedback (ideal for DevFest launch).

* **Setup**: Add a `jitpack.yml` file configured for KMP compatibility and publish via GitHub Release.

### C. Phase 2: Stable Release (Maven Central)

* **Goal**: Official, trusted distribution for production use.

* **Setup**:

  * **Namespace**: Utilize the free GitHub namespace (`io.github.yourusername`) via Sonatype.

  * **Tooling**: Implement the `vanniktech/gradle-maven-publish-plugin` to automate KMP publishing complexities, GPG signing, and POM synchronization.

### D. Phase 3: CI/CD Automation

* **GitHub Actions**:

  * Run builds and linting on every Pull Request.

  * Trigger deployment workflows via GitHub Tags to automatically publish artifacts to Maven Central.

## 3. Documentation

* Use **Dokka** to extract KDoc comments from the Kotlin DSL functions.

* Host the generated static site on **GitHub Pages** for easy, accessible API references.

* Provide the `app` module as a live, interactive UI catalog for users to experiment with modifiers.