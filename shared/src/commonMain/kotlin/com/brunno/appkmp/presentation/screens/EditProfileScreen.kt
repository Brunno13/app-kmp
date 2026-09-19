package com.brunno.appkmp.presentation.screens

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brunno.appkmp.presentation.components.AlertType
import com.brunno.appkmp.presentation.components.AppModal
import com.brunno.appkmp.presentation.components.AppTextField
import com.brunno.appkmp.presentation.components.AppTopBar
import com.brunno.appkmp.presentation.components.ProfileAvatar
import com.brunno.appkmp.presentation.theme.dimens
import com.brunno.appkmp.presentation.utils.asString
import com.brunno.appkmp.presentation.utils.decodeBase64ToImageBitmap
import com.brunno.appkmp.presentation.utils.rememberCameraLauncher
import com.brunno.appkmp.presentation.viewmodels.ProfileActionState
import com.brunno.appkmp.presentation.viewmodels.ProfileViewModel
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

private data class SelectedProfileImage(
    val base64: String,
    val fileName: String,
    val mimeType: String
)

private class ProfileImagePickerActions(
    val launchCamera: () -> Unit,
    val launchGallery: () -> Unit
)


@OptIn(ExperimentalEncodingApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var name by remember(currentUser) { mutableStateOf(currentUser?.name ?: "") }
    var selectedImage by remember { mutableStateOf<SelectedProfileImage?>(null) }
    var showImageSourceSheet by remember { mutableStateOf(false) }

    val imagePickerActions = rememberProfileImagePickerActions(
        onImageSelected = { selectedImage = it }
    )

    val hasNameChanged = name.isNotBlank() && name != currentUser?.name
    val hasChanges = hasNameChanged || selectedImage != null

    EditProfileContent(
        onBack = onBack,
        photoContent = {
            ProfilePhotoSection(
                selectedBase64 = selectedImage?.base64,
                avatarData = currentUser?.avatarData,
                userName = currentUser?.name,
                onChangePhotoClick = { showImageSourceSheet = true }
            )
        },
        formContent = {
            EditProfileFormSection(
                name = name,
                onNameChange = { name = it },
                hasChanges = hasChanges,
                isLoading = uiState is ProfileActionState.Loading,
                onSave = {
                    submitProfileChanges(
                        viewModel = viewModel,
                        selectedImage = selectedImage,
                        hasNameChanged = hasNameChanged,
                        name = name
                    )
                }
            )
        }
    )

    EditProfileResultModal(
        uiState = uiState,
        onSuccessDismiss = {
            viewModel.resetState()
            onBack()
        },
        onErrorDismiss = viewModel::resetState
    )

    ImageSourceSheet(
        visible = showImageSourceSheet,
        onDismiss = { showImageSourceSheet = false },
        onTakePhoto = {
            showImageSourceSheet = false
            imagePickerActions.launchCamera()
        },
        onChooseFromGallery = {
            showImageSourceSheet = false
            imagePickerActions.launchGallery()
        }
    )
}

