# RULE-218: Manual Test Plan
## Story 7.3: Navigate to Rules from Library

**Test Date:** 2026-02-11
**Build:** feature/rule-218 (commit: 431e693)

---

## Test Environment

- **Device:** Android Emulator / Physical Device
- **Android Version:** API 24+ (minimum) / API 34+ (for predictive back)
- **Build Type:** Debug APK (`:app:assembleDebug`)

---

## Manual Test Cases

### Test 1: Tap Game Card → Rules Screen Opens with Slide Animation

**Precondition:** Library screen shows at least one game card

**Steps:**
1. Launch app and navigate to Library tab
2. Observe game cards displayed in 2-column grid
3. Tap on any game card (e.g., "Catan")
4. Observe transition animation

**Expected Result:**
- Rules screen slides in from right edge over 300ms
- Library screen fades out while Rules slides in
- Rules screen displays correct game title in header
- Rules content loads instantly (no loading spinner - data is from Room)
- Smooth, fluid animation with no jank

**Verification Status:** ⏳ Pending manual test

---

### Test 2: Back Button → Library Screen Returns with Slide Animation

**Precondition:** Rules screen is open from Library navigation

**Steps:**
1. From Rules screen, tap back arrow in header bar
2. Observe transition animation

**Expected Result:**
- Rules screen slides out to right edge over 300ms
- Library screen slides in from left edge over 300ms
- Library screen shows same scroll position as before
- Smooth, fluid animation with no jank

**Verification Status:** ⏳ Pending manual test

---

### Test 3: System Back Gesture → Predictive Back Preview

**Precondition:**
- Android 14+ device with predictive back gesture enabled
- Rules screen is open from Library navigation

**Steps:**
1. From Rules screen, start back gesture (swipe from left edge)
2. Hold gesture midway without releasing
3. Observe preview animation
4. Complete gesture by releasing

**Expected Result:**
- During gesture: Library screen preview appears behind Rules screen
- During gesture: Rules screen scales down slightly (predictive back preview)
- On release: Rules screen slides out to right, Library slides in from left
- Same 300ms transition as back button
- Gesture can be cancelled by swiping back to edge

**Verification Status:** ⏳ Pending manual test (requires Android 14+)

---

### Test 4: Material 3 Ripple Effect on Card Tap

**Precondition:** Library screen shows game cards

**Steps:**
1. Navigate to Library tab
2. Tap on a game card
3. Observe ripple animation during tap

**Expected Result:**
- Ripple effect emanates from tap point
- Ripple color matches Material 3 theme (surface variant)
- Ripple completes before navigation transition begins
- No visual glitches or overlaps

**Verification Status:** ⏳ Pending manual test

---

### Test 5: Correct Game Data Loads in Rules Screen

**Precondition:** Multiple games saved in library

**Steps:**
1. Note the title of a specific game card (e.g., "Pandemic")
2. Tap that game card
3. Observe Rules screen header and content

**Expected Result:**
- Rules screen header title matches tapped game ("Pandemic")
- Rules content matches the selected game (correct overview, setup, etc.)
- Game thumbnail (if exists) displays correctly
- No data mixup between games

**Verification Status:** ⏳ Pending manual test

---

## Build Verification Results

All automated verification PASSED:

✅ **Build Verification:**
- `:feature:library:build` — SUCCESS
- `:feature:rules:build` — SUCCESS
- `:app:assembleDebug` — SUCCESS

✅ **Test Verification:**
- `:feature:library:test` — All tests PASS
- `:feature:rules:test` — All tests PASS

✅ **New Tests Added:**
- `LibraryNavigationTest` — Verifies navigation callback wiring (4 tests)
- `RulebookNavHostTransitionsTest` — Verifies slide transitions (5 tests)
- `RulesNavigationTest` — Verifies back navigation (4 tests)

---

## Manual Test Execution Instructions

To execute manual tests:

```bash
# Build and install debug APK
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch app
adb shell am start -n com.rulebook/.MainActivity

# For predictive back testing (Android 14+):
adb shell settings put global enable_back_gesture_predictive_animations 1
```

**Test Data Setup:**
- Ensure at least 2-3 games are saved in library for testing
- Games should have different titles for verification
- At least one game should have a thumbnail URL

---

## Notes

- All implementation for this story was completed in previous stories (7.1, 7.2, Epic 6)
- This test plan verifies existing functionality works as specified
- No new code was written - only verification tests were added
- Manual tests confirm end-to-end integration that unit tests cannot verify

---

## Sign-off

**Automated Tests:** ✅ PASS
**Manual Tests:** ⏳ Awaiting execution
**Story Status:** Ready for manual QA verification
