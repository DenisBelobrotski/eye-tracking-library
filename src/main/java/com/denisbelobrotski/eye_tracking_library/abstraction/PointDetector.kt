package com.denisbelobrotski.eye_tracking_library.abstraction

import com.denisbelobrotski.eye_tracking_library.exception.EyeTrackerNotPreparedException
import com.denisbelobrotski.eye_tracking_library.util.SessionFileManager
import org.opencv.core.Mat
import org.opencv.core.Point

abstract class PointDetector : IPointDetector {
    private var mutableDetectedPoint = Point()

    override var processingImage: Mat? = null

    override val detectedPoint: Point
        get() = mutableDetectedPoint

    var sessionFileManager: SessionFileManager? = null

    final override fun detect() {
        if (processingImage == null) {
            throw EyeTrackerNotPreparedException("processingImage not set")
        }

        val processingImage = this.processingImage!!

        mutableDetectedPoint = getDetectedPoint(processingImage)
    }

    override fun clear() {}

    protected abstract fun getDetectedPoint(processingImage: Mat): Point
}
