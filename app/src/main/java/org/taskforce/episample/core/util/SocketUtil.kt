package org.taskforce.episample.core.util

import java.io.*
import java.nio.ByteBuffer

class SocketUtil {
    companion object {
        private const val BUFFER_SIZE = 1024
        private const val COMPLETE_TEXT = "TRANSFER_COMPLETE"
        private val COMPLETE_TEXT_BYTE_SIZE: Int
            get() = COMPLETE_TEXT.toCharArray().size * 2
        private val fileTransferComplete: ByteBuffer
            get() = ByteBuffer.allocate(COMPLETE_TEXT_BYTE_SIZE).apply {
            COMPLETE_TEXT.toCharArray().forEach {
                putChar(it)
            }
        }

        fun writeSocketToFile(inputStream: InputStream, file: File) {
            val fileOutputStream = file.outputStream()
            val buffer = ByteArray(BUFFER_SIZE)

            var readSize = inputStream.read(buffer)
            var fileComplete = false
            var bytesCarriedOver = 0
            var bytesAccumulated = readSize
            while (readSize != -1 && !fileComplete) {

                println("Accumulating")
                println("bytesCarriedOver $bytesCarriedOver, accumulated $bytesAccumulated")
                while (readSize != -1 &&
                        bytesAccumulated + bytesCarriedOver <= COMPLETE_TEXT_BYTE_SIZE) {
                    // we always want a little more than the end signal size
                    readSize = inputStream.read(buffer, bytesAccumulated + bytesCarriedOver, BUFFER_SIZE - bytesAccumulated - bytesCarriedOver)
                    bytesAccumulated += readSize
                    println("bytesCarriedOver $bytesCarriedOver, accumulated $bytesAccumulated")
                }

                val totalBytes = bytesAccumulated + bytesCarriedOver
                val tailRangeBegin = totalBytes - COMPLETE_TEXT_BYTE_SIZE
                val tailRange = buffer.copyOfRange(tailRangeBegin, totalBytes)
                val hasEndSignal = tailRange.contentEquals(fileTransferComplete.array())

                if (hasEndSignal) {
                    fileComplete = true
                    fileOutputStream.write(buffer, 0, totalBytes - COMPLETE_TEXT_BYTE_SIZE)
                } else {
                    fileOutputStream.write(buffer, 0, totalBytes - COMPLETE_TEXT_BYTE_SIZE)
                    System.arraycopy(tailRange, 0, buffer, 0, COMPLETE_TEXT_BYTE_SIZE)
                    bytesCarriedOver = COMPLETE_TEXT_BYTE_SIZE
                }
                bytesAccumulated = 0
            }

            fileOutputStream.close()
        }

        fun writeFileToSocketStream(file: File, out: OutputStream) {
            var fileInputStream: InputStream? = null
            try {
                fileInputStream = file.inputStream()
                // Don't use file util to copy the file (it closes the stream and the file transfer complete bit cannot be sent
                copyFile(fileInputStream, out)
            } catch (e: FileNotFoundException) {
                print(e.toString())
            }
            fileInputStream?.close()
            out.write(fileTransferComplete.array(), 0, COMPLETE_TEXT_BYTE_SIZE)
        }

        private fun copyFile(inputStream: InputStream, out: OutputStream): Boolean {
            val buf = ByteArray(1024)
            var len = 0
            try {
                while (len != -1) {
                    len = inputStream.read(buf)
                    if (len != -1) {
                        out.write(buf, 0, len)
                    }
                }
            } catch (e: IOException) {
                println(e)
                return false
            }

            return true
        }
    }
}