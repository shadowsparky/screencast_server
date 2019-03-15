package ru.shadowsparky.screencast

import java.io.Serializable

class TransferByteArray(
    val data: ByteArray,
    val length: Int
) : Serializable