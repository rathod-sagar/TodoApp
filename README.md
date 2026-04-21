# Todo App

A production-grade offline-first Android todo app built 
with Jetpack Compose and Clean Architecture.

## Tech Stack
- Kotlin + Jetpack Compose
- Clean Architecture (Data → Domain → Presentation)
- Retrofit
- Room
- Koin DI
- Coroutines + StateFlow + Channel
- KSP + libs.versions.toml

## Architecture Decisions
- **Offline-first:** Room is single source of truth
- **Optimistic updates:** Room updates before API call
- **State + Event:** StateFlow for state, Channel for one-shot events
- **Lazy loading:** onStart operator with hasLoadedInitialData flag

## Features
- Create, edit, delete, complete todos
- Offline support with local caching
- Swipe to delete with animations
- Progress tracking
- Error handling with snackbars
- Loading and empty states

## Testing (~90% coverage)
- ViewModel unit tests
- Usecase unit tests  
- Repository unit tests
- Mapping unit tests
- DAO instrumented tests

## Setup
1. Clone the repo
2. Open in Android Studio
3. Sync Gradle
4. Run on device or emulator

## Possible Improvements
- Real backend integration
- User authentication
- Pagination with Paging 3
- Push notifications with WorkManager
- Search and filtering
- Categories and tags
