package ru.shadowsparky.screencast

class TransferByteArray(
    val data: ByteArray,
    val length: Int
) {
    fun replaceBytes() {
        while(data.toList().contains('1'.toByte())) {
            val index = data.toMutableList().indexOf('1'.toByte())
            data[index] = "12".toByte()
        }
    }
}