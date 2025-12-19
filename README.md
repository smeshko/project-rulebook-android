# Rulebook Android Application

Hello from Linear

## Overview

Rulebook is a modern Android application built using Kotlin and Jetpack Compose. The app provides a comprehensive platform for managing and accessing rules and regulations.

## Project Structure

This project follows a modular architecture with clear separation of concerns:

### Core Modules
- **analytics** - Analytics tracking and event management
- **billing** - In-app purchase and billing functionality
- **common** - Shared utilities and common code
- **data** - Data layer and repository implementations
- **database** - Local database management using Room
- **datastore** - Preferences and settings storage
- **designsystem** - UI components and design tokens
- **model** - Data models and domain entities
- **network** - Network layer and API clients

### Feature Modules
- **camera** - Camera functionality for capturing rules
- **library** - Rules library and management
- **onboarding** - User onboarding experience
- **purchase** - Purchase flow and premium features
- **rules** - Core rules viewing and management
- **settings** - App settings and preferences

### App Module
- Main application module that orchestrates all features

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Build System**: Gradle with Kotlin DSL
- **Architecture**: Multi-module clean architecture
- **Serialization**: Kotlin Serialization
- **Code Generation**: KSP (Kotlin Symbol Processing)

## Build Configuration

The project uses a custom build-logic module for convention plugins and shared build configuration.

## Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17 or later
- Android SDK with minimum API level as specified in gradle files

### Building the Project

```bash
./gradlew build
```

### Running Tests

```bash
./gradlew test
```

### Running the App

```bash
./gradlew installDebug
```

## Development

This project follows modern Android development best practices:
- Modular architecture for scalability
- Dependency injection ready
- Reactive programming patterns
- Material Design 3 components

## License

[License information to be added]
