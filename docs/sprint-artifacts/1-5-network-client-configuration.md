# Story 1.5: Network Client Configuration

Status: Ready for Review

## Linear Issue

- **ID:** RULE-112
- **URL:** https://linear.app/project-rulebook/issue/RULE-112/story-15-network-client-configuration

## Story

As a developer,
I want Retrofit and OkHttp configured for API communication,
So that the app can communicate with the backend.

## Acceptance Criteria

1. **Given** the `core/network` module
   **When** the network client is configured
   **Then** `RulebookApiClient.kt` provides Retrofit instance with:
   - Base URL configuration (from BuildConfig)
   - kotlinx.serialization converter
   - OkHttp client with logging interceptor (debug only)
   - 30-second timeout default

2. **And** API interface defines endpoints (placeholder for now):
   ```kotlin
   interface RulebookApi {
       @POST("analyze")
       suspend fun analyzeImage(@Body request: AnalyzeRequest): AnalyzeResponse

       @POST("generate")
       suspend fun generateRules(@Body request: GenerateRequest): GenerateResponse
   }
   ```

3. **And** request/response models use `@Serializable` annotation

4. **And** `@SerialName` maps to snake_case API fields

## Tasks / Subtasks

- [x] Task 1: Add network dependencies (AC: #1)
  - [x] Add retrofit to version catalog
  - [x] Add okhttp to version catalog
  - [x] Add okhttp-logging-interceptor
  - [x] Add kotlinx-serialization-json
  - [x] Add retrofit-kotlinx-serialization-converter
- [x] Task 2: Create OkHttp client (AC: #1)
  - [x] Create OkHttpClient builder
  - [x] Add logging interceptor (debug only)
  - [x] Configure 30-second connect timeout
  - [x] Configure 30-second read timeout
  - [x] Configure 30-second write timeout
- [x] Task 3: Create Retrofit instance (AC: #1)
  - [x] Create Retrofit builder
  - [x] Set base URL from BuildConfig
  - [x] Add kotlinx.serialization converter factory
  - [x] Set OkHttp client
- [x] Task 4: Create API interface (AC: #2)
  - [x] Create RulebookApi interface
  - [x] Add analyzeImage endpoint (placeholder)
  - [x] Add generateRules endpoint (placeholder)
- [x] Task 5: Create request/response models (AC: #3, #4)
  - [x] Create AnalyzeRequest with @Serializable
  - [x] Create AnalyzeResponse with @Serializable
  - [x] Create GenerateRequest with @Serializable
  - [x] Create GenerateResponse with @Serializable
  - [x] Use @SerialName for snake_case mapping
- [x] Task 6: Configure network security (AC: #1)
  - [x] Create network_security_config.xml
  - [x] Enforce HTTPS only
  - [x] Reference in AndroidManifest.xml
- [x] Task 7: Add Network to Koin DI
  - [x] Create provideOkHttpClient function
  - [x] Create provideRetrofit function
  - [x] Create provideRulebookApi function
  - [x] Register in NetworkModule

## Dev Notes

### Architecture Patterns

- Retrofit 2.11.0 + OkHttp 4.12.0
- kotlinx.serialization for JSON (no reflection)
- Network security config enforces HTTPS

### Implementation Pattern

```kotlin
@Serializable
data class AnalyzeRequest(
    @SerialName("image_data") val imageData: String,
    @SerialName("image_format") val imageFormat: String = "jpeg"
)

@Serializable
data class AnalyzeResponse(
    @SerialName("game_title") val gameTitle: String,
    val confidence: Float,
    @SerialName("thumbnail_url") val thumbnailUrl: String?
)

interface RulebookApi {
    @POST("analyze")
    suspend fun analyzeImage(@Body request: AnalyzeRequest): AnalyzeResponse

    @POST("generate")
    suspend fun generateRules(@Body request: GenerateRequest): GenerateResponse
}

// OkHttp client setup
val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .apply {
        if (BuildConfig.DEBUG) {
            addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }
    }
    .build()
```

### PRD Requirements Mapped

- NFR12: All API communication over HTTPS
- NFR16: Backend API timeout handling (30s default)

### Project Structure Notes

- API models are separate from domain models
- Mapper functions convert between API and domain models
- Logging only enabled in debug builds

### References

- [Source: docs/architecture.md#Network Architecture]
- [Source: docs/architecture.md#API Conventions]
- [Source: docs/prd.md#NFR12] - HTTPS only
- [Source: docs/prd.md#NFR16] - 30s timeout

## Dev Agent Record

### Context Reference
- Story file: docs/sprint-artifacts/1-5-network-client-configuration.md
- Architecture reference: docs/architecture.md#Network Architecture

### Agent Model Used
- Claude Opus 4.5

### Debug Log References
- Build verification blocked by Java 25 incompatibility with Gradle/AGP (environmental issue)

### Completion Notes List
- Task 1-7: All tasks completed in single implementation session
- Dependencies already existed in version catalog from Story 1.1
- Created RulebookApiClient.kt with OkHttp and Retrofit configuration
- Created API interface with analyze/generate endpoints per AC#2
- Created request/response models with @Serializable and @SerialName per AC#3-4
- Configured network security with HTTPS-only enforcement per NFR12
- Integrated all network components into Koin DI module
- Added comprehensive unit tests for models and API client configuration

### File List
- core/network/build.gradle.kts (modified - added buildConfig, BASE_URL, test deps)
- core/network/src/main/kotlin/com/rulebook/core/network/RulebookApiClient.kt (new)
- core/network/src/main/kotlin/com/rulebook/core/network/api/RulebookApi.kt (new)
- core/network/src/main/kotlin/com/rulebook/core/network/di/NetworkModule.kt (modified)
- core/network/src/main/kotlin/com/rulebook/core/network/model/AnalyzeRequest.kt (new)
- core/network/src/main/kotlin/com/rulebook/core/network/model/AnalyzeResponse.kt (new)
- core/network/src/main/kotlin/com/rulebook/core/network/model/GenerateRequest.kt (new)
- core/network/src/main/kotlin/com/rulebook/core/network/model/GenerateResponse.kt (new)
- core/network/src/test/kotlin/com/rulebook/core/network/RulebookApiClientTest.kt (new)
- core/network/src/test/kotlin/com/rulebook/core/network/model/NetworkModelsTest.kt (new)
- app/src/main/res/xml/network_security_config.xml (new)
- app/src/main/AndroidManifest.xml (modified - added networkSecurityConfig)

## Dependencies

- **Depends On:** Story 1.1
- **Blocks:** None
- **Can Parallel With:** Story 1.2, Story 1.3, Story 1.4, Story 1.6, Story 1.7, Story 1.12, Story 1.13

### Dependency Rationale
- Story 1.1: Network client requires network module and version catalog to exist
