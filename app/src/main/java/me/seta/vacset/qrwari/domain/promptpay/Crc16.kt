package me.seta.vacset.qrwari.domain.promptpay

/**
 * CRC-16/CCITT (X25): poly 0x1021, init 0xFFFF, refIn/out=true, xorOut=0xFFFF.
 * Calculate over the whole payload including "63" + "04", but not the CRC value itself.
 */
object Crc16 {
    fun ccitt(bytes: ByteArray): String {
        var crc = 0xFFFF
        for (b in bytes) {
            val data = b.toInt() and 0xFF
            for (i in 0 until 8) {
                val bit = ((data shr (7 - i)) and 1) == 1
                val c15 = (crc shr 15 and 1) == 1
                crc = crc shl 1
                if (c15 xor bit) crc = crc xor 0x1021
                crc = crc and 0xFFFF
            }
        }
        crc = crc and 0xFFFF
        return String.format("%04X", crc)
    }
}
