package player.media.com.funcionara;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ListActivity {
    private static final int UPDATE_FREQUENCY = 500;
    private static final int STEP_VALUE = 4000;

    private TextView selectedFile = null;
    private SeekBar seekBar = null;
    private MediaPlayer player = null;
    private ImageButton prev = null;
    private ImageButton play = null;
    private ImageButton next = null;
    private ImageButton stop = null;
    private ImageButton shuffle = null;

    private boolean isStarted = true;
    private String currentFile = "";
    private boolean isMovingSeekBar = false;
    private final Handler handler = new Handler();

    private List<Song> songs = null;

    private final Runnable updatePositionRunnable = new Runnable() {
        @Override
        public void run() {
            updatePosition();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        @SuppressLint("Recycle") Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.
                EXTERNAL_CONTENT_URI, null, null, null, null);

        if (null != cursor) {
            cursor.moveToFirst();
            MediaCursorAdapter adapter = new MediaCursorAdapter(this, R.layout.item, cursor);
            setListAdapter(adapter);
            while (!cursor.isAfterLast()){
                String title = cursor.getString(8);
                String author = cursor.getString(12);
                Integer duration = cursor.getInt(10);
                Integer id = cursor.getInt(0);
                if (duration <= 10000) {cursor.moveToNext(); continue;}
                songs.add(new Song(id, duration, title, author));
                cursor.moveToNext();
            }
        }
    }

    private void setupListeners() {
        prev.setOnClickListener(OnButtonClick);
        play.setOnClickListener(OnButtonClick);
        next.setOnClickListener(OnButtonClick);
        stop.setOnClickListener(OnButtonClick);
        shuffle.setOnClickListener(OnButtonClick);
        player.setOnCompletionListener(onCompletion);
        player.setOnErrorListener(onError);
        seekBar.setOnSeekBarChangeListener(seekBarChanged);

    }

    private void init() {
        selectedFile = findViewById(R.id.selecteditem);
        seekBar = findViewById(R.id.seekBar);
        prev = findViewById(R.id.previous);
        play = findViewById(R.id.play);
        next = findViewById(R.id.next);
        stop = findViewById(R.id.stop);
        shuffle = findViewById(R.id.shuffle);
        player = new MediaPlayer();
        songs = new ArrayList<>();
        setupListeners();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        currentFile = (String) v.getTag();
        startPlay(currentFile);
    }

    private void startPlay(String file) {
        Log.i("Selected: ", file);
        selectedFile.setText(file);
        seekBar.setProgress(0);
        player.stop();
        player.reset();

        try {
            player.setDataSource(file);
            player.prepare();
            player.start();
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }
        seekBar.setMax(player.getDuration());
        play.setImageResource(android.R.drawable.ic_media_pause);
        updatePosition();
        isStarted = true;
    }

    private void stopPlay() {
        player.stop();
        player.reset();
        play.setImageResource(android.R.drawable.ic_media_play);
        handler.removeCallbacks(updatePositionRunnable);
        seekBar.setProgress(0);
        isStarted = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updatePositionRunnable);
        player.stop();
        player.reset();
        player.release();
        player = null;
    }

    private void updatePosition() {
        handler.removeCallbacks(updatePositionRunnable);
        seekBar.setProgress(player.getCurrentPosition());
        handler.postDelayed(updatePositionRunnable, UPDATE_FREQUENCY);
    }

    private View.OnClickListener OnButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.play: {
                    if (player.isPlaying()) {
                        handler.removeCallbacks(updatePositionRunnable);
                        player.pause();
                        play.setImageResource(android.R.drawable.ic_media_play);
                        break;
                    }
                    if (isStarted) {
                        player.start();
                        play.setImageResource(android.R.drawable.ic_media_pause);
                        updatePosition();
                        break;
                    }
                    startPlay(currentFile);
                    break;
                }

                case R.id.next: {
                    int seekTo = player.getCurrentPosition() + STEP_VALUE;
                    if (seekTo > player.getDuration()) {
                        seekTo = player.getDuration();
                    }
                    player.pause();
                    player.seekTo(seekTo);
                    player.start();
                    break;
                }

                case R.id.previous: {
                    int seekTo = player.getCurrentPosition() - STEP_VALUE;
                    if (seekTo < 0) {
                        seekTo = 0;
                    }
                    player.pause();
                    player.seekTo(seekTo);
                    player.start();
                    break;
                }

                case R.id.stop: {
                    if (player.isPlaying()) {
                        stopPlay();
                    }
                    break;
                }
            }
        }
    };
    private MediaPlayer.OnCompletionListener onCompletion = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            stopPlay();
        }
    };
    private MediaPlayer.OnErrorListener onError = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            return false;
        }
    };

    private SeekBar.OnSeekBarChangeListener seekBarChanged =
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (isMovingSeekBar) {
                        player.seekTo(progress);
                        Log.i("OnSeekBarChangeListener", "OnProgressChanged");
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    isMovingSeekBar = true;
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    isMovingSeekBar = false;
                }
            };
}