package ip.partyplaylist.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;


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

    private Button mAddButton, mStartParty;
    private TextView songTitle, songArtist, mPlaylistName;
    private ImageView albumCover;
    private ImageButton mPlayButton;

    private SlidingUpPanelLayout mSwipeUpPanel;
    private ImageView mSwipeUpBarImage;
    private TextView mSwipeUpBarTitle, mSwipeUpBarDetail, mPlayerTimeForward, tmpPartyIdHolder;
    private ImageButton mSwipeUpBarButton;
    private SeekBar mSeekBar;

    private PartifyTracksAdapter mPartifyTrackListAdapter;
    private Handler mHandler = new Handler();
    private Map<Song, Boolean> mTrackMap = new HashMap<>();
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
        mPlayButton = (ImageButton) findViewById(R.id.playerPlay);

        mSwipeUpPanel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mSwipeUpBarButton = (ImageButton) findViewById(R.id.btnSwipeUpBar);
        mSwipeUpBarImage = (ImageView) findViewById(R.id.swipeUpBarImage);
        mSwipeUpBarTitle = (TextView) findViewById(R.id.txtSwipeUpBarTitle);
        mSwipeUpBarDetail = (TextView) findViewById(R.id.txtSwipeUpBarDetail);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mPlayerTimeForward = (TextView) findViewById(R.id.playerTimeForward);
        tmpPartyIdHolder = (TextView) findViewById(R.id.tmpPartyIdTxt);


        //hide the swipe up bar at first
        mSwipeUpPanel.setPanelState(PanelState.HIDDEN);

        //set name of playlist in main view
        mPlaylistName.setText(mSharedPreferenceHelper.getCurrentPlaylistName());
        tmpPartyIdHolder.setText(mSharedPreferenceHelper.getCurrentPartyId());

        //add song button clicked action
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHostPlayerController.onAddTrackButtonPressed();
            }
        });

        //populates arraylist of Song objects
        mTrackList = mHostPlayerController.createSongModelArrayList();

        //creates Party object with the current tracklist
        mCurrentParty = mHostPlayerController.getInitialParty(mTrackList);
        mCurrentParty.setPartyAccessToken(mSharedPreferenceHelper.getCurrentSpotifyToken());

        for(Song s : mTrackList){
            mTrackMap.put(s,false);
        }

        mPartifyTrackListAdapter = new PartifyTracksAdapter(mTrackMap, mTrackList, this);
        mTrackListView.setAdapter(mPartifyTrackListAdapter);

        mHostPlayerController.updateCurrentFirebaseParty(mCurrentParty);


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

        mPlayButton.setTag(1);

        mHandler.postDelayed(run,1000);

        mStartParty.setEnabled(false);
        mStartParty.setVisibility(View.GONE);
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
                    mTrackMap.put(trackToAdd,false);
                    mPartifyTrackListAdapter.notifyDataSetChanged();

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


        //queue initial songs in list
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

        //code to show icon in list view of song that is playing
        if(event.equals(PlayerEvent.kSpPlaybackNotifyPlay) || event.equals(PlayerEvent.kSpPlaybackNotifyTrackChanged )) {

            for (Map.Entry<Song, Boolean> currentEntry : mTrackMap.entrySet()) {
                if (mPlayerMetaData.currentTrack != null && mPlayerMetaData.currentTrack.uri.equals(currentEntry.getKey().songID)) {

                    mTrackMap.put(currentEntry.getKey(), true);
                    mPartifyTrackListAdapter.notifyDataSetChanged();
                } else {
                    mTrackMap.put(currentEntry.getKey(), false);
                    mPartifyTrackListAdapter.notifyDataSetChanged();
                }
            }

            Map<Song, Boolean> tempMap = new HashMap<>();
            for(Map.Entry<Song, Boolean> e : mTrackMap.entrySet()){
                if(!tempMap.containsKey(e.getKey())){
                    tempMap.put(e.getKey(), e.getValue());
                }
            }

            ListView list = mTrackListView;
            int start = list.getFirstVisiblePosition();
            for(int i=start, j=list.getLastVisiblePosition();i<=j;i++){
                boolean flag = false;
                View view = list.getChildAt(i-start);

                Iterator<Map.Entry<Song,Boolean>> iter = tempMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<Song,Boolean> currentEntry = iter.next();

                    if(currentEntry.getKey()==list.getItemAtPosition(i) && currentEntry.getValue()){
                        flag = true;
                        iter.remove();
                    }
                }
                if(flag){
                    (view.findViewById(R.id.currentTrackPlayingIcon)).setVisibility(View.VISIBLE);
                    ((ImageView)view.findViewById(R.id.currentTrackPlayingIcon)).setImageResource(android.R.drawable.ic_media_play);
                }
                else{
                    (view.findViewById(R.id.currentTrackPlayingIcon)).setVisibility(View.GONE);

                }
            }
        }

        //update of UI showing name, artist, image of song
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



//    private class HttpRequestTask extends AsyncTask<Void, Void, String> {
//        @Override
//        protected Greeting doInBackground(Void... params) {
//            try {
//                final String url = "http://rest-service.guides.spring.io/greeting";
//                RestTemplate restTemplate = new RestTemplate();
//                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
//                String OAuthRefreshToken = restTemplate.getForObject(url, Greeting.class);
//                return greeting;
//            } catch (Exception e) {
//                Log.e("MainActivity", e.getMessage(), e);
//            }
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Greeting greeting) {
//            TextView greetingIdText = (TextView) findViewById(R.id.id_value);
//            TextView greetingContentText = (TextView) findViewById(R.id.content_value);
//            greetingIdText.setText(greeting.getId());
//            greetingContentText.setText(greeting.getContent());
//        }
//
//    }



    @Override
    protected void onDestroy() {
        // VERY IMPORTANT! This must always be called or else you will leak resources
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

}
