package com.rulebook.core.data.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.rulebook.core.common.Result
import com.rulebook.core.common.safeCall
import com.rulebook.core.data.util.NetworkErrorMapper
import com.rulebook.core.model.ScanResult
import com.rulebook.core.network.api.RulebookApi
import com.rulebook.core.network.mapper.toDomain
import com.rulebook.core.network.model.AnalyzeRequest
import java.io.ByteArrayOutputStream

class ScanRepositoryImpl(
    private val api: RulebookApi,
    private val context: Context,
) : ScanRepository {

    override suspend fun analyzeImage(imageUri: String): Result<ScanResult> =
        safeCall {
            val base64 = compressAndEncode(imageUri)
            val request = AnalyzeRequest(imageData = base64)
            val response = api.analyzeImage(request)
            response.toDomain()
        }.let { result ->
            when (result) {
                is Result.Success -> result
                is Result.Error -> Result.Error(
                    message = NetworkErrorMapper.mapToUserMessage(result.cause),
                    cause = result.cause,
                )
            }
        }

    private fun compressAndEncode(imageUri: String): String {
        val inputStream = context.contentResolver.openInputStream(Uri.parse(imageUri))
            ?: throw IllegalStateException("Cannot open image URI: $imageUri")

        return inputStream.use { stream ->
            val bitmap = BitmapFactory.decodeStream(stream)
                ?: throw IllegalStateException("Cannot decode image from URI: $imageUri")

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
            bitmap.recycle()

            Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        }
    }
}
