package com.rulebook.feature.camera.util

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Retrieves the URI of the most recent photo in the device gallery.
 *
 * This function queries MediaStore for the latest image by date added,
 * which is then displayed as a thumbnail preview on the gallery button.
 *
 * @param context Android context for ContentResolver access.
 * @return URI of the most recent photo, or null if no photos exist or access is denied.
 */
suspend fun getLastPhotoThumbnailUri(context: Context): Uri? = withContext(Dispatchers.IO) {
    val projection = arrayOf(MediaStore.Images.Media._ID)
    val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

    try {
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val id = cursor.getLong(idColumnIndex)
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            } else {
                null
            }
        }
    } catch (e: SecurityException) {
        // Permission denied - return null
        null
    }
}
