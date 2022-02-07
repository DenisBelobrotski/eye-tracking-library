package com.denisbelobrotski.eye_tracking_library.exception

class EyeTrackerNotPreparedException(reason: String) :
    Exception("Eye tracker hasn't prepared yet (reason: ${reason})") {
}
