package com.betterdo.app.ui.nav

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.betterdo.app.data.prefs.Settings
import com.betterdo.app.di.AppContainer
import com.betterdo.app.domain.model.Tag
import com.betterdo.app.domain.model.Todo
import com.betterdo.app.domain.model.TodoIcon
import com.betterdo.app.domain.model.TodoSource
import com.betterdo.app.notifications.ReminderScheduler
import com.betterdo.app.ui.components.BottomBar
import com.betterdo.app.ui.screens.derive.DeriveScreen
import com.betterdo.app.ui.screens.derive.DeriveViewModel
import com.betterdo.app.ui.screens.detail.DetailViewModel
import com.betterdo.app.ui.screens.detail.TodoDetailScreen
import com.betterdo.app.ui.screens.onboarding.ImportScreen
import com.betterdo.app.ui.screens.onboarding.ImportViewModel
import com.betterdo.app.ui.screens.quickadd.QuickAddSheet
import com.betterdo.app.ui.screens.review.ReviewScreen
import com.betterdo.app.ui.screens.settings.SettingsScreen
import com.betterdo.app.ui.screens.today.TodayScreen
import com.betterdo.app.ui.screens.today.TodayViewModel
import com.betterdo.app.ui.theme.BdTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

object Routes {
    const val IMPORT = "import"
    const val TODAY = "today"
    const val REVIEW = "review"
    const val DERIVE = "derive"
    const val SETTINGS = "settings"
    const val DETAIL = "detail/{id}"
    fun detail(id: String) = "detail/$id"
}

@Composable
fun AppNav(container: AppContainer, settings: Settings) {
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var showQuickAdd by remember { mutableStateOf(false) }

    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showToast: (String) -> Unit = { msg -> scope.launch { snackbar.showSnackbar(msg) } }

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(settings.onboarded) {
        if (settings.onboarded && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val start = if (settings.onboarded) Routes.TODAY else Routes.IMPORT
    val showBottomBar = currentRoute == Routes.TODAY || currentRoute == Routes.REVIEW

    Scaffold(
        containerColor = BdTheme.colors.paper,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (showBottomBar) {
                BottomBar(
                    currentRoute = currentRoute,
                    onToday = { nav.navigateTab(Routes.TODAY) },
                    onReview = { nav.navigateTab(Routes.REVIEW) },
                    onQuickAdd = { showQuickAdd = true },
                )
            }
        },
    ) { inner ->
        NavHost(navController = nav, startDestination = start, modifier = Modifier.fillMaxSize()) {
            composable(Routes.IMPORT) {
                val vm: ImportViewModel = viewModel(factory = viewModelFactory { initializer { ImportViewModel(container) } })
                ImportScreen(vm = vm, onDone = { tone, usedSample ->
                    // The VM has already persisted the list/sample + saved the tone.
                    // Seed reminders point at the sample todos, so only schedule them then.
                    if (usedSample) ReminderScheduler.scheduleSeedReminders(context, tone)
                    nav.navigate(Routes.TODAY) { popUpTo(Routes.IMPORT) { inclusive = true } }
                })
            }
            composable(Routes.TODAY) {
                val vm: TodayViewModel = viewModel(factory = viewModelFactory { initializer { TodayViewModel(container) } })
                TodayScreen(
                    vm = vm,
                    onOpenTodo = { nav.navigate(Routes.detail(it)) },
                    onOpenDerive = { nav.navigate(Routes.DERIVE) },
                    onOpenSettings = { nav.navigate(Routes.SETTINGS) },
                    showToast = showToast,
                    contentPadding = inner,
                )
            }
            composable(Routes.REVIEW) {
                ReviewScreen(container, contentPadding = inner)
            }
            composable(
                Routes.DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                val vm: DetailViewModel = viewModel(factory = viewModelFactory { initializer { DetailViewModel(container, id) } })
                TodoDetailScreen(vm, onBack = { nav.popBackStack() })
            }
            composable(Routes.DERIVE) {
                val vm: DeriveViewModel = viewModel(factory = viewModelFactory { initializer { DeriveViewModel(container) } })
                DeriveScreen(vm, onBack = { nav.popBackStack() })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(container, onBack = { nav.popBackStack() })
            }
        }
    }

    if (showQuickAdd) {
        QuickAddSheet(
            tone = settings.tone,
            onDismiss = { showQuickAdd = false },
            onAdd = { title ->
                scope.launch { addQuickTodo(container, title) }
                showToast(container.agent.quickAddAck(settings.tone))
                showQuickAdd = false
            },
        )
    }
}

private fun NavController.navigateTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private suspend fun addQuickTodo(container: AppContainer, title: String) {
    val all = container.todoRepository.observeTodos().first()
    val pos = (all.maxOfOrNull { it.position } ?: 0) + 1
    val todo = Todo(
        id = "u-" + UUID.randomUUID().toString().take(8),
        title = title,
        tag = Tag.LIFE,
        icon = TodoIcon.SPARKLE,
        source = TodoSource.USER,
        createdAt = System.currentTimeMillis(),
        position = pos,
    )
    container.todoRepository.addTodo(todo)
    container.todoRepository.appendComment(todo.id, container.agent.commentOn(todo))
}
