package ip.partyplaylist.controllers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import ip.partyplaylist.activity.CreatePartyActivity;
import ip.partyplaylist.activity.HostPlayerActivity;
import ip.partyplaylist.activity.SearchPartyActivity;
import ip.partyplaylist.model.Party;
import ip.partyplaylist.screen_actions.SearchPartyScreenActions;
import ip.partyplaylist.util.SharedPreferenceHelper;


public class SearchPartyController {

    private final Context mContext;
    private final SharedPreferenceHelper mSharedPreferenceHelper;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("parties");
    private boolean isPartyIdValid;

    public SearchPartyController(Context context) {
        mContext = context;
        mSharedPreferenceHelper = new SharedPreferenceHelper(context);
    }

    public boolean onEnterButtonPressed(String enteredText){
        isPartyIdValid = false;
        if(enteredText.length() == 5){

            myRef.child(enteredText).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        isPartyIdValid = true;
                    }
                    else {
                        Toast.makeText(mContext, "Party ID not found", Toast.LENGTH_LONG).show();
                        isPartyIdValid = false;
                    }
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) { }
            });

        }
        else{
            Toast.makeText(mContext, "Party ID not Valid", Toast.LENGTH_LONG).show();
            isPartyIdValid = false;
        }

        return isPartyIdValid;
    }

//    public void onRetrievePartyList(SearchPartyActivity searchPartyActivity) {
//        final Party currentLocation = new Party();
//
//        final ArrayList<Party> parties = new ArrayList<>();
//
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference myRef = database.getReference("parties");
//
//        myRef.addListenerForSingleValueEvent(
//                new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        for (DataSnapshot partySnapshot : dataSnapshot.getChildren()) {
//                            Party party = partySnapshot.getValue(Party.class);
//                            if(party.hostId.equals(mSharedPreferenceHelper.getCurrentUserId())) {
//                                parties.clear();
//                                parties.add(party);
//                                break;
//                            }
//                        }
//
//                        ((SearchPartyScreenActions) mContext).refreshPartyListInScreen(parties);
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//                        Log.w("Search", "getUser:onCancelled", databaseError.toException());
//                    }
//                });
//    }
}
