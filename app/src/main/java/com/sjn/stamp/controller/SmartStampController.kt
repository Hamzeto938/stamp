package com.sjn.stamp.controller

import android.content.Context
import com.sjn.stamp.constant.RecordType
import com.sjn.stamp.db.Song
import com.sjn.stamp.db.dao.SongHistoryDao
import com.sjn.stamp.utils.RealmHelper

internal class SmartStampController(private val mContext: Context) {
    internal enum class SmartStamp(var mStamp: String) {
        HEAVY_ROTATION("Heavy Rotation") {
            override fun isTarget(context: Context, song: Song, playCount: Int, recordType: RecordType): Boolean {
                var result = false
                val songHistoryDao = SongHistoryDao.getInstance()
                var counter = 0
                RealmHelper.getRealmInstance().use { realm ->
                    val songHistoryList = songHistoryDao.findAll(realm, RecordType.PLAY.value)
                    for (songHistory in songHistoryList) {
                        if (songHistory.song == null || songHistory.song != song) {
                            break
                        }
                        counter++
                        if (counter >= 10) {
                            result = true
                            break
                        }
                    }
                }
                return result
            }

            override fun register(context: Context, song: Song, playCount: Int, recordType: RecordType) {
                registerStamp(context, mStamp)
                val songController = SongController(context)
                songController.registerSystemStamp(mStamp, song)
            }
        },
        ARTIST_BEST("Artist Best") {
            override fun isTarget(context: Context, song: Song, playCount: Int, recordType: RecordType): Boolean =
                    false

            override fun register(context: Context, song: Song, playCount: Int, recordType: RecordType) {
                registerStamp(context, mStamp)

            }
        },
        BREAK_SONG("Break Song") {
            override fun isTarget(context: Context, song: Song, playCount: Int, recordType: RecordType): Boolean =
                    false

            override fun register(context: Context, song: Song, playCount: Int, recordType: RecordType) {
                registerStamp(context, mStamp)

            }
        };

        internal abstract fun isTarget(context: Context, song: Song, playCount: Int, recordType: RecordType): Boolean

        abstract fun register(context: Context, song: Song, playCount: Int, recordType: RecordType)

        internal fun registerStamp(context: Context, stamp: String) {
            val stampController = StampController(context)
            stampController.register(stamp)
        }
    }

    fun calculateAsync(song: Song, playCount: Int, recordType: RecordType) {
        Thread(Runnable { calculate(song, playCount, recordType) }).start()
    }

    private fun calculate(song: Song, playCount: Int, recordType: RecordType) {
        SmartStamp.values()
                .filter { it.isTarget(mContext, song, playCount, recordType) }
                .forEach { it.register(mContext, song, playCount, recordType) }
    }
}