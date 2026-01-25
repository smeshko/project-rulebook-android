---
title: Testing Documentation
description: Testing strategy and quality assurance for project-rulebook-android
---

# Testing

Testing documentation and quality assurance guides.

## Documents

| Document | Description |
|----------|-------------|
| [testing-overview.md](testing-overview.md) | Testing strategy, patterns, and coverage analysis |

## Quick Reference

```bash
# Run all unit tests
./gradlew test

# Run instrumented tests (requires device)
./gradlew connectedAndroidTest

# Run specific module tests
./gradlew :feature:camera:test
```

## Test Summary

- **45 test files** across all modules
- **Fake implementations** (no mocking libraries)
- **Coroutine testing** with TestDispatcher patterns
- **Compose UI testing** for permission flows

## Coverage Gaps

- `feature/rules` - needs tests
- `feature/purchase` - needs tests

## Related Sections

- [Development](../development/) - Setup and build guides
- [Templates](../templates/) - ViewModel test patterns
