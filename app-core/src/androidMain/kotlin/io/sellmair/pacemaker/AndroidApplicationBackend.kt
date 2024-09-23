package io.sellmair.pacemaker

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import io.sellmair.evas.*
import io.sellmair.pacemaker.ble.AndroidBle
import io.sellmair.pacemaker.ble.Ble
import io.sellmair.pacemaker.bluetooth.HeartRateSensorBluetoothService
import io.sellmair.pacemaker.bluetooth.PacemakerBluetoothService
import io.sellmair.pacemaker.sql.PacemakerDatabase
import io.sellmair.pacemaker.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

class AndroidApplicationBackend : Service(), ApplicationBackend, CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Main + SupervisorJob() + Events() + States() +
            AndroidContextProvider(this)

    inner class MainServiceBinder(
        override val pacemakerBluetoothService: Deferred<PacemakerBluetoothService>,
        override val heartRateSensorBluetoothService: Deferred<HeartRateSensorBluetoothService>,
        override val userService: UserService,
        override val sessionService: SessionService,
        override val settings: Settings,
    ) : Binder(), ApplicationBackend {
        override val events get() = coroutineContext.eventsOrThrow
        override val states get() = coroutineContext.statesOrThrow
    }

    override val events: Events = coroutineContext.eventsOrThrow

    override val states: States = coroutineContext.statesOrThrow

    private val ble: Deferred<Ble> = async {
        AndroidBle(this@AndroidApplicationBackend) ?: never()
    }

    override val pacemakerBluetoothService = async {
        PacemakerBluetoothService(ble.await())
    }

    override val heartRateSensorBluetoothService = async {
        HeartRateSensorBluetoothService(ble.await())
    }

    private val notification = AndroidHeartRateNotification(this)


    override val settings: Settings by lazy {
        SharedPreferencesSettings(getSharedPreferences("app", Context.MODE_PRIVATE))
    }

    private val meId by lazy { settings.meId }

    private val pacemakerDatabase = SafePacemakerDatabase {
        PacemakerDatabase(
            AndroidSqliteDriver(
                schema = PacemakerDatabase.Schema, context = this, name = "test.db"
            )
        )
    }

    override val userService: UserService by lazy {
        SqlUserService(pacemakerDatabase, meId)
    }

    override val sessionService: SessionService by lazy {
        SqlSessionService(pacemakerDatabase)
    }

    override fun onCreate() {
        super.onCreate()
        notification.startForeground(this)
        launchApplicationBackend(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    override fun onBind(intent: Intent?): IBinder {
        return MainServiceBinder(
            userService = userService,
            sessionService = sessionService,
            pacemakerBluetoothService = pacemakerBluetoothService,
            heartRateSensorBluetoothService = heartRateSensorBluetoothService,
            settings = settings
        )
    }
}
