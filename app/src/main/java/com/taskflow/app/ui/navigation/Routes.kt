package com.taskflow.app.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val ADMIN_DASHBOARD = "admin/dashboard"
    const val MANAGER_DASHBOARD = "manager/dashboard"
    const val USER_DASHBOARD = "user/dashboard"

    const val MANAGER_ID_ARG = "managerId"
    const val MANAGER_DASHBOARD_WITH_ID = "$MANAGER_DASHBOARD/{$MANAGER_ID_ARG}"

    fun managerDashboard(managerId: Long): String = "$MANAGER_DASHBOARD/$managerId"
}

