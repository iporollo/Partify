package ip.partyplaylist.activity;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import ip.partyplaylist.R;
import ip.partyplaylist.adapter.PartifyTracksAdapter;
import ip.partyplaylist.adapter.TracksAdapter;
import ip.partyplaylist.controllers.HostPlayerController;
import ip.partyplaylist.model.Party;
import ip.partyplaylist.model.Song;
import ip.partyplaylist.screen_actions.HostPlayerScreenActions;
import ip.partyplaylist.util.SharedPreferenceHelper;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;



public class HostPlayerActivity extends AppCompatActivity implements SpotifyPlayer.NotificationCallback, HostPlayerScreenActions{//, CreatePartyScreenActions {

    private static final String CLIENT_ID = "ea09225ef2974242a1549f3812a15496";

    private Party mCurrentParty;
    private ArrayList<Song> mTrackList;
    private ListView mTrackListView;
    private Player mPlayer;
    private PlaybackState mCurrentPlayerState;
    private  Metadata mPlayerMetaData;
    private HostPlayerController mHostPlayerController;
    private SharedPreferenceHelper mSharedPreferenceHelper;
    private PartifyTracksAdapter mPartifyTrackListAdapter;
    private Handler mHandler = new Handler();

    private Button mAddButton;
    private TextView songTitle, songArtist, mPlaylistName;
    private ImageView albumCover;
    private ImageButton mSkipBackButton, mPlayButton, mSkipForwardButton, mRepeatButton, mShuffleButton;

