package com.p.xunjian.DataType;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.lef.scanner.IBeacon;
import com.p.xunjian.Activity.MainActivity;
import com.p.xunjian.R;

import java.util.ArrayList;

public class WaitListAdapter extends RecyclerView.Adapter<WaitListAdapter.ViewHolder> {
    private ArrayList<IBeacon> mIBeaconDataset;
    private PublicData publicData;

    Context context;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View myview;
        public ViewHolder(View v) {
            super(v);
            myview = v;
        }
    }
    // Provide a suitable constructor (depends on the kind of dataset)
    public WaitListAdapter(Context context) {
        mIBeaconDataset = new ArrayList<IBeacon>();
        publicData = PublicData.getInstance();
        this.context = context;
    }
    // Create new views (invoked by the layout manager)
    @Override
    public WaitListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_wait, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        TextView tv_major = (TextView)holder.myview.findViewById(R.id.tv_major_val);
        if(tv_major != null)
            tv_major.setText(String.valueOf(mIBeaconDataset.get(position).getMajor()));
        TextView tv_minor = (TextView)holder.myview.findViewById(R.id.tv_minor_val);
        if(tv_minor != null)
            tv_minor.setText(String.valueOf(mIBeaconDataset.get(position).getMinor()));
        TextView tv_id = (TextView)holder.myview.findViewById(R.id.tv_id);
        if(tv_id != null){
            tv_id.setText(PublicData.getInstance().getBeaconAddress(mIBeaconDataset.get(position)));
        }
    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mIBeaconDataset.size();
    }
    public void updateIBeaconData(ArrayList<IBeacon> data){
        ArrayList<IBeacon> newData = new ArrayList<IBeacon>();
        newData.addAll(data);
        mIBeaconDataset = newData;
    }

}