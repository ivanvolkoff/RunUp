package com.volkov.runup.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "running_table")//running table will be name of the table in our database
data class Run(
    var img: Bitmap? = null,
    var timeStamp: Long = 0L, // saving time as Long, not as Date because of easier sorting later
    var avgSpeedInKMH: Float = 0f,
    var distanceInMeters: Int = 0,
    var timeInMillis: Long = 0L,
    var calloriesBurned: Int = 0
) {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

}