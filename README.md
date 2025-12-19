# Rulebook - Android

> From box to playing in 60 seconds

An AI-powered board game companion app that instantly generates digestible game rules from box photos, eliminating the friction of learning new board games.

## 🎯 Project Overview

**Rulebook** is a native Android application that helps board game enthusiasts quickly learn and play new games. Point your camera at any board game box, and the app uses AI to identify the game and generate structured, easy-to-follow rules in a progressive disclosure format.

### Key Features

- **AI-Powered Game Recognition** - Identify games instantly from box photos
- **Smart Rules Generation** - Structured rules with progressive disclosure (Overview → Setup → First Round → Advanced)
- **Offline-First Library** - Save and access game rules without internet
- **Credit-Based System** - Free credits on onboarding + in-app purchases
- **Brutalist Design** - Distinctive, bold aesthetic that stands out

### Target Users

Board game collectors and enthusiasts who:
- Have unplayed games gathering dust
- Want to quickly learn new games at game nights
- Need quick rule refreshers for infrequently played games

## 🏗️ Architecture

This project follows **multi-module Clean Architecture** based on Google's "Now in Android" reference architecture.

### Architecture Pattern
- **MVI (Model-View-Intent)** for state management
- **Offline-first** with Room database
- **Feature-scoped modules** with strict dependency boundaries
- **Unidirectional data flow**

### Module Structure (16 Modules)

```
project-rulebook-android/
├── app/                         # Application entry point
├── feature/
│   ├── library/                 # Game library grid view
│   ├── camera/                  # Photo capture & gallery picker
│   ├── rules/                   # AI rules generation & display
│   ├── settings/                # User preferences
│   ├── onboarding/              # First-time user experience
│   └── purchase/                # Credit purchase system
├── core/
│   ├── designsystem/            # Brutalist design theme & components
│   ├── data/                    # Repository implementations
│   ├── database/                # Room database (games, rules)
│   ├── network/                 # Retrofit API client
│   ├── model/                   # Domain models (pure Kotlin)
│   ├── common/                  # Shared utilities
│   ├── analytics/               # TelemetryDeck integration
│   ├── billing/                 # Google Play Billing
│   └── datastore/               # User preferences storage
└── build-logic/                 # Convention plugins
```

## 🛠️ Technology Stack

| Category | Technology | Version |
|----------|------------|---------|
| **Language** | Kotlin | 2.0.21 |
| **UI Framework** | Jetpack Compose | BOM 2024.11.00 |
| **Build Tool** | Gradle (Kotlin DSL) | AGP 8.7.2 |
| **DI Framework** | Koin | 4.0.0 |
| **Database** | Room | 2.6.1 |
| **HTTP Client** | Retrofit + OkHttp | 2.11.0 / 4.12.0 |
| **Serialization** | kotlinx.serialization | 1.7.3 |
| **Image Loading** | Coil | 3.0.3 |
| **Navigation** | Compose Navigation | 2.8.4 |
| **Camera** | CameraX | 1.4.1 |
| **In-App Billing** | Play Billing | 7.1.1 |
| **Analytics** | TelemetryDeck | 6.3.0 |

### Requirements
- **Min SDK:** API 34 (Android 14)
- **Target SDK:** API 35 (Android 15)
- **Compile SDK:** API 35
- **JDK:** 17+

## 🚀 Getting Started

### Prerequisites

```bash
# Required
- Android Studio Ladybug | 2024.2.1 or later
- JDK 17 or later
- Android SDK with API 35

# Recommended
- Kotlin 2.0.21+
- Gradle 8.7.2+
```

### Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd project-rulebook-android
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. **Sync Gradle**
   - Android Studio should automatically sync Gradle
   - Or manually: File → Sync Project with Gradle Files

4. **Configure API Keys** (if applicable)
   ```bash
   # Create local.properties in project root
   echo "API_KEY=your_api_key_here" >> local.properties
   ```

5. **Build and Run**
   ```bash
   ./gradlew :app:assembleDebug
   # Or use Android Studio's Run button
   ```

## 📂 Project Documentation

Comprehensive documentation is available in the `docs/` directory:

- **[PRD (Product Requirements)](docs/prd.md)** - 52 functional requirements, user journeys, success criteria
- **[Architecture Decision Document](docs/architecture.md)** - Technology stack, module structure, implementation patterns
- **[UX Design Specification](docs/ux-design-specification.md)** - Brutalist design system, component library, screen layouts
- **[Epic & Story Breakdown](docs/epics.md)** - Implementation-ready user stories with acceptance criteria
- **[iOS Migration Reference](docs/ios/)** - iOS source reference for feature parity

### Documentation Structure
```
docs/
├── analysis/          # Codebase analysis and findings
├── architecture/      # Architecture documents
├── design/            # Design documents
├── development/       # Development guides
├── documentation/     # External documentation references
├── planning/          # Roadmaps, work phases, task lists
├── product/          # PRDs, requirements
└── testing/          # Testing strategies
```

## 🧪 Testing

### Running Tests

