---
title: Koin Module Template
description: Template for creating Koin dependency injection modules
author: Ivo
date: 2026-01-25
---

# Koin Module Template

## When to Use

- Creating DI configuration for a new feature module
- Need to register ViewModels, repositories, or services
- Want modular DI that can be composed
- Setting up dependency injection for any module

## Quick Reference

| Aspect | Value |
|--------|-------|
| Feature Location | `feature/{name}/src/main/kotlin/.../di/{Feature}Module.kt` |
| Core Location | `core/{name}/src/main/kotlin/.../di/{Name}Module.kt` |
| Pattern | Koin DSL module |
| Naming | `{Feature}Module.kt`, variable `{feature}Module` |

## Code Template - Feature Module

```kotlin
package com.rulebook.feature.{featurename}.di

import com.rulebook.feature.{featurename}.{Feature}ViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for {Feature} feature.
 *
 * Provides:
 * - {Feature}ViewModel
 */
val {feature}Module = module {
    viewModel { {Feature}ViewModel(get()) }
}
```

## Code Template - Core Module (Repository)

```kotlin
package com.rulebook.core.data.di

import com.rulebook.core.data.repository.{Entity}Repository
import com.rulebook.core.data.repository.{Entity}RepositoryImpl
import org.koin.dsl.module

/**
 * Koin module for data layer.
 *
 * Provides:
 * - Repository implementations
 */
val dataModule = module {
    // Repositories
    single<{Entity}Repository> { {Entity}RepositoryImpl(get()) }
}
```

## Code Template - Core Module (Database)

```kotlin
package com.rulebook.core.database.di

import androidx.room.Room
import com.rulebook.core.database.RulebookDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin module for database layer.
 *
 * Provides:
 * - RulebookDatabase instance
 * - All DAOs
 */
val databaseModule = module {
    // Database
    single {
        Room.databaseBuilder(
            androidContext(),
            RulebookDatabase::class.java,
            "rulebook.db"
        ).build()
    }

    // DAOs
    single { get<RulebookDatabase>().gameDao() }
    single { get<RulebookDatabase>().{entity}Dao() }
}
```

## Code Template - App Module (Aggregator)

```kotlin
package com.rulebook.app.di

import com.rulebook.core.analytics.di.analyticsModule
import com.rulebook.core.data.di.dataModule
import com.rulebook.core.database.di.databaseModule
import com.rulebook.core.network.di.networkModule
import com.rulebook.feature.camera.di.cameraModule
import com.rulebook.feature.library.di.libraryModule
import com.rulebook.feature.{featurename}.di.{feature}Module
import org.koin.dsl.module

/**
 * Aggregates all Koin modules for the application.
 */
val appModules = listOf(
    // Core modules
    databaseModule,
    networkModule,
    dataModule,
    analyticsModule,

    // Feature modules
    libraryModule,
    cameraModule,
    {feature}Module  // Add new feature module here
)
```

## Existing Patterns

Reference implementations in the codebase:
- [LibraryModule.kt](../../feature/library/src/main/kotlin/com/rulebook/feature/library/di/LibraryModule.kt)
- [DatabaseModule.kt](../../core/database/src/main/kotlin/com/rulebook/core/database/di/DatabaseModule.kt)

## Key Patterns

### Binding Types

```kotlin
module {
    // Singleton - one instance for app lifetime
    single { MyService() }

    // Singleton with interface binding
    single<MyInterface> { MyImplementation() }

    // Factory - new instance each time
    factory { MyFactory() }

    // ViewModel - scoped to ViewModelStore
    viewModel { MyViewModel(get()) }

    // ViewModel with SavedStateHandle
    viewModel { params -> MyViewModel(get(), params.get()) }
}
```

### Dependency Injection

```kotlin
// get() resolves dependency from Koin
viewModel { MyViewModel(get(), get(), get()) }

// Named dependencies
single(named("api")) { "https://api.example.com" }
single { ApiClient(get(named("api"))) }
```

### Scoped Modules

```kotlin
// For Activity-scoped dependencies
scope<MyActivity> {
    scoped { MyScopedService() }
}
```

## Integrations

1. **Create module file:** In feature's `di/` folder
2. **Add to aggregator:** In `app/di/AppModule.kt`
3. **Initialize in Application:**
   ```kotlin
   class RulebookApplication : Application() {
       override fun onCreate() {
           super.onCreate()
           startKoin {
               androidContext(this@RulebookApplication)
               modules(appModules)
           }
       }
   }
   ```

## Checklist

- [ ] Created `{Feature}Module.kt` in `di/` folder
- [ ] Defined `val {feature}Module = module { ... }`
- [ ] Used `viewModel { }` for ViewModels
- [ ] Used `single { }` for singletons
- [ ] Used `factory { }` for transient objects
- [ ] Used `get()` for dependency resolution
- [ ] Added module to `appModules` list in AppModule.kt
- [ ] KDoc comment describing what module provides

## References

- [viewmodel-template.md](viewmodel-template.md) - ViewModel to register
- [repository-template.md](repository-template.md) - Repository to register
