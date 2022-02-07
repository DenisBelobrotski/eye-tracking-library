package com.denisbelobrotski.eye_tracking_library.config

import com.denisbelobrotski.eye_tracking_library.abstraction.IEyeProcessorConfig

class EyeProcessorConfig : IEyeProcessorConfig {
    override var topOffsetPercentage: Int = 40
    override var bottomOffsetPercentage: Int = 0
}
