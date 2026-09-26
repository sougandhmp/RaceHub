package org.gce.racehub.auth.presentation

/** Pure: the only place [SignUpState] changes. */
internal object SignUpReducer {
    fun reduce(state: SignUpState, mutation: SignUpMutation): SignUpState = when (mutation) {
        // Any edit clears the previous error.
        is SignUpMutation.FieldsChanged -> mutation.transform(state).copy(error = null)
        SignUpMutation.Submitted -> state.copy(isLoading = true, error = null)
        is SignUpMutation.Failed -> state.copy(isLoading = false, error = mutation.failure)
        SignUpMutation.Succeeded -> SignUpState()
    }
}
