package com.volkov.runup.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class Converters {

    @TypeConverter
    fun tobitmap(bytes: ByteArray): Bitmap {
        //using Bitmap factory to decode ByteArray
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    //from bitmap to bytes
    @TypeConverter
    fun fromBitmap(bmp: Bitmap): ByteArray {
        //creating outputstream for our bytes
        val outputStream = ByteArrayOutputStream()
        //compressing btm and writing bytes to outputStream
        bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }
}