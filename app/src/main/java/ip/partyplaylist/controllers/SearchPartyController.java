package ip.partyplaylist.controllers;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import ip.partyplaylist.activity.SearchPartyActivity;
import ip.partyplaylist.model.Party;
import ip.partyplaylist.screen_actions.SearchPartyScreenActions;
import ip.partyplaylist.util.SharedPreferenceHelper;

/**
 * Created by az on 22/05/16.
 */
public class SearchPartyController {

    private static final String TAG = SearchPartyController.class.getSimpleName();
    private final Context mContext;
    private final SharedPreferenceHelper mSharedPreferenceHelper;

    public SearchPartyController(Context context) {
        mContext = context;
        mSharedPreferenceHelper = new SharedPreferenceHelper(context);
    }

    public void onRetrievePartyList(SearchPartyActivity searchPartyActivity) {
        final Party currentLocation = new Party();

        final ArrayList<Party> parties = new ArrayList<>();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("parties");

        myRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot partySnapshot : dataSnapshot.getChildren()) {
                            Party party = partySnapshot.getValue(Party.class);
                            if(party.hostId.equals(mSharedPreferenceHelper.getCurrentUserId())) {
                                parties.clear();
                                parties.add(party);
                                break;
                            }
                        }

                        ((SearchPartyScreenActions) mContext).refreshPartyListInScreen(parties);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("Search", "getUser:onCancelled", databaseError.toException());
                    }
                });
    }
}
