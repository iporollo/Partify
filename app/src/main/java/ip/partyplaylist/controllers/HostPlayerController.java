package ip.partyplaylist.controllers;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;

import ip.partyplaylist.model.Party;
import ip.partyplaylist.model.Song;
import ip.partyplaylist.screen_actions.HostPlayerScreenActions;
import ip.partyplaylist.util.SharedPreferenceHelper;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Ivan on 12/13/2016.
 */

public class HostPlayerController {

    private final HostPlayerScreenActions mHostPlayerScreenActions;
    private final SharedPreferenceHelper mSharedPreferenceHelper;
    private final SpotifyService mSpotifyService;
    private Party mCurrentParty;

    public HostPlayerController(HostPlayerScreenActions hostPlayerScreenActions,
                                  Party currentParty) {
        mHostPlayerScreenActions = hostPlayerScreenActions;

        mCurrentParty = currentParty;

        mSharedPreferenceHelper = new SharedPreferenceHelper((Context) mHostPlayerScreenActions);

        SpotifyApi mSpotifyApi = new SpotifyApi();
        mSpotifyApi.setAccessToken(mSharedPreferenceHelper.getCurrentSpotifyToken());
        mSpotifyService = mSpotifyApi.getService();
    }

    public void onAddTrackButtonPressed() {
        mHostPlayerScreenActions.showSearchTrackScreen();
    }


    public void onUserClicksAddTrack() {
        mHostPlayerScreenActions.showSearchTrackScreen();
    }

    //IDK if I need this if the activity result method in the activity class does this for me
    public void onUserAddedTrack(Song trackToAdd) {
        mCurrentParty.addTrack(trackToAdd);

        //updateParty();
    }


    public void onHostClicksTrack(final Song trackToAdd) {
        HashMap parametersMap = new HashMap();
        parametersMap.put("uris", trackToAdd.songID);

        mSpotifyService.addTracksToPlaylist(
                mCurrentParty.hostId,
                mCurrentParty.playlistId,
                parametersMap,
                new HashMap<String, Object>(),
                new Callback<Pager<PlaylistTrack>>() {
                    @Override
                    public void success(Pager<PlaylistTrack> playlistTrackPager, Response response) {
                        removeTrackFromParty(trackToAdd);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("HostPlayerActivity", "Adding Tracks to Playlist Failed.");
                    }
                });
    }


    //IDK WHAT THIS IS FOR
    private void removeTrackFromParty(Song trackToAdd) {
        mCurrentParty.trackList.remove(trackToAdd);

        //updateParty();
    }
}


