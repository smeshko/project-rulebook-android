#!/bin/bash
#
# Bumps versionName (semver) and increments versionCode in app/build.gradle.kts
# Usage: ./scripts/bump-version.sh [major|minor|patch]
#        Default: patch

set -euo pipefail

GRADLE_FILE="app/build.gradle.kts"
BUMP_TYPE="${1:-patch}"

if [[ ! -f "$GRADLE_FILE" ]]; then
    echo "Error: $GRADLE_FILE not found. Run from project root."
    exit 1
fi

if [[ "$BUMP_TYPE" != "major" && "$BUMP_TYPE" != "minor" && "$BUMP_TYPE" != "patch" ]]; then
    echo "Usage: $0 [major|minor|patch]"
    exit 1
fi

# Extract current values
CURRENT_VERSION=$(grep -oP 'versionName\s*=\s*"\K[^"]+' "$GRADLE_FILE")
CURRENT_CODE=$(grep -oP 'versionCode\s*=\s*\K\d+' "$GRADLE_FILE")

if [[ -z "$CURRENT_VERSION" || -z "$CURRENT_CODE" ]]; then
    echo "Error: Could not parse versionName or versionCode from $GRADLE_FILE"
    exit 1
fi

# Parse semver
IFS='.' read -r MAJOR MINOR PATCH <<< "$CURRENT_VERSION"

case "$BUMP_TYPE" in
    major) MAJOR=$((MAJOR + 1)); MINOR=0; PATCH=0 ;;
    minor) MINOR=$((MINOR + 1)); PATCH=0 ;;
    patch) PATCH=$((PATCH + 1)) ;;
esac

NEW_VERSION="$MAJOR.$MINOR.$PATCH"
NEW_CODE=$((CURRENT_CODE + 1))

# Apply changes
sed -i '' "s/versionName = \"$CURRENT_VERSION\"/versionName = \"$NEW_VERSION\"/" "$GRADLE_FILE"
sed -i '' "s/versionCode = $CURRENT_CODE/versionCode = $NEW_CODE/" "$GRADLE_FILE"

echo "Bumped version: $CURRENT_VERSION ($CURRENT_CODE) -> $NEW_VERSION ($NEW_CODE)"
