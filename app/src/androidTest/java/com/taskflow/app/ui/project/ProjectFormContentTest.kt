package com.taskflow.app.ui.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.taskflow.app.R
import com.taskflow.app.domain.util.ProjectStatus
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProjectFormContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun projectForm_submitsEnteredValues() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var savedName = ""
        var savedDescription = ""
        var savedStatus = ProjectStatus.CANCELLED

        composeTestRule.setContent {
            MaterialTheme {
                ProjectFormContent(
                    edit = false,
                    initialName = "",
                    initialDescription = "",
                    initialStartDate = null,
                    initialEndDate = null,
                    initialManagerId = null,
                    initialStatus = ProjectStatus.ACTIVE,
                    managers = listOf(1L to "Joao Silva"),
                    onDirtyChange = {},
                    onSave = { name, description, _, _, _, status ->
                        savedName = name
                        savedDescription = description
                        savedStatus = status
                    },
                    onCancel = {}
                )
            }
        }

        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextInput("Projeto Alpha")
        composeTestRule.onAllNodes(hasSetTextAction())[1].performTextInput("Descricao do projeto")
        composeTestRule.onNodeWithText(context.getString(R.string.create_project)).performClick()

        assertEquals("Projeto Alpha", savedName)
        assertEquals("Descricao do projeto", savedDescription)
        assertEquals(ProjectStatus.ACTIVE, savedStatus)
    }
}
