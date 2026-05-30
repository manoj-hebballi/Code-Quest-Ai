# CodeQuest AI 🚀

CodeQuest AI is a state-of-the-art, AI-powered coding practice and gamification platform built with **Kotlin** and **Jetpack Compose**. It is engineered to help developers master data structures and algorithms through interactive coding environments, immediate AI feedback, real-time metrics, dynamic gamification, and social sharing.

---

## 🌟 Key Features

### 1. 📂 Algorithmic Challenges & Dynamic Editor
*   **Comprehensive Problem Sets**: Curated directory of algorithmic tasks categorized by topic and difficulty constraints.
*   **Interactive Code Playground**: Clean-styled, feature-rich syntax editor with a live compiling environment.
*   **Performance Metrics**: Integrated tracking of execution times, code correctness, and space/time complexity profiling.

### 2. 🧠 Server-Side Gemini AI Insights
*   **Detailed Analytics**: Powered by **Google Gemini AI**, the app evaluates your compiling solutions and explains edge cases, time-complexity optimizations, and code architecture improvements.
*   **Targeted Hints**: Receive dynamic, contextual guidelines without revealing the solution directly, promoting healthy problem-solving habits.

### 3. 🏆 Gamification Arena Hub
*   **Interactive Leaderboard**: Competitive rankings featuring peer scorecards, active daily streaks, and points tracking.
*   **Trophy Cabinet**: Lock and unlock unique interactive achievements and badges based on your performance:
    *   *First Step*: Successfully compile and solve your first challenge.
    *   *Streak Master*: Maintain active practice across consecutive days.
    *   *Hardcore Solved*: Master complex constraints.
    *   *Speed Demon*: Compile solutions inside critical margins under 30s.
*   **Dynamic Reward Dialogs**: Enjoy visually rich celebratory rewards detailing base points, speed modifiers, and streak multipliers upon successful execution.

### 4. 💼 LinkedIn Integration & Sharing
*   **Visual Proof-of-Work**: Seamlessly translate achievements and solved certificates into customizable draft updates ready to be published on LinkedIn.
*   **Portfolio Highlighter**: Showcase newly unlocked badges, active solving streaks, and skill badges directly to recruiter networks.

---

## 🎨 Design Philosophy
CodeQuest AI is styled after the **Cosmic Slate Theme** — an aesthetic design language featuring:
*   **Deep Obsidian Canvas** with high-contrast emerald and gold accents for premium readability during late-night coding sessions.
*   **Material 3 Design System**: Smooth rounded corners, strict tactile feedback, and accessible minimum text-contrast sizes.
*   **Fluid Responsive Layouts**: Built mobile-first but fully optimized for medium tablets and foldables using dynamic insets and containers.

---

## 🛠️ Technology Stack & Architecture

*   **UI Framework**: 100% Jetpack Compose with declarative navigation.
*   **Language**: Kotlin (Modern functional paradigms & Coroutines).
*   **Local Persistence**: Jetpack Room for fast, safe SQL-backed storage of challenges, historical submissions, and user profiles.
*   **AI Integration**: Generative AI via server-side Google Gemini REST Client.
*   **State Management**: MVVM Architecture with stateful ViewModels and secure `MutableStateFlow` bindings.
*   **Network & Serialization**: Type-safe data streaming and configuration profiles.

---

## 🚀 Getting Started

### Prerequisites
*   Android Studio Ladybug (or newer).
*   Gradle 8.x.
*   Android SDK Level 34+ (Target Platform).

### Configuration
1. **Secure API Credentials**: Log in to Google AI Studio and copy your Gemini API key.
2. **Setup Secrets**: Create a `.env` file at the root in Android Studio (or add it via your system environment variables) with the following key:
   ```env
   GEMINI_API_KEY=your_gemini_api_key_here
   ```
3. **Build & Run**: Sync Gradle dependencies and execute the launch configuration on your physical device or Android Emulator.

---

## 📜 Repository Structure
```text
├── app/
│   ├── src/main/java/com/example/
│   │   ├── data/             # Models, Room Local DB Tables, & Network API Clients
│   │   ├── ui/
│   │   │   ├── screens/      # Dashboard, Practice Canvas, and LinkedIn Sharing Panels
│   │   │   ├── theme/        # Centralized Color Palettes, Typography & Shapes
│   │   │   └── viewmodel/    # MVVM States & Game Engines
│   │   └── MainActivity.kt   # App Entrypoint & Scaffold Compose Router
│   └── build.gradle.kts      # Application Configuration & Build Specifications
└── build.gradle.kts          # Workspace Plugins & Versions Manifest
```

---

*Formulated with care directly within the Google AI Studio Sandbox.* 🛠️
