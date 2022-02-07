package com.denisbelobrotski.eye_tracking_library.config

import com.denisbelobrotski.eye_tracking_library.abstraction.ICascadeClassifierConfig

class FaceCascadeClassifierConfig : ICascadeClassifierConfig {
    override var assetPath: String = "cascades/haarcascade_frontalface_alt2.xml"
    override var scaleFactor: Double = 1.3
    override var minNeighbours: Int = 5
    override var flags: Int = 0
    override var minSizeRatio: Double = 0.4
    override var maxSizeRatio: Double = 0.9
}
