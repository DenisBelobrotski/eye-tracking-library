package com.denisbelobrotski.eye_tracking_library.abstraction

import org.opencv.core.Mat
import org.opencv.core.Point

interface IPointDetector : IClearable {
    var processingImage: Mat?
    val detectedPoint: Point

    fun detect()
}
