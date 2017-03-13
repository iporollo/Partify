package ip.partyplaylist.adapter;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import ip.partyplaylist.fragments.PartyDashboardTabFragment;
import ip.partyplaylist.fragments.YourPartiesFragment;

/**
 * Created by Ivan on 3/10/2017.
 */

public class TabFragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {
    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[] { "Party Dashboard", "Your Parties" };
    private Context context;

    public TabFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                return PartyDashboardTabFragment.newInstance();
            case 1:
                return YourPartiesFragment.newInstance();
            default:
                return PartyDashboardTabFragment.newInstance();
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}