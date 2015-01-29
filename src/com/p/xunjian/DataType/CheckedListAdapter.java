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

public class CheckedListAdapter extends RecyclerView.Adapter<CheckedListAdapter.ViewHolder> {
    private ArrayList<IBeacon> mIBeaconDataset;
    private PublicData publicData;

    public int getShowType() {
        return showType;
    }

    public void setShowType(int showType) {
        this.showType = showType;
    }

    private int showType;
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
    public CheckedListAdapter(Context context) {
        mIBeaconDataset = new ArrayList<IBeacon>();
        publicData = PublicData.getInstance();
        this.context = context;
    }
    // Create new views (invoked by the layout manager)
    @Override
    public CheckedListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_new, parent, false);

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
        ImageView rssi_img = (ImageView)holder.myview.findViewById(R.id.rssi_image);
        if(rssi_img != null)
            rssi_img.setImageResource(getRSSIView(mIBeaconDataset.get(position)));
        TextView tv_minor = (TextView)holder.myview.findViewById(R.id.tv_minor_val);
        if(tv_minor != null)
            tv_minor.setText(String.valueOf(mIBeaconDataset.get(position).getMinor()));
        TextView tv_id = (TextView)holder.myview.findViewById(R.id.tv_id);
        if(tv_id != null){
            tv_id.setText(PublicData.getInstance().getBeaconAddress(mIBeaconDataset.get(position)));
        }

        ImageView upload_img = (ImageView)holder.myview.findViewById(R.id.upload_img);

            if(publicData.uploadBeaconSet.contains(mIBeaconDataset.get(position).getBluetoothAddress())){
                upload_img.setImageResource(R.drawable.upload);
            }


    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mIBeaconDataset.size();
    }
    private int getRSSIView(IBeacon beacon) {
        if (beacon.getRssi() <=-110) {
            return R.drawable.icon_rssi1;
        } else if (beacon.getRssi() <= -100) {
            return R.drawable.icon_rssi2;
        } else if (beacon.getRssi() <= -90) {
            return R.drawable.icon_rssi3;
        } else if (beacon.getRssi() <= -80) {
            return R.drawable.icon_rssi4;
        } else if (beacon.getRssi() <= -70) {
            return R.drawable.icon_rssi5;
        } else if (beacon.getRssi() > -70) {
            return R.drawable.icon_rssi6;
        }
        return R.drawable.icon_rssi1;
    }

    public void updateIBeaconData(ArrayList<IBeacon> data){
        ArrayList<IBeacon> newData = new ArrayList<IBeacon>();
        newData.addAll(data);
        mIBeaconDataset = newData;
    }
}