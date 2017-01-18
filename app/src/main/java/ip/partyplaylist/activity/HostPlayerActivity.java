package ip.partyplaylist.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Picture;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import ip.partyplaylist.R;
import ip.partyplaylist.adapter.PartifyTracksAdapter;
import ip.partyplaylist.controllers.HostPlayerController;
import ip.partyplaylist.model.Party;
import ip.partyplaylist.model.Song;
import ip.partyplaylist.screen_actions.HostPlayerScreenActions;
import ip.partyplaylist.util.SharedPreferenceHelper;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;
import com.squareup.picasso.Picasso;


public class HostPlayerActivity extends AppCompatActivity implements SpotifyPlayer.NotificationCallback, HostPlayerScreenActions{//, CreatePartyScreenActions {

    private static final String CLIENT_ID = "ea09225ef2974242a1549f3812a15496";

    private Party mCurrentParty;
    private ArrayList<Song> mTrackList;
    private ListView mTrackListView;
    private Player mPlayer;
    private PlaybackState mCurrentPlayerState;
    private Metadata mPlayerMetaData;
    private HostPlayerController mHostPlayerController;
    private SharedPreferenceHelper mSharedPreferenceHelper;
    private PartifyTracksAdapter mPartifyTrackListAdapter;
    private Handler mHandler = new Handler();

    private Button mAddButton, mStartParty;
    private TextView songTitle, songArtist, mPlaylistName;
    private ImageView albumCover;
    private ImageButton mSkipBackButton, mPlayButton, mSkipForwardButton, mRepeatButton, mShuffleButton;

    private SlidingUpPanelLayout mSwipeUpPanel;
    private RelativeLayout mSwipeUpBar;
    private ImageView mSwipeUpBarImage;
    private TextView mSwipeUpBarTitle, mSwipeUpBarDetail, mPlayerTimeForward;
    private ImageButton mSwipeUpBarButton;
    private SeekBar mSeekBar;

    private ImageView mCurrentTrackPlayingIcon;

    private boolean queueFlag = false;

