package com.denisbelobrotski.eye_tracking_library.config

import com.denisbelobrotski.eye_tracking_library.abstraction.ICascadeClassifierConfig

class EyeCascadeClassifierConfig : ICascadeClassifierConfig {
    override var assetPath: String = "cascades/haarcascade_righteye_2splits.xml"
    override var scaleFactor: Double = 1.3
    override var minNeighbours: Int = 5
    override var flags: Int = 0
    override var minSizeRatio: Double = 0.1
    override var maxSizeRatio: Double = 0.6
}
