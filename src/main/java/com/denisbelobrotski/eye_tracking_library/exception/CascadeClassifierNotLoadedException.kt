package com.denisbelobrotski.eye_tracking_library.exception

class CascadeClassifierNotLoadedException(name: String) :
    Exception("Cascade classifier ${name} not loaded.") {
}
