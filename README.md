# 🎙️ EchoJournal

An Android application that allows users to record their thoughts and track their moods using voice notes.
Built with a modern **Offline-First** approach, ensuring data privacy and instant access without an internet connection.

---

## 🖼 Screenshots
*(Add screenshots here when your UI is ready – e.g., Home Screen, Recording Sheet, Mood Selector)*

| Home Screen | Create Record | Mood Selection |
|:-----------:|:-------------:|:--------------:|
| <img src="" width="200" /> | <img src="" width="200" /> | <img src="" width="200" /> |

---

## 🛠 Tech Stack
- **Language:** Kotlin
- **UI Toolkit:** Jetpack Compose (Material 3 Design)
- **Architecture:** MVVM (Model-View-ViewModel)
- **Asynchronous:** Kotlin Coroutines & Flows
- **State Management:** StateFlow (Reactive UI updates)
- **Database:** Room Database (SQLite abstraction for local persistence)
- **Hardware Integration:** Android MediaRecorder API (Audio recording)
- **IDE:** Android Studio Ladybug

---

## ✨ Features
- 🎙️ **Voice Journaling:** Seamlessly record audio notes within the app.
- 🎭 **Mood Tracking:** Associate recordings with moods (Excited, Peaceful, Neutral, Sad, Stressed).
- 🏠 **Home Dashboard:** View a list of past journals with visual mood indicators.
- 📂 **Offline Storage:** All data (audio paths & metadata) is stored locally using Room.
- 🎨 **Modern UI:** Clean, responsive interface built 100% with Jetpack Compose.
- ⚡ **Reactive Updates:** Real-time UI updates using StateFlow and LiveData.

---

## 🏗 App Structure
The app follows the **Clean Architecture** & **Separation of Concerns** principles:

- **UI Layer (`ui/`):**
  - **Screens:** Composable functions (HomeScreen, CreateRecordScreen).
  - **ViewModels:** Manages UI state using `StateFlow` and communicates with the Repository.
- **Data Layer (`data/`):**
  - **Repository:** `JournalRepository` acts as the single source of truth.
  - **LocalDataSource:** Handles direct database operations.
  - **Room:** `JournalDatabase` & `JournalEntryDao` for SQL queries.
- **Model Layer (`model/`):**
  - Data classes (`JournalEntry`, `Mood`) representing the core business objects.

---

## ▶️ How to Run
1. Clone the repository:
   ```bash
   git clone [https://github.com/andreasGks/EchoJournal.git](https://github.com/andreasGks/EchoJournal.git)
