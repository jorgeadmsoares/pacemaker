package io.sellmair.pacemaker

import io.sellmair.evas.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy
import kotlin.time.Duration.Companion.seconds

internal fun CoroutineScope.launchVibrationWarningActor() = launch {
    while (isActive) {
        delay(1.seconds)
        if (CriticalGroupState.value() != null && UtteranceState.value() >= UtteranceState.Warnings) {
            UIImpactFeedbackGenerator(UIImpactFeedbackStyleHeavy).impactOccurredWithIntensity(1.0)
        }
    }
}