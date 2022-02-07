package com.denisbelobrotski.eye_tracking_library.algorithm

import com.denisbelobrotski.eye_tracking_library.abstraction.IEyeProcessor
import com.denisbelobrotski.eye_tracking_library.abstraction.IEyeTrackerConfig
import com.denisbelobrotski.eye_tracking_library.abstraction.IRectDetector
import com.denisbelobrotski.eye_tracking_library.cv_util.DrawUtils
import com.denisbelobrotski.eye_tracking_library.cv_util.OpenCvUtils
import com.denisbelobrotski.eye_tracking_library.exception.EyeTrackerNotPreparedException
import com.denisbelobrotski.eye_tracking_library.util.SessionFileManager
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class EyeTracker(val config: IEyeTrackerConfig) {
    private val processingImage = Mat()

    private val faceDetector: IRectDetector = config.faceDetector
    private val eyeDetector: IRectDetector = config.eyeDetector
    private val eyeProcessor: IEyeProcessor = config.eyeProcessor
    private val sessionFileManager: SessionFileManager? = config.sessionFileManager

    private var detectedFaceRect: Rect? = null
    private var detectedLeftEyeRect: Rect? = null
    private var detectedRightEyeRect: Rect? = null

    private var leftEyeGazeDirectionIndex: Int = DEFAULT_GAZE_DIRECTION_INDEX
    private var rightEyeGazeDirectionIndex: Int = DEFAULT_GAZE_DIRECTION_INDEX


    var sourceImage: Mat? = null

    val lastDetectedFaceRect: Rect?
        get() = detectedFaceRect
    val lastDetectedLeftEyeRect: Rect?
        get() = detectedLeftEyeRect
    val lastDetectedRightEyeRect: Rect?
        get() = detectedRightEyeRect

    val leftEyeBestGazeDirectionIndex: Int
        get() = leftEyeGazeDirectionIndex
    val rightEyeBestGazeDirectionIndex: Int
        get() = rightEyeGazeDirectionIndex


    fun detect() {
        sessionFileManager?.addLog("EyeTracker - detection started")

        checkAvailability()

        val sourceImage = this.sourceImage!!

        sessionFileManager?.saveMat(sourceImage, "source_image", true)
        sessionFileManager?.addLog("EyeTracker - source image saved", true)

        if (config.grayscaleEnabled) {
            Imgproc.cvtColor(sourceImage, processingImage, Imgproc.COLOR_RGB2GRAY, 1)
            sessionFileManager?.addLog("EyeTracker - RGB to GRAY done")

            sessionFileManager?.saveMat(processingImage, "gray_image", true)
            sessionFileManager?.addLog("EyeTracker - gray image saved", true)
        }

        if (config.histogramEqualizationEnabled) {
            Imgproc.equalizeHist(processingImage, processingImage)
            sessionFileManager?.addLog("EyeTracker - image histogram equalized")

            sessionFileManager?.saveMat(processingImage, "hist_equalized_image", true)
            sessionFileManager?.addLog("EyeTracker - histogram equalized image saved", true)
        }

        faceDetector.processingImage = processingImage
        faceDetector.detect()
        sessionFileManager?.addLog("EyeTracker - face detection done")

        val faceRects = faceDetector.detectedRects

        detectedFaceRect = getBestFace(faceRects)

        detectedFaceRect?.let { faceRect ->
            val sourceFaceRoi = sourceImage.submat(faceRect)
            val processingFaceRoi = processingImage.submat(faceRect)
            sessionFileManager?.addLog("EyeTracker - face submats taken")

            sessionFileManager?.saveMat(processingFaceRoi, "detected_face", true)
            sessionFileManager?.addLog("EyeTracker - detected face saved", true)

            eyeDetector.processingImage = processingFaceRoi
            eyeDetector.detect()
            sessionFileManager?.addLog("EyeTracker - eye detection done")

            tryDrawFaceRect(sourceImage, faceRect)


            val eyeRects = eyeDetector.detectedRects
            val eyes = getBestEyes(eyeRects, sourceFaceRoi)

            detectedLeftEyeRect = eyes.first
            detectedRightEyeRect = eyes.second

            leftEyeGazeDirectionIndex = DEFAULT_GAZE_DIRECTION_INDEX
            rightEyeGazeDirectionIndex = DEFAULT_GAZE_DIRECTION_INDEX

            detectedLeftEyeRect?.let { leftEyeRect ->
                processEye(leftEyeRect, eyeProcessor, sourceFaceRoi, processingFaceRoi, true)
                leftEyeGazeDirectionIndex =
                    getBestDirectionIndex(config.gazeDirections, eyeProcessor, leftEyeRect)
            }
            detectedRightEyeRect?.let { rightEyeRect ->
                processEye(rightEyeRect, eyeProcessor, sourceFaceRoi, processingFaceRoi, false)
                rightEyeGazeDirectionIndex =
                    getBestDirectionIndex(config.gazeDirections, eyeProcessor, rightEyeRect)
            }
        }

        sessionFileManager?.addLog("EyeTracker - detection done")
    }


    private fun checkAvailability() {
        val exceptionReason = isDetectionAvailable()
        if (exceptionReason != null) {
            throw EyeTrackerNotPreparedException(exceptionReason)
        }
    }


    private fun isDetectionAvailable(): String? {
        if (sourceImage == null) {
            return "Target image not set"
        }

        return null
    }


    private fun getBestFace(faceRects: List<Rect>): Rect? {
        val rectsCount = faceRects.count()

        if (rectsCount == 0) {
            return null
        }

        var relevantFaceRect = faceRects[0]
        var maxArea = relevantFaceRect.area()

        for (index in 1 until rectsCount) {
            val currentRect = faceRects[index]
            val currentArea = currentRect.area()

            if (currentArea > maxArea) {
                maxArea = currentArea
                relevantFaceRect = currentRect
            }
        }

        return relevantFaceRect
    }


    private fun getBestEyes(eyeRects: List<Rect>, faceMat: Mat): Pair<Rect?, Rect?> {
        val rectsCount = eyeRects.count()

        if (rectsCount == 0) {
            return Pair(null, null)
        }

        var leftEye: Rect? = null
        var rightEye: Rect? = null

        val faceCenter = OpenCvUtils.getMatCenter(faceMat)

        for (index in 0 until rectsCount) {
            val eyeRect = eyeRects[index]
            val eyeCenter = OpenCvUtils.getRectCenter(eyeRect)

            if (eyeCenter.y > faceCenter.y) {
                continue
            }

            if (leftEye == null && eyeCenter.x < faceCenter.x) {
                leftEye = eyeRect
                continue
            }

            if (rightEye == null && eyeCenter.x > faceCenter.x) {
                rightEye = eyeRect
                continue
            }
        }

        return if (!config.mirrorEyes) {
            Pair(leftEye, rightEye)
        } else {
            Pair(rightEye, leftEye)
        }
    }


    private fun processEye(
            eyeRect: Rect, eyeProcessor: IEyeProcessor,
            sourceFaceRoi: Mat, processingFaceRoi: Mat,
            left: Boolean) {
        val sourceEyeRoi = sourceFaceRoi.submat(eyeRect)
        val processingEyeRoi = processingFaceRoi.submat(eyeRect)
        sessionFileManager?.addLog("EyeTracker - eye submats taken")

        val eyeName = if (left) "left" else "right"
        sessionFileManager?.saveMat(processingEyeRoi, "detected_eye_$eyeName", true)
        sessionFileManager?.addLog("EyeTracker - detected eye saved ($eyeName)", true)


        eyeProcessor.sourceImage = sourceEyeRoi
        eyeProcessor.process()
        sessionFileManager?.addLog("EyeTracker - eye processed")


        tryDrawEyeRect(sourceFaceRoi, eyeRect, left)
        tryDrawEyeMarkers(sourceEyeRoi, eyeProcessor)
    }


    private fun getBestDirectionIndex(
            directions: Array<Point>, eyeProcessor: IEyeProcessor, eyeRect: Rect): Int {
        val directionsCount = directions.count()

        if (directionsCount == 0) {
            return DEFAULT_GAZE_DIRECTION_INDEX
        }

        val diff = OpenCvUtils.getDifference(
                eyeProcessor.detectedPupilCenter, eyeProcessor.detectedEyeCenter)

        val diffLength = OpenCvUtils.getLength(diff)
        val halfMaxLength =
            OpenCvUtils.getLength(Point(eyeRect.width.toDouble(), eyeRect.height.toDouble())) * 0.5

        val lengthFactor = (diffLength / halfMaxLength * 100).toInt()
        val minCenterFactor = config.gazeCenterDirectionOffset

        if (lengthFactor < minCenterFactor) {
            return CENTER_GAZE_DIRECTION_INDEX
        }

        var maxDotProductIndex = 0
        var maxDotProduct = directions[maxDotProductIndex].dot(diff)

        for (index in 1 until directionsCount) {
            val direction = directions[index]
            val dotProduct = direction.dot(diff)

            if (dotProduct > maxDotProduct) {
                maxDotProduct = dotProduct
                maxDotProductIndex = index
            }
        }

        return maxDotProductIndex
    }


    private fun tryDrawFaceRect(sourceImage: Mat, faceRect: Rect) {
        if (config.drawDebugFaceRects) {
            val thickness = DrawUtils.getLineThicknessForMat(sourceImage, 100, 1)
            val lineType = Imgproc.LINE_8
            Imgproc.rectangle(sourceImage, faceRect, faceRectColor, thickness, lineType)
        }
    }


    private fun tryDrawEyeRect(sourceFaceRoi: Mat, eyeRect: Rect, left: Boolean) {
        if (config.drawDebugEyeRects) {
            val color = if (left) leftEyeRectColor else rightEyeRectColor
            val thickness = DrawUtils.getLineThicknessForMat(sourceFaceRoi, 100, 1)
            val lineType = Imgproc.LINE_8
            Imgproc.rectangle(sourceFaceRoi, eyeRect, color, thickness, lineType)
        }
    }


    private fun tryDrawEyeMarkers(sourceEyeRoi: Mat, eyeProcessor: IEyeProcessor) {
        if (config.drawDebugEyeMarkers) {
            val markerSize = DrawUtils.getMarkerSizeForMat(sourceEyeRoi, 20, 2)
            val thickness = DrawUtils.getLineThicknessForMat(sourceEyeRoi, 30, 1)
            val lineType = Imgproc.LINE_8
            val roiCenter = OpenCvUtils.getMatCenter(sourceEyeRoi)

            Imgproc.drawMarker(
                    sourceEyeRoi, roiCenter, eyeRoiCenterColor,
                    Imgproc.MARKER_DIAMOND, markerSize, thickness, lineType)
            Imgproc.drawMarker(
                    sourceEyeRoi, eyeProcessor.detectedEyeCenter, eyeCenterColor,
                    Imgproc.MARKER_DIAMOND, markerSize, thickness, lineType)
            Imgproc.drawMarker(
                    sourceEyeRoi, eyeProcessor.detectedPupilCenter, pupilCenterColor,
                    Imgproc.MARKER_DIAMOND, markerSize, thickness, lineType)

            sessionFileManager?.addLog("EyeTracker - debug markers drawn")
        }
    }


    companion object {
        private val faceRectColor = Scalar(0.0, 255.0, 0.0, 255.0)

        private val leftEyeRectColor = Scalar(255.0, 0.0, 255.0, 255.0)
        private val rightEyeRectColor = Scalar(0.0, 255.0, 255.0, 255.0)

        private val eyeRoiCenterColor = Scalar(255.0, 255.0, 0.0, 255.0)
        private val eyeCenterColor = Scalar(0.0, 255.0, 0.0, 255.0)
        private val pupilCenterColor = Scalar(255.0, 0.0, 0.0, 255.0)

        const val DEFAULT_GAZE_DIRECTION_INDEX = Int.MIN_VALUE
        const val CENTER_GAZE_DIRECTION_INDEX = -1
    }
}
