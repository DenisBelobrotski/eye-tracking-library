package com.denisbelobrotski.eye_tracking_library.abstraction

interface IPupilDetectorConfig {
    var shouldEqualizeHistogram: Boolean
    var threshold: Int
    var maxThreshold: Int
    var erosionIterationsCount: Int
    var dilationIterationsCount: Int
    var isErosionEnabled: Boolean
    var isDilationEnabled: Boolean
}
