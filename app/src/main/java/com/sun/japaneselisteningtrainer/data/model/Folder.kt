package com.sun.japaneselisteningtrainer.data.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class Folder (
    val id : Int = 0,
    val name: String = "",
    val description: String = "",
    val createdAt: Long = 0
)

@RequiresApi(Build.VERSION_CODES.O)
fun unixToDateTime(timestamp: Long): String {
    val instant = Instant.ofEpochSecond(timestamp)  // nếu timestamp tính theo giây
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.systemDefault())           // múi giờ local
    return formatter.format(instant)
}
