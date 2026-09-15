package com.brunno.appkmp.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brunno.appkmp.presentation.components.AlertType
import com.brunno.appkmp.presentation.components.AppModal
import com.brunno.appkmp.presentation.components.AppTextField
import com.brunno.appkmp.presentation.components.AppTopBar
import com.brunno.appkmp.presentation.theme.dimens
import com.brunno.appkmp.presentation.utils.asString
import com.brunno.appkmp.presentation.utils.decodeBase64ToImageBitmap
import com.brunno.appkmp.presentation.utils.rememberCameraLauncher
import com.brunno.appkmp.presentation.viewmodels.AuthViewModel
import com.brunno.appkmp.presentation.viewmodels.LoginUiState
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import kmpprojectbrunno.shared.generated.resources.Res
import kmpprojectbrunno.shared.generated.resources.action_change_photo
import kmpprojectbrunno.shared.generated.resources.action_choose_from_gallery
import kmpprojectbrunno.shared.generated.resources.action_save_changes
import kmpprojectbrunno.shared.generated.resources.action_take_photo
import kmpprojectbrunno.shared.generated.resources.desc_camera
import kmpprojectbrunno.shared.generated.resources.desc_edit_profile_photo
import kmpprojectbrunno.shared.generated.resources.desc_gallery
import kmpprojectbrunno.shared.generated.resources.modal_error_title
import kmpprojectbrunno.shared.generated.resources.modal_success_profile_update
import kmpprojectbrunno.shared.generated.resources.modal_success_title
import kmpprojectbrunno.shared.generated.resources.placeholder_full_name
import kmpprojectbrunno.shared.generated.resources.title_change_profile_picture
import kmpprojectbrunno.shared.generated.resources.title_edit_profile
import kmpprojectbrunno.shared.generated.resources.title_update_profile
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random


private const val PROFILE_IMAGE_SUFFIX_MIN = 10_000
private const val PROFILE_IMAGE_SUFFIX_MAX_EXCLUSIVE = 99_999
@OptIn(ExperimentalEncodingApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var name by remember(currentUser) { mutableStateOf(currentUser?.name ?: "") }
    var selectedBase64 by remember { mutableStateOf<String?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedMimeType by remember { mutableStateOf<String?>(null) }
    var showImageSourceSheet by remember { mutableStateOf(false) }

    val singleImagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { byteArrays ->
            byteArrays.firstOrNull()?.let { bytes ->
                selectedBase64 = Base64.encode(bytes)
                val fileSuffix = Random.nextInt(
                    PROFILE_IMAGE_SUFFIX_MIN,
                    PROFILE_IMAGE_SUFFIX_MAX_EXCLUSIVE
                )
                selectedFileName = "profile_gallery_$fileSuffix.jpg"
                selectedMimeType = "image/jpeg"
            }
        }
    )

    val cameraPicker = rememberCameraLauncher { bytes ->
        if (bytes != null) {
            selectedBase64 = Base64.encode(bytes)
            val fileSuffix = Random.nextInt(
                PROFILE_IMAGE_SUFFIX_MIN,
                PROFILE_IMAGE_SUFFIX_MAX_EXCLUSIVE
            )
            selectedFileName = "profile_camera_$fileSuffix.jpg"
            selectedMimeType = "image/jpeg"
        }
    }

    val bitmapToDisplay = remember(selectedBase64, currentUser?.avatarData) {
        val base64ToUse = selectedBase64 ?: currentUser?.avatarData
        base64ToUse?.let { decodeBase64ToImageBitmap(it) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = stringResource(Res.string.title_edit_profile), onBackClick = onBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = MaterialTheme.dimens.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceExtraLarge))

            Box(contentAlignment = Alignment.BottomCenter) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmapToDisplay != null) {
                        Image(
                            bitmap = bitmapToDisplay,
                            contentDescription = stringResource(Res.string.desc_edit_profile_photo),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = currentUser?.name?.take(1)?.uppercase() ?: "",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = { showImageSourceSheet = true },
                    modifier = Modifier.offset(y = 12.dp).height(32.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = stringResource(Res.string.action_change_photo),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceXXL))

            Text(
                text = stringResource(Res.string.title_update_profile),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceLarge))

            AppTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = stringResource(Res.string.placeholder_full_name)
            )

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceLarge))

            Button(
                onClick = {
                    if (selectedBase64 != null && selectedFileName != null && selectedMimeType != null) {
                        viewModel.updateAvatar(selectedBase64!!, selectedFileName!!, selectedMimeType!!)
                        if (name.isNotBlank() && name != currentUser?.name) {
                            viewModel.updateUser(name)
                        }
                    } else if (name.isNotBlank() && name != currentUser?.name) {
                        viewModel.updateUser(name)
                    }
                },
                enabled =
                    (
                        (name.isNotBlank() && name != currentUser?.name) ||
                                selectedBase64 != null
                        ) &&
                        uiState !is LoginUiState.Loading,
                modifier = Modifier.fillMaxWidth().height(MaterialTheme.dimens.buttonHeight),
                shape = MaterialTheme.shapes.medium
            ) {
                if (uiState is LoginUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(MaterialTheme.dimens.spaceLarge),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = stringResource(Res.string.action_save_changes), fontWeight = FontWeight.Bold)
                }
            }
        }

        // Modais de Resultado
        when (val state = uiState) {
            is LoginUiState.Success -> {
                AppModal(
                    title = stringResource(Res.string.modal_success_title),
                    message = stringResource(Res.string.modal_success_profile_update),
                    type = AlertType.SUCCESS,
                    onDismiss = {
                        viewModel.resetState()
                        onBack()
                    }
                )
            }
            is LoginUiState.Error -> {
                AppModal(
                    title = stringResource(Res.string.modal_error_title),
                    message = state.error.asString(),
                    type = AlertType.ERROR,
                    onDismiss = {
                        viewModel.resetState()
                    }
                )
            }
            else -> {}
        }

        if (showImageSourceSheet) {
            ModalBottomSheet(
                onDismissRequest = { showImageSourceSheet = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.title_change_profile_picture),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    ListItem(
                        headlineContent = { Text(stringResource(Res.string.action_take_photo)) },
                        leadingContent = {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = stringResource(Res.string.desc_camera)
                            )
                        },
                        modifier = Modifier.clickable {
                            showImageSourceSheet = false
                            cameraPicker.launch()
                        },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                    )

                    ListItem(
                        headlineContent = { Text(stringResource(Res.string.action_choose_from_gallery)) },
                        leadingContent = {
                            Icon(
                                Icons.Default.PhotoLibrary,
                                contentDescription = stringResource(Res.string.desc_gallery)
                            )
                        },
                        modifier = Modifier.clickable {
                            showImageSourceSheet = false
                            singleImagePicker.launch()
                        },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
