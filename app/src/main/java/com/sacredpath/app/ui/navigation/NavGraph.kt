package com.sacredpath.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.*
import com.sacredpath.app.ui.screens.bible.*
import com.sacredpath.app.ui.screens.quiz.*
import com.sacredpath.app.ui.screens.prayer.*
import com.sacredpath.app.ui.screens.profile.*
import com.sacredpath.app.ui.screens.study.*
import com.sacredpath.app.ui.screens.memorization.MemHomeScreen
import com.sacredpath.app.ui.screens.sermons.*
import com.sacredpath.app.ui.screens.kids.KidsHomeScreen
import com.sacredpath.app.ui.screens.groups.GroupsHomeScreen
import com.sacredpath.app.ui.screens.onboarding.*

// ── Route constants ────────────────────────────────────────────────────────────
object Routes {
    // Onboarding
    const val ONBOARDING        = "onboarding"
    const val WELCOME           = "welcome"
    const val CREATE_PROFILE    = "create_profile"
    const val DOWNLOAD_BIBLE    = "download_bible"
    const val SET_REMINDER      = "set_reminder"

    // Main
    const val MAIN              = "main"
    const val HOME              = "home"

    // Bible
    const val BOOK_PICKER       = "book_picker/{translationId}"
    const val CHAPTER_PICKER    = "chapter_picker/{translationId}/{bookId}/{bookName}"
    const val BIBLE_READER      = "bible_reader/{translationId}/{bookId}/{bookName}/{chapter}?highlightVerse={highlightVerse}"
    const val BIBLE_SEARCH      = "bible_search/{translationId}"
    const val BOOKMARKS         = "bookmarks"
    const val HIGHLIGHTS        = "highlights"
    const val READING_HISTORY   = "reading_history"
    const val COMPARE           = "compare/{translationId}/{bookId}/{bookName}/{chapter}"

    // Study
    const val STUDY_HOME        = "study_home"
    const val DEVOTIONALS       = "devotionals"
    const val TOPIC_EXPLORER    = "topic_explorer"
    const val BIBLE_MAPS        = "bible_maps"
    const val BIBLE_TIMELINE    = "bible_timeline"

    // Quiz
    const val QUIZ_HOME         = "quiz_home"
    const val QUIZ_SESSION      = "quiz_session/{category}/{difficulty}/{mode}"
    const val QUIZ_RESULT       = "quiz_result/{score}/{total}/{xpEarned}/{category}/{difficulty}/{mode}"

    // Prayer
    const val PRAYER_HOME       = "prayer_home"
    const val ADD_PRAYER        = "add_prayer?existingId={existingId}"
    const val ANSWERED_PRAYERS  = "answered_prayers"

    // Profile
    const val PROFILE_HOME      = "profile_home"
    const val SETTINGS          = "settings"
    const val THEME_SELECTOR    = "theme_selector"
    const val ACHIEVEMENTS      = "achievements"
    const val STATISTICS        = "statistics"
    const val PROFILE_SWITCHER  = "profile_switcher"

    // Drawer screens
    const val READING_PLANS     = "reading_plans"
    const val MEMORIZATION      = "memorization"
    const val SERMONS           = "sermons"
    const val SERMON_EDITOR     = "sermon_editor?sermonId={sermonId}"
    const val KIDS_MODE         = "kids_mode"
    const val CHURCH_GROUPS     = "church_groups"
}

@Composable
fun SacredPathNavHost(startDestination: String) {
    val navController = rememberNavController()

    NavHost(
        navController    = navController,
        startDestination = startDestination
    ) {
        // ── Onboarding ──────────────────────────────────────────────────────
        navigation(startDestination = Routes.WELCOME, route = Routes.ONBOARDING) {
            composable(Routes.WELCOME)        { WelcomeScreen(navController) }
            composable(Routes.CREATE_PROFILE) { CreateProfileScreen(navController) }
            composable(Routes.DOWNLOAD_BIBLE) { DownloadBibleScreen(navController) }
            composable(Routes.SET_REMINDER)   { SetReminderScreen(navController) }
        }

        // ── Main ────────────────────────────────────────────────────────────
        composable(Routes.MAIN) {
            MainScaffold(navController)
        }
    }
}

