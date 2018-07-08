package player.media.com.funcionara;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MediaCursorAdapter extends SimpleCursorAdapter {
    private List<Song> songs = new ArrayList<>();
    private Integer index = 0;
    private Integer xtraMins = 0;

    MediaCursorAdapter(Context context, int layout, Cursor c) {
        super(context, layout, c, new String[]{MediaStore.MediaColumns.DISPLAY_NAME,
                        MediaStore.MediaColumns.TITLE, MediaStore.Audio.AudioColumns.DURATION},
                new int[]{R.id.displayname, R.id.title, R.id.duration});
    }


    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView name = view.findViewById(R.id.displayname);
        TextView duration = view.findViewById(R.id.duration);
        Song song = getSongById(cursor.getInt(0));
        
        name.setText(song.getName());
        duration.setText(convertDuration(song.getDuration()));

        view.setTag(song.getFullPath());
    }


    private String convertDuration(long duration) {
        long hours;
        try {
            hours = (duration / 3600000);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        long minutesLeft = (duration - (hours * 3600000)) / 60000;
        String minutes = String.valueOf(minutesLeft);
        if (minutes.equals("0")) {
            minutes = "00";
        }
        long secondsLeft = (duration - (hours * 3600000) - (minutesLeft * 60000));
        String seconds = String.valueOf(secondsLeft);
        seconds = seconds.length() < 2 ? "00" : seconds.substring(0,2);
        return hours > 0 ? hours + ":" + minutes + ":" + seconds : minutes + ":" + seconds;
    }
    
    private Song getSongById(int id) {
        Song returnValue = null;
        for (Song song : songs) {
            if (song.getId().equals(id)) {
                returnValue = song;
            }
        }
        return returnValue;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.item, parent, false);
        bindView(v, context, cursor);
        return v;
    }
}