    private final Player.OperationCallback mOperationCallback = new Player.OperationCallback() {
        @Override
        public void onSuccess() {
            Log.i("HostPlayerActivity","OK!");
        }

        @Override
        public void onError(Error error) {
            Log.i("HostPlayerActivity","ERROR:" + error);
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_player);

        //intialize vars
        mSharedPreferenceHelper = new SharedPreferenceHelper(this);
        mHostPlayerController = new HostPlayerController(this);

        mTrackListView=(ListView)findViewById(R.id.lstviewTracks);

        mPlaylistName = (TextView) findViewById(R.id.playlistName);
        mAddButton = (Button) findViewById(R.id.btnAddSong);
        mStartParty = (Button) findViewById(R.id.btnStartParty);
        songTitle = (TextView) findViewById(R.id.txtSongTitle);
        songArtist = (TextView) findViewById(R.id.txtSongArtist);
        albumCover = (ImageView) findViewById(R.id.imgSongCover);
//        mSkipBackButton = (ImageButton) findViewById(R.id.playerSkipBack);
        mPlayButton = (ImageButton) findViewById(R.id.playerPlay);
        mSkipForwardButton= (ImageButton) findViewById(R.id.playerSkipForward);
//        mRepeatButton= (ImageButton) findViewById(R.id.playerRepeatButton);
//        mShuffleButton= (ImageButton) findViewById(R.id.playerShuffleButton);
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

//        mTrackListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position,
//                                    long id) {
//
//                //todo add a sound icon to the song that is playing
//                //attempt at this right below this
//
//                mCurrentTrackPlayingIcon = (ImageView) view.findViewById(R.id.currentTrackPlayingIcon);
//                mCurrentTrackPlayingIcon.setVisibility(View.VISIBLE);
//                mCurrentTrackPlayingIcon.setImageResource(android.R.drawable.ic_media_play);
//
//                mPlayButton.setTag(1);
//                mRepeatButton.setTag(0);
//                mShuffleButton.setTag(0);
//
//                mSwipeUpPanel.setPanelState(PanelState.COLLAPSED);
//
//                songTitle.setText(mTrackList.get(position).songName);
//                songArtist.setText(mTrackList.get(position).songArtistName);
//
//                mSwipeUpBarTitle.setText(songTitle.getText());
//                mSwipeUpBarDetail.setText(songArtist.getText());
//
//                String img_url = mTrackList.get(position).imageURL;
//                Picasso.with(HostPlayerActivity.this).load(img_url).into(albumCover);
//                Picasso.with(HostPlayerActivity.this).load(img_url).into(mSwipeUpBarImage);
//
//                mPlayer.playUri(null, mTrackList.get(position).songID, 0, 0);
//
//                mHandler.postDelayed(run,1000);
//
//                for(Song song : mTrackList){
//                    String tempSongID = song.songID;
//                    mPlayer.queue(null, tempSongID);
//                }
//            }
//        });
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

    //start party button
    public void startParty(View v){

        int currentPosition = 0;

        mPlayer.playUri(null, mTrackList.get(currentPosition).songID, 0, 0);


        mSwipeUpPanel.setPanelState(PanelState.COLLAPSED);

        songTitle.setText(mTrackList.get(currentPosition).songName);
        songArtist.setText(mTrackList.get(currentPosition).songArtistName);

        mSwipeUpBarTitle.setText(songTitle.getText());
        mSwipeUpBarDetail.setText(songArtist.getText());

        String img_url = mTrackList.get(currentPosition).imageURL;
        Picasso.with(HostPlayerActivity.this).load(img_url).into(albumCover);
        Picasso.with(HostPlayerActivity.this).load(img_url).into(mSwipeUpBarImage);

        //todo need to highlight the song that is playing in the list
//        mCurrentTrackPlayingIcon = (ImageView) mTrackListView.findViewById(R.id.currentTrackPlayingIcon);
//        mCurrentTrackPlayingIcon.setVisibility(View.VISIBLE);
//        mCurrentTrackPlayingIcon.setImageResource(android.R.drawable.ic_media_play);

        mPlayButton.setTag(1);

        mHandler.postDelayed(run,1000);



//        for(Song song : mTrackList){
//            String tempSongID = song.songID;
//            mPlayer.queue(mOperationCallback, tempSongID);
//            Log.d("HostPlayerActivity", "adding to queue");
//}

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

                    mHostPlayerController.updateCurrentSpotifyPlaylist(mCurrentParty);
                    mPlayer.queue(mOperationCallback,trackToAdd.songID);
                    Toast.makeText(this, "Song Added!", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, "Already in playlist!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //seekbar and time of song update
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

        //todo show notification in the android notification bar
        Log.i("hostplayeractivity", "Event: " + event);
        mCurrentPlayerState = mPlayer.getPlaybackState();
        mPlayerMetaData = mPlayer.getMetadata();
        Log.i("hostplayeractivity", "Player state: " + mCurrentPlayerState);
        Log.i("hostplayeractivity", "Metadata: " + mPlayerMetaData);

        if(event.equals(PlayerEvent.kSpPlaybackNotifyPlay)){
            if(!queueFlag){

                for(Song song : mTrackList){
                    String tempSongID = song.songID;
                    if(!tempSongID.equals(mPlayerMetaData.currentTrack.uri)){
                        Log.i("hostplayeractivity",tempSongID);
                        try
                        {Thread.sleep(100);}
                        catch (Exception e)
                        {e.printStackTrace();}
                        mPlayer.queue(mOperationCallback, tempSongID);
                    }
                }

                queueFlag = true;
            }
        }

        if(mPlayerMetaData.currentTrack!= null && !mPlayerMetaData.currentTrack.name.equals(songTitle)){
            songTitle.setText(mPlayerMetaData.currentTrack.name);
            songArtist.setText(mPlayerMetaData.currentTrack.artistName);

            mSwipeUpBarTitle.setText(songTitle.getText());
            mSwipeUpBarDetail.setText(songArtist.getText());

            String img_url = mPlayerMetaData.currentTrack.albumCoverWebUrl;
            Picasso.with(HostPlayerActivity.this).load(img_url).into(albumCover);
            Picasso.with(HostPlayerActivity.this).load(img_url).into(mSwipeUpBarImage);
        }

    }

    @Override
    protected void onDestroy() {
        // VERY IMPORTANT! This must always be called or else you will leak resources
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

}
