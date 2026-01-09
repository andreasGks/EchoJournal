# 🎙️ EchoJournal

An Android application that allows users to record their thoughts, track their moods, and organize their life using voice notes.
Built with a modern **Offline-First** approach, ensuring data privacy and instant access without an internet connection.

---

## 🛠 Tech Stack
- **Language:** Kotlin
- **UI Toolkit:** Jetpack Compose (Material 3 Design)
- **Architecture:** MVVM (Model-View-ViewModel) + Clean Architecture Principles
- **Dependency Injection:** Dagger Hilt 💉
- **Asynchronous:** Kotlin Coroutines & Flows
- **State Management:** StateFlow (Reactive UI updates)
- **Database:** Room Database (SQLite abstraction for local persistence)
- **Preferences:** Jetpack DataStore (Settings & Defaults)
- **Hardware Integration:** Android MediaRecorder API (Audio recording)
- **Testing:** JUnit4, Mockito, Kotlin Coroutines Test
- **IDE:** Android Studio Ladybug

---

## ✨ Features (Version 1.0)
- 🎙️ **Voice Journaling:** Seamlessly record audio notes within the app.
- 🏷️ **Topics & Tags:** Organize entries with custom tags (e.g., #Work, #Ideas, #Family).
- 🎭 **Mood Tracking:** Associate recordings with moods (Excited, Peaceful, Neutral, Sad, Stressed).
- 🔍 **Smart Search:** Instantly find journals by title or content.
- ⚡ **Advanced Filtering:** Filter your history by specific Moods or Topics.
- ⚙️ **Customizable Settings:** Set default moods and preferred topics for faster logging.
- 🏠 **Home Dashboard:** View a timeline of past journals with visual indicators.
- 📂 **Offline Storage:** All data (audio paths & metadata) is stored locally.
- 🎨 **Modern UI:** Clean, responsive interface built 100% with Jetpack Compose.

---

## 🏗 App Structure
The app follows the **MVVM** pattern with specific layers:

- **UI Layer (`presentation/`):**
  - **Screens:** Composable functions (HistoryScreen, RecordScreen, SettingsScreen).
  - **ViewModels:** Manages UI state using `StateFlow` (e.g., `HistoryViewModel`).
- **Data Layer (`data/`):**
  - **Repository:** `JournalRepository` & `SettingsRepository` act as single sources of truth.
  - **Local:** Room Database (`JournalDatabase`) for entries, DataStore for user preferences.
- **DI Layer (`di/`):**
  - Hilt Modules (`AppModule`) for dependency injection.
- **Model Layer (`domain/model/`):**
  - Data classes (`JournalEntry`, `Mood`) representing business logic.

---

## ▶️ How to Run
1. Clone the repository:
   ```bash
   git clone [https://github.com/andreasGks/EchoJournal.git](https://github.com/andreasGks/EchoJournal.git)