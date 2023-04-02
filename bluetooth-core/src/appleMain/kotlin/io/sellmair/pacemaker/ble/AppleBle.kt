package io.sellmair.pacemaker.ble

import io.sellmair.pacemaker.ble.impl.BlePeripheralServiceImpl
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow

fun Ble(): Ble = AppleBle()

internal class AppleBle : Ble {

    private val scope = CoroutineScope(Dispatchers.ble + SupervisorJob())

    private val queue = BleQueue(scope)

    override suspend fun scanForPeripherals(service: BleServiceDescriptor): Flow<BleConnectable> {
        TODO("Not yet implemented")
    }

    override suspend fun createPeripheralService(service: BleServiceDescriptor): BlePeripheralService {
        return withContext(scope.coroutineContext) {
            val peripheralHardware = ApplePeripheralHardware(scope, service)
            val peripheralController = ApplePeripheralController(scope, peripheralHardware)
            BlePeripheralServiceImpl(queue, peripheralController, service)
        }
    }

    override fun close() {
        scope.cancel()
    }
}
