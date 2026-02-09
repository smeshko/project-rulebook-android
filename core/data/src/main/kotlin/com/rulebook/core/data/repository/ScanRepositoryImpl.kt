package com.rulebook.core.data.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.rulebook.core.common.Result
import com.rulebook.core.common.safeCall
import com.rulebook.core.data.util.NetworkErrorMapper
import com.rulebook.core.model.Rules
import com.rulebook.core.model.ScanResult
import com.rulebook.core.network.api.RulebookApi
import com.rulebook.core.network.mapper.toDomain
import com.rulebook.core.network.model.AnalyzeRequest
import com.rulebook.core.network.model.GenerateRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

private const val MAX_IMAGE_DIMENSION = 1920

class ScanRepositoryImpl(
    private val api: RulebookApi,
    private val context: Context,
) : ScanRepository {

    override suspend fun analyzeImage(imageUri: String): Result<ScanResult> =
        safeCall {
            val base64 = withContext(Dispatchers.IO) { compressAndEncode(imageUri) }
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

    override suspend fun generateRules(gameTitle: String, thumbnailUrl: String?): Result<Rules> =
        safeCall {
            val request = GenerateRequest(gameTitle = gameTitle, thumbnailUrl = thumbnailUrl)
            val response = api.generateRules(request)
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
        val uri = Uri.parse(imageUri)

        val sampleSize = context.contentResolver.openInputStream(uri)?.use { stream ->
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(stream, null, options)
            calculateSampleSize(options.outWidth, options.outHeight)
        } ?: throw IllegalStateException("Cannot open image URI: $imageUri")

        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Cannot open image URI: $imageUri")

        return inputStream.use { stream ->
            val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val bitmap = BitmapFactory.decodeStream(stream, null, options)
                ?: throw IllegalStateException("Cannot decode image from URI: $imageUri")

            val outputStream = ByteArrayOutputStream()
            val compressed = bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
            bitmap.recycle()

            if (!compressed) {
                throw IllegalStateException("Failed to compress image from URI: $imageUri")
            }

            Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        }
    }

    private fun calculateSampleSize(width: Int, height: Int): Int {
        var sampleSize = 1
        var w = width
        var h = height
        while (w > MAX_IMAGE_DIMENSION || h > MAX_IMAGE_DIMENSION) {
            sampleSize *= 2
            w /= 2
            h /= 2
        }
        return sampleSize
    }
}
