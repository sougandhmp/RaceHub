package org.gce.racehub.login

/**
 * Immutable snapshot of everything the Login screen needs to render itself.
 *
 * A new instance is produced by the ViewModel for every state change;
 * the View never mutates this object directly.
 */
data class LoginState(

    /** Current text in the email input field. */
    val email: String = "",

    /** Current text in the password input field. */
    val password: String = "",

    /** When true the password field renders plain text instead of dots. */
    val isPasswordVisible: Boolean = false,

    /** True while the login request is in-flight; used to show a spinner
     *  and disable the submit button. */
    val isLoading: Boolean = false,

    /** Non-null when a validation or server error should be shown to the user.
     *  Cleared by dispatching [LoginIntent.DismissError]. */
    val errorMessage: String? = null
)
