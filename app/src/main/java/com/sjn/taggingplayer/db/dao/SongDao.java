package com.sjn.taggingplayer.db.dao;

import com.sjn.taggingplayer.db.Song;

import io.realm.Realm;

public class SongDao extends BaseDao {

    private static SongDao sInstance;

    public static SongDao getInstance() {
        if (sInstance == null) {
            sInstance = new SongDao();
        }
        return sInstance;
    }

    public Song findOrCreate(Realm realm, Song rawSong) {
        Song song = realm.where(Song.class).equalTo("mTitle", rawSong.getTitle()).equalTo("mArtist", rawSong.getArtist()).findFirst();
        if (song == null) {
            rawSong.setId(getAutoIncrementId(realm, Song.class));
            song = realm.copyToRealm(rawSong);
        }
        return song;
    }

    public Song newStandalone() {
        return new Song();
    }
}