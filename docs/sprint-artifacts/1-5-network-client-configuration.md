# Story 1.5: Network Client Configuration

Status: ready-for-dev

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

- [ ] Task 1: Add network dependencies (AC: #1)
  - [ ] Add retrofit to version catalog
  - [ ] Add okhttp to version catalog
  - [ ] Add okhttp-logging-interceptor
  - [ ] Add kotlinx-serialization-json
  - [ ] Add retrofit-kotlinx-serialization-converter
- [ ] Task 2: Create OkHttp client (AC: #1)
  - [ ] Create OkHttpClient builder
  - [ ] Add logging interceptor (debug only)
  - [ ] Configure 30-second connect timeout
  - [ ] Configure 30-second read timeout
  - [ ] Configure 30-second write timeout
- [ ] Task 3: Create Retrofit instance (AC: #1)
  - [ ] Create Retrofit builder
  - [ ] Set base URL from BuildConfig
  - [ ] Add kotlinx.serialization converter factory
  - [ ] Set OkHttp client
- [ ] Task 4: Create API interface (AC: #2)
  - [ ] Create RulebookApi interface
  - [ ] Add analyzeImage endpoint (placeholder)
  - [ ] Add generateRules endpoint (placeholder)
- [ ] Task 5: Create request/response models (AC: #3, #4)
  - [ ] Create AnalyzeRequest with @Serializable
  - [ ] Create AnalyzeResponse with @Serializable
  - [ ] Create GenerateRequest with @Serializable
  - [ ] Create GenerateResponse with @Serializable
  - [ ] Use @SerialName for snake_case mapping
- [ ] Task 6: Configure network security (AC: #1)
  - [ ] Create network_security_config.xml
  - [ ] Enforce HTTPS only
  - [ ] Reference in AndroidManifest.xml
- [ ] Task 7: Add Network to Koin DI
  - [ ] Create provideOkHttpClient function
  - [ ] Create provideRetrofit function
  - [ ] Create provideRulebookApi function
  - [ ] Register in NetworkModule

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

### Agent Model Used

### Debug Log References

### Completion Notes List

### File List

## Dependencies

- **Depends On:** Story 1.1
- **Blocks:** None
- **Can Parallel With:** Story 1.2, Story 1.3, Story 1.4, Story 1.6, Story 1.7, Story 1.12, Story 1.13

### Dependency Rationale
- Story 1.1: Network client requires network module and version catalog to exist
