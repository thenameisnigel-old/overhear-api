package com.afollestad.overhearapi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class Playlist {

	private Playlist() {
    }

    private long _id;
	private String name;
    private String data;
    private long dateAdded;
    private long dateModified;

    public long getId() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public String getData() {
        return data;
    }

    public Calendar getDateAdded() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateAdded);
        return cal;
    }

    public Calendar getDateModified() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateModified);
        return cal;
    }


	public JSONObject getJSON() {
		JSONObject json = new JSONObject();
		try {
            json.put("_id", this._id);
            json.put("_data", this.data);
			json.put("name", this.name);
			json.put("date_added", this.dateAdded);
            json.put("date_modified", this.dateModified);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return json;
	}

    public Uri getSongUri() {
        return MediaStore.Audio.Playlists.Members.getContentUri("external", getId());
    }

    public ArrayList<Song> getSongs(Context context) {
        ArrayList<Song> toreturn = Song.getAllFromUri(context, getSongUri(), null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        for(int i = 0; i < toreturn.size(); i++) {
            toreturn.set(i, toreturn.get(i).setFromPlaylist(getId()));
        }
        return toreturn;
    }

	public static Playlist fromJSON(String json) {
		try {
			return fromJSON(new JSONObject(json));
		} catch (JSONException e) {
			e.printStackTrace();
			throw new Error(e.getMessage());
		}
	}
	public static Playlist fromJSON(JSONObject json) {
		Playlist playlist = new Playlist();
		try {
            playlist._id = json.getLong("_id");
            playlist.data = json.getString("_data");
			playlist.name = json.getString("name");
            playlist.dateAdded = json.getLong("date_added");
            playlist.dateModified = json.getLong("date_modified");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return playlist;
	}

	public static Playlist fromCursor(Cursor cursor) {
		Playlist playlist = new Playlist();

		playlist._id = cursor.getLong(cursor.getColumnIndex("_id"));
        playlist.data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.PlaylistsColumns.DATA));
        playlist.name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.PlaylistsColumns.NAME));
        playlist.dateAdded = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.PlaylistsColumns.DATE_ADDED));
        playlist.dateModified = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.PlaylistsColumns.DATE_MODIFIED));

		return playlist;
	}

    public void insertSong(Context context, Song song) {
        context.getContentResolver().insert(getSongUri(), song.getContentValues(false));
    }

    public void insertSongs(Context context, ArrayList<Song> songs) {
        for(Song s : songs) {
            context.getContentResolver().insert(getSongUri(), s.getContentValues(false));
        }
    }

    public static ArrayList<Playlist> getAllPlaylists(Context context) {
        ArrayList<Playlist> toreturn = new ArrayList<Playlist>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, null,
                null, null, MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
        while(cursor.moveToNext()) {
            toreturn.add(Playlist.fromCursor(cursor));
        }
        cursor.close();
        return toreturn;
    }

    public static Playlist create(Context context, String name) {
        ContentValues values = new ContentValues();
        Calendar now = Calendar.getInstance();
        values.put(MediaStore.Audio.Playlists.NAME, name);
        values.put(MediaStore.Audio.Playlists.DATE_ADDED, now.getTimeInMillis());
        values.put(MediaStore.Audio.Playlists.DATE_MODIFIED, now.getTimeInMillis());
        Uri uri = context.getContentResolver().insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values);
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        Playlist toreturn = Playlist.fromCursor(cursor);
        cursor.close();
        return toreturn;
    }
}