```bash
# Unit tests
./gradlew test

# Android instrumented tests
./gradlew connectedAndroidTest

# Specific module tests
./gradlew :core:database:test
./gradlew :feature:library:test

# Test coverage report
./gradlew jacocoTestReport
```

### Test Structure
- **Unit Tests:** `src/test/kotlin/` - ViewModel, repository, mapper tests
- **Instrumented Tests:** `src/androidTest/kotlin/` - DAO, component, navigation tests
- **Test Coverage:** Comprehensive testing across all modules

## 🎨 Design System

The app uses a distinctive **Brutalist design aesthetic**:

- **Typography:** Brutal Type family (ExtraBold, Bold, Medium, Regular, Thin)
- **Colors:** High-contrast with distinctive primary/secondary palette
- **Components:** Thick borders, bold shadows, strong visual hierarchy
- **Theme Support:** Light, Dark, and System modes

Key components:
- `RulebookButton` - Brutalist-styled buttons with thick borders
- `RulebookCard` - Cards with prominent shadows
- `RulebookTextField` - Input fields with bold styling
- `TopAppBar` - Custom app bars with brutalist aesthetic

## 🔄 Development Workflow

### Git Workflow

```
main          ← Production-ready code
  ↑
staging       ← Integration branch
  ↑
feature/*     ← Feature development branches
```

### Branch Naming
- `feature/` - New features
- `story/` - Story implementation
- `refactoring/` - Code refactoring
- `documentation/` - Documentation updates
- `hotfix/` - Critical bug fixes

### Commit Standards

Follow conventional commit format:
```
type(scope): description

Examples:
feat(camera): add zoom controls
fix(library): resolve grid layout crash
docs(readme): update setup instructions
test(rules): add ViewModel unit tests
```

### Pull Request Process

1. Create feature branch from `staging`
2. Implement changes with tests
3. Ensure build passes: `./gradlew build`
4. Create PR to `staging` (NOT main)
5. Include comprehensive description:
   - Work completed
   - Architectural decisions
   - Testing performed
   - Documentation updates
6. Request code review
7. Wait for approval before merging

## 📊 Project Status

### Current Phase
🔄 **Phase 4: Implementation** - Systematic story-by-story development

### Completed Stories
- ✅ Story 1.1: Project Initialization
- ✅ Story 4.9: Camera Permission Handling
- ✅ Story 4.10: Camera Close/Back Navigation
- ✅ Settings Feature
- ✅ Library Foundation
- ✅ Onboarding Screens

### In Progress
- 🔄 Rules Generation Pipeline
- 🔄 Purchase/Billing Integration
- 🔄 AI Integration

### Metrics
- **Lines of Kotlin:** ~15,539
- **Test Coverage:** Comprehensive unit tests across modules
- **Modules:** 16 feature + core modules
- **Build Time:** ~30-45s (clean build)

## 🤝 Contributing

### Code Standards

- Follow Kotlin coding conventions
- Use KSP (not KAPT) for annotation processing
- Maintain single responsibility principle
- Write tests for new functionality
- Keep functions small and focused

### Before Committing

```bash
# Run checks
./gradlew check
./gradlew test
./gradlew detekt

# Ensure build succeeds
./gradlew build
```

### Code Review Checklist
- [ ] All tests pass
- [ ] Code follows project conventions
- [ ] Documentation updated
- [ ] No security vulnerabilities
- [ ] Performance considered
- [ ] Accessibility verified

## 📱 App Features

### Onboarding & Credits (FR1-4)
- 2-screen introduction with skip option
- 3 free credits awarded on completion
- Onboarding state persistence

### Photo Capture (FR5-11)
- Camera capture with flash, zoom, tap-to-focus
- Gallery picker integration
- Image compression/optimization
- Credit balance display

### Game Recognition (FR12-17)
- AI-powered game identification
- Confidence scoring display
- Manual game name entry fallback
- Multi-model AI fallback

### Rules Generation (FR18-26)
- Structured rules with progressive disclosure
- Checklist-style setup instructions
- Expandable/collapsible sections
- Share functionality

### Library Management (FR27-33)
- Grid layout with saved games
- Sort options (recent, alphabetical, date)
- Delete with confirmation
- Offline access to saved rules

### Credit System (FR34-41)
- Credit tracking and consumption
- Purchase packs (1, 3, 10 credits)
- Paywall display
- Google Play Billing integration

### Settings (FR42-47)
- Theme switching (light/dark/system)
- Haptic feedback toggle
- Support links
- Data clearing with confirmation

## 🐛 Known Issues

Track issues in the project's issue tracker. Key areas:
- AI integration pending completion
- Purchase flow in development
- End-to-end testing in progress

## 📄 License

[License information to be added]

## 📞 Support

- **Documentation:** See `docs/` directory
- **Issues:** [GitHub Issues](link-to-issues)
- **Contact:** [Contact information]

## 🙏 Acknowledgments

- Architecture inspired by Google's "Now in Android" reference
- iOS version provides feature parity guidance
- Built with modern Android development best practices

---

**Built with ❤️ for board game enthusiasts**

*Version: 1.0.0 (In Development)*
