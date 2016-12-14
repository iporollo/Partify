package ip.partyplaylist.controllers;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import ip.partyplaylist.model.Song;
import ip.partyplaylist.model.Party;
import ip.partyplaylist.screen_actions.CreatePartyScreenActions;
import ip.partyplaylist.util.SharedPreferenceHelper;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;



public class CreatePartyController {
    private static final String TAG = CreatePartyController.class.getSimpleName();
    private final CreatePartyScreenActions mCreatePartyScreenActions;
    private final SharedPreferenceHelper mSharedPreferenceHelper;
    private final SpotifyService mSpotifyService;
    private Party mCurrentParty;

    public CreatePartyController(CreatePartyScreenActions createPartyScreenActions) {
        mCreatePartyScreenActions = createPartyScreenActions;
        mSharedPreferenceHelper = new SharedPreferenceHelper((Context) createPartyScreenActions);

        SpotifyApi mSpotifyApi = new SpotifyApi();
        mSpotifyApi.setAccessToken(mSharedPreferenceHelper.getCurrentSpotifyToken());
        mSpotifyService = mSpotifyApi.getService();

    }

    public void onSaveParty(String partyName, ArrayList<Song> trackList) {
        if (partyName.trim().length() == 0) {
            mCreatePartyScreenActions.showError("Party Name Invalid");
        } else {
            mCurrentParty = new Party(partyName, trackList);

            createSpotifyPlaylist();
        }
    }

    private void createSpotifyPlaylist() {
        mSpotifyService.getMe(new Callback<UserPrivate>() {
            @Override
            public void success(UserPrivate userPrivate, Response response) {
                Log.d(TAG, "Obtained User Information.");

                mSharedPreferenceHelper.saveCurrentUserId(userPrivate.id);

                mCurrentParty.setHostId(userPrivate.id);

                createPlaylist();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "FAILED to Obtain User Information.");
            }
        });
    }

    private void createPlaylist() {
        HashMap<String, Object> playlistParams = new HashMap<String, Object>();
        playlistParams.put("name", mCurrentParty.name);
        playlistParams.put("public", true);

        mSpotifyService.createPlaylist(mCurrentParty.hostId, playlistParams, new Callback<Playlist>() {
            @Override
            public void success(Playlist playlist, Response response) {
                Log.d(TAG, "Playlist Created successfully: " + playlist.name);

                mSharedPreferenceHelper.saveCurrentPlayListId(playlist.id);

                mCurrentParty.setPlaylistId(playlist.id);

                addTracksToSpotifyPlaylist();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "Playlist Creation Failed.");
            }
        });
    }

    private void addTracksToSpotifyPlaylist() {

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
                        saveNewParty(mCurrentParty);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d(TAG, "Adding Tracks to Playlist Failed.");
                    }
                });
    }

    private void saveNewParty(Party tmpParty) {
        // Tracks have been added to Spotify playlist, so no need to add them to Firebase too
        mCurrentParty.trackList.clear();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("parties");

        myRef.child(tmpParty.name).setValue(tmpParty, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                mCreatePartyScreenActions.showPartyCreatedScreen();
            }
        });
    }

    public void onAddTrackButtonPressed() {
        mCreatePartyScreenActions.showSearchTrackScreen();
    }
}
