package ip.partyplaylist.controllers;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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


    public CreatePartyController(CreatePartyScreenActions createPartyScreenActions) {
        mCreatePartyScreenActions = createPartyScreenActions;

    }

    public void onCreateParty(String partyName, boolean guestSongAddState, boolean downVoteState){
        if (partyName.trim().length() == 0) {
            mCreatePartyScreenActions.showError("Party Name Invalid");
        } else {
            Party party = new Party(partyName, guestSongAddState, downVoteState);
            mCreatePartyScreenActions.showPartyCreatedScreen(party);
        }
    }



}
