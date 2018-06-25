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

    MediaCursorAdapter(Context context, int layout, Cursor c) {
        super(context, layout, c, new String[]{MediaStore.MediaColumns.DISPLAY_NAME,
                        MediaStore.MediaColumns.TITLE,MediaStore.Audio.AudioColumns.DURATION},
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
        // Setup the items
        TextView name = view.findViewById(R.id.displayname);
        TextView duration = view.findViewById(R.id.duration);
        for (Song song : songs){

            name.setText(song.getName());
            long durationInMS = song.getDuration();
            double durationInMin = ((double) durationInMS / 1000.0) / 60.0;

            durationInMin = new BigDecimal(Double.toString(durationInMin)).
                    setScale(2, BigDecimal.ROUND_UP).doubleValue();

            duration.setText("" + durationInMin);
            view.setTag(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA)));
        }

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.item, parent, false);
        bindView(v, context, cursor);
        return v;
    }
}
