package com.denisbelobrotski.eye_tracking_library.detector

import com.denisbelobrotski.eye_tracking_library.abstraction.IEyeProcessor
import com.denisbelobrotski.eye_tracking_library.abstraction.IEyeProcessorConfig
import com.denisbelobrotski.eye_tracking_library.abstraction.IPointDetector
import com.denisbelobrotski.eye_tracking_library.exception.EyeTrackerNotPreparedException
import com.denisbelobrotski.eye_tracking_library.util.SessionFileManager
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Range
import org.opencv.imgproc.Imgproc

class EyeProcessor(
        override val config: IEyeProcessorConfig,
        override val eyePreciser: IPointDetector,
        override val pupilDetector: IPointDetector) : IEyeProcessor {

    private val processingImage = Mat()

    private val hsvChannels: MutableList<Mat> = ArrayList(3)

    private var mutableDetectedEyeCenter = Point()
    private var mutableDetectedPupilCenter = Point()

    override var sourceImage: Mat? = null
    override var sessionFileManager: SessionFileManager? = null

    override val detectedEyeCenter: Point
        get() = mutableDetectedEyeCenter
    override val detectedPupilCenter: Point
        get() = mutableDetectedPupilCenter

    override fun process() {
        sessionFileManager?.addLog("EyeProcessor - process started")

        if (sourceImage == null) {
            throw EyeTrackerNotPreparedException("Source image cannot be null")
        }

        val sourceImage = this.sourceImage!!

        val rowsCount = sourceImage.rows()
        val colsCount = sourceImage.cols()

        val topOffset = rowsCount * config.topOffsetPercentage / 100
        val bottomOffset = rowsCount * config.bottomOffsetPercentage / 100

        val rowsRange = Range(topOffset, rowsCount - bottomOffset)
        val colsRange = Range(0, colsCount)

        val cutImage = sourceImage.submat(rowsRange, colsRange)
        sessionFileManager?.addLog("EyeProcessor - submat taken")

        Imgproc.cvtColor(cutImage, processingImage, Imgproc.COLOR_RGB2HSV)
        sessionFileManager?.addLog("EyeProcessor - RGB to HSV")

        Core.split(processingImage, hsvChannels)
        sessionFileManager?.addLog("EyeProcessor - HSV splitted")

        val hue = hsvChannels[0]
        val saturation = hsvChannels[1]
        val value = hsvChannels[2]

        sessionFileManager?.addLog("EyeProcessor - HSV splitted")

        sessionFileManager?.saveMat(hue, "eye_hue", true)
        sessionFileManager?.saveMat(saturation, "eye_saturation", true)
        sessionFileManager?.saveMat(value, "eye_value", true)
        sessionFileManager?.addLog("EyeProcessor - channels saved", true)

        pupilDetector.processingImage = value
        pupilDetector.detect()
        mutableDetectedPupilCenter = pupilDetector.detectedPoint
        mutableDetectedPupilCenter.y += topOffset
        sessionFileManager?.addLog("EyeProcessor - pupil detection done")

        eyePreciser.processingImage = saturation
        eyePreciser.detect()
        mutableDetectedEyeCenter = eyePreciser.detectedPoint
        mutableDetectedEyeCenter.y += topOffset
        sessionFileManager?.addLog("EyeProcessor - eye precision done")

        hue.release()
        saturation.release()
        value.release()

        hsvChannels.clear()

        sessionFileManager?.addLog("EyeProcessor - memory cleared")

        sessionFileManager?.addLog("EyeProcessor - process done")
    }
}
