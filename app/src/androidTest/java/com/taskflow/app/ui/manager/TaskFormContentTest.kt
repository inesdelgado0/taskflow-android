package com.taskflow.app.ui.manager

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.taskflow.app.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TaskFormContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun taskForm_submitsEditedFields() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var savedTitle = ""
        var savedDescription = ""

        composeTestRule.setContent {
            MaterialTheme {
                TaskFormContent(
                    edit = false,
                    initialTitle = "",
                    initialDescription = "",
                    projectName = "Projeto Alpha",
                    onSave = { title, description ->
                        savedTitle = title
                        savedDescription = description
                    },
                    onCancel = {}
                )
            }
        }

        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextInput("Implementar login")
        composeTestRule.onAllNodes(hasSetTextAction())[1].performTextInput("Criar ecra e validacao")
        composeTestRule.onNodeWithText(context.getString(R.string.create_task_title)).performClick()

        assertEquals("Implementar login", savedTitle)
        assertEquals("Criar ecra e validacao", savedDescription)
    }
}
