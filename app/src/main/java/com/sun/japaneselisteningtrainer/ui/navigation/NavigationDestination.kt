package com.sun.japaneselisteningtrainer.ui.navigation

import com.sun.japaneselisteningtrainer.R



interface NavigationDestination {
    /**
     * Unique name to define the path for a composable
     */
    val route: String

    /**
     * String resource id to that contains title to be displayed for the screen.
     */
    val titleRes: Int?
}
object MusicDestination : NavigationDestination {
    override val route: String = "music"
    override val titleRes: Int? = R.string.app_name // hoặc null nếu không dùng
}

/** Màn hiển thị transcript/lyrics (Japanese) */
object LyricsDestination : NavigationDestination {
    const val ARG_TRACK_ID = "trackId"

    // pattern route để khai báo trong NavHost
    override val route: String = "lyrics/{$ARG_TRACK_ID}"
    override val titleRes: Int? = R.string.app_name // thay resource phù hợp

    // helper để tạo route cụ thể khi navigate
    fun createRoute(trackId: String): String = "lyrics/$trackId"
}

