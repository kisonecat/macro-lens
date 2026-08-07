package com.macrolens.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.util.UUID

class ThumbnailRepository(private val context: Context) {

    private val thumbnailDir: File
        get() = File(context.filesDir, "thumbnails").also { it.mkdirs() }

    private val imageDir: File
        get() = File(context.filesDir, "images").also { it.mkdirs() }

    suspend fun saveThumbnail(jpegBytes: ByteArray): String? = withContext(Dispatchers.IO) {
        deleteOldThumbnails()
        runCatching {
            val bitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
                ?: return@runCatching null
            val scaled = scaleBitmap(bitmap, MAX_THUMBNAIL_DIMENSION)
            val file = File(thumbnailDir, "${LocalDate.now()}_${UUID.randomUUID()}.jpg")
            FileOutputStream(file).use { scaled.compress(Bitmap.CompressFormat.JPEG, 75, it) }
            file.absolutePath
        }.getOrNull()
    }

    suspend fun saveImage(jpegBytes: ByteArray): String? = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(imageDir, "${UUID.randomUUID()}.jpg")
            FileOutputStream(file).use { it.write(jpegBytes) }
            file.absolutePath
        }.getOrNull()
    }

    private fun deleteOldThumbnails() {
        val today = LocalDate.now().toString()
        thumbnailDir.listFiles()?.forEach { if (!it.name.startsWith(today)) it.delete() }
    }

    private fun scaleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= maxDimension && h <= maxDimension) return bitmap
        val scale = maxDimension.toFloat() / maxOf(w, h)
        return Bitmap.createScaledBitmap(bitmap, (w * scale).toInt(), (h * scale).toInt(), true)
    }

    companion object {
        private const val MAX_THUMBNAIL_DIMENSION = 200
    }
}
