package io.sellmair.pacemaker

import io.sellmair.pacemaker.ble.BleConnectable
import io.sellmair.pacemaker.bluetooth.HeartRateSensor
import io.sellmair.pacemaker.bluetooth.toHeartRateSensorId
import io.sellmair.pacemaker.model.HeartRate
import io.sellmair.pacemaker.model.HeartRateSensorId
import io.sellmair.pacemaker.model.User
import io.sellmair.pacemaker.service.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class HeartRateSensorViewModelImpl(
    private val scope: CoroutineScope,
    private val userService: UserService,
    private val heartRateSensor: HeartRateSensor,
) : HeartRateSensorViewModel {

    override val name: String? = heartRateSensor.deviceName
    override val id: HeartRateSensorId = heartRateSensor.deviceId.toHeartRateSensorId()

    // TODO
    override val rssi: StateFlow<Int?> = MutableStateFlow(null)

    override val state: StateFlow<BleConnectable.ConnectionState> = heartRateSensor.connectionState

    override val heartRate: StateFlow<HeartRate?> =
        heartRateSensor.heartRate.map { it.heartRate }.stateIn(scope, WhileSubscribed(), null)

    override val associatedUser: StateFlow<User?> = flow {
        emit(userService.findUser(id))
        userService.onChange.collect {
            emit(userService.findUser(id))
        }
    }.stateIn(scope, WhileSubscribed(), null)

    override val associatedHeartRateLimit: StateFlow<HeartRate?> = associatedUser
        .map { user -> if (user == null) return@map null else userService.findUpperHeartRateLimit(user) }
        .stateIn(scope, WhileSubscribed(), null)

    override fun tryConnect() {
        heartRateSensor.connectIfPossible(true)
    }

    override fun tryDisconnect() {
        heartRateSensor.connectIfPossible(false)
    }
}
