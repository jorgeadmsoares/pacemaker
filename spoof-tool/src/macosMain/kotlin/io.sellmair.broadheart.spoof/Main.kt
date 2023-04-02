@file:Suppress("OPT_IN_USAGE")

package io.sellmair.pacemaker.spoof

import io.sellmair.pacemaker.ble.Ble
import io.sellmair.pacemaker.bluetooth.PacemakerCentralService
import io.sellmair.pacemaker.bluetooth.PacemakerPeripheralService
import io.sellmair.pacemaker.model.HeartRate
import io.sellmair.pacemaker.model.HeartRateSensorId
import io.sellmair.pacemaker.model.User
import io.sellmair.pacemaker.model.UserId
import io.sellmair.pacemaker.utils.LogTag
import io.sellmair.pacemaker.utils.info
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import platform.CoreFoundation.CFRunLoopRun


private val ble = Ble()

fun main() {
    //launchSendBroadcasts()
    launchReceiveBroadcasts()
    /* launchReceiveHeartRates() */
    CFRunLoopRun()
}

private fun launchSendBroadcasts() = MainScope().launch {
    MainScope().launch(Dispatchers.Default) {
        val user = User(
            isMe = true,
            id = UserId(2412),
            name = "Felix Werner"
        )

        val pacemakerPeripheral = PacemakerPeripheralService(ble)
        pacemakerPeripheral.setUser(user)
        pacemakerPeripheral.setHeartRateLimit(HeartRate(120))
        pacemakerPeripheral.startAdvertising()

        while (isActive) {
            val line = readln()
            if (line.startsWith("l")) {
                val heartRateLimit = line.removePrefix("l").toIntOrNull() ?: continue
                pacemakerPeripheral.setHeartRateLimit(HeartRate(heartRateLimit))
                println("Updated spoof hr-limit: $heartRateLimit")
            } else {
                pacemakerPeripheral.setHeartRate(
                    HeartRateSensorId("👻"), HeartRate(line.toIntOrNull() ?: continue)
                )
                println("Updated spoof hr: ${line.toIntOrNull()}")
            }
        }
    }
}

private fun launchReceiveBroadcasts() = MainScope().launch(Dispatchers.Default) {
    val pacemaker = PacemakerCentralService(ble)
    pacemaker.connections.collect { connection ->
        LogTag("spoof").info("Received connection ${connection.deviceId}")
    }
}

/*
private fun launchReceiveBroadcasts() = MainScope().launch(Dispatchers.Default) {
    val pacemakerBle = PacemakerBle(ble)
    pacemakerBle.connections.flatMapMerge { connection ->
        println("Found pacemaker peripheral: ${connection.id}")
        connection.receivePacemakerBroadcastPackages()
    }.collect { pkg ->
        println("${pkg.userName}: ${pkg.heartRate.value.roundToInt()}/${pkg.heartRateLimit.value.roundToInt()}")
    }
}

private fun launchReceiveHeartRates() = MainScope().launch(Dispatchers.Default) {/*
    Ble(this).receiveHeartRateMeasurements().collect { measurement ->
        println("HR: ${measurement.heartRate} | Device: ${measurement.sensorInfo.id}")
    }*/
}


 */