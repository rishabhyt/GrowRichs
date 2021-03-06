package com.nhattuan.growrichs.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nhattuan.growrichs.Adapter.ChoiseRingtoneApdater;
import com.nhattuan.growrichs.Adapter.SelectableViewHolder;
import com.nhattuan.growrichs.Contants.ListSong;
import com.nhattuan.growrichs.R;
import com.nhattuan.growrichs.controller.MusicController;
import com.nhattuan.growrichs.model.SelectableItem;
import com.nhattuan.growrichs.model.Song;
import com.nhattuan.growrichs.services.MusicService;
import com.nhattuan.growrichs.ultil.MyDividerItemDecoration;
import com.nhattuan.growrichs.ultil.Utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class ListSongActivity extends AppCompatActivity
        implements MediaController.MediaPlayerControl, SelectableViewHolder.OnItemSelectedListener {
    private static final String TAG = "ListSongActivity";
    private List<Song> songList;
    private RecyclerView songView;
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;
    private MusicController controller;

    private TextView mTextDuration;
    private TextView mTextSeekto;
    private SeekBar mSeekbarAudio;
    private boolean mUserIsSeeking = false;
    private Utilities utils;
    private Song nameSong;
    private ChoiseRingtoneApdater songAdt;

    private Button btn_done;
    private Button btn_cancel;
    private LinearLayout ln_seekbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_song);
        songView = findViewById(R.id.song_list);
        songList = new ArrayList<>();

        songList.addAll(ListSong.getListSong());

        Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getmName().compareTo(b.getmName());
            }
        });


        setController();
        initializeUI();
        initializeSeekbar();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        songView.setLayoutManager(layoutManager);
        songView.setItemAnimator(new DefaultItemAnimator());
        songView.addItemDecoration(new MyDividerItemDecoration(this, DividerItemDecoration.VERTICAL, 0));
         songAdt = new ChoiseRingtoneApdater(songList, this, this, false);
        songView.setAdapter(songAdt);


    }

    private void setController() {
        //set the controller up
        controller = new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);

    }


    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
       if (playIntent == null)
        {
            playIntent = new Intent(this, MusicService.class);
            this.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        //if (playIntent == null)
        {
            playIntent = new Intent(this, MusicService.class);
            this.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        }
        songAdt.Changlist(songList);
    }

    private void resetTimemSeekbarAudio() {
        mSeekbarAudio.setProgress(0);
        mSeekbarAudio.setMax(100);
        // Updating progress bar
        updateProgressBar();
    }


    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: " + musicBound);
        super.onStop();

        if (musicBound) {
            // Hủy giàng buộc kết nối với dịch vụ.
            if (isPlaying()) {
                pause();
            }
            musicSrv = null;
            this.unbindService(musicConnection);
            musicBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: " + musicBound);
        if (musicBound) {
            // Hủy giàng buộc kết nối với dịch vụ.
            musicSrv = null;
            this.unbindService(musicConnection);
            musicBound = false;
        }

        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @Override
    public void start() {
        musicSrv.go();
    }

    @Override
    public void pause() {
        musicSrv.pausePlayer();
    }

    @Override
    public int getDuration() {
        if (musicSrv != null && musicBound) {
            return musicSrv.getDur();
        } else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (musicSrv != null && musicBound && musicSrv.isPng()) {
            return musicSrv.getPosn();
        } else return 0;
    }


    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);

    }

    @Override
    public boolean isPlaying() {
        if (musicSrv != null && musicBound)
            return musicSrv.isPng();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    //play next
    private void playNext() {
        musicSrv.playNext();
        controller.show(0);
    }

    //play previous
    private void playPrev() {
        musicSrv.playPrev();
        controller.show(0);
    }

    private void initializeUI() {
        mTextDuration = (TextView) findViewById(R.id.tv_duration);
        mTextSeekto = (TextView) findViewById(R.id.tv_seekto);
        ln_seekbar = findViewById(R.id.ln_seekbar);

        mSeekbarAudio = (SeekBar) findViewById(R.id.seekbar_audio);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_done = findViewById(R.id.btn_done);
        utils = new Utilities();

        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nameSong!=null) {
                    result();
                }else{
                    Toast.makeText(ListSongActivity.this,"not select ringtone",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onStop: " + musicBound);
        super.onBackPressed();
        finish();
    }

    private void result() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("NAMESONG", nameSong);
        setResult(ListSongActivity.RESULT_OK, returnIntent);
        finish();
    }


    private void initializeSeekbar() {
        mSeekbarAudio.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int userSelectedPosition = 0;

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        mUserIsSeeking = true;
                        new Handler().removeCallbacks(mUpdateTimeTask);
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            userSelectedPosition = progress;
                        }
                    }


                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mUserIsSeeking = false;
                        musicSrv.seek(userSelectedPosition);
                        new Handler().removeCallbacks(mUpdateTimeTask);
                        int totalDuration = getDuration();
                        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

                        // forward or backward to certain seconds
                        seekTo(currentPosition);

                        // update timer progress again
                        updateProgressBar();
                    }
                });
    }

    public void updateProgressBar() {
        new Handler().postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            if (isPlaying()){
                ln_seekbar.setVisibility(View.VISIBLE);
            long totalDuration = getDuration();
            long currentDuration = getCurrentPosition();

            // Displaying Total Duration time
            mTextDuration.setText("" + utils.milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            mTextSeekto.setText("" + utils.milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = (int) (utils.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            mSeekbarAudio.setProgress(progress);

            // Running this thread after 100 milliseconds
            new Handler().postDelayed(this, 100);
            }
            else{
                ln_seekbar.setVisibility(View.GONE);
            }
        }
    };

    @Override
    public void onItemSelected(SelectableItem item) {
        nameSong = item;
        if (isPlaying()) {
            pause();
        }
        Log.d(TAG, "onItemSelected: " + nameSong);
    }

    @Override
    public void onClick(SelectableItem item, boolean play, int position) {
        if(nameSong!=null)
        if(position==nameSong.getmID()) {
            if (play) {
                if (musicSrv != null) {
                    musicSrv.setSong(position);
                    musicSrv.playSong();
                    resetTimemSeekbarAudio();
                }
            } else {
                if (isPlaying()) {
                    pause();
                }
            }
        }
        Log.d(TAG, "onClick: " + position);
    }
}
