# Sacred Path — Native Android (Kotlin + Jetpack Compose)

> *Your daily journey through God's Word*

Full native Android Bible app built with **Kotlin + Jetpack Compose + Hilt + Room**.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material3 |
| Navigation | Navigation Compose |
| DI | Hilt |
| Database | Room (SQLite) — all 66 books cached offline |
| Network | Retrofit + OkHttp + Kotlinx Serialization |
| Settings | DataStore Preferences |
| Background | WorkManager (daily verse notifications) |
| Audio | Android TTS (built-in) + Media3/ExoPlayer (sermons) |
| CI/CD | Codemagic |

---

## Features

### 📖 Bible
- All **66 books** — 39 Old Testament + 27 New Testament
- Book IDs are canonical API.Bible IDs (GEN, JHN, 1CO, SNG etc.) — never derived from name slicing
- Chapter counts bundled locally — no API call needed for navigation
- Multiple translations (KJV, NIV, ESV, NLT, NASB, AMP, WEB)
- Offline caching via Room SQLite
- Highlights (5 colors), bookmarks, notes per verse
- Copy & share any verse

### 🔊 Auto-Read (TTS)
- Android built-in TextToSpeech engine
- Adjustable speed (0.75× to 2×)
- Follow-along verse highlighting while reading
- Play/pause/stop controls
- Skip chapter forward/back

### ✨ Daily Bible Verse
- **Truly random** at runtime — `kotlin.random.Random` picks:
  1. Random book from all 66
  2. Random chapter from that book
  3. Random verse from that chapter
- Cached per calendar day — same verse all day, changes at midnight
- Pull-to-refresh for a new random verse
- Works offline (falls back to curated offline pool, also randomized)

### 🧠 Quizzes
- **Truly random** at runtime — `List.shuffled()` from a 60+ question bank
- New random selection every session
- Categories: OT, NT, Characters, Prophecy, Theology, Mixed
- Difficulty: Easy, Medium, Hard
- Explanation shown after each answer
- XP rewards saved to DataStore

### 🙏 Prayer Journal
- Add, categorize, mark answered
- 6 categories with color coding
- Streak tracking

### 💎 Verse Memorization
- SM-2 spaced repetition algorithm
- Rate recall 1–5 after each review
- Status: New → Learning → Mastered

### 🎤 Sermon Notes
- Title, speaker, series fields
- Rich text content
- Full CRUD

### 👶 Kids Mode
- PIN-protected (default: 1234)
- 8 Bible story cards
- Built-in kids quiz

### 👥 Church Groups
- Create groups with human-readable codes (e.g. DOVE-3842)
- Join by code
- Shared prayer board

### 🏆 Gamification
- 9 ranks: Seeker → Prophet
- XP for every activity
- 9 achievements
- Daily streak

---

## Project Structure

