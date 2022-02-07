package com.denisbelobrotski.eye_tracking_library.legacy

import com.denisbelobrotski.eye_tracking_library.algorithm.getMassCenter8UC1
import com.denisbelobrotski.eye_tracking_library.abstraction.PointDetector
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc

class EyePreciserSaturation(private val config: EyePreciserSaturationConfig) : PointDetector() {
    private val erosionKernel = Mat()
    private val erosionAnchor = Point(-1.0, -1.0)
    private val dilationKernel = Mat()
    private val dilationAnchor = Point(-1.0, -1.0)

    override fun getDetectedPoint(processingImage: Mat): Point {
        sessionFileManager?.addLog("EyePreciserSaturation - detection started")

        sessionFileManager?.saveMat(processingImage, "eye_preciser_source", true)
        sessionFileManager?.addLog("EyePreciserSaturation - source saved", true)

        if (config.shouldEqualizeHistogram) {
            Imgproc.equalizeHist(processingImage, processingImage)
            sessionFileManager?.addLog("EyePreciserSaturation - hist equalized")

            sessionFileManager?.saveMat(processingImage, "eye_preciser_equalize_hist", true)
            sessionFileManager?.addLog("EyePreciserSaturation - equalize hist saved", true)
        }

        Imgproc.threshold(
                processingImage, processingImage,
                config.threshold.toDouble(), config.maxThreshold.toDouble(),
                Imgproc.THRESH_BINARY_INV)
        sessionFileManager?.addLog("EyePreciserSaturation - threshold")

        sessionFileManager?.saveMat(processingImage, "eye_preciser_threshold", true)
        sessionFileManager?.addLog("EyePreciserSaturation - threshold", true)

        if (config.isErosionEnabled) {
            Imgproc.erode(
                    processingImage, processingImage,
                    erosionKernel, erosionAnchor, config.erosionIterationsCount)
            sessionFileManager?.addLog("EyePreciserSaturation - erode")

            sessionFileManager?.saveMat(processingImage, "eye_preciser_erode", true)
            sessionFileManager?.addLog("EyePreciserSaturation - erode", true)
        }

        if (config.isDilationEnabled) {
            Imgproc.dilate(
                    processingImage, processingImage,
                    dilationKernel, dilationAnchor, config.dilationIterationsCount)
            sessionFileManager?.addLog("EyePreciserSaturation - dilate")

            sessionFileManager?.saveMat(processingImage, "eye_preciser_dilate", true)
            sessionFileManager?.addLog("EyePreciserSaturation - dilate", true)
        }

        val center = getMassCenter8UC1(processingImage)

        sessionFileManager?.addLog("EyePreciserSaturation - center of mass done")

        sessionFileManager?.addLog("EyePreciserSaturation - detection done")

        return center
    }

    override fun clear() {
        super.clear()

        erosionKernel.release()
        dilationKernel.release()
    }
}
