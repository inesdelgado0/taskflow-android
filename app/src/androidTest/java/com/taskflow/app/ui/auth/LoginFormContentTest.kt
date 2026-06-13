package com.taskflow.app.ui.auth

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.taskflow.app.R
import com.taskflow.app.domain.util.UserRole
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LoginFormContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginForm_submitsEmailPasswordAndRole() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var email by mutableStateOf("")
        var password by mutableStateOf("")
        var selectedRole by mutableStateOf(UserRole.ADMIN)
        var submittedEmail = ""
        var submittedPassword = ""
        var submittedRole = UserRole.ADMIN

        composeTestRule.setContent {
            MaterialTheme {
                LoginFormContent(
                    modifier = androidx.compose.ui.Modifier,
                    email = email,
                    password = password,
                    passwordVisible = false,
                    selectedRole = selectedRole,
                    formState = AuthFormState(),
                    uiState = AuthUiState.Idle,
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onPasswordVisibilityChange = {},
                    onRoleChange = { selectedRole = it },
                    onLogin = {
                        submittedEmail = email
                        submittedPassword = password
                        submittedRole = selectedRole
                    },
                    onRegisterClick = {}
                )
            }
        }

        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextInput("user@example.com")
        composeTestRule.onAllNodes(hasSetTextAction())[1].performTextInput("password123")
        composeTestRule.onNodeWithText(context.getString(R.string.user_role_user_short)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.login_btn_submit)).performClick()

        assertEquals("user@example.com", submittedEmail)
        assertEquals("password123", submittedPassword)
        assertEquals(UserRole.USER, submittedRole)
    }

    @Test
    fun loginForm_showsErrorMessage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val errorMessage = context.getString(R.string.error_invalid_credentials)

        composeTestRule.setContent {
            MaterialTheme {
                LoginFormContent(
                    modifier = androidx.compose.ui.Modifier,
                    email = "",
                    password = "",
                    passwordVisible = false,
                    selectedRole = UserRole.ADMIN,
                    formState = AuthFormState(),
                    uiState = AuthUiState.Error(R.string.error_invalid_credentials),
                    onEmailChange = {},
                    onPasswordChange = {},
                    onPasswordVisibilityChange = {},
                    onRoleChange = {},
                    onLogin = {},
                    onRegisterClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }
}