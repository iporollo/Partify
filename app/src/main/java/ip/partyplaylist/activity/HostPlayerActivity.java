package ip.partyplaylist.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import ip.partyplaylist.R;
import ip.partyplaylist.adapter.PartifyTracksAdapter;
import ip.partyplaylist.controllers.HostPlayerController;
import ip.partyplaylist.controllers.PartyDetailsController;
import ip.partyplaylist.model.Song;
import ip.partyplaylist.util.SharedPreferenceHelper;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistSimple;


public class HostPlayerActivity extends AppCompatActivity implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback{//, CreatePartyScreenActions {

    private HostPlayerController hostPlayerController;


    private Button mAddButton;
    private TextView songTitle;
    private TextView songArtist;
    private ImageView albumCover;

    private static final String CLIENT_ID = "ea09225ef2974242a1549f3812a15496";
    public static final int SEARCH_SONG_REQUEST_CODE = 1;

    private PartyDetailsController mPartyDetailsController;
    private SharedPreferenceHelper mSharedPreferenceHelper;
    private SpotifyService mSpotifyService;
    private Pager<PlaylistSimple> userPlaylists;
    private ArrayList<Song> mPartyTrackList;
    private ListView mPartyTrackListView;
    private PartifyTracksAdapter mTracksAdapter;
    private boolean mIsCurrentPartyOwnedByCurrentUser;

    //private ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
    private long songDuration = 1;

    private Player mPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_view_main);
        mSharedPreferenceHelper = new SharedPreferenceHelper(this);

        //mCreatePartyController = new CreatePartyController(this);

        mAddButton = (Button) findViewById(R.id.btnAddSong);
        songTitle = (TextView) findViewById(R.id.txtSongTitle);
        songArtist = (TextView) findViewById(R.id.txtSongArtist);
        albumCover = (ImageView) findViewById(R.id.imgSongCover);

        songTitle.setVisibility(View.GONE);
        songArtist.setVisibility(View.GONE);
/////////////////////////////////////////////////////////////////////////////////////ADD BUTTON

        //todo create controller
        //todo figure out how to update playlist
//        Bundle intentExtras = getIntent().getExtras();
//        Party mCurrentParty = (Party) intentExtras.get(SearchPartyActivity.CURRENT_PARTY);
//        mPartyDetailsController = new PartyDetailsController(this, mCurrentParty);
//        mAddButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mPartyDetailsController.onUserClicksAddTrack();
//            }
//        });
/////////////////////////////////////////////////////////////////////////////////////ADD BUTTON

//////////////////////////////////////////////////////////////////////////////////////PLAYER
        final SpotifyApi mSpotifyApi = new SpotifyApi();
        mSpotifyApi.setAccessToken(mSharedPreferenceHelper.getCurrentSpotifyToken());
        mSpotifyService = mSpotifyApi.getService();

        //creates spotify music player
        Config playerConfig = new Config(HostPlayerActivity.this, mSharedPreferenceHelper.getCurrentSpotifyToken(), CLIENT_ID);
        Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver(){
            @Override
            public void onInitialized(SpotifyPlayer spotifyPlayer) {
                mPlayer = spotifyPlayer;
                mPlayer.addConnectionStateCallback(HostPlayerActivity.this);
                mPlayer.addNotificationCallback(HostPlayerActivity.this);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
            }
        });



        // retrives playlist to be displayed
        final Playlist p = populateList();

        // generates arraylist to populate GUI list
        ArrayList<String> songList = new ArrayList<>();
        for (int i = 0; i < p.tracks.total; i++) {

            songList.add(p.tracks.items.get(i).track.artists.get(0).name + " - " + p.tracks.items.get(i).track.name);
        }

        // Get the reference of ListViewAnimals
        final ListView lstviewTracksGUI=(ListView)findViewById(R.id.lstviewTracks);

        // Create The Adapter with passing ArrayList as 3rd parameter
        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, songList);
        // Set The Adapter
        lstviewTracksGUI.setAdapter(arrayAdapter);
        lstviewTracksGUI.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {

                //todo needs to be in controller

                songTitle.setVisibility(View.VISIBLE);
                songArtist.setVisibility(View.VISIBLE);

                String selectedFromList =(String) (lstviewTracksGUI.getItemAtPosition(position));
                songArtist.setText(selectedFromList.substring(0, p.tracks.items.get(position).track.artists.get(0).name.length()));
                songTitle.setText(selectedFromList.substring(p.tracks.items.get(position).track.artists.get(0).name.length()+3, selectedFromList.length()));

                //initializeProgressBar();
                songDuration = p.tracks.items.get(position).track.duration_ms;

                //miliseconds timer, call setProgressBar()

                try {
                    String img_url= p.tracks.items.get(position).track.album.images.get(0).url;
                    URL url = new URL(img_url);
                    Bitmap bmp;
                    bmp= BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    albumCover.setImageBitmap(bmp);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mPlayer.playUri(null, p.tracks.items.get(position).track.uri, 0, 0);

            }
        });

    }

    //////////////////////////////////////////////////////////////////////////////////////PLAYER

    @Override
    protected void onDestroy() {
        // VERY IMPORTANT! This must always be called or else you will leak resources
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }


    @Override
    public void onLoggedIn() {
        Log.d("LoginActivity", "User logged in");
    }

    @Override
    public void onLoginFailed(int error) {
        Log.d("LoginActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("LoginActivity", "Temporary error occurred");
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("LoginActivity", "Login failed");
    }

    @Override
    public void onPlaybackEvent(PlayerEvent event) {
        Log.d("LoginActivity", "Login failed");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("LoginActivity", "Login failed");
    }

    @Override
    public void onLoggedOut() {
        Log.d("LoginActivity", "User logged out");
    }

    //////////////////////////////////////////////////////////////////////////////////////PLAYER
    private Playlist populateList() {
        // retrives all user playlists
        userPlaylists = mSpotifyService.getMyPlaylists();

        String playlistDesired = mSharedPreferenceHelper.getCurrentPlaylistId();

        Playlist p = mSpotifyService.getPlaylist(mSharedPreferenceHelper.getCurrentUserId(), playlistDesired);
        return p;
    }
//////////////////////////////////////////////////////////////////////////////////////PLAYER


//    @Override
//    public void showSearchTrackScreen() {
//        Intent startSearchSongActivity = new Intent(this, SearchTrackActivity.class);
//        startActivityForResult(startSearchSongActivity, SEARCH_SONG_REQUEST_CODE);
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == SEARCH_SONG_REQUEST_CODE) {
//            if (resultCode == RESULT_OK) {
//
//                Song trackToAdd = (Song) data.getExtras().get(SearchTrackActivity.TRACK);
//                mCurrentTrackList.add(trackToAdd);
//                mTracksAdapter = new PartifyTracksAdapter(mCurrentTrackList, CreatePartyActivity.this);
//                mPartyTrackList.setAdapter(mTracksAdapter);
//                mPartyTrackList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                        mCurrentTrackList.remove(position);
//                        mTracksAdapter = new PartifyTracksAdapter(mCurrentTrackList, CreatePartyActivity.this);
//                        mPartyTrackList.setAdapter(mTracksAdapter);
//                    }
//
//                });
//
//            }
//        }
//    }


//    private void initializeProgressBar() {
//        mProgressBar.setMax(100);
//        mProgressBar.setProgress(0);
//    }
//
//    private void setProgressBar(int val) { //miliseconds
//        mProgressBar.setProgress((val / (int) songDuration));
//    }
}
