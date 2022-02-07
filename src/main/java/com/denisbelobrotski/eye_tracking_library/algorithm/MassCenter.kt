package com.denisbelobrotski.eye_tracking_library.algorithm

import org.opencv.core.Mat
import org.opencv.core.Point
import kotlin.math.round

// MARK: fast Mat enumeration: https://stackoverflow.com/questions/12394364/opencv-for-android-access-elements-of-mat/12493075
@ExperimentalUnsignedTypes
@Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")
fun getMassCenter8UC1(mat: Mat): Point {
    val rowsCount = mat.rows()
    val columnsCount = mat.cols()
    val channelsCount = mat.channels()

    val dataBuffer = ByteArray(rowsCount * columnsCount * channelsCount)
    mat.get(0, 0, dataBuffer)

    var ySum = 0UL
    var xSum = 0UL
    var weightSum = 0UL

    for (i in 0 until rowsCount) {
        val rowOffset = i * columnsCount * channelsCount

        for (j in 0 until columnsCount) {
            val columnOffset = j * channelsCount
            val pixelOffset = rowOffset + columnOffset

            val color = dataBuffer[pixelOffset].toUByte()

            val weight = color.toULong()

            ySum += i.toULong() * weight
            xSum += j.toULong() * weight
            weightSum += weight
        }
    }

    val yCenter = round(ySum.toDouble() / weightSum.toDouble())
    val xCenter = round(xSum.toDouble() / weightSum.toDouble())

    return Point(xCenter, yCenter)
}
