package org.gce.racehub.signup

/**
 * Immutable snapshot of everything the Sign-Up screen needs to render itself.
 *
 * A new instance is produced by the ViewModel for every state change;
 * the View never mutates this object directly.
 */
data class SignUpState(

    /** Current text in the full-name input field. */
    val name: String = "",

    /** Current text in the email input field. */
    val email: String = "",

    /** Current text in the password input field. */
    val password: String = "",

    /** Current text in the confirm-password input field. */
    val confirmPassword: String = "",

    /** When true the password field renders plain text instead of dots. */
    val isPasswordVisible: Boolean = false,

    /** When true the confirm-password field renders plain text instead of dots. */
    val isConfirmPasswordVisible: Boolean = false,

    /** True while the sign-up request is in-flight; used to show a spinner
     *  and disable the submit button. */
    val isLoading: Boolean = false,

    /** Non-null when a validation or server error should be shown to the user. */
    val errorMessage: String? = null
)
