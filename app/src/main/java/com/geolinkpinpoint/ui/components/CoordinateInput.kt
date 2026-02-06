package com.geolinkpinpoint.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.geolinkpinpoint.R

@Composable
fun CoordinateInput(
    label: String,
    onSubmit: (Double, Double) -> Unit,
    onUseCurrentLocation: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    var latText by remember { mutableStateOf("") }
    var lngText by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = latText,
                onValueChange = { latText = it },
                label = { Text(stringResource(R.string.latitude)) },
                leadingIcon = {
                    IconButton(onClick = {
                        latText = if (latText.startsWith("-")) latText.removePrefix("-") else "-$latText"
                    }) {
                        Text(
                            text = stringResource(R.string.plus_minus),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = lngText,
                onValueChange = { lngText = it },
                label = { Text(stringResource(R.string.longitude)) },
                leadingIcon = {
                    IconButton(onClick = {
                        lngText = if (lngText.startsWith("-")) lngText.removePrefix("-") else "-$lngText"
                    }) {
                        Text(
                            text = stringResource(R.string.plus_minus),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val lat = latText.toDoubleOrNull()
                    val lng = lngText.toDoubleOrNull()
                    if (lat != null && lng != null) {
                        onSubmit(lat, lng)
                        latText = ""
                        lngText = ""
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.set_point, label))
            }
            if (onUseCurrentLocation != null) {
                OutlinedButton(
                    onClick = onUseCurrentLocation,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.use_current))
                }
            }
        }
    }
}
