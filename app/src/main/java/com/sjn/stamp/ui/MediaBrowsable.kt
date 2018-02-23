package com.sjn.stamp.ui

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.view.View

interface MediaBrowsable {
    val mediaBrowser: MediaBrowserCompat?

    fun search(query: String, extras: Bundle?, callback: MediaBrowserCompat.SearchCallback)

    fun playByCategory(mediaId: String)

    fun playByMediaId(mediaId: String)

    fun navigateToBrowser(mediaId: String?, sharedElement: View?, sharedElementName: String?)

    fun sendCustomAction(action: String, extras: Bundle?, callback: MediaBrowserCompat.CustomActionCallback?)
}