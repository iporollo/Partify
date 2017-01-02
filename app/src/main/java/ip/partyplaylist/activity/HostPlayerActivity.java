package ip.partyplaylist.activity;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import ip.partyplaylist.R;
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
    private ArrayList<String> mSongListOfStrings;
    private Player mPlayer;
    private PlaybackState mCurrentPlayerState;
    private HostPlayerController mHostPlayerController;
    private SharedPreferenceHelper mSharedPreferenceHelper;

    private Button mAddButton;
    private TextView songTitle;
    private TextView songArtist;
    private TextView mPlaylistName;
    private ImageView albumCover;
    private ImageButton mSkipBackButton;
    private ImageButton mPlayButton;
    private ImageButton mPauseButton;
    private ImageButton mSkipForwardButton;
    private ImageButton mRepeatButton;
    private ImageButton mShuffleButton;

    private ImageView mSwipeUpBarImage;
    private TextView mSwipeUpBarTitle;
    private TextView mSwipeUpBarDetail;
    private ImageButton mSwipeUpBarButton;
    private SlidingUpPanelLayout mSwipeUpPanel;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_player);
        mSharedPreferenceHelper = new SharedPreferenceHelper(this);
        mHostPlayerController = new HostPlayerController(this);

        mPlaylistName = (TextView) findViewById(R.id.playlistName);
        mAddButton = (Button) findViewById(R.id.btnAddSong);
        songTitle = (TextView) findViewById(R.id.txtSongTitle);
        songArtist = (TextView) findViewById(R.id.txtSongArtist);
        albumCover = (ImageView) findViewById(R.id.imgSongCover);
        mSkipBackButton = (ImageButton) findViewById(R.id.playerSkipBack);
        mPlayButton = (ImageButton) findViewById(R.id.playerPlay);
        mPauseButton = (ImageButton) findViewById(R.id.playerPause);
        mSkipForwardButton= (ImageButton) findViewById(R.id.playerSkipForward);
        mRepeatButton= (ImageButton) findViewById(R.id.playerRepeatButton);
        mShuffleButton= (ImageButton) findViewById(R.id.playerShuffleButton);

        mSwipeUpPanel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mSwipeUpPanel.setPanelState(PanelState.HIDDEN);

        mPlaylistName.setText(mSharedPreferenceHelper.getCurrentPlaylistName());

        songTitle.setVisibility(View.GONE);
        songArtist.setVisibility(View.GONE);

        mSkipBackButton.setClickable(false);
        mPlayButton.setClickable(false);
        mPauseButton.setClickable(false);
        mPauseButton.setVisibility(View.GONE);
        mSkipForwardButton.setClickable(false);
        mRepeatButton.setClickable(false);
        mShuffleButton.setClickable(false);


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


        mTrackList = mHostPlayerController.createSongModelArrayList();
        mSongListOfStrings = mHostPlayerController.createSongStringArrayList();

        mCurrentParty = mHostPlayerController.getInitialParty(mTrackList);

        // Get the reference of ListViewAnimals
        final ListView lstviewTracksGUI=(ListView)findViewById(R.id.lstviewTracks);

        // Create The Adapter with passing ArrayList as 3rd parameter
        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mSongListOfStrings);
        // Set The Adapter
        lstviewTracksGUI.setAdapter(arrayAdapter);
        lstviewTracksGUI.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {

                songTitle.setVisibility(View.VISIBLE);
                songArtist.setVisibility(View.VISIBLE);

                songArtist.setText(mTrackList.get(position).songArtistName);
                songTitle.setText(mTrackList.get(position).songName);

                try {
                    String img_url = mTrackList.get(position).imageURL;
                    URL url = new URL(img_url);
                    Bitmap bmp;
                    bmp= BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    albumCover.setImageBitmap(bmp);
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

                //todo add a sound icon to the song tht is playing

                mSkipBackButton.setClickable(true);
                mPauseButton.setClickable(true);
                mSkipForwardButton.setClickable(true);
                mRepeatButton.setClickable(true);
                mShuffleButton.setClickable(true);

            }
        });

        //todo create separate methods for these, should not be in onCreate
//
//        mPlayButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mPlayButton.setClickable(false);
//                mPlayButton.setVisibility(View.GONE);
//                mPauseButton.setClickable(true);
//                mPauseButton.setVisibility(View.VISIBLE);
//                mPlayer.resume(null);
//            }
//        });
//
//        mPauseButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mPlayButton.setClickable(true);
//                mPlayButton.setVisibility(View.VISIBLE);
//                mPauseButton.setClickable(false);
//                mPauseButton.setVisibility(View.GONE);
//                mPlayer.pause(null);
//            }
//        });
//
//        mSkipBackButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//mPlayer.skipToPrevious(null);            }
//        });
//
//        mSkipForwardButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mPlayer.skipToNext(null);
//            }
//        });
//
//        mShuffleButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mCurrentPlayerState = mPlayer.getPlaybackState();
//                if(!mCurrentPlayerState.isShuffling){
//                    mPlayer.setShuffle(null, true);
//                    mShuffleButton.setBackgroundColor(Color.RED);
//                }
//                else{
//                    mPlayer.setShuffle(null, false);
//                    mShuffleButton.setBackgroundColor(Color.WHITE);
//                }
//            }
//        });
//
//        mRepeatButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mCurrentPlayerState = mPlayer.getPlaybackState();
//                if(!mCurrentPlayerState.isRepeating){
//                    mPlayer.setRepeat(null, true);
//                    mRepeatButton.setBackgroundColor(Color.RED);
//                }
//                else{
//                    mPlayer.setRepeat(null, false);
//                    mRepeatButton.setBackgroundColor(Color.WHITE);
//                }
//            }
//        });
    }


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
                    mSongListOfStrings.add(trackToAdd.songArtistName + " - " + trackToAdd.songName);

                    mHostPlayerController.updateCurrentSpotifyPlaylist(mCurrentParty);
                    mPlayer.queue(null,trackToAdd.songID);
                    Toast.makeText(this, "Song Added!", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, "Already in playlist!", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }


    @Override
    public void onPlaybackError(Error error) {
        Log.d("HostPlayerActivity", "Playback failed");
    }

    @Override
    public void onPlaybackEvent(PlayerEvent event) {
        Log.d("HostPlayerActivity", "playback event logging right here");

        mCurrentPlayerState = mPlayer.getPlaybackState();
        String time = String.valueOf(mCurrentPlayerState.positionMs);
//        mSongTime.setText(time);
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
