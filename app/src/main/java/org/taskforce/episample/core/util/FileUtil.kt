package org.taskforce.episample.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.util.zip.ZipInputStream


class FileUtil {
    companion object {
        private const val TAG = "FileUtil"
        private const val BUFFER = 1024

        fun delete(files: List<File>) {
            files.forEach {
                if (it.exists()) {
                    it.delete()
                }
            }
        }

        fun zip(_files: Array<String>, zipFilePathName: String) {
            try {
                var origin: BufferedInputStream?
                val dest = FileOutputStream(zipFilePathName)
                val out = ZipOutputStream(BufferedOutputStream(
                        dest))

                val data = ByteArray(BUFFER)

                for (i in _files.indices) {
                    val fi = FileInputStream(_files[i])
                    origin = BufferedInputStream(fi, BUFFER)

                    val entry = ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1))
                    out.putNextEntry(entry)
                    var count = 0

                    while (count != -1) {
                        count = origin.read(data, 0, BUFFER)
                        if (count != -1) {
                            out.write(data, 0, count)
                        }
                    }
                    origin.close()
                }

                out.close()
            } catch (e: Exception) {

                e.printStackTrace()
            }

        }

        @Throws(IOException::class)
        fun unzip(zipFile: File, targetDirectory: File) {
            val zis = ZipInputStream(
                    BufferedInputStream(FileInputStream(zipFile)))
            try {
                var ze: ZipEntry? = zis.nextEntry
                var count: Int
                val buffer = ByteArray(8192)
                while (ze != null) {
                    val file = File(targetDirectory, ze.name)
                    val dir = if (ze.isDirectory) file else file.parentFile
                    if (!dir.isDirectory && !dir.mkdirs())
                        throw FileNotFoundException("Failed to ensure directory: " + dir.absolutePath)
                    if (ze.isDirectory)
                        continue
                    val fout = FileOutputStream(file)
                    try {
                        count = zis.read(buffer)
                        while (count != -1) {
                            fout.write(buffer, 0, count)
                            count = zis.read(buffer)
                        }
                    } finally {
                        fout.close()
                    }
                    ze = zis.nextEntry
                }
            } finally {
                zis.close()
            }
        }

        fun copyFile(inputStream: InputStream, out: OutputStream): Boolean {
            val buf = ByteArray(1024)
            var len = 0
            try {
                while (len != -1) {
                    len = inputStream.read(buf)
                    if (len != -1) {
                        out.write(buf, 0, len)
                    }
                }
                out.close()
                inputStream.close()
            } catch (e: IOException) {
                Log.d(TAG, e.toString())
                return false
            }

            return true
        }

        fun createImageFile(context: Context): File {
            // Create an image file name
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            return File.createTempFile(
                    "GPSSample_${timeStamp}_", /* prefix */
                    ".jpg", /* suffix */
                    storageDir /* directory */
            ).apply {
                // Save a file: path for use with ACTION_VIEW intents
                return this
            }
        }
        
        fun compressBitmap(context: Context, photoUri: Uri, compressionScale: Int) {
            val original = MediaStore.Images.Media.getBitmap(context.contentResolver, photoUri)
            val out = ByteArrayOutputStream()
            original.compress(Bitmap.CompressFormat.JPEG, compressionScale, out)
            
            var fileOut: OutputStream? = null
            try {
                fileOut = context.contentResolver.openOutputStream(photoUri, "w")
                
                out.writeTo(fileOut)
            } catch (ex: IOException) {
                Log.d(TAG, ex.toString())
            } finally {
                fileOut?.close()
            }
        }
    }
}
