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
            val buf = ByteArray(BUFFER_SIZE)


            var readSize = inputStream.read(buf)
            while (readSize != -1) {
                readSize = if (buf.copyOfRange(readSize - COMPLETE_TEXT_BYTE_SIZE, readSize).contentEquals(fileTransferComplete.array())) {
                    fileOutputStream.write(buf, 0, readSize - COMPLETE_TEXT_BYTE_SIZE)
                    -1
                } else {
                    fileOutputStream.write(buf, 0, readSize)
                    inputStream.read(buf)
                }
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