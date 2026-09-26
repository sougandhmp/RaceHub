package org.gce.racehub.auth.presentation

/** Pure: the only place [ForgotPasswordState] changes. */
internal object ForgotPasswordReducer {
    fun reduce(state: ForgotPasswordState, mutation: ForgotPasswordMutation): ForgotPasswordState = when (mutation) {
        is ForgotPasswordMutation.FieldsChanged -> mutation.transform(state).copy(error = null)
        ForgotPasswordMutation.Submitted -> state.copy(isLoading = true, error = null)
        is ForgotPasswordMutation.Failed -> state.copy(isLoading = false, error = mutation.failure)
        ForgotPasswordMutation.CodeSent -> state.copy(isLoading = false, step = ForgotPasswordStep.Confirm)
        // Leaving the flow: clear passwords and the code.
        ForgotPasswordMutation.ResetCompleted -> ForgotPasswordState()
    }
}
