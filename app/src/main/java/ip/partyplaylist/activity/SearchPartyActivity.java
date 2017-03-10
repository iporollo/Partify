package ip.partyplaylist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ip.partyplaylist.R;
import ip.partyplaylist.controllers.SearchPartyController;

public class SearchPartyActivity extends AppCompatActivity{

    private SearchPartyController mSearchPartyController;
    private EditText mPartyID;
    private Button mEnterParty;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("parties");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_party);

        mSearchPartyController = new SearchPartyController(this);

        mPartyID = (EditText) findViewById(R.id.party_id_edit_text);
        mEnterParty = (Button) findViewById(R.id.btnEnterParty);


        mEnterParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPartyID.getText().toString().length() == 5){

                    myRef.child(mPartyID.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Toast.makeText(SearchPartyActivity.this, "Party Joined!", Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(SearchPartyActivity.this, JoinedPartyActivity.class);
                                i.putExtra("ENTERED_PARTYID", mPartyID.getText().toString());
                                startActivity(i);
                            }
                            else {
                                Toast.makeText(SearchPartyActivity.this, "Party ID not found", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError firebaseError) { }
                    });

                }
                else{
                    Toast.makeText(SearchPartyActivity.this, "Party ID not Valid", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

}
