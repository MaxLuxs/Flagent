# Changelog

All notable changes to the Flagent Frontend will be documented in this file.

## [2.0.0] - 2026-01-28

### Added

#### Phase 1: Foundation
- ✨ Clean Architecture with ViewModels and State Management
- ✨ Singleton ApiClient with centralized error handling
- ✨ AppConfig for environment variable management
- ✨ Comprehensive logging with AppLogger
- ✨ ErrorHandler for user-friendly error messages
- ✨ GlobalState for application-wide state
- ✨ Common UI components (Pagination, SkeletonLoader, EmptyState, ConfirmDialog, NotificationToast)
- 📚 Complete documentation (README, ARCHITECTURE, TESTING)

#### Phase 2: Authentication & Multi-tenancy
- 🔐 JWT authentication support
- 🔐 SSO/SAML integration UI
- 🏢 Multi-tenancy with tenant switcher
- 🏢 Tenant management UI
- 🏢 API key management
- 👤 User profile management

#### Phase 3: Metrics & AI Features
- 📊 Metrics Dashboard with real-time analytics
- 📊 Aggregated metrics visualization
- 🤖 Smart Rollout configuration UI
- 🤖 AI-powered gradual rollout management
- 🚨 Anomaly Detection configuration
- 🚨 Anomaly alerts list with severity levels
- 🔄 Real-time updates via Server-Sent Events (SSE)
- 🔔 Live notifications for flag changes

#### Phase 4: Billing & Integrations
- 💳 Stripe billing integration
- 💳 Subscription management UI
- 💳 Plan selection and upgrade flow
- 📤 Export panel for evaluation cache and database
- 🔗 Slack integration configuration (planned)

#### Phase 5: Testing & Polish
- ✅ Unit tests for utilities and state management
- ✅ Test infrastructure setup
- 📖 Testing guide documentation
- 🎨 Improved UI/UX with smooth animations
- ♿ Basic accessibility improvements

### Changed
- 🔄 Refactored App component to use GlobalState
- 🔄 Updated all components to use ViewModels
- 🔄 Improved error handling across the application
- 🎨 Modernized theme with consistent styling

### Fixed
- 🐛 Fixed hardcoded API base URL
- 🐛 Improved error messages for API failures
- 🐛 Better handling of loading states

### Infrastructure
- 🏗️ Added kotlinx-datetime dependency
- 🏗️ Configured webpack for environment variables
- 🏗️ Set up test infrastructure

## [0.1.0] - Initial Release

- Basic flag management (CRUD)
- Segment management with drag & drop
- Constraint editor with 12 operators
- Distribution management
- Variant management
- Tag management
- Flag history with diff visualization
- Debug console for evaluation
- Search and filtering
- Compose for Web UI
