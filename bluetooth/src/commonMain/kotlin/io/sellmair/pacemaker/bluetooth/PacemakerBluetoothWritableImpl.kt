package io.sellmair.pacemaker.bluetooth

import io.sellmair.pacemaker.ble.BleWritable
import io.sellmair.pacemaker.model.HeartRate
import io.sellmair.pacemaker.model.HeartRateSensorId
import io.sellmair.pacemaker.model.User
import io.sellmair.pacemaker.model.encodeToByteArray


internal fun PacemakerBluetoothWritable(underlying: BleWritable) = object : PacemakerBluetoothWritable {
    override suspend fun setUser(user: User) {
        underlying.setValue(PacemakerServiceDescriptors.userNameCharacteristic, user.name.encodeToByteArray())
        underlying.setValue(PacemakerServiceDescriptors.userIdCharacteristic, user.id.encodeToByteArray())
    }

    override suspend fun setHeartRate(sensorId: HeartRateSensorId, heartRate: HeartRate) {
        underlying.setValue(PacemakerServiceDescriptors.sensorIdCharacteristic, sensorId.value.encodeToByteArray())
        underlying.setValue(PacemakerServiceDescriptors.heartRateCharacteristic, heartRate.encodeToByteArray())
    }

    override suspend fun setHeartRateLimit(heartRate: HeartRate) {
        underlying.setValue(PacemakerServiceDescriptors.heartRateLimitCharacteristic, heartRate.encodeToByteArray())
    }
}