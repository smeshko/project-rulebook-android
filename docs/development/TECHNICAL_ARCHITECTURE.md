# Technical Architecture Guide

> Comprehensive guide to the Rulebook Android app's technical architecture, patterns, and implementation details.

## Table of Contents
- [Architecture Overview](#architecture-overview)
- [Module Architecture](#module-architecture)
- [Data Flow](#data-flow)
- [Design Patterns](#design-patterns)
- [Dependency Management](#dependency-management)
- [Build System](#build-system)
- [Testing Strategy](#testing-strategy)
- [Code Organization](#code-organization)

## Architecture Overview

### Clean Architecture Principles

Rulebook follows **Clean Architecture** with strict dependency rules:

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (Compose UI, ViewModels, UiState)      │
└──────────────┬──────────────────────────┘
               │ depends on
┌──────────────▼──────────────────────────┐
│          Domain Layer                    │
│     (Models, Use Cases - Future)         │
└──────────────┬──────────────────────────┘
               │ depends on
┌──────────────▼──────────────────────────┐
│           Data Layer                     │
│  (Repositories, Data Sources, Network)   │
└──────────────────────────────────────────┘
```

**Key Principles:**
- **Unidirectional Data Flow** - Data flows down, events flow up
- **Single Source of Truth** - Room database is the source of truth
- **Offline-First** - App works without network connectivity
- **Separation of Concerns** - Each layer has a single responsibility

## Module Architecture

### Multi-Module Structure

The project uses 16 modules organized by feature and layer:

```
project-rulebook-android/
├── app                      # Application module
├── feature/                 # Feature modules (UI layer)
│   ├── library
│   ├── camera
│   ├── rules
│   ├── settings
│   ├── onboarding
│   └── purchase
├── core/                    # Core modules (shared infrastructure)
│   ├── designsystem         # UI components and theme
│   ├── data                 # Repository implementations
│   ├── database             # Room database
│   ├── network              # API client
│   ├── model                # Domain models
│   ├── common               # Utilities
│   ├── analytics            # Analytics tracking
│   ├── billing              # In-app purchases
│   └── datastore            # User preferences
└── build-logic/             # Build configuration
    └── convention/          # Convention plugins
```

### Module Dependencies

#### Dependency Rules
1. **Feature modules:**
   - Can depend on `core/*` modules
   - Cannot depend on other feature modules
   - Cannot depend on `app`

2. **Core modules:**
   - Can depend on other `core/*` modules
   - Cannot depend on `feature/*` modules
   - Cannot depend on `app`

3. **App module:**
   - Can depend on all `feature/*` modules
   - Can depend on all `core/*` modules
   - Aggregates navigation and DI setup

#### Dependency Graph

```
app
 ├─► feature:library ────┬─► core:designsystem
 ├─► feature:camera      │    ├─► core:model
 ├─► feature:rules       │    └─► core:common
 ├─► feature:settings    │
 ├─► feature:onboarding  ├─► core:data ────┬─► core:database
 └─► feature:purchase    │    ├─► core:network  └─► core:model
                         │    └─► core:model
                         │
                         ├─► core:analytics
                         ├─► core:billing
                         └─► core:datastore
```

### Module Templates

#### Feature Module Structure
```
feature/{feature-name}/
├── src/
│   ├── main/kotlin/com/rulebook/feature/{feature}/
│   │   ├── {Feature}Screen.kt          # Composable screen
│   │   ├── {Feature}ViewModel.kt       # State management
│   │   ├── {Feature}UiState.kt         # UI state data class
│   │   ├── navigation/
│   │   │   └── {Feature}Navigation.kt  # Navigation setup
│   │   └── components/                  # Feature-specific components
│   └── test/kotlin/                     # Unit tests
└── build.gradle.kts                     # Module build config
```

#### Core Module Structure
```
core/{core-name}/
├── src/
│   ├── main/kotlin/com/rulebook/core/{core}/
│   │   ├── model/                       # Data models
│   │   ├── repository/                  # Repositories (if data module)
│   │   ├── network/                     # Network services (if network module)
│   │   └── di/                          # Dependency injection
│   ├── test/kotlin/                     # Unit tests
│   └── androidTest/kotlin/              # Android instrumented tests
└── build.gradle.kts
```

## Data Flow

### MVI Pattern (Model-View-Intent)

Each feature follows the MVI pattern:

```
┌─────────────┐
│    View     │ ◄──── UiState
│  (Compose)  │
└──────┬──────┘
       │
       │ User Events
       ▼
┌──────────────┐
│  ViewModel   │ ──► Repository ──► Database/Network
│              │ ◄──
└──────────────┘
```

#### Example: Library Screen

```kotlin
// 1. UI State (core/model or feature module)
data class LibraryUiState(
    val games: List<Game> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val sortOption: SortOption = SortOption.RECENT
)

// 2. ViewModel (feature module)
class LibraryViewModel(
    private val gamesRepository: GamesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadGames()
    }

    // Intent: Load games
    fun loadGames() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            gamesRepository.getAllGames()
                .onSuccess { games ->
                    _uiState.update {
                        it.copy(games = games, isLoading = false)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(error = error.message, isLoading = false)
                    }
                }
        }
    }

    // Intent: Sort games
    fun sortGames(option: SortOption) {
        _uiState.update { it.copy(sortOption = option) }
        loadGames()
    }

    // Intent: Delete game
    fun deleteGame(gameId: String) {
        viewModelScope.launch {
            gamesRepository.deleteGame(gameId)
            loadGames()
        }
    }
}

// 3. Screen (feature module)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LibraryScreenContent(
        games = uiState.games,
        isLoading = uiState.isLoading,
        onGameClick = { /* navigate to rules */ },
        onDeleteGame = { viewModel.deleteGame(it) },
        onSortChange = { viewModel.sortGames(it) }
    )
}
```

### Data Layer Architecture

#### Repository Pattern

```
┌──────────────┐
│  ViewModel   │
└──────┬───────┘
       │
       ▼
┌──────────────────────┐
│    Repository        │ ◄─── Interface (core:data)
│  (core:data)         │      Implementation
└──────┬───────┬───────┘
       │       │
       ▼       ▼
   ┌────┐   ┌─────┐
   │ DAO│   │ API │
   └────┘   └─────┘
```

**Example: GamesRepository**

```kotlin
// Interface (core:data)
interface GamesRepository {
    suspend fun getAllGames(): Result<List<Game>>
    suspend fun getGameById(id: String): Result<Game>
    suspend fun saveGame(game: Game): Result<Unit>
    suspend fun deleteGame(id: String): Result<Unit>
}

// Implementation (core:data)
class OfflineFirstGamesRepository(
    private val gamesDao: GamesDao,
    private val networkDataSource: RulebookApiService
) : GamesRepository {

    override suspend fun getAllGames(): Result<List<Game>> {
        return try {
            // Offline-first: Read from database
            val games = gamesDao.getAllGames()
                .map { it.toExternalModel() }
            Result.success(games)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveGame(game: Game): Result<Unit> {
        return try {
            // Save to local database
            gamesDao.insertGame(game.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

#### Database Layer (Room)

**Location:** `core/database`

```kotlin
// Entity
@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val id: String,
    val name: String,
    val imageUrl: String?,
    val dateAdded: Long,
    val lastAccessed: Long
)

// DAO
@Dao
interface GamesDao {
    @Query("SELECT * FROM games ORDER BY lastAccessed DESC")
    fun getAllGames(): List<GameEntity>

    @Query("SELECT * FROM games WHERE id = :gameId")
    suspend fun getGameById(gameId: String): GameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity)

    @Delete
    suspend fun deleteGame(game: GameEntity)
}

// Database
@Database(
    entities = [GameEntity::class, RuleEntity::class],
    version = 1,
    exportSchema = true
)
abstract class RulebookDatabase : RoomDatabase() {
    abstract fun gamesDao(): GamesDao
    abstract fun rulesDao(): RulesDao
}
```

#### Network Layer (Retrofit)

**Location:** `core/network`

```kotlin
// API Service
interface RulebookApiService {
    @POST("identify")
    suspend fun identifyGame(
        @Body request: IdentifyGameRequest
    ): IdentifyGameResponse

    @POST("generate-rules")
    suspend fun generateRules(
        @Body request: GenerateRulesRequest
    ): GenerateRulesResponse
}

// Network Module (DI)
val networkModule = module {
    single {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .client(get())
            .build()
    }

    single { get<Retrofit>().create(RulebookApiService::class.java) }
}
```

## Design Patterns

### State Management

**StateFlow for UI State:**
```kotlin
class MyViewModel : ViewModel() {
    // Private mutable state
    private val _uiState = MutableStateFlow(MyUiState())

    // Public immutable state
    val uiState: StateFlow<MyUiState> = _uiState.asStateFlow()

    // Update state immutably
    fun updateState() {
        _uiState.update { currentState ->
            currentState.copy(isLoading = true)
        }
    }
}
```

### Navigation

**Type-Safe Navigation with Kotlin Serialization:**

```kotlin
// Navigation destinations (app module)
@Serializable
object LibraryRoute

@Serializable
data class RulesRoute(val gameId: String)

// Navigation setup (feature module)
fun NavGraphBuilder.libraryScreen(
    onGameClick: (String) -> Unit
) {
    composable<LibraryRoute> {
        LibraryScreen(
            onGameClick = onGameClick
        )
    }
}

// Navigation graph (app module)
NavHost(navController = navController, startDestination = LibraryRoute) {
    libraryScreen(
        onGameClick = { gameId ->
            navController.navigate(RulesRoute(gameId))
        }
    )
    rulesScreen()
}
```

### Dependency Injection (Koin)

**Module Organization:**

```kotlin
// Feature Module (feature:library)
val libraryModule = module {
    viewModel { LibraryViewModel(get()) }
}

// Data Module (core:data)
val dataModule = module {
    single<GamesRepository> {
        OfflineFirstGamesRepository(get(), get())
    }
}

// Database Module (core:database)
val databaseModule = module {
    single {
        Room.databaseBuilder(
            get(),
            RulebookDatabase::class.java,
            "rulebook-database"
        ).build()
    }

    single { get<RulebookDatabase>().gamesDao() }
}

// Application setup (app module)
class RulebookApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@RulebookApplication)
            modules(
                libraryModule,
                dataModule,
                databaseModule,
                networkModule
                // ... all modules
            )
        }
    }
}
```

## Dependency Management

### Version Catalog (`libs.versions.toml`)

```toml
[versions]
kotlin = "2.0.21"
compose-bom = "2024.11.00"
koin = "4.0.0"

[libraries]
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
koin-android = { group = "io.insert-koin", name = "koin-android", version.ref = "koin" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
```

**Usage in module:**
```kotlin
// build.gradle.kts
dependencies {
    implementation(libs.androidx.compose.ui)
    implementation(libs.koin.android)
}
```

### Convention Plugins

**Location:** `build-logic/convention/src/main/kotlin/`

Reusable build configuration:

```kotlin
// AndroidFeatureConventionPlugin.kt
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<LibraryExtension> {
                defaultConfig.targetSdk = 35
                configureKotlinAndroid(this)
            }

            dependencies {
                add("implementation", project(":core:designsystem"))
                add("implementation", project(":core:model"))
                // ... standard feature dependencies
            }
        }
    }
}
```

**Usage:**
```kotlin
// feature/library/build.gradle.kts
plugins {
    alias(libs.plugins.rulebook.android.feature)
}
```

## Build System

### Gradle Configuration

**Root `build.gradle.kts`:**
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
```

**Module `build.gradle.kts` Example:**
```kotlin
plugins {
    alias(libs.plugins.rulebook.android.feature)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.rulebook.feature.library"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(project(":core:data"))

    // BOM ensures version consistency
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
}
```

### Build Variants

```kotlin
android {
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

## Testing Strategy

### Testing Pyramid

```
        ┌──────────┐
        │    E2E   │ (Few) - Full app flows
        └──────────┘
      ┌──────────────┐
      │ Integration  │ (Some) - Module interactions
      └──────────────┘
    ┌──────────────────┐
    │   Unit Tests     │ (Many) - Logic, ViewModels, Repos
    └──────────────────┘
```

### Unit Tests

**ViewModel Test Example:**
```kotlin
@Test
fun `when loadGames succeeds, state contains games`() = runTest {
    // Given
    val fakeGames = listOf(Game("1", "Catan"))
    val repository = FakeGamesRepository(fakeGames)
    val viewModel = LibraryViewModel(repository)

    // When
    viewModel.loadGames()
    advanceUntilIdle()

    // Then
    val state = viewModel.uiState.value
    assertEquals(fakeGames, state.games)
    assertFalse(state.isLoading)
    assertNull(state.error)
}
```

### Android Instrumented Tests

**DAO Test Example:**
```kotlin
@RunWith(AndroidJUnit4::class)
class GamesDaoTest {
    private lateinit var database: RulebookDatabase
    private lateinit var gamesDao: GamesDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, RulebookDatabase::class.java)
            .build()
        gamesDao = database.gamesDao()
    }

    @Test
    fun insertGame_retrievesSameGame() = runTest {
        // Given
        val game = GameEntity("1", "Catan", null, 0L, 0L)

        // When
        gamesDao.insertGame(game)
        val retrieved = gamesDao.getGameById("1")

        // Then
        assertEquals(game, retrieved)
    }

    @After
    fun teardown() {
        database.close()
    }
}
```

### Test Doubles

**Fake Repository:**
```kotlin
class FakeGamesRepository : GamesRepository {
    private val games = mutableListOf<Game>()
    var shouldReturnError = false

    override suspend fun getAllGames(): Result<List<Game>> {
        return if (shouldReturnError) {
            Result.failure(Exception("Test error"))
        } else {
            Result.success(games.toList())
        }
    }

    override suspend fun saveGame(game: Game): Result<Unit> {
        games.add(game)
        return Result.success(Unit)
    }
}
```

## Code Organization

### Naming Conventions

| Component | Convention | Example |
|-----------|------------|---------|
| **Screen** | `{Feature}Screen.kt` | `LibraryScreen.kt` |
| **ViewModel** | `{Feature}ViewModel.kt` | `LibraryViewModel.kt` |
| **UI State** | `{Feature}UiState.kt` | `LibraryUiState.kt` |
| **Navigation** | `{Feature}Navigation.kt` | `LibraryNavigation.kt` |
| **Repository** | `{Domain}Repository.kt` | `GamesRepository.kt` |
| **DAO** | `{Entity}Dao.kt` | `GamesDao.kt` |
| **Entity** | `{Model}Entity.kt` | `GameEntity.kt` |
| **Network Model** | `{Model}NetworkModel.kt` | `GameNetworkModel.kt` |

### Package Structure

```
com.rulebook.feature.library/
├── LibraryScreen.kt
├── LibraryViewModel.kt
├── LibraryUiState.kt
├── navigation/
│   └── LibraryNavigation.kt
└── components/
    ├── GameCard.kt
    └── GameGrid.kt

com.rulebook.core.data/
├── repository/
│   ├── GamesRepository.kt
│   └── OfflineFirstGamesRepository.kt
├── model/
│   └── Game.kt
└── di/
    └── DataModule.kt
```

### File Organization

**Single Responsibility:**
- One screen per file
- One ViewModel per file
- Related UI state in same file as ViewModel or separate if complex

**Component Organization:**
- Stateless composables in components directory
- Stateful screen composables at feature root
- Preview composables at bottom of file

## Best Practices

### Compose Guidelines

1. **Stateless Composables:**
```kotlin
@Composable
fun GameCard(
    game: Game,
    onGameClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Pure UI, no state management
}
```

2. **State Hoisting:**
```kotlin
@Composable
fun LibraryScreen(viewModel: LibraryViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LibraryScreenContent(
        games = uiState.games,
        onGameClick = { /* handle */ }
    )
}

@Composable
private fun LibraryScreenContent(
    games: List<Game>,
    onGameClick: (String) -> Unit
) {
    // Stateless content
}
```

3. **Reusable Components:**
```kotlin
// core/designsystem
@Composable
fun RulebookButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(/* brutalist colors */)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
```

### Performance

1. **Remember expensive operations:**
```kotlin
@Composable
fun ExpensiveList(items: List<Item>) {
    val sortedItems = remember(items) {
        items.sortedBy { it.name }
    }
}
```

2. **Use derivedStateOf for computed values:**
```kotlin
val hasGames by remember {
    derivedStateOf { games.isNotEmpty() }
}
```

3. **Lazy loading:**
```kotlin
LazyColumn {
    items(games, key = { it.id }) { game ->
        GameCard(game = game)
    }
}
```

## Summary

This architecture provides:
- ✅ **Scalability** - Easy to add new features
- ✅ **Testability** - Clear boundaries for testing
- ✅ **Maintainability** - Consistent patterns throughout
- ✅ **Separation of Concerns** - Each module has a single responsibility
- ✅ **Offline-First** - Works without network
- ✅ **Type Safety** - Kotlin and Compose ensure compile-time safety

For more details, see:
- [Architecture Decision Document](../architecture.md)
- [PRD](../prd.md)
- [UX Design Specification](../ux-design-specification.md)
