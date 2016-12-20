package ip.partyplaylist.activity;

import android.content.Intent;
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
import java.util.HashMap;

import ip.partyplaylist.R;
import ip.partyplaylist.adapter.PartifyTracksAdapter;
import ip.partyplaylist.controllers.HostPlayerController;
import ip.partyplaylist.model.Party;
import ip.partyplaylist.model.Song;
import ip.partyplaylist.screen_actions.HostPlayerScreenActions;
import ip.partyplaylist.util.SharedPreferenceHelper;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class HostPlayerActivity extends AppCompatActivity implements SpotifyPlayer.NotificationCallback, HostPlayerScreenActions{//, CreatePartyScreenActions {

    private static final String CLIENT_ID = "ea09225ef2974242a1549f3812a15496";

    private Party mCurrentParty;
    private ArrayList<Song> mTrackList;
    private ArrayList<String> mSongListOfStrings;
    private Player mPlayer;
    private HostPlayerController mHostPlayerController;
    private SharedPreferenceHelper mSharedPreferenceHelper;
    private SpotifyService mSpotifyService;

    private Button mAddButton;
    private TextView songTitle;
    private TextView songArtist;
    private ImageView albumCover;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_player);
        mSharedPreferenceHelper = new SharedPreferenceHelper(this);

        mAddButton = (Button) findViewById(R.id.btnAddSong);
        songTitle = (TextView) findViewById(R.id.txtSongTitle);
        songArtist = (TextView) findViewById(R.id.txtSongArtist);
        albumCover = (ImageView) findViewById(R.id.imgSongCover);

        songTitle.setVisibility(View.GONE);
        songArtist.setVisibility(View.GONE);

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHostPlayerController.onAddTrackButtonPressed();
            }
        });

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
                //mPlayer.addConnectionStateCallback(HostPlayerActivity.this);
                mPlayer.addNotificationCallback(HostPlayerActivity.this);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("HostPlayerActivity", "Could not initialize player: " + throwable.getMessage());
            }
        });



        // retrives playlist to be displayed
        Playlist p = getCurrentPlaylist();

        // generates arraylist to populate GUI list
        mSongListOfStrings = new ArrayList<>();
        mTrackList = new ArrayList<>();

        for (int i = 0; i < p.tracks.total; i++) {

            Song currentSong = new Song(p.tracks.items.get(i).track.name,
                    p.tracks.items.get(i).track.artists.get(0).name,
                    p.tracks.items.get(i).track.uri,
                    p.tracks.items.get(i).track.album.images.get(0).url);

            mTrackList.add(currentSong);

            mSongListOfStrings.add(currentSong.songArtistName + " - " + currentSong.songName);
        }

        //todo possibly needs to be in controller
        mCurrentParty = new Party(mSharedPreferenceHelper.getCurrentPlaylistName(), mTrackList);
        mCurrentParty.setPlaylistId(mSharedPreferenceHelper.getCurrentPlaylistId());
        mCurrentParty.setHostId(mSharedPreferenceHelper.getCurrentUserId());
        mHostPlayerController = new HostPlayerController(this, mCurrentParty);

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

            //todo place into controller
                songTitle.setVisibility(View.VISIBLE);
                songArtist.setVisibility(View.VISIBLE);

                songArtist.setText(mTrackList.get(position).songArtistName);
                songTitle.setText(mTrackList.get(position).songName);

                //songArtist.setText(selectedFromList.substring(0, p.tracks.items.get(position).track.artists.get(0).name.length()));
                //songTitle.setText(selectedFromList.substring(p.tracks.items.get(position).track.artists.get(0).name.length()+3, selectedFromList.length()));

                try {
                    String img_url = mTrackList.get(position).imageURL;
                    //String img_url= p.tracks.items.get(position).track.album.images.get(0).url;
                    URL url = new URL(img_url);
                    Bitmap bmp;
                    bmp= BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    albumCover.setImageBitmap(bmp);
                } catch (IOException e) {
                    System.err.print("Error getting album image");
                }

                mPlayer.playUri(null, mTrackList.get(position).songID, 0, 0);
                //mPlayer.playUri(null, p.tracks.items.get(position).track.uri, 0, 0);

            }
        });

    }



    //todo place in controller
    private Playlist getCurrentPlaylist() {
        // retrives all user playlists
        String playlistDesired = mSharedPreferenceHelper.getCurrentPlaylistId();

        Playlist p = mSpotifyService.getPlaylist(mSharedPreferenceHelper.getCurrentUserId(), playlistDesired);
        return p;
    }

    @Override
    public void updateCurrentFirebaseParty(Party mCurrentParty) {
        //todo firebase update of party
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

                mTrackList.add(trackToAdd);
                mCurrentParty.addTrack(trackToAdd);
                mSongListOfStrings.add(trackToAdd.songArtistName + " - " + trackToAdd.songName);

                updateCurrentSpotifyPlaylist();
            }
        }
    }

    //this may have a problem due to re-adding of songs into playlist...
    //does spotify throw an error or automatically detect the re-addition of a song
    //may need to pull playlist, loop through it, add songs that are not already in it
    public void updateCurrentSpotifyPlaylist() {

        StringBuffer tracksParams = new StringBuffer();

        for (Song track : mCurrentParty.trackList) {
            tracksParams.append(track.songID).append(",");
        }

        HashMap parametersMap = new HashMap();
        parametersMap.put("uris", tracksParams.toString());


        mSpotifyService.addTracksToPlaylist(
                mCurrentParty.hostId,
                mCurrentParty.playlistId,
                parametersMap,
                new HashMap<String, Object>(),
                new Callback<Pager<PlaylistTrack>>() {
                    @Override
                    public void success(Pager<PlaylistTrack> playlistTrackPager, Response response) {
                        updateCurrentFirebaseParty(mCurrentParty);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("HostPlayerActivity", "Adding Tracks to Playlist Failed.");
                    }
                });

    }


    @Override
    public void onPlaybackError(Error error) {
        Log.d("HostPlayerActivity", "Playback failed");
    }

    @Override
    public void onPlaybackEvent(PlayerEvent event) {
        Log.d("HostPlayerActivity", "playback event logging right here");
    }

    @Override
    protected void onDestroy() {
        // VERY IMPORTANT! This must always be called or else you will leak resources
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }


//    @Override
//    public void onLoggedIn() {
//        Log.d("LoginActivity", "User logged in");
//    }
//
//    @Override
//    public void onLoginFailed(int error) {
//        Log.d("LoginActivity", "Login failed");
//    }
//
//    @Override
//    public void onTemporaryError() {
//        Log.d("LoginActivity", "Temporary error occurred");
//    }
//

//
//    @Override
//    public void onConnectionMessage(String message) {
//        Log.d("LoginActivity", "Login failed");
//    }
//
//    @Override
//    public void onLoggedOut() {
//        Log.d("LoginActivity", "User logged out");
//    }


}