// MainScaffold hosts the bottom nav + all tabs
@Composable
fun MainScaffold(rootNav: NavController) {
    val tabNavController = rememberNavController()

    androidx.compose.material3.Scaffold(
        bottomBar = { SacredBottomBar(tabNavController) }
    ) { padding ->
        NavHost(
            navController    = tabNavController,
            startDestination = Routes.HOME,
            modifier         = androidx.compose.ui.Modifier.padding(padding)
        ) {
            // Bible tab
            composable(Routes.HOME) {
                HomeScreen(navController = tabNavController, rootNav = rootNav)
            }
            composable(
                route     = Routes.BOOK_PICKER,
                arguments = listOf(navArgument("translationId") { type = NavType.StringType })
            ) { backStack ->
                BookPickerScreen(
                    navController  = tabNavController,
                    translationId  = backStack.arguments?.getString("translationId") ?: ""
                )
            }
            composable(
                route     = Routes.CHAPTER_PICKER,
                arguments = listOf(
                    navArgument("translationId") { type = NavType.StringType },
                    navArgument("bookId")        { type = NavType.StringType },
                    navArgument("bookName")      { type = NavType.StringType }
                )
            ) { backStack ->
                ChapterPickerScreen(
                    navController = tabNavController,
                    translationId = backStack.arguments?.getString("translationId") ?: "",
                    bookId        = backStack.arguments?.getString("bookId") ?: "",
                    bookName      = backStack.arguments?.getString("bookName") ?: ""
                )
            }
            composable(
                route     = Routes.BIBLE_READER,
                arguments = listOf(
                    navArgument("translationId")  { type = NavType.StringType },
                    navArgument("bookId")         { type = NavType.StringType },
                    navArgument("bookName")       { type = NavType.StringType },
                    navArgument("chapter")        { type = NavType.IntType    },
                    navArgument("highlightVerse") { type = NavType.IntType; defaultValue = -1 }
                )
            ) { backStack ->
                BibleReaderScreen(
                    navController  = tabNavController,
                    translationId  = backStack.arguments?.getString("translationId") ?: "",
                    bookId         = backStack.arguments?.getString("bookId") ?: "",
                    bookName       = backStack.arguments?.getString("bookName") ?: "",
                    chapter        = backStack.arguments?.getInt("chapter") ?: 1,
                    highlightVerse = backStack.arguments?.getInt("highlightVerse") ?: -1
                )
            }
            composable(
                route     = Routes.BIBLE_SEARCH,
                arguments = listOf(navArgument("translationId") { type = NavType.StringType })
            ) { backStack ->
                BibleSearchScreen(
                    navController = tabNavController,
                    translationId = backStack.arguments?.getString("translationId") ?: ""
                )
            }
            composable(Routes.BOOKMARKS)       { BookmarksScreen(tabNavController) }
            composable(Routes.HIGHLIGHTS)      { HighlightsScreen(tabNavController) }
            composable(Routes.READING_HISTORY) { ReadingHistoryScreen(tabNavController) }
            composable(
                route     = Routes.COMPARE,
                arguments = listOf(
                    navArgument("translationId") { type = NavType.StringType },
                    navArgument("bookId")        { type = NavType.StringType },
                    navArgument("bookName")      { type = NavType.StringType },
                    navArgument("chapter")       { type = NavType.IntType    }
                )
            ) { backStack ->
                CompareTranslationsScreen(
                    navController = tabNavController,
                    translationId = backStack.arguments?.getString("translationId") ?: "",
                    bookId        = backStack.arguments?.getString("bookId") ?: "",
                    bookName      = backStack.arguments?.getString("bookName") ?: "",
                    chapter       = backStack.arguments?.getInt("chapter") ?: 1
                )
            }

            // Study tab
            composable(Routes.STUDY_HOME)    { StudyHomeScreen(tabNavController) }
            composable(Routes.DEVOTIONALS)   { DevotionalsScreen(tabNavController) }
            composable(Routes.TOPIC_EXPLORER){ TopicExplorerScreen(tabNavController) }
            composable(Routes.BIBLE_MAPS)    { BibleMapsScreen(tabNavController) }
            composable(Routes.BIBLE_TIMELINE){ BibleTimelineScreen(tabNavController) }

            // Quiz tab
            composable(Routes.QUIZ_HOME) { QuizHomeScreen(tabNavController) }
            composable(
                route     = Routes.QUIZ_SESSION,
                arguments = listOf(
                    navArgument("category")   { type = NavType.StringType },
                    navArgument("difficulty") { type = NavType.StringType },
                    navArgument("mode")       { type = NavType.StringType }
                )
            ) { backStack ->
                QuizSessionScreen(
                    navController = tabNavController,
                    category      = backStack.arguments?.getString("category") ?: "mixed",
                    difficulty    = backStack.arguments?.getString("difficulty") ?: "medium",
                    mode          = backStack.arguments?.getString("mode") ?: "casual"
                )
            }
            composable(
                route     = Routes.QUIZ_RESULT,
                arguments = listOf(
                    navArgument("score")      { type = NavType.IntType    },
                    navArgument("total")      { type = NavType.IntType    },
                    navArgument("xpEarned")   { type = NavType.IntType    },
                    navArgument("category")   { type = NavType.StringType },
                    navArgument("difficulty") { type = NavType.StringType },
                    navArgument("mode")       { type = NavType.StringType }
                )
            ) { backStack ->
                QuizResultScreen(
                    navController = tabNavController,
                    score         = backStack.arguments?.getInt("score") ?: 0,
                    total         = backStack.arguments?.getInt("total") ?: 0,
                    xpEarned      = backStack.arguments?.getInt("xpEarned") ?: 0,
                    category      = backStack.arguments?.getString("category") ?: "",
                    difficulty    = backStack.arguments?.getString("difficulty") ?: "",
                    mode          = backStack.arguments?.getString("mode") ?: ""
                )
            }

            // Prayer tab
            composable(Routes.PRAYER_HOME)     { PrayerHomeScreen(tabNavController) }
            composable(
                route     = Routes.ADD_PRAYER,
                arguments = listOf(navArgument("existingId") { nullable = true; defaultValue = null })
            ) { backStack ->
                AddPrayerScreen(
                    navController = tabNavController,
                    existingId    = backStack.arguments?.getString("existingId")
                )
            }
            composable(Routes.ANSWERED_PRAYERS) { AnsweredPrayersScreen(tabNavController) }

            // Profile tab
            composable(Routes.PROFILE_HOME)    { ProfileHomeScreen(tabNavController) }
            composable(Routes.SETTINGS)        { SettingsScreen(tabNavController) }
            composable(Routes.THEME_SELECTOR)  { ThemeSelectorScreen(tabNavController) }
            composable(Routes.ACHIEVEMENTS)    { AchievementsScreen(tabNavController) }
            composable(Routes.STATISTICS)      { StatisticsScreen(tabNavController) }
            composable(Routes.PROFILE_SWITCHER){ ProfileSwitcherScreen(tabNavController) }

            // Drawer screens
            composable(Routes.READING_PLANS)   { ReadingPlansScreen(tabNavController) }
            composable(Routes.MEMORIZATION)    { MemHomeScreen(tabNavController) }
            composable(Routes.SERMONS)         { SermonsListScreen(tabNavController) }
            composable(
                route     = Routes.SERMON_EDITOR,
                arguments = listOf(navArgument("sermonId") { nullable = true; defaultValue = null })
            ) { backStack ->
                SermonEditorScreen(
                    navController = tabNavController,
                    sermonId      = backStack.arguments?.getString("sermonId")
                )
            }
            composable(Routes.KIDS_MODE)       { KidsHomeScreen(tabNavController) }
            composable(Routes.CHURCH_GROUPS)   { GroupsHomeScreen(tabNavController) }
        }
    }
}
