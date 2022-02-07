package com.denisbelobrotski.eye_tracking_library.exception

class DetectorNotPreparedException(reason: String) :
    Exception("Detector hasn't prepared yet (reason: ${reason})") {
}
