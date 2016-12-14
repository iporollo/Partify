package ip.partyplaylist.controllers;

import ip.partyplaylist.model.Party;
import ip.partyplaylist.util.SharedPreferenceHelper;
import kaaes.spotify.webapi.android.SpotifyService;

/**
 * Created by Ivan on 12/13/2016.
 */

public class HostPlayerController {

    private final SharedPreferenceHelper mSharedPreferenceHelper;
    private final SpotifyService mSpotifyService;
    private Party mCurrentParty;
}