```
SacredPath-Android/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/sacredpath/app/
│   │   │   ├── MainActivity.kt
│   │   │   ├── SacredPathApplication.kt
│   │   │   ├── data/
│   │   │   │   ├── api/ApiBibleService.kt        # Retrofit interface
│   │   │   │   ├── db/BibleDatabase.kt           # Room DB + all DAOs
│   │   │   │   ├── model/BibleModels.kt          # Domain + network models
│   │   │   │   └── repository/
│   │   │   │       ├── BibleRepository.kt        # Chapter fetch + cache
│   │   │   │       ├── DailyVerseRepository.kt   # Runtime random verse
│   │   │   │       ├── QuizRepository.kt         # Runtime random questions
│   │   │   │       └── SettingsRepository.kt     # DataStore prefs
│   │   │   ├── di/AppModule.kt                   # Hilt module
│   │   │   ├── services/AudioPlaybackService.kt
│   │   │   ├── ui/
│   │   │   │   ├── components/SharedComponents.kt
│   │   │   │   ├── navigation/NavGraph.kt        # Full nav graph
│   │   │   │   ├── navigation/BottomBar.kt
│   │   │   │   ├── theme/SacredPathTheme.kt      # 4 themes
│   │   │   │   └── screens/
│   │   │   │       ├── bible/                   # Home, BookPicker, ChapterPicker
│   │   │   │       │                            # BibleReader (TTS), Search,
│   │   │   │       │                            # Bookmarks, Highlights, History
│   │   │   │       ├── quiz/QuizScreens.kt       # Home, Session, Result
│   │   │   │       ├── prayer/PrayerScreens.kt
│   │   │   │       ├── profile/ProfileScreens.kt
│   │   │   │       ├── study/StudyScreens.kt
│   │   │   │       ├── memorization/MemHomeScreen.kt
│   │   │   │       ├── sermons/SermonsScreens.kt
│   │   │   │       ├── kids/KidsHomeScreen.kt
│   │   │   │       ├── groups/GroupsHomeScreen.kt
│   │   │   │       ├── onboarding/OnboardingScreens.kt
│   │   │   │       └── ReadingPlansScreen.kt
│   │   │   ├── utils/BibleBookData.kt            # ALL 66 books with correct IDs
│   │   │   └── workers/DailyVerseWorker.kt       # WorkManager notifications
│   │   └── res/
│   │       ├── values/{strings, colors, themes}.xml
│   │       ├── drawable/{ic_notification, ic_launcher_foreground}.xml
│   │       └── font/  ← place font TTFs here (see below)
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   ├── libs.versions.toml                        # Version catalog
│   └── wrapper/gradle-wrapper.properties
├── build.gradle.kts
├── settings.gradle.kts
├── codemagic.yaml
└── .gitignore
```

---

## Setup

### 1. Open in Android Studio
File → Open → select `SacredPath-Android/`
Android Studio will generate `gradle-wrapper.jar` and sync automatically.

### 2. Add Fonts
Download from Google Fonts and place in `app/src/main/res/font/`:
- [Playfair Display](https://fonts.google.com/specimen/Playfair+Display): `playfair_display_regular.ttf`, `playfair_display_bold.ttf`, `playfair_display_italic.ttf`
- [Lora](https://fonts.google.com/specimen/Lora): `lora_regular.ttf`, `lora_bold.ttf`, `lora_italic.ttf`
- [Inter](https://fonts.google.com/specimen/Inter): `inter_regular.ttf`, `inter_medium.ttf`, `inter_bold.ttf`

### 3. Add API Key
Create `local.properties` (already git-ignored):
```
API_BIBLE_KEY=your_key_from_scripture.api.bible
```

### 4. Run
```
./gradlew assembleDebug
```
Or press Run in Android Studio.

---

## Codemagic Setup

### Environment variable groups needed:

**`sacred_path_android`** (for release builds):
- `API_BIBLE_KEY`
- `ANDROID_KEYSTORE_BASE64` — base64 of your `.jks` keystore
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`
- `GCLOUD_SERVICE_ACCOUNT_CREDENTIALS` — Google Play service account JSON

**`sacred_path_ios`** (for iOS):
- `APP_STORE_CONNECT_PRIVATE_KEY`
- `APP_STORE_CONNECT_KEY_IDENTIFIER`
- `APP_STORE_CONNECT_ISSUER_ID`

---

## Bible Book Coverage — All 66 Books

### Old Testament (39)
GEN EXO LEV NUM DEU JOS JDG RUT 1SA 2SA 1KI 2KI 1CH 2CH EZR NEH EST JOB PSA PRO ECC SNG ISA JER LAM EZK DAN HOS JOL AMO OBA JON MIC NAM HAB ZEP HAG ZEC MAL

### New Testament (27)
MAT MRK LUK JHN ACT ROM 1CO 2CO GAL EPH PHP COL 1TH 2TH 1TI 2TI TIT PHM HEB JAS 1PE 2PE 1JN 2JN 3JN JUD REV

All IDs are **canonical API.Bible identifiers** — never derived from name slicing.