@Composable
private fun ProfilePhotoSection(
    selectedBase64: String?,
    avatarData: String?,
    userName: String?,
    onChangePhotoClick: () -> Unit
) {
    val bitmapToDisplay = remember(selectedBase64, avatarData) {
        val base64ToUse = selectedBase64 ?: avatarData
        base64ToUse?.let { decodeBase64ToImageBitmap(it) }
    }

    Box(contentAlignment = Alignment.BottomCenter) {
        ProfileAvatar(
            bitmap = bitmapToDisplay,
            userName = userName,
            contentDescription = stringResource(
                Res.string.desc_edit_profile_photo
            )
        )

        Button(
            onClick = onChangePhotoClick,
            modifier = Modifier.offset(y = 12.dp).height(32.dp),
            shape = CircleShape,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = stringResource(Res.string.action_change_photo),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}


@Composable
private fun EditProfileFormSection(
    name: String,
    onNameChange: (String) -> Unit,
    hasChanges: Boolean,
    isLoading: Boolean,
    onSave: () -> Unit
) {
    Text(
        text = stringResource(
            Res.string.title_update_profile
        ),
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.onBackground
    )

    Spacer(
        modifier = Modifier.height(
            MaterialTheme.dimens.spaceLarge
        )
    )

    AppTextField(
        value = name,
        onValueChange = onNameChange,
        placeholder = stringResource(
            Res.string.placeholder_full_name
        )
    )

    Spacer(
        modifier = Modifier.height(
            MaterialTheme.dimens.spaceLarge
        )
    )

    Button(
        onClick = onSave,
        enabled = hasChanges && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(MaterialTheme.dimens.buttonHeight),
        shape = MaterialTheme.shapes.medium
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(
                    MaterialTheme.dimens.spaceLarge
                ),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = stringResource(
                    Res.string.action_save_changes
                ),
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
private fun EditProfileResultModal(
    uiState: ProfileActionState,
    onSuccessDismiss: () -> Unit,
    onErrorDismiss: () -> Unit
) {
    when (uiState) {
        is ProfileActionState.Success -> {
            AppModal(
                title = stringResource(
                    Res.string.modal_success_title
                ),
                message = stringResource(
                    Res.string.modal_success_profile_update
                ),
                type = AlertType.SUCCESS,
                onDismiss = onSuccessDismiss
            )
        }

        is ProfileActionState.Error -> {
            AppModal(
                title = stringResource(
                    Res.string.modal_error_title
                ),
                message = uiState.error.asString(),
                type = AlertType.ERROR,
                onDismiss = onErrorDismiss
            )
        }

        else -> Unit
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImageSourceSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onChooseFromGallery: () -> Unit
) {
    if (!visible) {
        return
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 24.dp,
                    vertical = 16.dp
                )
        ) {
            Text(
                text = stringResource(
                    Res.string.title_change_profile_picture
                ),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(
                    bottom = 16.dp
                )
            )

            ImageSourceOption(
                title = stringResource(
                    Res.string.action_take_photo
                ),
                icon = Icons.Default.CameraAlt,
                contentDescription = stringResource(
                    Res.string.desc_camera
                ),
                onClick = onTakePhoto
            )

            ImageSourceOption(
                title = stringResource(
                    Res.string.action_choose_from_gallery
                ),
                icon = Icons.Default.PhotoLibrary,
                contentDescription = stringResource(
                    Res.string.desc_gallery
                ),
                onClick = onChooseFromGallery
            )

            Spacer(
                modifier = Modifier.height(32.dp)
            )
        }
    }
}


@Composable
private fun ImageSourceOption(
    title: String,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(title)
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription
            )
        },
        modifier = Modifier.clickable(
            onClick = onClick
        ),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@OptIn(ExperimentalEncodingApi::class)
@Composable
private fun rememberProfileImagePickerActions(
    onImageSelected: (SelectedProfileImage) -> Unit
): ProfileImagePickerActions {
    val scope = rememberCoroutineScope()

    val galleryPicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { byteArrays ->
            byteArrays.firstOrNull()?.let { bytes ->
                val fileSuffix = Random.nextInt(
                    PROFILE_IMAGE_SUFFIX_MIN,
                    PROFILE_IMAGE_SUFFIX_MAX_EXCLUSIVE
                )

                onImageSelected(
                    SelectedProfileImage(
                        base64 = Base64.encode(bytes),
                        fileName = "profile_gallery_$fileSuffix.jpg",
                        mimeType = "image/jpeg"
                    )
                )
            }
        }
    )

    val cameraPicker = rememberCameraLauncher { bytes ->
        if (bytes != null) {
            val fileSuffix = Random.nextInt(
                PROFILE_IMAGE_SUFFIX_MIN,
                PROFILE_IMAGE_SUFFIX_MAX_EXCLUSIVE
            )

            onImageSelected(
                SelectedProfileImage(
                    base64 = Base64.encode(bytes),
                    fileName = "profile_camera_$fileSuffix.jpg",
                    mimeType = "image/jpeg"
                )
            )
        }
    }

    return remember(galleryPicker, cameraPicker) {
        ProfileImagePickerActions(
            launchCamera = { cameraPicker.launch() },
            launchGallery = { galleryPicker.launch() }
        )
    }
}

@Composable
private fun EditProfileContent(
    onBack: () -> Unit,
    photoContent: @Composable () -> Unit,
    formContent: @Composable () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = stringResource(Res.string.title_edit_profile),
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = MaterialTheme.dimens.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(
                modifier = Modifier.height(
                    MaterialTheme.dimens.spaceExtraLarge
                )
            )

            photoContent()

            Spacer(
                modifier = Modifier.height(
                    MaterialTheme.dimens.spaceXXL
                )
            )

            formContent()
        }
    }
}

@Preview(
    name = "Edit Profile",
    showBackground = true
)
@Composable
private fun EditProfileScreenPreview() {
    EditProfileContent(
        onBack = {},
        photoContent = {
            ProfilePhotoSection(
                selectedBase64 = null,
                avatarData = null,
                userName = "Brunno Silva",
                onChangePhotoClick = {}
            )
        },
        formContent = {
            EditProfileFormSection(
                name = "Brunno Silva",
                onNameChange = {},
                hasChanges = true,
                isLoading = false,
                onSave = {}
            )
        }
    )
}

private fun submitProfileChanges(
    viewModel: ProfileViewModel,
    selectedImage: SelectedProfileImage?,
    hasNameChanged: Boolean,
    name: String
) {
    selectedImage?.let { image ->
        viewModel.updateAvatar(
            image.base64,
            image.fileName,
            image.mimeType
        )
    }

    if (hasNameChanged) {
        viewModel.updateUser(name)
    }
}
