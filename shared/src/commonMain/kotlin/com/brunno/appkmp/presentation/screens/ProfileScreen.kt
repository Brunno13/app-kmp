package com.brunno.appkmp.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brunno.appkmp.domain.enums.ThemeMode
import com.brunno.appkmp.presentation.components.AppBottomBar
import com.brunno.appkmp.presentation.components.AppButton
import com.brunno.appkmp.presentation.components.MenuCard
import com.brunno.appkmp.presentation.components.MenuCardWithTrailingContent
import com.brunno.appkmp.presentation.components.ProfileAvatar
import com.brunno.appkmp.presentation.navigation.ProfileNavigationActions
import com.brunno.appkmp.presentation.navigation.Routes
import com.brunno.appkmp.presentation.utils.decodeBase64ToImageBitmap
import com.brunno.appkmp.presentation.viewmodels.AuthViewModel
import com.brunno.appkmp.presentation.viewmodels.ProfileViewModel
import com.brunno.appkmp.presentation.viewmodels.ThemeViewModel
import kmpprojectbrunno.shared.generated.resources.Res
import kmpprojectbrunno.shared.generated.resources.action_sign_out
import kmpprojectbrunno.shared.generated.resources.desc_offline_mode
import kmpprojectbrunno.shared.generated.resources.menu_edit_profile
import kmpprojectbrunno.shared.generated.resources.menu_security
import kmpprojectbrunno.shared.generated.resources.theme_auto
import kmpprojectbrunno.shared.generated.resources.theme_dark
import kmpprojectbrunno.shared.generated.resources.theme_light
import kmpprojectbrunno.shared.generated.resources.title_app_theme
import kmpprojectbrunno.shared.generated.resources.title_offline_mode
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private data class ProfileUiState(
    val userName: String?,
    val userEmail: String?,
    val avatarData: String?,
    val themeMode: ThemeMode,
    val offlineMode: Boolean
)

@Composable
fun ProfileScreen(
    navigation: ProfileNavigationActions,
    onLogoutSuccess: () -> Unit,
    authViewModel: AuthViewModel = koinViewModel(),
    themeViewModel: ThemeViewModel = koinViewModel(),
    profileViewModel: ProfileViewModel = koinViewModel(),
) {
    val currentUser by profileViewModel.currentUser.collectAsState()
    val themeMode by themeViewModel.themeMode.collectAsState()
    var offlineMode by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser?.avatarFilename) {
        profileViewModel.syncAvatarIfNeeded(
            currentUser?.avatarFilename
        )
    }

    ProfileContent(
        state = ProfileUiState(
            userName = currentUser?.name,
            userEmail = currentUser?.email,
            avatarData = currentUser?.avatarData,
            themeMode = themeMode,
            offlineMode = offlineMode
        ),
        navigation = navigation,
        onThemeChange = { themeViewModel.setTheme(it) },
        onOfflineModeChange = { offlineMode = it },
        onLogout = {
            authViewModel.logout {
                onLogoutSuccess()
            }
        }
    )
}

@Composable
private fun ProfileContent(
    state: ProfileUiState,
    navigation: ProfileNavigationActions,
    onThemeChange: (ThemeMode) -> Unit,
    onOfflineModeChange: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AppBottomBar(
                currentRoute = Routes.PROFILE,
                onNavigateToHome = navigation.onNavigateToHome,
                onNavigateToProfile = navigation.onNavigateToProfile
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileHeader(
                userName = state.userName,
                userEmail = state.userEmail,
                avatarData = state.avatarData
            )

            ProfileSettings(
                state = state,
                navigation = navigation,
                onThemeChange = onThemeChange,
                onOfflineModeChange = onOfflineModeChange,
                onLogout = onLogout
            )
        }
    }
}

@Composable
private fun ProfileHeader(
    userName: String?,
    userEmail: String?,
    avatarData: String?
) {
    val avatarBitmap = remember(avatarData) {
        avatarData?.let { decodeBase64ToImageBitmap(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        ProfileAvatar(
            bitmap = avatarBitmap,
            userName = userName,
            contentDescription = "Profile Photo"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = userName ?: "...",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = userEmail ?: "...",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun ColumnScope.ProfileSettings(
    state: ProfileUiState,
    navigation: ProfileNavigationActions,
    onThemeChange: (ThemeMode) -> Unit,
    onOfflineModeChange: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MenuCard(
            title = stringResource(Res.string.menu_edit_profile),
            icon = Icons.Default.Edit,
            onClick = navigation.onNavigateToEditProfile
        )

        Spacer(modifier = Modifier.height(12.dp))

        MenuCard(
            title = stringResource(Res.string.menu_security),
            icon = Icons.Default.Security,
            onClick = navigation.onNavigateToSecurity
        )

        Spacer(modifier = Modifier.height(12.dp))

        ProfileThemeCard(
            themeMode = state.themeMode,
            onThemeChange = onThemeChange
        )

        Spacer(modifier = Modifier.height(12.dp))

        ProfileOfflineMode(
            offlineMode = state.offlineMode,
            onOfflineModeChange = onOfflineModeChange
        )

        Spacer(modifier = Modifier.height(32.dp))

        AppButton(
            text = stringResource(Res.string.action_sign_out),
            onClick = onLogout,
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun ProfileThemeCard(
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Brightness4,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = stringResource(Res.string.title_app_theme),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOptionButton(
                    text = stringResource(Res.string.theme_light).uppercase(),
                    isSelected = themeMode == ThemeMode.LIGHT,
                    onClick = { onThemeChange(ThemeMode.LIGHT) },
                    modifier = Modifier.weight(1f)
                )

                ThemeOptionButton(
                    text = stringResource(Res.string.theme_dark).uppercase(),
                    isSelected = themeMode == ThemeMode.DARK,
                    onClick = { onThemeChange(ThemeMode.DARK) },
                    modifier = Modifier.weight(1f)
                )

                ThemeOptionButton(
                    text = stringResource(Res.string.theme_auto).uppercase(),
                    isSelected = themeMode == ThemeMode.AUTO,
                    onClick = { onThemeChange(ThemeMode.AUTO) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ProfileOfflineMode(
    offlineMode: Boolean,
    onOfflineModeChange: (Boolean) -> Unit
) {
    MenuCardWithTrailingContent(
        title = stringResource(Res.string.title_offline_mode),
        icon = Icons.Default.Wifi,
        subtitle = stringResource(Res.string.desc_offline_mode),
        trailingContent = {
            Switch(
                checked = offlineMode,
                onCheckedChange = onOfflineModeChange
            )
        }
    )
}

@Composable
fun ThemeOptionButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isSelected) {
        Button(
            onClick = onClick,
            modifier = modifier.height(40.dp),
            shape = MaterialTheme.shapes.small,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(40.dp),
            shape = MaterialTheme.shapes.small,
            contentPadding = PaddingValues(0.dp),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Preview(
    name = "Profile",
    showBackground = true
)
@Composable
private fun ProfileScreenPreview() {
    ProfileContent(
        state = ProfileUiState(
            userName = "Brunno Silva",
            userEmail = "brunno@email.com",
            avatarData = null,
            themeMode = ThemeMode.AUTO,
            offlineMode = false
        ),
        navigation = ProfileNavigationActions(
            onNavigateToHome = {},
            onNavigateToProfile = {},
            onNavigateToEditProfile = {},
            onNavigateToSecurity = {}
        ),
        onThemeChange = {},
        onOfflineModeChange = {},
        onLogout = {}
    )
}
