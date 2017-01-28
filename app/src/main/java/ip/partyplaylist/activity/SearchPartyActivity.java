package ip.partyplaylist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

import ip.partyplaylist.R;
import ip.partyplaylist.adapter.PartiesAdapter;
import ip.partyplaylist.controllers.SearchPartyController;
import ip.partyplaylist.model.Song;
import ip.partyplaylist.model.Party;
import ip.partyplaylist.screen_actions.SearchPartyScreenActions;

public class SearchPartyActivity extends AppCompatActivity implements SearchPartyScreenActions{

    private SearchPartyController mSearchPartyController;
    private EditText mPartyID;
    private Button mEnterParty;

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
                showPartyJoinedScreen(mSearchPartyController.onEnterButtonPressed(mPartyID.getText().toString()));
            }
        });

    }

    @Override
    public void showPartyJoinedScreen(boolean isValidPartyID) {
        if(isValidPartyID){
            Toast.makeText(this, "Party Joined!", Toast.LENGTH_SHORT).show();
// todo redirect
//            Intent i = new Intent(SearchPartyActivity.this, somename.class);
//            startActivity(i);
        }
    }

}
