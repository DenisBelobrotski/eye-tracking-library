package com.denisbelobrotski.eye_tracking_library.config

import com.denisbelobrotski.eye_tracking_library.abstraction.IEyeProcessor
import com.denisbelobrotski.eye_tracking_library.abstraction.IEyeTrackerConfig
import com.denisbelobrotski.eye_tracking_library.abstraction.IRectDetector
import com.denisbelobrotski.eye_tracking_library.util.SessionFileManager
import org.opencv.core.Point

class EyeTrackerConfig(
        override val faceDetector: IRectDetector,
        override val eyeDetector: IRectDetector,
        override val eyeProcessor: IEyeProcessor) : IEyeTrackerConfig {
    override var sessionFileManager: SessionFileManager? = null

    override var grayscaleEnabled = true
    override var histogramEqualizationEnabled = true

    override var mirrorEyes = false
    override var gazeDirections = arrayOf(
            Point(-1.0, 0.0), //left
            Point(1.0, 0.0), //right
            Point(0.0, -1.0), //top
            Point(0.0, 1.0), //bottom
    )
    override var gazeCenterDirectionOffset: Int = 20

    override var drawDebugFaceRects = true
    override var drawDebugEyeRects = true
    override var drawDebugEyeMarkers = true

    var gazeDirectionNames = arrayOf(
            "left",
            "right",
            "top",
            "bottom",
    )
    var gazeDirectionCenterName = "center"
    var gazeDirectionNoResultName = "not detected"
}
