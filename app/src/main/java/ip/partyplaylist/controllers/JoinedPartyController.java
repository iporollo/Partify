package ip.partyplaylist.controllers;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import ip.partyplaylist.model.Party;
import ip.partyplaylist.model.Song;
import ip.partyplaylist.screen_actions.JoinedPartyScreenActions;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Ivan on 1/28/2017.
 */

public class JoinedPartyController {

    private Party mCurrentParty;
    private JoinedPartyScreenActions mJoinedPartyScreenActions;

    public JoinedPartyController(JoinedPartyScreenActions joinedPartyScreenActions){
        mJoinedPartyScreenActions = joinedPartyScreenActions;
    }

    public void onAddTrackButtonPressed() {
        mJoinedPartyScreenActions.showSearchTrackScreen();
    }


    public void updateCurrentFirebaseParty(Party currentParty) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("parties");

        myRef.child(currentParty.partyId).setValue(currentParty, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
            }
        });
    }
}
