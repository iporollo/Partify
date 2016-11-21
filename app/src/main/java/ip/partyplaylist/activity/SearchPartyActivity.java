package ip.partyplaylist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;

import ip.partyplaylist.R;
import ip.partyplaylist.adapter.PartiesAdapter;
import ip.partyplaylist.controllers.SearchPartyController;
import ip.partyplaylist.model.PartifyTrack;
import ip.partyplaylist.model.Party;
import ip.partyplaylist.screen_actions.SearchPartyScreenActions;

public class SearchPartyActivity extends AppCompatActivity implements
        SearchPartyScreenActions
        {

    private static final String TAG = SearchPartyActivity.class.getSimpleName();
    public static final String CURRENT_PARTY = "party";

    private SearchPartyController mSearchPartyController;
    private ListView mPartiesListView;
    private ProgressBar mLoadingPartiesProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_party);

        mSearchPartyController = new SearchPartyController(this);

        mPartiesListView = (ListView) findViewById(R.id.parties_list);
        mLoadingPartiesProgress = (ProgressBar) findViewById(R.id.loading_parties_list);

        mPartiesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Party selectedParty = (Party) mPartiesListView.getItemAtPosition(position);

                if(selectedParty.trackList == null) {
                    selectedParty.trackList = new ArrayList<PartifyTrack>();
                }

                Intent startPartyDetailsScreen = new Intent(SearchPartyActivity.this,
                        PartyDetailsActivity.class);

                startPartyDetailsScreen.putExtra(CURRENT_PARTY, selectedParty);

                startActivity(startPartyDetailsScreen);
            }
        });

    }

    protected void onStart() {
        super.onStart();
    }

    protected void onStop() {
        super.onStop();
    }

    @Override
    public void refreshPartyListInScreen(ArrayList<Party> parties) {
        PartiesAdapter mPartiesAdapter = new PartiesAdapter(this, parties);
        mPartiesListView.setAdapter(mPartiesAdapter);
        mLoadingPartiesProgress.setVisibility(View.GONE);
    }
}
