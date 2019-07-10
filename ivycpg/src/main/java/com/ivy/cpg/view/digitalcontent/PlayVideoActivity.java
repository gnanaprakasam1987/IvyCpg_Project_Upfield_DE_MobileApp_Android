package com.ivy.cpg.view.digitalcontent;

import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PlayVideoActivity extends IvyBaseActivityNoActionBar{

    private VideoView myVideoView;
    private int position = 0;
    private ProgressDialog progressDialog;
    private MediaController mediaControls;
    private String productID, digiContentId;
    private String vStart_Time;
    private boolean isFastForward = false;
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);

        mediaControls = new MediaController(PlayVideoActivity.this);

        //initialize the VideoView
        myVideoView = findViewById(R.id.video_view);

        // create a progress bar while the video file is loading
        progressDialog = new ProgressDialog(PlayVideoActivity.this);
        // set a title for the progress bar
        progressDialog.setTitle("Product Video");
        // set a message for the progress bar
        progressDialog.setMessage("Loading...");
        //set the progress bar not cancelable on users' touch
        progressDialog.setCancelable(true);
        // show the progress bar
        progressDialog.show();

        try {

            mediaControls.setAnchorView(myVideoView);
            mediaControls.setMediaPlayer(myVideoView);
            myVideoView.setMediaController(mediaControls);

            if (getIntent().getExtras() != null) {
                filePath = getIntent().getExtras().getString("videoPath", "");
                digiContentId = getIntent().getExtras().getString("DigiContentId", "");
                productID = getIntent().getExtras().getString("PId", "");
                myVideoView.setVideoURI(Uri.parse(filePath));
            }
            //set the uri of the video to be played


        } catch (Exception e) {
            Commons.printException(e);
            e.printStackTrace();
        }

        myVideoView.requestFocus();

        myVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            public void onPrepared(MediaPlayer mediaPlayer) {
                progressDialog.dismiss();

                myVideoView.seekTo(position);
                if (position == 0) {
                    vStart_Time = "" + DateTimeUtils.now(DateTimeUtils.DATE) + " " + DateTimeUtils.now(DateTimeUtils.TIME);
                    myVideoView.start();
                } else {
                    myVideoView.pause();
                }

                final int topContainerId1 = getResources().getIdentifier("mediacontroller_progress", "id", "android");
                final SeekBar seekbar =  mediaControls.findViewById(topContainerId1);

                seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        Commons.print("onProgressChanged - "+progress);

                        if (!fromUser) {
                            // We're not interested in programmatically generated changes to
                            // the progress bar's position.
                            return;
                        }

                        long duration = myVideoView.getDuration();
                        long newposition = (duration * progress) / 1000L;
                        myVideoView.seekTo( (int) newposition);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        Commons.print("onStartTrackingTouch");
                        isFastForward = true;
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        int seekValue = seekBar.getProgress();
                        //int newMinutes = Math.round((float)getTotalDuration() * (float)seekValue / (float)seekBar.getMax());
                        Commons.print("onStopTrackingTouch "+myVideoView.getDuration() + " ---- "+seekValue);

                        //myVideoView.seekTo(seekValue);
                    }
                });
            }
        });

        myVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                // TODO Auto-generated method stub
                myVideoView.setFocusable(false);
                progressDialog.dismiss();
                Toast.makeText(PlayVideoActivity.this, getResources().getString(R.string.unable_to_play), Toast.LENGTH_LONG).show();
                finish();
                return false;
            }
        });

        myVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

            }
        });


    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        //we use onSaveInstanceState in order to store the video playback position for orientation change
        savedInstanceState.putInt("Position", myVideoView.getCurrentPosition());
        myVideoView.pause();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //we use onRestoreInstanceState in order to play the video playback from the stored position
        position = savedInstanceState.getInt("Position");
        myVideoView.seekTo(position);
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        //super.onBackPressed();

        if(!filePath.contains("/" + DataMembers.PROMOTION + "/")) {
            String watchedDuration;

            int watchedSeconds = (int) (myVideoView.getCurrentPosition() / 1000) % 60;
            int watchedMinutes = (int) ((myVideoView.getCurrentPosition() / (1000 * 60)) % 60);
            int watchedHours = (int) ((myVideoView.getCurrentPosition() / (1000 * 60 * 60)) % 24);
            watchedDuration = "" + watchedHours + ":" + watchedMinutes + ":" + watchedSeconds;
            Commons.print("Duration : " + watchedDuration);

            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss", Locale.US);
            Date d;
            String vEnd_Time = "";

            try {
                String[] time = vStart_Time.split(" ");
                d = df.parse(time[1]);

                Calendar cal = Calendar.getInstance();
                cal.setTime(d);
                cal.add(Calendar.HOUR, watchedHours);
                cal.add(Calendar.MINUTE, watchedMinutes);
                cal.add(Calendar.SECOND, watchedSeconds);

                String newTime = df.format(cal.getTime());

                vEnd_Time = "" + DateTimeUtils.now(DateTimeUtils.DATE) + " " + newTime;

            } catch (Exception e) {
                Commons.printException(e);
            }

            DigitalContentHelper.getInstance(PlayVideoActivity.this).
                    saveDigitalContentDetails(PlayVideoActivity.this, digiContentId, productID,
                            vStart_Time, vEnd_Time, isFastForward);
        }
        finish();

    }
}
