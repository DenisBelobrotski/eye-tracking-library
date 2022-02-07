package com.denisbelobrotski.eye_tracking_library.abstraction

interface ICascadeClassifierConfig {
    var assetPath: String
    var scaleFactor: Double
    var minNeighbours: Int
    var flags: Int
    var minSizeRatio: Double
    var maxSizeRatio: Double
}
