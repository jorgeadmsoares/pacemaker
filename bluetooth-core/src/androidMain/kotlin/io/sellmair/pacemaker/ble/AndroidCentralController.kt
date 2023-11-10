package io.sellmair.pacemaker.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile.GATT
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import io.sellmair.pacemaker.utils.LogTag
import io.sellmair.pacemaker.utils.error
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

internal class AndroidCentralController(
    private val scope: CoroutineScope,
    private val hardware: AndroidCentralHardware,
    private val service: BleServiceDescriptor
) : BleCentralController {

    override val scanResults = Channel<BleCentralController.ScanResult>()

    override val connectedDevices = Channel<BleCentralController.ConnectedDevice>()

    @SuppressLint("MissingPermission")
    override fun startScanning() {
        /* Detect already connected devices */
        scope.launch {
            hardware.manager.getConnectedDevices(GATT).forEach { device ->
                connectedDevices.send(MyConnectedDevice(device))
            }
        }

        val scanCallback = object : ScanCallback() {
            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                log.error("onScanFailed(${BleStatusCode(errorCode)}")
            }

            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                if (result == null) return
                scope.launch {
                    scanResults.send(MyScanResult(result))
                }
            }
        }

        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(service.uuid))
            .build()

        val scanSettings = ScanSettings.Builder()
            .build()

        hardware.manager.adapter.bluetoothLeScanner.startScan(listOf(scanFilter), scanSettings, scanCallback)
        scope.coroutineContext.job.invokeOnCompletion {
            hardware.manager.adapter.bluetoothLeScanner.startScan(scanCallback)
        }
    }

    override fun createConnectableController(result: BleCentralController.ScanResult): BleConnectableController {
        result as MyScanResult
        return AndroidConnectableController(
            scope, service, AndroidConnectableHardware(
                hardware.context, result.result.device, AndroidGattCallback(scope)
            )
        )
    }

    override fun createConnectableController(device: BleCentralController.ConnectedDevice): BleConnectableController {
        device as MyConnectedDevice
        return AndroidConnectableController(
            scope, service, AndroidConnectableHardware(
                hardware.context, device.device, AndroidGattCallback(scope)
            )
        )
    }

    private class MyScanResult(val result: ScanResult) : BleCentralController.ScanResult {
        override val deviceId: BleDeviceId = result.device.deviceId
        override val rssi: Int = result.rssi
        override val isConnectable: Boolean = result.isConnectable
    }

    private class MyConnectedDevice(val device: BluetoothDevice) : BleCentralController.ConnectedDevice {
        override val deviceId: BleDeviceId = device.deviceId
    }

    companion object {
        val log = LogTag.ble.forClass<AndroidCentralController>()
    }
}