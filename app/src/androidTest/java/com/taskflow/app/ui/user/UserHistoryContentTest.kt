package com.taskflow.app.ui.user

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.taskflow.app.data.local.entity.UserTaskEntity
import com.taskflow.app.domain.model.Evaluation
import com.taskflow.app.domain.model.Project
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.util.ProjectStatus
import com.taskflow.app.domain.util.TaskPriority
import com.taskflow.app.domain.util.TaskStatus
import org.junit.Rule
import org.junit.Test

class UserHistoryContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun historyFilters_switchBetweenAllCompletedAndCancelled() {
        val now = System.currentTimeMillis()
        val tasks = listOf(
            Task(
                id = 1,
                projectId = 1,
                title = "Task Completed",
                priority = TaskPriority.MEDIUM,
                status = TaskStatus.COMPLETED,
                createdBy = 1,
                createdAt = now,
                updatedAt = now
            ),
            Task(
                id = 2,
                projectId = 1,
                title = "Task Cancelled",
                priority = TaskPriority.HIGH,
                status = TaskStatus.CANCELLED,
                createdBy = 1,
                createdAt = now,
                updatedAt = now
            )
        )
        val projects = listOf(
            Project(
                id = 1,
                name = "Projeto",
                description = null,
                startDate = null,
                endDate = null,
                status = ProjectStatus.ACTIVE,
                managerId = null,
                createdBy = 1,
                createdAt = now,
                updatedAt = now
            )
        )

        composeTestRule.setContent {
            MaterialTheme {
                UserHistoryContent(
                    tasks = tasks,
                    projects = projects,
                    evaluations = listOf(
                        Evaluation(
                            projectId = 1,
                            evaluatorId = 1,
                            evaluatedUserId = 1,
                            rating = 4,
                            createdAt = now
                        )
                    ),
                    assignments = listOf(
                        UserTaskEntity(
                            userId = 1L,
                            taskId = 1L,
                            completionPercentage = 100,
                            isCompleted = true,
                            updatedAt = now
                        ),
                        UserTaskEntity(
                            userId = 1L,
                            taskId = 2L,
                            completionPercentage = 100,
                            isCompleted = true,
                            updatedAt = now
                        )
                    ),
                    currentUserId = 1L
                )
            }
        }

        composeTestRule.onNodeWithText("Task Completed").assertIsDisplayed()
        composeTestRule.onNodeWithText("Task Cancelled").assertIsDisplayed()

        composeTestRule.onNodeWithTag("history_filter_completed").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Task Completed").assertIsDisplayed()

        composeTestRule.onNodeWithTag("history_filter_cancelled").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Task Cancelled").assertIsDisplayed()

        composeTestRule.onNodeWithTag("history_filter_all").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Task Completed").assertIsDisplayed()
        composeTestRule.onNodeWithText("Task Cancelled").assertIsDisplayed()
    }
}
