package org.gce.racehub.auth.presentation

/** Pure: the only place [SignUpState] changes. */
internal object SignUpReducer {
    fun reduce(state: SignUpState, mutation: SignUpMutation): SignUpState = when (mutation) {
        // Any edit clears the previous error.
        is SignUpMutation.FieldsChanged -> mutation.transform(state).copy(errorMessage = null)
        SignUpMutation.Submitted -> state.copy(isLoading = true, errorMessage = null)
        is SignUpMutation.Failed -> state.copy(isLoading = false, errorMessage = mutation.message)
        SignUpMutation.Succeeded -> SignUpState()
    }
}
