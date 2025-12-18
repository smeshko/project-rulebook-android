# Story 4.9: Camera Permission Handling

Status: ready-for-dev

## Story

As a user,
I want to be asked for camera permission only when I try to scan,
so that the app doesn't request unnecessary permissions at install.

## Acceptance Criteria

1. **Given** the user taps the camera FAB (FR51)
   **When** camera permission is not granted
   **Then** a permission rationale is shown explaining why camera is needed

2. **Given** the permission rationale is shown
   **When** the user proceeds
   **Then** the system permission dialog appears

3. **Given** the system permission dialog appears
   **When** the user grants permission
   **Then** camera opens immediately

4. **Given** the system permission dialog appears
   **When** the user denies permission
   **Then** helpful message with settings link appears (FR52)

5. **Given** the app launches
   **When** checking for camera permission
   **Then** permission is not requested at app launch or install

## Tasks / Subtasks

- [x] Task 1: Add Permission State to CameraUiState (AC: #1, #2, #3, #4)
  - [x] Add `permissionState: PermissionState` enum to CameraUiState
  - [x] States: GRANTED, DENIED, SHOULD_SHOW_RATIONALE, PERMANENTLY_DENIED
  - [x] Create action to check current permission state

- [ ] Task 2: Check Permission on Camera Screen Launch (AC: #1, #5)
  - [ ] Check permission state in ViewModel init or LaunchedEffect
  - [ ] Only request when user navigates to camera (not at app start)
  - [ ] Handle different permission states appropriately

- [ ] Task 3: Create PermissionRationale Composable (AC: #1)
  - [ ] Create `PermissionRationale.kt` in `feature/camera/components/`
  - [ ] Explain why camera is needed ("Scan game boxes")
  - [ ] Include "Continue" button to request permission
  - [ ] Apply brutalist styling

- [ ] Task 4: Implement Permission Request (AC: #2, #3)
  - [ ] Use `rememberPermissionState` from Accompanist OR manual approach
  - [ ] Request `Manifest.permission.CAMERA`
  - [ ] Handle grant result to show camera

- [ ] Task 5: Handle Permission Denial (AC: #4)
  - [ ] Detect when permission is denied
  - [ ] Show helpful error message
  - [ ] Include button to open app settings
  - [ ] Distinguish between "Don't ask again" and regular denial

- [ ] Task 6: Create PermissionDenied Composable (AC: #4)
  - [ ] Create `PermissionDenied.kt` in `feature/camera/components/`
  - [ ] Explain permission is required
  - [ ] Provide "Open Settings" button
  - [ ] Provide "Use Gallery" alternative

- [ ] Task 7: Implement Settings Deep Link (AC: #4)
  - [ ] Use `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`
  - [ ] Create intent with app package URI
  - [ ] Launch settings on button tap

- [ ] Task 8: Handle Permission Return from Settings (AC: #3)
  - [ ] Re-check permission when returning from settings
  - [ ] Auto-open camera if permission now granted
  - [ ] Keep showing denied state if still denied

## Dev Notes

### Permission State Enum
```kotlin
enum class CameraPermissionState {
    NOT_DETERMINED,  // Haven't checked yet
    GRANTED,         // Permission granted
    DENIED,          // User denied but can ask again
    PERMANENTLY_DENIED  // User selected "Don't ask again"
}
```

### Manual Permission Handling (Recommended)
```kotlin
@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit,
    viewModel: CameraViewModel = koinViewModel()
) {
    val context = LocalContext.current

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Check permission on first composition
    LaunchedEffect(Unit) {
        val permission = Manifest.permission.CAMERA
        when {
            ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED -> {
                viewModel.onPermissionGranted()
            }
            // shouldShowRequestPermissionRationale check happens in content
            else -> {
                viewModel.onPermissionNotGranted()
            }
        }
    }

    when (uiState.permissionState) {
        CameraPermissionState.GRANTED -> {
            CameraContent(/* ... */)
        }
        CameraPermissionState.NOT_DETERMINED,
        CameraPermissionState.DENIED -> {
            PermissionRationale(
                onRequestPermission = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                onNavigateBack = onNavigateBack
            )
        }
        CameraPermissionState.PERMANENTLY_DENIED -> {
            PermissionDeniedContent(
                onOpenSettings = {
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                    context.startActivity(intent)
                },
                onUseGallery = { /* Launch gallery picker */ },
                onNavigateBack = onNavigateBack
            )
        }
    }
}
```

### Permission Rationale Composable
```kotlin
@Composable
fun PermissionRationale(
    onRequestPermission: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = RulebookTheme.colors.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Camera Permission Needed",
            style = RulebookTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Rulebook needs camera access to photograph game boxes " +
                   "and identify them for you.",
            style = RulebookTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = RulebookTheme.colors.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        RulebookButton(
            text = "Allow Camera Access",
            onClick = onRequestPermission
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateBack) {
            Text("Not Now")
        }
    }
}
```

### Permission Denied Composable
```kotlin
@Composable
fun PermissionDeniedContent(
    onOpenSettings: () -> Unit,
    onUseGallery: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = RulebookTheme.colors.error
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Camera Permission Required",
            style = RulebookTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "To scan game boxes, please enable camera permission in Settings. " +
                   "Alternatively, you can select a photo from your gallery.",
            style = RulebookTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = RulebookTheme.colors.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        RulebookButton(
            text = "Open Settings",
            onClick = onOpenSettings
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(onClick = onUseGallery) {
            Text("Use Gallery Instead")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateBack) {
            Text("Go Back")
        }
    }
}
```

### Project Structure Notes

- Permission rationale: `feature/camera/components/PermissionRationale.kt`
- Permission denied: `feature/camera/components/PermissionDenied.kt`
- Integration: Update `CameraScreen.kt` and `CameraViewModel.kt`

### References

- [Source: docs/architecture.md#Permissions] - Runtime permissions at point of use
- [Source: docs/prd.md#FR51] - System requests camera permission at point of use
- [Source: docs/prd.md#FR52] - Users can navigate to system settings to grant permissions
- [Source: docs/epics/epic-4-photo-capture-flow.md#Story 4.9] - Full story definition

### Testing Requirements

- Test rationale shows when permission not granted
- Test system dialog appears after rationale
- Test camera opens on permission grant
- Test settings link opens app settings
- Test permission NOT requested at app launch

### Dependencies

- **Prerequisites:** Story 4.1 (camera screen structure)
- **Related:** Story 4.6 (gallery as alternative to camera)
- **Parallel with:** Stories 4.2, 4.3, 4.4, 4.5, 4.6, 4.8, 4.10

## Dev Agent Record

### Context Reference

<!-- Path(s) to story context XML will be added here by context workflow -->

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

### Completion Notes List

- Task 1: Added CameraPermissionState enum with NOT_DETERMINED, GRANTED, DENIED, PERMANENTLY_DENIED states. Added permissionState field to CameraUiState. Added onPermissionGranted(), onPermissionDenied(), and onPermissionPermanentlyDenied() methods to CameraViewModel. All 5 new unit tests pass.

### File List

- feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraUiState.kt (modified)
- feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraViewModel.kt (modified)
- feature/camera/src/test/kotlin/com/rulebook/feature/camera/CameraViewModelTest.kt (modified)

## Epic Dependencies

- **Depends On:** Story 4.1
- **Blocks:** None
- **Can Parallel With:** Story 4.2, Story 4.3, Story 4.4, Story 4.5, Story 4.6, Story 4.8, Story 4.10

### Dependency Rationale
- Story 4.1: Requires camera screen structure for permission handling flow
