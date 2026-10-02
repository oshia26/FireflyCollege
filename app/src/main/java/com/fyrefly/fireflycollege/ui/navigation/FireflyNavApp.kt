package com.fyrefly.fireflycollege.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fyrefly.fireflycollege.FireflyApp
import com.fyrefly.fireflycollege.ui.screens.assignments.AssignmentEditorScreen
import com.fyrefly.fireflycollege.ui.screens.calendar.CalendarScreen
import com.fyrefly.fireflycollege.ui.screens.courses.CourseDetailScreen
import com.fyrefly.fireflycollege.ui.screens.courses.CourseEditorScreen
import com.fyrefly.fireflycollege.ui.screens.courses.CoursesScreen
import com.fyrefly.fireflycollege.ui.screens.dashboard.DashboardScreen
import com.fyrefly.fireflycollege.ui.screens.search.SearchScreen
import com.fyrefly.fireflycollege.ui.screens.settings.SettingsScreen
import com.fyrefly.fireflycollege.util.viewModelFactory
import com.fyrefly.fireflycollege.viewmodel.AssignmentEditorViewModel
import com.fyrefly.fireflycollege.viewmodel.CourseDetailViewModel
import com.fyrefly.fireflycollege.viewmodel.CourseEditorViewModel
import com.fyrefly.fireflycollege.viewmodel.CoursesViewModel
import com.fyrefly.fireflycollege.viewmodel.DashboardViewModel

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.DASHBOARD, "Home", Icons.Rounded.Home),
    Tab(Routes.COURSES, "Courses", Icons.Rounded.School),
    Tab(Routes.CALENDAR, "Calendar", Icons.Rounded.CalendarMonth),
    Tab(Routes.SETTINGS, "Settings", Icons.Rounded.Settings)
)

private val topLevelRoutes = tabs.map { it.route }

@Composable
fun FireflyNavApp(app: FireflyApp) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in topLevelRoutes) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                            label = { androidx.compose.material3.Text(text = tab.label) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            when (currentRoute) {
                Routes.DASHBOARD -> FloatingActionButton(
                    onClick = { navController.navigate(Routes.assignmentEditor(null, null)) }
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "New assignment")
                }
                Routes.COURSES -> FloatingActionButton(
                    onClick = { navController.navigate(Routes.courseEditor(null)) }
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "New course")
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    viewModel = viewModel(factory = viewModelFactory { DashboardViewModel(app.container) }),
                    onOpenAssignment = { id ->
                        navController.navigate(Routes.assignmentEditor(id, null))
                    },
                    onOpenCourse = { id -> navController.navigate(Routes.courseDetail(id)) },
                    onOpenSearch = { navController.navigate(Routes.SEARCH) }
                )
            }

            composable(Routes.COURSES) {
                CoursesScreen(
                    viewModel = viewModel(factory = viewModelFactory { CoursesViewModel(app.container) }),
                    onOpenCourse = { id -> navController.navigate(Routes.courseDetail(id)) },
                    onEditCourse = { id -> navController.navigate(Routes.courseEditor(id)) }
                )
            }

            composable(Routes.CALENDAR) { CalendarScreen() }
            composable(Routes.SETTINGS) { SettingsScreen() }
            composable(Routes.SEARCH) { SearchScreen(onBack = { navController.popBackStack() }) }

            composable(
                route = Routes.COURSE_DETAIL,
                arguments = listOf(navArgument("courseId") { type = NavType.LongType })
            ) { entry ->
                val courseId = entry.arguments?.getLong("courseId") ?: 0L
                CourseDetailScreen(
                    viewModel = viewModel(factory = viewModelFactory { CourseDetailViewModel(app.container, courseId) }),
                    onBack = { navController.popBackStack() },
                    onEditCourse = { navController.navigate(Routes.courseEditor(it)) },
                    onAddAssignment = { navController.navigate(Routes.assignmentEditor(null, it)) },
                    onOpenAssignment = { navController.navigate(Routes.assignmentEditor(it, null)) }
                )
            }

            composable(
                route = Routes.COURSE_EDITOR,
                arguments = listOf(
                    navArgument("courseId") {
                        type = NavType.StringType
                        nullable = true
                    }
                )
            ) { entry ->
                val courseId = entry.arguments?.getString("courseId")?.toLongOrNull()
                CourseEditorScreen(
                    viewModel = viewModel(factory = viewModelFactory { CourseEditorViewModel(app.container, courseId) }),
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.ASSIGNMENT_EDITOR,
                arguments = listOf(
                    navArgument("assignmentId") {
                        type = NavType.StringType
                        nullable = true
                    },
                    navArgument("courseId") {
                        type = NavType.StringType
                        nullable = true
                    }
                )
            ) { entry ->
                val assignmentId = entry.arguments?.getString("assignmentId")?.toLongOrNull()
                val courseId = entry.arguments?.getString("courseId")?.toLongOrNull()
                AssignmentEditorScreen(
                    viewModel = viewModel(factory = viewModelFactory { AssignmentEditorViewModel(app.container, assignmentId, courseId) }),
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
