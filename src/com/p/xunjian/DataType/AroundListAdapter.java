package com.p.xunjian.DataType;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.lef.scanner.IBeacon;
import com.p.xunjian.Activity.MainActivity;
import com.p.xunjian.Activity.MapActivity;
import com.p.xunjian.R;

import java.util.ArrayList;

public class AroundListAdapter extends RecyclerView.Adapter<AroundListAdapter.ViewHolder> {
    private ArrayList<IBeacon> mIBeaconDataset;
    private PublicData publicData;
    private volatile boolean iswork = false;

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

    public boolean isWork() {
        return iswork;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AroundListAdapter(Context context) {
        mIBeaconDataset = new ArrayList<IBeacon>();
        publicData = PublicData.getInstance();
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AroundListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
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
        final IBeacon current_position_beacon = mIBeaconDataset.get(position);
        TextView tv_major = (TextView) holder.myview.findViewById(R.id.tv_major_val);
        if (tv_major != null)
            tv_major.setText(String.valueOf(current_position_beacon.getMajor()));
        ImageView rssi_img = (ImageView) holder.myview.findViewById(R.id.rssi_image);
        if (rssi_img != null)
            rssi_img.setImageResource(getRSSIView(current_position_beacon));
        TextView tv_minor = (TextView) holder.myview.findViewById(R.id.tv_minor_val);
        if (tv_minor != null)
            tv_minor.setText(String.valueOf(current_position_beacon.getMinor()));
        TextView tv_id = (TextView) holder.myview.findViewById(R.id.tv_id);
        if (tv_id != null) {
            tv_id.setText(PublicData.getInstance().getBeaconAddress(current_position_beacon));
        }
        ImageView local_img = (ImageView) holder.myview.findViewById(R.id.local_img);

        final DBIbeancon ibeancon = PublicData.getInstance().hasLocation(current_position_beacon.getBluetoothAddress());
        if (ibeancon != null) {
            local_img.setVisibility(View.VISIBLE);
            local_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ibeancon != null) {
                        Intent intent = new Intent(context, MapActivity.class);
                        intent.putExtra("mac",ibeancon.getBluetoothAddress());
                        intent.putExtra("building", ibeancon.getBuilding());
                        intent.putExtra("floor", ibeancon.getFloor());
                        intent.putExtra("coordx", ibeancon.getCoordx());
                        intent.putExtra("coordy", ibeancon.getCoordy());
                        context.startActivity(intent);
                    }
                }
            });
        }else{
            local_img.setVisibility(View.GONE);
            local_img.setOnClickListener(null);
        }

        ImageView check_img = (ImageView) holder.myview.findViewById(R.id.check_img);
        ImageView upload_img = (ImageView) holder.myview.findViewById(R.id.upload_img);
        if (getShowType() == MainActivity.WAIT) {
            check_img.setImageDrawable(new ColorDrawable(context.getResources().getColor(android.R.color.transparent)));
            upload_img.setImageDrawable(new ColorDrawable(context.getResources().getColor(android.R.color.transparent)));
        } else {

            if (publicData.uploadBeaconSet.contains(current_position_beacon.getBluetoothAddress())) {
                upload_img.setImageResource(R.drawable.upload);
                check_img.setImageResource(R.drawable.checkgou);
                check_img.setOnClickListener(null);
            } else {
                upload_img.setImageDrawable(new ColorDrawable(context.getResources().getColor(android.R.color.transparent)));
                if (publicData.checkBeaconSet.contains(current_position_beacon.getBluetoothAddress())) {
                    check_img.setImageResource(R.drawable.checkgou);
                    check_img.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ImageView view = (ImageView) v;
                            view.setImageResource(R.drawable.uncheck);

                            publicData.add2WaitList(current_position_beacon.getBluetoothAddress());
                            publicData.checkBeaconSet.remove(current_position_beacon.getBluetoothAddress());
                            publicData.checkedBeaconDataList.remove(current_position_beacon);
                            publicData.checkedBeaconMap.remove(current_position_beacon.getBluetoothAddress());
                            if (publicData.deleteCheckBeacon2Db(current_position_beacon)) {
                                Log.d(AroundListAdapter.class.getName(), "删除ibeacon导数据成功！");
                            }
                            if (getShowType() == MainActivity.CHECKED) {
                                mIBeaconDataset.remove(position);
                                notifyDataSetChanged();
                            }

                        }
                    });
                } else {
                    check_img.setImageResource(R.drawable.uncheck);
                    check_img.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            iswork = true;
                            ImageView view = (ImageView) v;
                            view.setImageResource(R.drawable.checkgou);

                            publicData.removeFromWaitList(current_position_beacon.getBluetoothAddress());
                            publicData.checkBeaconSet.add(current_position_beacon.getBluetoothAddress());
                            DBIbeancon adbibeacon = new DBIbeancon(current_position_beacon);
                            publicData.checkedBeaconDataList.add(adbibeacon);
                            publicData.checkedBeaconMap.put(adbibeacon.getBluetoothAddress(),adbibeacon);
                            if (publicData.saveCheckBeacon2Db(current_position_beacon)) {
                                Log.d(AroundListAdapter.class.getName(), "保存巡检ibeacon数据成功！");
                            } else {
                                Log.d(AroundListAdapter.class.getName(), "保存巡检ibeacon数据失败！");
                            }
                            final IBeacon currentBeacon = current_position_beacon;
                            String[] blocal = {"正常", "新位置"};
                            new AlertDialog.Builder(context)
                                    .setTitle("选择巡检到的Beacon位置")
                                    .setIcon(android.R.drawable.ic_dialog_info)
                                    .setSingleChoiceItems(blocal, 0, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            if (which == 1) {
                                                DBIbeancon ibeancon = PublicData.getInstance().dbBeaconMap.get(current_position_beacon.getBluetoothAddress());
                                                Intent intent = new Intent(context, MapActivity.class);
                                                intent.putExtra("mac",current_position_beacon.getBluetoothAddress());
                                                if (ibeancon != null) {
                                                    intent.putExtra("building", ibeancon.getBuilding());
                                                    intent.putExtra("floor", ibeancon.getFloor());
                                                    intent.putExtra("coordx", ibeancon.getCoordx());
                                                    intent.putExtra("coordy", ibeancon.getCoordy());
                                                }else{
                                                    intent.putExtra("nolocation",true);
                                                }
                                                context.startActivity(intent);
//                                                View v = LayoutInflater.from(context).inflate(R.layout.new_local, null);
//                                                final EditText ncx = (EditText) v.findViewById(R.id.new_coordx);
//                                                final EditText ncy = (EditText) v.findViewById(R.id.new_coordy);
//                                                final EditText nca = (EditText) v.findViewById(R.id.new_address);
//                                                new AlertDialog.Builder(context)
//                                                        .setTitle("输入新位置")
//                                                        .setIcon(android.R.drawable.ic_dialog_info)
//                                                        .setView(v)
//                                                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
//                                                            @Override
//                                                            public void onClick(DialogInterface dialog, int which) {
//                                                                String strncx = ncx.getText().toString();
//                                                                String strncy = ncy.getText().toString();
//                                                                String strnad = nca.getText().toString();
//                                                                PublicData.getInstance().saveBeaconAddress(currentBeacon, strncx, strncy, strnad);
//                                                                dialog.dismiss();
//                                                                iswork = false;
//                                                            }
//                                                        })
//                                                        .create().show();

                                            }
                                        }
                                    }).setPositiveButton("确认", null)
                                    .create().show();
                        }
                    });
                }
            }
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mIBeaconDataset.size();
    }

    private int getRSSIView(IBeacon beacon) {
        if (beacon.getRssi() <= -110) {
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

    public void updateIBeaconData(ArrayList<IBeacon> data) {
        ArrayList<IBeacon> newData = new ArrayList<IBeacon>();
        newData.addAll(data);
        mIBeaconDataset = newData;
    }
    public void updateIBeaconDataFromDb(ArrayList<DBIbeancon> data) {
        mIBeaconDataset.clear();
        for (DBIbeancon x : data)
            mIBeaconDataset.add(x);
    }
    public Object getDataItem(int p) {
        if (p < mIBeaconDataset.size())
            return mIBeaconDataset.get(p);
        else return null;
    }

}