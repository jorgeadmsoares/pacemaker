package io.sellmair.pacemaker.ble

typealias BleSimpleResult = BleResult<Unit>

sealed class BleResult<out T> {
    data class Success<T>(val value: T) : BleResult<T>()

    sealed class Failure : BleResult<Nothing>() {
        data class Code(val status: BleStatusCode) : Failure()
        data class Error(val reason: Throwable) : Failure()
        data class Message(val message: String) : Failure()
    }

    final override fun toString(): String = when (this) {
        is Failure.Code -> "Failure: $status"
        is Failure.Error -> "Failure: ${reason.message}"
        is Failure.Message -> "Failure: $message"
        is Success -> "Success"
    }

    companion object {
        val Success = Success(Unit)
    }
}

inline fun <T, R> BleResult<T>.map(mapper: (T) -> R): BleResult<R> {
    return when (this) {
        is BleResult.Success -> return BleResult.Success(mapper(value))
        is BleResult.Failure -> this
    }
}

inline fun <T, R> BleResult<T>.flatMap(mapper: (T) -> BleResult<R>): BleResult<R> {
    return when (this) {
        is BleResult.Success -> return mapper(value)
        is BleResult.Failure -> this
    }
}