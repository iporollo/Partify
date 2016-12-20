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
    private Party mCurrentParty;

    public HostPlayerController(HostPlayerScreenActions hostPlayerScreenActions,
                                  Party currentParty) {
        mHostPlayerScreenActions = hostPlayerScreenActions;

        mCurrentParty = currentParty;

    }

    public void onAddTrackButtonPressed() {
        mHostPlayerScreenActions.showSearchTrackScreen();
    }

}


