package com.brunno.appkmp.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kmpprojectbrunno.shared.generated.resources.Res
import kmpprojectbrunno.shared.generated.resources.btn_back
import kmpprojectbrunno.shared.generated.resources.details_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun DetailsScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(Res.string.details_title)
        )

        Button(onClick = onBack) {
            Text(
                text = stringResource(Res.string.btn_back)
            )
        }
    }
}

@Preview(
    name = "Details",
    showBackground = true
)
@Composable
private fun DetailsScreenPreview() {
    DetailsScreen(
        onBack = {}
    )
}
