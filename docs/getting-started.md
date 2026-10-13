# Getting Started

## Including in your project

```kotlin
// Compose Multiplatform (Android + iOS)
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.veroanggraa:reactify:0.1.0-alpha01")
        }
    }
}
```

```kotlin
// Android only
dependencies {
    implementation("io.github.veroanggraa:reactify:0.1.0-alpha01")
}
```

Supported targets: `android`, `iosArm64`, `iosSimulatorArm64`, `iosX64`.

## Where to go next

- [Transfer](transfer.md) — the list-item-to-card gooey animation and the matching notch shape.
- [Bottom Navigation Indicator](nav-indicator.md)
- [Segmented Control](segmented-control.md)
- [Choosing a Renderer](renderers.md)
- [Sample App](sample-app.md)
