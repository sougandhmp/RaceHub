package org.gce.racehub.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.gce.racehub.auth.domain.model.User
import org.koin.compose.viewmodel.koinViewModel

private val DarkBg = Color(0xFF0A0A0A)
private val CardBg = Color(0xFF161616)
private val CardBorder = Color(0xFF262626)
private val MutedGray = Color(0xFF8E8E93)
private val RacingRed = Color(0xFFE63946)

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel(),
    onSignedOut: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ProfileEffect.SignedOut -> onSignedOut()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        val user = state.user
        if (user == null) {
            Text(
                text = "Not signed in.",
                color = MutedGray,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileHeader(user = user)
                Spacer(modifier = Modifier.height(24.dp))
                ProfileStats(user = user)
                Spacer(modifier = Modifier.height(24.dp))
                ProfileDetails(user = user)
                state.errorMessage?.let { message ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = message, color = RacingRed, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(32.dp))
                SignOutButton(
                    isSigningOut = state.isSigningOut,
                    onClick = { viewModel.onIntent(ProfileIntent.SignOut) }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ProfileHeader(user: User) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(RacingRed),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = avatarInitials(user),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = user.name,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        user.username?.takeIf { it.isNotBlank() }?.let { handle ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "@$handle",
                color = MutedGray,
                fontSize = 14.sp
            )
        }
        user.role?.takeIf { it.isNotBlank() }?.let { role ->
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(RacingRed.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = role.uppercase(),
                    color = RacingRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun ProfileStats(user: User) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatCell(label = "POSTS", value = user.postsCount.toString())
        StatCell(label = "JOINED", value = formatJoined(user.joinedAt))
        StatCell(label = "COUNTRY", value = user.country?.uppercase() ?: "—")
    }
}

@Composable
private fun StatCell(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = MutedGray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun ProfileDetails(user: User) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        DetailRow(label = "Email", value = user.email)
        user.username?.takeIf { it.isNotBlank() }?.let {
            Spacer(modifier = Modifier.height(12.dp))
            DetailRow(label = "Username", value = it)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MutedGray,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(96.dp)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SignOutButton(isSigningOut: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isSigningOut,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = RacingRed,
            contentColor = Color.White,
            disabledContainerColor = CardBg,
            disabledContentColor = MutedGray
        )
    ) {
        if (isSigningOut) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(text = "Sign Out", fontWeight = FontWeight.Bold)
        }
    }
}

private fun avatarInitials(user: User): String {
    val source = user.avatar?.takeIf { it.isNotBlank() }
        ?: user.name.takeIf { it.isNotBlank() }
        ?: user.email
    return source.trim().split(" ").take(2).map { it.first() }.joinToString("").uppercase()
}

// "2024-08-13T..." → "Aug 2024"; falls back to the raw string if parsing fails.
private fun formatJoined(joinedAt: String?): String {
    if (joinedAt.isNullOrBlank()) return "—"
    val datePart = joinedAt.substringBefore('T')
    val parts = datePart.split('-')
    if (parts.size < 2) return datePart
    val month = parts[1].toIntOrNull() ?: return datePart
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val name = months.getOrNull(month - 1) ?: return datePart
    return "$name ${parts[0]}"
}
