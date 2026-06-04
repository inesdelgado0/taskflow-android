package com.taskflow.app.ui.navigation

object Routes {

    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val ADMIN_DASHBOARD = "admin/dashboard"
    const val MANAGER_DASHBOARD = "manager/dashboard"
    const val USER_DASHBOARD = "user/dashboard"

    const val ONBOARDING_TEAM = "onboarding/team"
    const val ONBOARDING_PROGRESS = "onboarding/progress"

    const val ADMIN_PROJECTS = "admin/projects"
    const val ADMIN_PROJECT_CREATE = "admin/projects/create"
    const val ADMIN_PROJECT_EDIT = "admin/projects/edit"
    const val ADMIN_PROJECT_DETAILS = "admin/projects/details"
    const val ADMIN_USERS_LIST = "admin/users"
    const val ADMIN_USER_CREATE = "admin/users/create"
    const val ADMIN_USER_EDIT = "admin/users/edit"
    const val ADMIN_STATS = "admin/statistics"
    const val ADMIN_PROFILE = "admin/profile"

    const val MANAGER_TASKS_LIST = "manager/tasks"
    const val MANAGER_TASK_CREATE = "manager/tasks/create"
    const val MANAGER_TASK_EDIT = "manager/tasks/edit"
    const val MANAGER_TASK_DETAILS = "manager/tasks/details"
    const val MANAGER_TEAM = "manager/team"
    const val MANAGER_ADD_TEAM = "manager/team/add"
    const val MANAGER_ASSIGN_USERS = "manager/tasks/assign-users"
    const val MANAGER_EVALUATE_USER = "manager/evaluate-user"
    const val MANAGER_PROJECTS = "manager/projects"
    const val MANAGER_PROJECT_DETAILS = "manager/projects/details"
    const val MANAGER_PROFILE = "manager/profile"

    const val USER_TASK_DETAILS = "user/tasks/details"
    const val USER_HISTORY = "user/history"
    const val USER_PROFILE = "user/profile"
    const val USER_TASKS = "user/tasks"
    const val USER_TASK_HISTORY = "user/tasks/history"

    const val MANAGER_ID_ARG = "managerId"
    const val MANAGER_DASHBOARD_WITH_ID = "$MANAGER_DASHBOARD/{$MANAGER_ID_ARG}"
    const val USER_TASK_ID_ARG = "taskId"
    const val USER_TASK_EXECUTION = "user/tasks/{$USER_TASK_ID_ARG}/execution"

    fun managerDashboard(managerId: Long): String = "$MANAGER_DASHBOARD/$managerId"
    fun userTaskExecution(taskId: Long): String = "user/tasks/$taskId/execution"
}

