package com.denisbelobrotski.eye_tracking_library.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class FileSystemUtils {
    companion object {
        fun loadBitmapResource(context: Context, assetPath: String): Bitmap {
            val assetInputStream = context.assets.open(assetPath)
            val resultBitmap = decodeBitmap(assetInputStream)
            assetInputStream.close()

            return resultBitmap
        }

        fun loadUserBitmap(context: Context, uri: Uri): Bitmap? {
            var resultBitmap: Bitmap? = null
            val inputStream = context.contentResolver.openInputStream(uri)

            inputStream?.let {
                resultBitmap =
                    decodeBitmap(inputStream)
            }

            return resultBitmap
        }

        fun decodeBitmap(inputStream: InputStream): Bitmap {
            var resultBitmap = BitmapFactory.decodeStream(inputStream)

            val bitmapConfig = resultBitmap.config

            if (bitmapConfig != Bitmap.Config.ARGB_8888 && bitmapConfig != Bitmap.Config.RGB_565) {
                resultBitmap = resultBitmap.copy(Bitmap.Config.ARGB_8888, false)
            }

            return resultBitmap
        }

        fun writeCacheFile(context: Context, inputStream: InputStream, outputFileName: String,
                rewrite: Boolean = false, public: Boolean = false): File {
            val cacheDir: File? = if (public) context.externalCacheDir else context.cacheDir
            val cachedFile = File(cacheDir, outputFileName)

            if (cachedFile.exists() && rewrite) {
                cachedFile.delete()
            }
            cachedFile.createNewFile()

            val outputStream = FileOutputStream(cachedFile)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            outputStream.close()

            return cachedFile
        }

        fun cacheAssetFile(context: Context, assetUri: Uri,
                rewrite: Boolean = false, public: Boolean = false): File? {
            val assetPath = assetUri.path
            val assetFileName = assetUri.lastPathSegment
            var cachedFile: File? = null

            assetPath?.let {
                assetFileName?.let {
                    val inputStream = context.assets.open(assetPath)

                    cachedFile =
                        writeCacheFile(context, inputStream, assetFileName, rewrite, public)

                    inputStream.close()
                }
            }

            return cachedFile
        }

        fun cacheUserFile(
                context: Context, userFileUri: Uri,
                rewrite: Boolean = false, public: Boolean = false): File? {
            var cachedFile: File? = null
            val userFileName =
                getUserFileName(context, userFileUri)
            val userFileInputStream =
                openUserFileInputStream(context, userFileUri)

            if (userFileName != null && userFileInputStream != null) {
                cachedFile =
                    writeCacheFile(context, userFileInputStream, userFileName, rewrite, public)
            }

            userFileInputStream?.close()

            return cachedFile
        }

        fun openUserFileInputStream(context: Context, userFileUri: Uri): InputStream? {
            return context.contentResolver.openInputStream(userFileUri)
        }

        fun getUserFileName(context: Context, uri: Uri): String? {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.let {
                if (cursor.count <= 0) {
                    cursor.close()
                    throw IllegalArgumentException("Can't obtain file name, cursor is empty")
                }
                cursor.moveToFirst()
                val fileName =
                    cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                cursor.close()

                return fileName
            }

            return null
        }

        fun saveBitmap(
                bitmap: Bitmap,
                outputStream: OutputStream,
                format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
                quality: Int = 100) {
            outputStream.use {
                bitmap.compress(format, quality, outputStream)
            }
        }
    }
}
