package ip.partyplaylist.fragments;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;

import ip.partyplaylist.R;
import ip.partyplaylist.activity.CreatePartyActivity;
import ip.partyplaylist.activity.LoginActivity;
import ip.partyplaylist.activity.SearchPartyActivity;

/**
 * Created by Ivan on 3/11/2017.
 */

public class PartyDashboardTabFragment extends Fragment{

    private Button mCreateNewParty, mSearchParty;
    private GridView mPartiesNearYou;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_party_dashboard, container, false);

        mCreateNewParty = (Button) v.findViewById(R.id.btnCreateNewParty);
        mSearchParty = (Button) v.findViewById(R.id.btnSearchPartyWithCode);

        mCreateNewParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreatePartyScreen();
            }
        });

        mSearchParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchPartyScreen();
            }
        });

        return v;
    }

    public static PartyDashboardTabFragment newInstance() {

        PartyDashboardTabFragment f = new PartyDashboardTabFragment();

        return f;
    }


    public void showCreatePartyScreen() {
        Intent createParty = new Intent(getActivity(), CreatePartyActivity.class);
        startActivity(createParty);
    }

    public void showSearchPartyScreen() {
        Intent loadJoinPartyScreen = new Intent(getActivity(), SearchPartyActivity.class);
        startActivity(loadJoinPartyScreen);
    }
}
