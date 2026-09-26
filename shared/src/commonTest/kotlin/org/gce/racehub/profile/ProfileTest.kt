package org.gce.racehub.profile

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.gce.racehub.core.domain.model.User
import org.gce.racehub.core.domain.session.UserSession
import org.gce.racehub.auth.domain.usecase.LogoutUseCase
import org.gce.racehub.core.domain.DataError
import org.gce.racehub.fake.FakeAuthRepository
import org.gce.racehub.fake.FakeProfileRepository
import org.gce.racehub.fake.FakeSessionStorage
import org.gce.racehub.home.presentation.HomeIntent
import org.gce.racehub.home.presentation.HomeTab
import org.gce.racehub.home.presentation.HomeViewModel
import org.gce.racehub.profile.presentation.ProfileEffect
import org.gce.racehub.profile.presentation.ProfileIntent
import org.gce.racehub.profile.presentation.ProfileMutation
import org.gce.racehub.profile.presentation.ProfileReducer
import org.gce.racehub.profile.presentation.ProfileState
import org.gce.racehub.profile.presentation.ProfileViewModel
import org.gce.racehub.profile.domain.model.UserProfile
import org.gce.racehub.profile.domain.usecase.GetMyProfileUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileTest {

    private val dispatcher = StandardTestDispatcher()
    private val homeRepo = FakeProfileRepository()
    private val authRepo = FakeAuthRepository()
    private val ann = User(id = "u1", email = "a@b.c", name = "Ann", token = "tok", postsCount = 2)
    private val profile = UserProfile("ann", "a@b.c", "A", postsCount = 5, savedCount = 1, listOf("Recent"), listOf("Saved"))

    private fun viewModel(session: UserSession) =
        ProfileViewModel(session, LogoutUseCase(authRepo, session), GetMyProfileUseCase(homeRepo))

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    // ── Reducer ─────────────────────────────────────────────────────────────

    @Test
    fun `a different user never sees the previous profile`() {
        val shown = ProfileState(user = ann, profile = profile)
        val other = ann.copy(id = "u2")
        assertEquals(ProfileState(user = other), ProfileReducer.reduce(shown, ProfileMutation.UserChanged(other)))
    }

    @Test
    fun `stats fall back to the session user until the profile loads`() {
        val beforeLoad = ProfileState(user = ann)
        assertEquals(2, beforeLoad.postsCount)
        assertNull(beforeLoad.savedCount)

        val loaded = ProfileReducer.reduce(beforeLoad, ProfileMutation.Loaded(profile))
        assertEquals(5, loaded.postsCount)
        assertEquals(1, loaded.savedCount)
        assertEquals(listOf("Recent"), loaded.recentThreadTitles)
    }

    // ── ViewModel ───────────────────────────────────────────────────────────

    @Test
    fun `loads the signed-in user's profile on creation`() = runTest(dispatcher) {
        homeRepo.profileResult = profile
        val vm = viewModel(UserSession(FakeSessionStorage(ann)))
        advanceUntilIdle()
        assertEquals(ann, vm.state.value.user)
        assertEquals(profile, vm.state.value.profile)
        assertFalse(vm.state.value.isLoadingProfile)
    }

    @Test
    fun `profile load failure emits an error effect`() = runTest(dispatcher) {
        homeRepo.profileError = DataError.Network
        val vm = viewModel(UserSession(FakeSessionStorage(ann)))
        advanceUntilIdle()
        assertEquals(ProfileEffect.ShowLoadError(DataError.Network), vm.effects.first())
    }

    @Test
    fun `sign out clears the session even when the server revoke fails`() = runTest(dispatcher) {
        authRepo.logoutResult = false
        val session = UserSession(FakeSessionStorage(ann))
        val vm = viewModel(session)
        advanceUntilIdle()

        vm.onIntent(ProfileIntent.SignOut)
        advanceUntilIdle()

        assertNull(session.currentUser.value)
        assertEquals(ProfileState(), vm.state.value)
        assertEquals(ProfileEffect.SignedOut, vm.effects.first())
    }

    @Test
    fun `home tab selection`() {
        val vm = HomeViewModel()
        vm.onIntent(HomeIntent.TabSelected(HomeTab.Profile))
        assertEquals(HomeTab.Profile, vm.state.value.selectedTab)
    }
}
