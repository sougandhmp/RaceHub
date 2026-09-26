package org.gce.racehub.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import org.gce.racehub.auth.presentation.EmailVerificationViewModel
import org.gce.racehub.auth.presentation.ForgotPasswordViewModel
import org.gce.racehub.auth.presentation.LoginViewModel
import org.gce.racehub.auth.presentation.SignUpViewModel
import org.gce.racehub.forum.presentation.CreateThreadViewModel
import org.gce.racehub.forum.presentation.ForumViewModel
import org.gce.racehub.forum.presentation.ThreadDetailViewModel
import org.gce.racehub.home.presentation.HomeViewModel
import org.gce.racehub.profile.presentation.ProfileViewModel
import org.gce.racehub.race.presentation.RaceViewModel
import org.koin.mp.KoinPlatform

/**
 * Owns the shared ViewModels of one SwiftUI screen. Swift must call [clear]
 * when the screen goes away: that runs `onCleared()` and cancels
 * `viewModelScope`, exactly as Android does when a screen leaves the back stack.
 */
class ViewModelOwner : ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()

    fun clear() = viewModelStore.clear()
}

/**
 * Swift entry point for shared ViewModels. Each call returns the ViewModel held
 * by [owner], creating it through Koin on first use.
 */
object SharedViewModels {

    fun race(owner: ViewModelOwner): RaceViewModel = owner.provide { KoinPlatform.getKoin().get<RaceViewModel>() }

    fun forum(owner: ViewModelOwner): ForumViewModel = owner.provide { KoinPlatform.getKoin().get<ForumViewModel>() }

    fun threadDetail(owner: ViewModelOwner): ThreadDetailViewModel = owner.provide { KoinPlatform.getKoin().get<ThreadDetailViewModel>() }

    fun createThread(owner: ViewModelOwner): CreateThreadViewModel = owner.provide { KoinPlatform.getKoin().get<CreateThreadViewModel>() }

    fun profile(owner: ViewModelOwner): ProfileViewModel = owner.provide { KoinPlatform.getKoin().get<ProfileViewModel>() }

    fun home(owner: ViewModelOwner): HomeViewModel = owner.provide { KoinPlatform.getKoin().get<HomeViewModel>() }

    fun login(owner: ViewModelOwner): LoginViewModel = owner.provide { KoinPlatform.getKoin().get<LoginViewModel>() }

    fun signUp(owner: ViewModelOwner): SignUpViewModel = owner.provide { KoinPlatform.getKoin().get<SignUpViewModel>() }

    fun forgotPassword(owner: ViewModelOwner): ForgotPasswordViewModel = owner.provide { KoinPlatform.getKoin().get<ForgotPasswordViewModel>() }

    fun emailVerification(owner: ViewModelOwner): EmailVerificationViewModel =
        owner.provide { KoinPlatform.getKoin().get<EmailVerificationViewModel>() }

    private inline fun <reified VM : ViewModel> ViewModelOwner.provide(crossinline create: () -> VM): VM =
        ViewModelProvider.create(this, viewModelFactory { initializer { create() } })[VM::class]
}
