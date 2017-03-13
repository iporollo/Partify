package ip.partyplaylist.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ip.partyplaylist.R;

/**
 * Created by Ivan on 3/11/2017.
 */

public class YourPartiesFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_your_parties, container, false);

        return v;
    }

    public static YourPartiesFragment newInstance() {

        YourPartiesFragment f = new YourPartiesFragment();

        return f;
    }


}
