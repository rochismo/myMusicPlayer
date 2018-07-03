package player.media.com.funcionara;

// Android stuff

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Java stuff

public class MainActivity extends ListActivity {
    private static final int UPDATE_FREQUENCY = 500;
    private static final int STEP_VALUE = 4000;
    private final Handler handler = new Handler();

    private TextView selectedFile = null;
    private SeekBar seekBar = null;
    private MediaPlayer player = null;
    private ImageButton prev = null;
    private ImageButton play = null;
    private ImageButton next = null;
    private ImageButton stop = null;
    private ImageButton shuffle = null;

    private boolean isStarted = true;
    private boolean isMovingSeekBar = false;
    private boolean isShuffled = false;
    private Song currentSong = null;
    private Integer backButtonCount = 0;
    private Map<Song, Integer> songRelations = null;
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
            createSongs(cursor);
            MediaCursorAdapter adapter = new MediaCursorAdapter(this, R.layout.item, cursor);
            adapter.setSongs(songs);
            setListAdapter(adapter);
        }
    }

    private void createSongs(Cursor cursor) {
        int idx = 0;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String title = cursor.getString(8);
            String author = cursor.getString(12);
            Integer duration = cursor.getInt(10);
            Integer id = cursor.getInt(0);
            String fullPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            if (duration <= 10000) {
                cursor.moveToNext();
                continue;
            }
            songs.add(new Song(id, duration, title, author, fullPath));
            cursor.moveToNext();
        }
        for (Song song : songs) {
            songRelations.put(song, idx);
            idx++;
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
        songRelations = new HashMap<>();
    }

    @Override
    public void onBackPressed() {
        if (isMovingSeekBar) {
            Toast.makeText(this, "Press the back button once again to put the app in the background.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Press the back button once again to close the application.", Toast.LENGTH_SHORT).show();
        }
        checkStatus(backButtonCount, isMovingSeekBar);
    }

    private void checkStatus(Integer count, boolean seekBar) {
        if (count >= 1 && !seekBar) {
            System.exit(0);
        }
        if (count >= 1 && seekBar) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            this.backButtonCount = 0;
            return;
        }
        backButtonCount++;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        position -= 2;
        currentSong = songs.get(position);
        startPlay(currentSong);
    }

    private void startPlay(Song song) {
        Log.i("Selected: ", song.getName());
        selectedFile.setText(song.getName());
        seekBar.setProgress(0);
        player.stop();
        player.reset();
        try {
            // This causes a bug for unknown reasons
            player.setDataSource(song.getFullPath());
            player.prepare();
            player.start();
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }
        seekBar.setMax(player.getDuration());
        play.setImageResource(android.R.drawable.ic_media_pause);
        updatePosition();
        isStarted = true;
        isMovingSeekBar = true;
    }

    private void stopPlay() {
        player.stop();
        player.reset();
        play.setImageResource(android.R.drawable.ic_media_play);
        handler.removeCallbacks(updatePositionRunnable);
        seekBar.setProgress(0);
        isStarted = false;
        isMovingSeekBar = false;
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
                    // If we didn't select a song, we are not going to uselessly throw an exception
                    final String NO_FILE_SELECTED = "No file Selected";
                    if (selectedFile.getText().equals(NO_FILE_SELECTED)) {
                        break;
                    }
                    // Check for the current status
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
                    // We play the very first song
                    startPlay(songs.get(0));
                    break;
                }

                case R.id.next: {
                    // Move to the next song
                    if (isShuffled) {
                        stopPlay();
                        break;
                    }
                    int nextIndex = songRelations.get(currentSong) + 1;
                    int index = nextIndex >= songs.size() - 1 ? 0 : nextIndex;
                    startPlay(songs.get(index));
                    break;
                }

                case R.id.previous: {
                    if (isShuffled) {
                        stopPlay();
                        break;
                    }
                    int nextIndex = songRelations.get(currentSong) - 1;
                    int index = nextIndex <= 0 ? songs.size() - 1 : nextIndex;
                    startPlay(songs.get(index));
                    break;
                }

                case R.id.stop: {
                    // Just stop
                    if (player.isPlaying()) {
                        stopPlay();
                    }
                    break;
                }

                case R.id.shuffle: {
                    isShuffled = !isShuffled;
                    break;
                }
            }
        }
    };
    private MediaPlayer.OnCompletionListener onCompletion = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (isShuffled) {
                startPlay(determineNextSong());
                return;
            }
            stopPlay();
        }

        private Song determineNextSong() {
            return songs.get((int) (Math.random() * songs.size()));
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