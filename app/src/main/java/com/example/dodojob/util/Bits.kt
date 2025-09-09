package com.example.dodojob.util

object Bits {
    /** ex) "100001" -> ByteArray(0x21)  (LSB=오른쪽) */
    fun binStringToBytes(bin: String): ByteArray {
        require(bin.all { it == '0' || it == '1' }) { "binary only" }
        if (bin.isEmpty() || bin.all { it == '0' }) return byteArrayOf(0x00)
        val out = ByteArray((bin.length + 7) / 8)
        // 오른쪽(LSB)부터 채움
        bin.reversed().forEachIndexed { i, c ->
            if (c == '1') {
                val byteIndex = i / 8
                val bitIndex  = i % 8
                out[byteIndex] = (out[byteIndex].toInt() or (1 shl bitIndex)).toByte()
            }
        }
        return trimTrailingZeros(out)
    }

    /** Int/Long 마스크 → 최소 바이트 배열 (little-endian) */
    fun intToBytes(mask: Int): ByteArray {
        if (mask == 0) return byteArrayOf(0x00)
        val out = mutableListOf<Byte>()
        var m = mask
        while (m != 0) { out += (m and 0xFF).toByte(); m = m ushr 8 }
        return out.toByteArray()
    }

    fun longToBytes(mask: Long): ByteArray {
        if (mask == 0L) return byteArrayOf(0x00)
        val out = mutableListOf<Byte>()
        var m = mask
        while (m != 0L) { out += (m and 0xFF).toByte(); m = m ushr 8 }
        return out.toByteArray()
    }

    fun bytesToBinString(bytes: ByteArray, width: Int? = null): String {
        if (bytes.isEmpty()) return "0"
        val sb = StringBuilder(bytes.size * 8)
        // 출력은 MSB-left로 보기 좋게 (big-endian 형태로 보여줌)
        for (i in bytes.indices.reversed()) {
            val b = bytes[i].toInt() and 0xFF
            for (bit in 7 downTo 0) sb.append(if ((b and (1 shl bit)) != 0) '1' else '0')
        }
        val s = sb.toString().trimStart('0').ifEmpty { "0" }
        return if (width != null) s.padStart(width, '0') else s
    }

    private fun trimTrailingZeros(arr: ByteArray): ByteArray {
        var last = arr.size - 1
        while (last > 0 && arr[last] == 0.toByte()) last--
        return arr.copyOf(last + 1)
    }
}
