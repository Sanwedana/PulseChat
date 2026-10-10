# PulseChat 💬

**PulseChat** is a real-time 1-on-1 messaging Android application built in **Java**, **XML**, and **Firebase (Auth & Firestore)** using Material 3 design principles.

---

## ✨ Features

- 🔐 **Firebase Authentication**: Email and password sign-up, login, and password reset.
- 💬 **Real-time Messaging**: Instant 1-on-1 messaging powered by Firebase Firestore snapshot listeners.
- 👥 **User Discovery**: Search and discover registered users by name.
- 🔔 **Unread Message Badges**: Real-time unread counts that clear automatically when opening a chat.
- 📅 **Smart Date Headers & Timestamps**: Grouped message dates ("Today", "Yesterday", or date) and time labels.
- ⚡ **Offline Support & Indication**: Persistent session, cache loading, and an offline status banner.
- 👤 **Profile & Status Customization**: Custom bio/status updates and one-tap logout.
- 🎨 **Modern UI**: Built with Material 3 components, initial-letter avatars, and custom chat bubbles.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Java 11
- **UI Framework**: Native Android XML, Material Components 3 (`Theme.Material3.Light.NoActionBar`), ViewBinding
- **Min SDK**: API 24 (Android 7.0)
- **Target / Compile SDK**: API 35 / 37
- **Backend & Database**:
  - **Firebase Authentication** (Email/Password)
  - **Cloud Firestore** (Real-time NoSQL database)
- **Dependency Management**: Gradle Version Catalog (`gradle/libs.versions.toml`)

---

## 📁 Project Structure

```text
PulseChat/
├── app/
│   ├── google-services.json
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/pulsechat/
│       │   ├── models/           # UserModel, Message, ChatItem
│       │   ├── adapters/         # ChatListAdapter, UserAdapter, MessageAdapter
│       │   ├── SplashActivity.java
│       │   ├── LoginActivity.java
│       │   ├── RegisterActivity.java
│       │   ├── ForgotPasswordActivity.java
│       │   ├── MainActivity.java
│       │   ├── ChatsFragment.java
│       │   ├── UsersFragment.java
│       │   ├── ProfileFragment.java
│       │   ├── ChatActivity.java
│       │   └── Utils.java
│       └── res/                  # Drawables, Layouts, Menus, Values
├── gradle/
│   └── libs.versions.toml
└── README.md
```

---

## 🚀 Getting Started & Setup

### Prerequisites
- **Android Studio** (2024.1.1 or newer recommended)
- **Android SDK** API 34+
- A **Firebase Account**

### 1. Firebase Setup

1. Go to the [Firebase Console](https://console.firebase.google.com/) and create a project named `PulseChat`.
2. Register an Android App with package name:
   ```text
   com.example.pulsechat
   ```
3. Download `google-services.json` and place it in the project's `app/` folder:
   ```text
   PulseChat/app/google-services.json
   ```
4. Enable **Email/Password** Authentication:
   - Go to **Build** $\rightarrow$ **Authentication** $\rightarrow$ **Sign-in method**.
   - Enable **Email/Password**.
5. Enable **Firestore Database**:
   - Go to **Build** $\rightarrow$ **Firestore Database** $\rightarrow$ **Create database**.
   - Under the **Rules** tab, paste the following security rules and click **Publish**:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    match /users/{uid} {
      allow read, write: if request.auth != null;
    }

    match /chats/{chatId} {
      allow read, write: if request.auth != null;

      match /messages/{messageId} {
        allow read, write: if request.auth != null;
      }
    }
  }
}
```

---

### 2. Run the App

1. Open the project folder in **Android Studio**.
2. Click **Sync Project with Gradle Files**.
3. Launch an **Android Emulator** or connect a physical device via USB debugging.
4. Click **Run ▶** (or press `Shift + F10`).

---

## 📱 Database Schema

```text
users/{uid}
  ├── uid: String
  ├── name: String
  ├── nameLower: String
  ├── email: String
  ├── status: String
  └── createdAt: Timestamp

chats/{chatId}                   (chatId = deterministic min(uidA, uidB) + "_" + max(uidA, uidB))
  ├── participants: [uidA, uidB]
  ├── names: { uidA: "NameA", uidB: "NameB" }
  ├── lastMessage: String
  ├── lastSenderId: String
  ├── lastMessageTime: Timestamp
  ├── unread: { uidA: 0, uidB: 1 }
  └── messages/{messageId}
        ├── senderId: String
        ├── text: String
        └── timestamp: Timestamp
```

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
