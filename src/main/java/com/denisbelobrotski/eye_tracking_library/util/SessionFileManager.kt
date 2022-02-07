package com.denisbelobrotski.eye_tracking_library.util

import android.content.Context
import android.graphics.Bitmap
import com.denisbelobrotski.eye_tracking_library.cv_util.OpenCvUtils
import org.opencv.core.Mat
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class SessionFileManager(private val context: Context) {

    companion object {
        private const val FileNameDateFormat = "yyyy-MM-dd_HH:mm:ss:SSS"
        private const val FolderNameBase = "session"
        private const val ImageExtension = "png"
        private const val LogExtension = "log"
    }


    private val fileNameDateFormatter = SimpleDateFormat(FileNameDateFormat)
    private var folderName = "${FolderNameBase}_${currentFormattedDate}"
    private val sessionFilesDirectory: File
    private val logger = SimpleLogger()


    private val currentFormattedDate: String
        get() = fileNameDateFormatter.format(Calendar.getInstance().time)
    private val userFilesFolder: File?
        get() = context.getExternalFilesDir(null)


    var isDebugImageSavingEnabled = false
    var isDebugLogSavingEnabled = false


    init {
        sessionFilesDirectory = File(userFilesFolder, folderName)
        if (!sessionFilesDirectory.exists()) {
            sessionFilesDirectory.mkdir()
        }
    }


    /**
     * @param mat is a valid output Mat object of the RGB(A) format
     * */
    fun saveMat(mat: Mat, fileNameBase: String, debug: Boolean = false) {
        if (!isDebugImageSavingEnabled && debug) {
            return
        }

        val bitmap = OpenCvUtils.getBitmapFromMat(mat)
        saveBitmap(bitmap, fileNameBase)
        bitmap.recycle()
    }


    fun saveBitmap(bitmap: Bitmap, fileNameBase: String, debug: Boolean = false) {
        if (!isDebugImageSavingEnabled && debug) {
            return
        }

        val outputFile = createFile(fileNameBase, ImageExtension)
        val outputStream = FileOutputStream(outputFile)
        FileSystemUtils.saveBitmap(bitmap, outputStream)
    }


    fun createFile(fileNameBase: String, fileExtension: String): File {
        val fileName = generateFileName(fileNameBase, fileExtension)

        val file = File(sessionFilesDirectory, fileName)

        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()

        return file
    }


    fun addLog(message: String, skipIfImagesDisabled: Boolean = false) {
        if (!isDebugImageSavingEnabled && skipIfImagesDisabled) {
            return
        }

        logger.addLog(message)
    }


    fun saveLogFile(fileNameBase: String, debug: Boolean = false) {
        if (!logger.isEnabled) {
            return
        }

        val logString = logger.flushLogs()

        if (!isDebugLogSavingEnabled && debug) {
            return
        }

        val outputFile = createFile(fileNameBase, LogExtension)
        val outputStream = FileOutputStream(outputFile)
        outputStream.use {
            outputStream.write(logString.toByteArray())
        }
    }


    private fun generateFileName(fileNameBase: String, fileExtension: String): String {
        val currentTime = Calendar.getInstance().time
        val formattedDate = fileNameDateFormatter.format(currentTime)
        return "${fileNameBase}_${formattedDate}.${fileExtension}"
    }
}
