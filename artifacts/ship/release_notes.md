## [Unreleased] - 2026-03-20

### Added
- Register ReceiptRepository as singleton in Koin DataModule
- Add ReceiptRepository interface and implementation
- Add network-to-domain mappers for receipt validation
- Add ReceiptValidationApi retrofit interface and register in Koin
- Add network request/response models for receipt validation
- Add domain models for receipt validation

### Fixed
- Add request payload assertions to ReceiptRepositoryImplTest
- Generalize NetworkErrorMapper timeout message

### Documentation
- Add feature documentation for server-side receipt validation network layer (RULE-255)

### Other
- Add unit tests for receipt validation and update repository to accept packageName
