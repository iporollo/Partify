package ip.partyplaylist.controllers;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ip.partyplaylist.util.SharedPreferenceHelper;


public class SearchPartyController {

    private final Context mContext;

    public SearchPartyController(Context context) {
        mContext = context;
    }

}