    private SlidingUpPanelLayout mSwipeUpPanel;
    private RelativeLayout mSwipeUpBar;
    private ImageView mSwipeUpBarImage;
    private TextView mSwipeUpBarTitle, mSwipeUpBarDetail, mPlayerTimeForward;
    private ImageButton mSwipeUpBarButton;
    private SeekBar mSeekBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_player);

        mHandler.postDelayed(run,1000);

        //intialize vars
        mSharedPreferenceHelper = new SharedPreferenceHelper(this);
        mHostPlayerController = new HostPlayerController(this);

        mTrackListView=(ListView)findViewById(R.id.lstviewTracks);

        mPlaylistName = (TextView) findViewById(R.id.playlistName);
        mAddButton = (Button) findViewById(R.id.btnAddSong);
        songTitle = (TextView) findViewById(R.id.txtSongTitle);
        songArtist = (TextView) findViewById(R.id.txtSongArtist);
        albumCover = (ImageView) findViewById(R.id.imgSongCover);
        mSkipBackButton = (ImageButton) findViewById(R.id.playerSkipBack);
        mPlayButton = (ImageButton) findViewById(R.id.playerPlay);
        mSkipForwardButton= (ImageButton) findViewById(R.id.playerSkipForward);
        mRepeatButton= (ImageButton) findViewById(R.id.playerRepeatButton);
        mShuffleButton= (ImageButton) findViewById(R.id.playerShuffleButton);
        mSwipeUpPanel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mSwipeUpBarButton = (ImageButton) findViewById(R.id.btnSwipeUpBar);
        mSwipeUpBarImage = (ImageView) findViewById(R.id.swipeUpBarImage);
        mSwipeUpBarTitle = (TextView) findViewById(R.id.txtSwipeUpBarTitle);
        mSwipeUpBarDetail = (TextView) findViewById(R.id.txtSwipeUpBarDetail);
        mSwipeUpBar = (RelativeLayout) findViewById(R.id.SwipeUpBarLayout);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mPlayerTimeForward = (TextView) findViewById(R.id.playerTimeForward);


        //hide the swipe up bar at first
        mSwipeUpPanel.setPanelState(PanelState.HIDDEN);

        //set name of playlist in main view
        mPlaylistName.setText(mSharedPreferenceHelper.getCurrentPlaylistName());

        //add song button clicked action
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHostPlayerController.onAddTrackButtonPressed();
            }
        });

        //creates spotify music player
        Config playerConfig = new Config(HostPlayerActivity.this, mSharedPreferenceHelper.getCurrentSpotifyToken(), CLIENT_ID);
        Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver(){
            @Override
            public void onInitialized(SpotifyPlayer spotifyPlayer) {
                mPlayer = spotifyPlayer;
                mPlayer.addNotificationCallback(HostPlayerActivity.this);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("HostPlayerActivity", "Could not initialize player: " + throwable.getMessage());
            }
        });


        //populates arraylist of Song objects
        mTrackList = mHostPlayerController.createSongModelArrayList();

        //creates Party object with the current tracklist
        mCurrentParty = mHostPlayerController.getInitialParty(mTrackList);

        mPartifyTrackListAdapter = new PartifyTracksAdapter(mTrackList, this);
        mTrackListView.setAdapter(mPartifyTrackListAdapter);

        mTrackListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {

                mPlayButton.setTag(1);
                mRepeatButton.setTag(0);
                mShuffleButton.setTag(0);

                mSwipeUpPanel.setPanelState(PanelState.COLLAPSED);

                songTitle.setText(mTrackList.get(position).songName);
                songArtist.setText(mTrackList.get(position).songArtistName);

                mSwipeUpBarTitle.setText(songTitle.getText());
                mSwipeUpBarDetail.setText(songArtist.getText());

                try {
                    String img_url = mTrackList.get(position).imageURL;
                    URL url = new URL(img_url);
                    Bitmap bmp;
                    bmp= BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    albumCover.setImageBitmap(bmp);
                    mSwipeUpBarImage.setImageBitmap(bmp);
                } catch (IOException e) {
                    System.err.print("Error getting album image");
                }

                mPlayer.playUri(null, mTrackList.get(position).songID, 0, 0);

                int remainingPosition = position - 1;
                for(int i = position+1; i <mTrackList.size(); i++){
                    String tempSongID = mTrackList.get(i).songID;
                    mPlayer.queue(null, tempSongID);
                }
                for(int j = 0; j < remainingPosition; j++ ){
                    String tempSongID = mTrackList.get(j).songID;
                    mPlayer.queue(null, tempSongID);
                }

                //todo add a sound icon to the song that is playing
            }
        });
    }


    //play pause button
    public void playerPlayAction(View v){
        final int status =(Integer) mPlayButton.getTag();
        if(status == 1) {
            //current status is playing
            mPlayer.pause(null);
            mPlayButton.setImageResource(android.R.drawable.ic_media_play);
            mSwipeUpBarButton.setImageResource(android.R.drawable.ic_media_play);
            mPlayButton.setTag(0);
            mSwipeUpBarButton.setTag(0);
            //paused on click
        } else {
            //current status is paused
            mPlayer.resume(null);
            mPlayButton.setImageResource(android.R.drawable.ic_media_pause);
            mSwipeUpBarButton.setImageResource(android.R.drawable.ic_media_pause);
            mPlayButton.setTag(1);
            mSwipeUpBarButton.setTag(1);
            //playing on click
        }
    }

    //skip back button
    public void playerSkipBackAction(View v){
        mPlayer.skipToPrevious(null);
    }

    //skip forward button
    public void playerSkipForwardAction(View v){
        mPlayer.skipToNext(null);
    }

    //repeat song button
    public void playerRepeatAction(View v){
        final int status =(Integer) mRepeatButton.getTag();
        if(status == 1) {
            //current status is repeat ON
            mPlayer.setRepeat(null, false);
            mRepeatButton.setTag(0);
            //turn OFF repeat song on click
        } else {
            //current status is repeat OFF
            mPlayer.setRepeat(null, true);
            mRepeatButton.setTag(1);
            //turn ON repeat song on click
        }
    }
    //shuffle button
    public void playerShuffleAction(View v){
        final int status =(Integer) mShuffleButton.getTag();
        if(status == 1) {
            //current status is shuffle ON
            mPlayer.setShuffle(null, false);
            mShuffleButton.setTag(0);
            //turn OFF shuffle song on click
        } else {
            //current status is shuffle OFF
            mPlayer.setShuffle(null, true);
            mShuffleButton.setTag(1);
            //turn ON shuffle song on click
        }
    }


    //Add song functionality
    @Override
    public void showSearchTrackScreen() {
        Intent startSearchSongActivity = new Intent(this, SearchTrackActivity.class);
        startActivityForResult(startSearchSongActivity, CreatePartyActivity.SEARCH_SONG_REQUEST_CODE); // activates the method below
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CreatePartyActivity.SEARCH_SONG_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Song trackToAdd = (Song) data.getExtras().get(SearchTrackActivity.TRACK);
                boolean isAlreadyInList = false;

                for(int i = 0; i <mTrackList.size(); i++){
                    if(mTrackList.get(i).songID.equals(trackToAdd.songID)){
                        isAlreadyInList = true;
                    }
                }

                if(!isAlreadyInList){
                    mTrackList.add(trackToAdd);
                    //mSongListOfStrings.add(trackToAdd.songArtistName + " - " + trackToAdd.songName);

                    mHostPlayerController.updateCurrentSpotifyPlaylist(mCurrentParty);
                    mPlayer.queue(null,trackToAdd.songID);
                    Toast.makeText(this, "Song Added!", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, "Already in playlist!", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    //seekbar update
    private final Runnable run = new Runnable() {

        @Override
        public void run() {
            if(mPlayer != null){
                mCurrentPlayerState = mPlayer.getPlaybackState();
                if(mCurrentPlayerState.isPlaying){
                    mPlayerMetaData = mPlayer.getMetadata();

                    long mCurrentPosition = mCurrentPlayerState.positionMs / 1000;
                    mSeekBar.setMax((int) (mPlayerMetaData.currentTrack.durationMs / 1000));
                    mSeekBar.setProgress((int) mCurrentPosition);

                    int seconds = (int) (mCurrentPlayerState.positionMs / 1000) % 60 ;
                    int minutes = (int) ((mCurrentPlayerState.positionMs / (1000*60)) % 60);
                    mPlayerTimeForward.setText(String.format("%d:%02d",minutes,seconds));

                    Log.d("HostPlayerActivity", "handler");
                }
            }
            mHandler.postDelayed(this, 1000);


            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if(mPlayer != null && fromUser){
                        mPlayer.seekToPosition(null, progress * 1000);

                        int seconds = (int) (mCurrentPlayerState.positionMs / 1000) % 60 ;
                        int minutes = (int) ((mCurrentPlayerState.positionMs / (1000*60)) % 60);
                        mPlayerTimeForward.setText(String.format("%d:%02d",minutes,seconds));
                    }
                }
            });
        }

    };

    //Player methods
    @Override
    public void onPlaybackError(Error error) {
        Log.d("HostPlayerActivity", "Playback failed");
    }

    @Override
    public void onPlaybackEvent(PlayerEvent event) {
        Log.d("HostPlayerActivity", "playback event logging right here");


//        mSongTime.setText(String.format("%d:%02d", minutes,seconds);
//        mSongProgress.setProgress((int) mCurrentPlayerState.positionMs);
        //add animation

        //todo show notification in the android notification bar

    }

    @Override
    protected void onDestroy() {
        // VERY IMPORTANT! This must always be called or else you will leak resources
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

}
