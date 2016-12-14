package ip.partyplaylist.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ip.partyplaylist.R;
import ip.partyplaylist.model.Song;


public class PartifyTracksAdapter extends BaseAdapter {

    private final Context mContext;
    private boolean mIsCurrentPartyOwnedByCurrentUser;
    private List<Song> mTracks;

    public PartifyTracksAdapter(List<Song> tracks, Context context,
                                boolean isCurrentPartyOwnedByCurrentUser) {
        mContext = context;
        mTracks = tracks;
        mIsCurrentPartyOwnedByCurrentUser = isCurrentPartyOwnedByCurrentUser;
    }

    public PartifyTracksAdapter(List<Song> tracks, Context context) {
        mContext = context;
        mTracks = tracks;
    }

    @Override
    public int getCount() {
        return mTracks.size();
    }

    @Override
    public Song getItem(int position) {
        return mTracks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;

        if(convertView==null){
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(R.layout.track_list_item_layout, parent, false);

            viewHolder = new ViewHolderItem();
            viewHolder.trackNameTV = (TextView) convertView.findViewById(R.id.track_name);
            viewHolder.trackArtistTV = (TextView) convertView.findViewById(R.id.track_artist);
            viewHolder.addTrackIcon = (ImageView) convertView.findViewById(R.id.add_track_icon);

            convertView.setTag(viewHolder);

        }else{

            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        Song track = mTracks.get(position);

        if(track != null) {
            viewHolder.trackNameTV.setText(track.songName);
            viewHolder.trackArtistTV.setText("(" + track.songArtistName + ")");
        }

        viewHolder.addTrackIcon.setVisibility(mIsCurrentPartyOwnedByCurrentUser ?
                View.VISIBLE :
                View.GONE);

        return convertView;
    }

    static class ViewHolderItem {
        TextView trackNameTV;
        TextView trackArtistTV;
        ImageView addTrackIcon;
    }
}
