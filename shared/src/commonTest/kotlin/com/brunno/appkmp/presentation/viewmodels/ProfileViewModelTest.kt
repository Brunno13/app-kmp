package com.brunno.appkmp.presentation.viewmodels

import com.brunno.appkmp.data.local.UserEntity
import com.brunno.appkmp.domain.error.AppError
import com.brunno.appkmp.domain.error.AppResult
import com.brunno.appkmp.domain.error.NetworkError
import com.brunno.appkmp.domain.repository.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(
            StandardTestDispatcher()
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsIdle() = runTest {
        val repository = FakeProfileRepository()
        val viewModel = ProfileViewModel(repository)

        assertEquals(
            ProfileActionState.Idle,
            viewModel.uiState.value
        )

        assertEquals(
            null,
            viewModel.currentUser.value
        )
    }

    @Test
    fun updateUserSuccessUpdatesUiState() = runTest {
        val repository = FakeProfileRepository(
            updateUserResult = AppResult.Success(Unit)
        )

        val viewModel = ProfileViewModel(repository)

        viewModel.updateUser(
            name = "Updated Name"
        )

        advanceUntilIdle()

        assertEquals(
            1,
            repository.updateUserCalls
        )

        assertEquals(
            "Updated Name",
            repository.lastUpdateUserName
        )

        assertEquals(
            ProfileActionState.Success,
            viewModel.uiState.value
        )
    }

    @Test
    fun updateUserErrorUpdatesUiState() = runTest {
        val repository = FakeProfileRepository(
            updateUserResult = AppResult.Error(
                NetworkError.SERVER_ERROR
            )
        )

        val viewModel = ProfileViewModel(repository)

        viewModel.updateUser(
            name = "Updated Name"
        )

        advanceUntilIdle()

        val state = assertIs<ProfileActionState.Error>(
            viewModel.uiState.value
        )

        assertEquals(
            NetworkError.SERVER_ERROR,
            state.error
        )
    }

    @Test
    fun updateAvatarSuccessForwardsArguments() = runTest {
        val repository = FakeProfileRepository(
            updateAvatarResult = AppResult.Success(Unit)
        )

        val viewModel = ProfileViewModel(repository)

        viewModel.updateAvatar(
            base64 = "avatar-base64",
            fileName = "avatar.jpg",
            mimeType = "image/jpeg"
        )

        advanceUntilIdle()

        assertEquals(
            1,
            repository.updateAvatarCalls
        )

        assertEquals(
            "avatar-base64",
            repository.lastUpdateAvatarBase64
        )

        assertEquals(
            "avatar.jpg",
            repository.lastUpdateAvatarFileName
        )

        assertEquals(
            "image/jpeg",
            repository.lastUpdateAvatarMimeType
        )

        assertEquals(
            ProfileActionState.Success,
            viewModel.uiState.value
        )
    }

    @Test
    fun updateAvatarErrorUpdatesUiState() = runTest {
        val repository = FakeProfileRepository(
            updateAvatarResult = AppResult.Error(
                NetworkError.SERVER_ERROR
            )
        )

        val viewModel = ProfileViewModel(repository)

        viewModel.updateAvatar(
            base64 = "avatar-base64",
            fileName = "avatar.jpg",
            mimeType = "image/jpeg"
        )

        advanceUntilIdle()

        val state = assertIs<ProfileActionState.Error>(
            viewModel.uiState.value
        )

        assertEquals(
            NetworkError.SERVER_ERROR,
            state.error
        )
    }

    @Test
    fun syncAvatarIfNeededCallsRepositoryForValidFilename() = runTest {
        val repository = FakeProfileRepository()
        val viewModel = ProfileViewModel(repository)

        viewModel.syncAvatarIfNeeded(
            "https://example.com/api/avatar/avatar.png"
        )

        advanceUntilIdle()

        assertEquals(
            1,
            repository.syncAvatarCalls
        )

        assertEquals(
            "https://example.com/api/avatar/avatar.png",
            repository.lastSyncAvatarFilename
        )
    }

    @Test
    fun syncAvatarIfNeededIgnoresNullAndBlankFilename() = runTest {
        val repository = FakeProfileRepository()
        val viewModel = ProfileViewModel(repository)

        viewModel.syncAvatarIfNeeded(null)
        viewModel.syncAvatarIfNeeded("")
        viewModel.syncAvatarIfNeeded("   ")

        advanceUntilIdle()

        assertEquals(
            0,
            repository.syncAvatarCalls
        )

        assertEquals(
            null,
            repository.lastSyncAvatarFilename
        )
    }

    @Test
    fun currentUserReflectsRepositoryUpdatesWhileSubscribed() = runTest {
        val repository = FakeProfileRepository()
        val viewModel = ProfileViewModel(repository)

        backgroundScope.launch(
            UnconfinedTestDispatcher(testScheduler)
        ) {
            viewModel.currentUser.collect()
        }

        val user = UserEntity(
            id = 10,
            name = "Test User",
            email = "test@example.com",
            avatarFilename = "avatar.png",
            avatarData = "avatar-base64"
        )

        repository.currentUserFlow.value = user

        advanceUntilIdle()

        assertEquals(
            user,
            viewModel.currentUser.value
        )
    }

    @Test
    fun resetStateReturnsUiToIdle() = runTest {
        val repository = FakeProfileRepository()
        val viewModel = ProfileViewModel(repository)

        viewModel.updateUser("Updated Name")

        advanceUntilIdle()

        assertEquals(
            ProfileActionState.Success,
            viewModel.uiState.value
        )

        viewModel.resetState()

        assertEquals(
            ProfileActionState.Idle,
            viewModel.uiState.value
        )
    }

    private class FakeProfileRepository(
        var updateUserResult: AppResult<Unit, AppError> =
            AppResult.Success(Unit),
        var updateAvatarResult: AppResult<Unit, AppError> =
            AppResult.Success(Unit)
    ) : ProfileRepository {

        val currentUserFlow =
            MutableStateFlow<UserEntity?>(null)

        var updateUserCalls = 0
            private set

        var lastUpdateUserName: String? = null
            private set

        var updateAvatarCalls = 0
            private set

        var lastUpdateAvatarBase64: String? = null
            private set

        var lastUpdateAvatarFileName: String? = null
            private set

        var lastUpdateAvatarMimeType: String? = null
            private set

        var syncAvatarCalls = 0
            private set

        var lastSyncAvatarFilename: String? = null
            private set

        override fun observeCurrentUser(): Flow<UserEntity?> =
            currentUserFlow

        override suspend fun updateUser(
            name: String
        ): AppResult<Unit, AppError> {
            updateUserCalls++
            lastUpdateUserName = name

            return updateUserResult
        }

        override suspend fun updateAvatar(
            base64: String,
            fileName: String,
            mimeType: String
        ): AppResult<Unit, AppError> {
            updateAvatarCalls++
            lastUpdateAvatarBase64 = base64
            lastUpdateAvatarFileName = fileName
            lastUpdateAvatarMimeType = mimeType

            return updateAvatarResult
        }

        override suspend fun syncAvatar(
            filename: String
        ) {
            syncAvatarCalls++
            lastSyncAvatarFilename = filename
        }
    }
}
