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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import ip.partyplaylist.R;
import ip.partyplaylist.controllers.CreatePartyController;
import ip.partyplaylist.controllers.LoginActivityController;
import ip.partyplaylist.util.SharedPreferenceHelper;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistSimple;


public class PlaylistPlayerActivity extends AppCompatActivity implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback {

    private Button mAddButton;
    private TextView songTitle;
    private TextView songArtist;
    private ImageView albumCover;


    private CreatePartyController mCreatePartyController;
    private SharedPreferenceHelper mSharedPreferenceHelper;
    private SpotifyService mSpotifyService;
    private Pager<PlaylistSimple> userPlaylists;

    //private ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
    private long songDuration = 1;

    private Player mPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_view_main);

        //mCreatePartyController = new CreatePartyController(this);

        mAddButton = (Button) findViewById(R.id.btnAddSong);
        songTitle = (TextView) findViewById(R.id.txtSongTitle);
        songArtist = (TextView) findViewById(R.id.txtSongArtist);
        albumCover = (ImageView) findViewById(R.id.imgSongCover);

        songTitle.setVisibility(View.GONE);
        songArtist.setVisibility(View.GONE);

//        mPlayer = spotifyPlayer;
//        mPlayer.addConnectionStateCallback(MainActivity.this);
//        mPlayer.addNotificationCallback(MainActivity.this);


        //todo needs to show add song screen
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mCreatePartyController.onAddTrackButtonPressed();
            }
        });

        mSharedPreferenceHelper = new SharedPreferenceHelper(this);

        final SpotifyApi mSpotifyApi = new SpotifyApi();
        mSpotifyApi.setAccessToken(mSharedPreferenceHelper.getCurrentSpotifyToken());
        mSpotifyService = mSpotifyApi.getService();

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

            }
        });

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


    private Playlist populateList() {
        // retrives all user playlists
        userPlaylists = mSpotifyService.getMyPlaylists();

        String playlistDesired = mSharedPreferenceHelper.getCurrentPlaylistId();

        Playlist p = mSpotifyService.getPlaylist(mSharedPreferenceHelper.getCurrentUserId(), playlistDesired);
        return p;
    }

//    private void initializeProgressBar() {
//        mProgressBar.setMax(100);
//        mProgressBar.setProgress(0);
//    }
//
//    private void setProgressBar(int val) { //miliseconds
//        mProgressBar.setProgress((val / (int) songDuration));
//    }
}
