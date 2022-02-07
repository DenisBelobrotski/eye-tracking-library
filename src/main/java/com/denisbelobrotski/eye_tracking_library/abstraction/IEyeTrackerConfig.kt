package com.denisbelobrotski.eye_tracking_library.abstraction

import com.denisbelobrotski.eye_tracking_library.util.SessionFileManager
import org.opencv.core.Point

interface IEyeTrackerConfig {
    val faceDetector: IRectDetector
    val eyeDetector: IRectDetector
    val eyeProcessor: IEyeProcessor
    var sessionFileManager: SessionFileManager?

    var grayscaleEnabled: Boolean
    var histogramEqualizationEnabled: Boolean

    var mirrorEyes: Boolean
    var gazeDirections: Array<Point>
    var gazeCenterDirectionOffset: Int

    var drawDebugFaceRects: Boolean
    var drawDebugEyeRects: Boolean
    var drawDebugEyeMarkers: Boolean
}
