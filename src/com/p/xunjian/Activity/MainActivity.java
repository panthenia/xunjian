package com.p.xunjian.Activity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.lef.scanner.*;
import com.p.xunjian.DataType.AroundListAdapter;
import com.p.xunjian.DataType.PublicData;
import com.p.xunjian.R;
import com.p.xunjian.Util.NetWorkService;

import java.util.*;

/**
 * Created by p on 2015/1/19.
 */
public class MainActivity extends Activity implements
        com.lef.scanner.IBeaconConsumer {
    private RecyclerView mRecyclerView;
    private AroundListAdapter aroundAdapter;
    public final static int WAIT = 1;
    public final static int CHECKED = 2;
    public final static int AROUND = 3;
    private int showType;
    private RecyclerView.LayoutManager mLayoutManager;
    protected static final int BLUTETOOTH = 1;
    private static final int UPDATEUI = 1;
    public static final int REQUEST_FINISH_SUCCESS = 2;
    public static final int KEY_TIME_OUT = 4;
    public static final int REQUEST_FINISH_FAIL = 3;
    public static final int UPLOAD_WAIT_SUCCESS = 5;
    public static final int UPLOAD_WAIT_FAIL = 6;
    public static final int UPLOAD_TYPE_CHECKED = 7;
    public static final int UPLOAD_TYPE_UNCHECKED = 8;
    private Button btWait, btAround, btChecked;
    private ProgressDialog pro_dialog;
    private IBeaconManager iBeaconManager;
    private ArrayList<IBeacon> aroundBeaconDataList = new ArrayList<IBeacon>();

    private Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        public void handleMessage(final android.os.Message msg) {
            switch (msg.what) {
                case UPDATEUI:
                    if (showType == AROUND) {
                        //Collections.sort(aroundBeaconDataList,comparator);
                        if (!aroundAdapter.isWork()) {
                            aroundAdapter.updateIBeaconData(aroundBeaconDataList);
                            Log.d("UI","UI更新");
                            aroundAdapter.notifyDataSetChanged();
                        }
                    }
                    break;
                case REQUEST_FINISH_SUCCESS:
                    Toast.makeText(MainActivity.this, "巡检数据上报成功！", Toast.LENGTH_LONG);
                    aroundAdapter.notifyDataSetChanged();
                    break;
                case REQUEST_FINISH_FAIL:
                    Toast.makeText(MainActivity.this, "巡检数据上报失败！", Toast.LENGTH_LONG);
                    break;
                case KEY_TIME_OUT:
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("警告")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setMessage("登录信息已过期，请重新登录!")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (msg.arg1){
                                        case MainActivity.UPLOAD_TYPE_CHECKED:
                                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                            startActivityForResult(intent, 5);
                                            break;
                                        case MainActivity.UPLOAD_TYPE_UNCHECKED:
                                            Intent intent1 = new Intent(MainActivity.this, LoginActivity.class);
                                            startActivityForResult(intent1, 6);
                                            break;
                                    }
                                }
                            })
                            .create().show();
                    break;
                case UPLOAD_WAIT_SUCCESS:
                    Toast.makeText(MainActivity.this, "未巡检数据上报成功！", Toast.LENGTH_LONG).show();
                    //PublicData.getInstance().checkedBeaconDataList.addAll(PublicData.getInstance().waitBeaconDataList);
                    PublicData.getInstance().waitBeaconDataList.clear();
                    if (showType == WAIT)
                        onWaitBeaconClick(btWait);
                    break;
                case UPLOAD_WAIT_FAIL:
                    Toast.makeText(MainActivity.this, "未巡检数据上报失败！", Toast.LENGTH_LONG).show();

                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setTitle("周围的Beacons");
        int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        if (actionBarTitleId > 0) {
            TextView title = (TextView) findViewById(actionBarTitleId);
            if (title != null) {
                title.setTextColor(Color.WHITE);
            }
        }
        PublicData.getInstance().getHandlerHashMap().put(MainActivity.class.getName(), handler);
        getActionBar().setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.backcolor_norock));
        iBeaconManager = IBeaconManager.getInstanceForApplication(this);

        setContentView(R.layout.main_activity);
        btAround = (Button) findViewById(R.id.bt_around);
        btWait = (Button) findViewById(R.id.bt_wait);
        btChecked = (Button) findViewById(R.id.bt_checked);
        btAround.setTextColor(Color.rgb(0xff, 0xff, 0xff));
        showType = AROUND;
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        //mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
        //    @Override public void onItemClick(View view, int position) {
        //    }
        //}));
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // specify an adapter (see also next example)
        aroundAdapter = new AroundListAdapter(this);
        mRecyclerView.setAdapter(aroundAdapter);


    }

    public void onWaitBeaconClick(View v) {
        Button button = (Button) v;
        button.setTextColor(Color.rgb(0xff, 0xff, 0xff));
        showType = WAIT;
        aroundAdapter.updateIBeaconData(PublicData.getInstance().waitBeaconDataList);
        aroundAdapter.setShowType(WAIT);
        aroundAdapter.notifyDataSetChanged();
        getActionBar().setTitle("待巡检的beacons");
        btAround.setTextColor(Color.rgb(0x99, 0x99, 0x99));
        btChecked.setTextColor(Color.rgb(0x99, 0x99, 0x99));
    }

    public void onAroundBeaconClick(View v) {
        Button button = (Button) v;
        showType = AROUND;
        aroundAdapter.updateIBeaconData(aroundBeaconDataList);
        aroundAdapter.setShowType(AROUND);
        aroundAdapter.notifyDataSetChanged();
        getActionBar().setTitle("周围的beacons");
        btWait.setTextColor(Color.rgb(0x99, 0x99, 0x99));
        btChecked.setTextColor(Color.rgb(0x99, 0x99, 0x99));
        button.setTextColor(Color.rgb(0xff, 0xff, 0xff));
    }

    public void onCheckedBeaconClick(View v) {
        Button button = (Button) v;
        button.setTextColor(Color.rgb(0xff, 0xff, 0xff));
        getActionBar().setTitle("已巡检的beacons");
        showType = CHECKED;
        aroundAdapter.setShowType(CHECKED);
        aroundAdapter.updateIBeaconDataFromDb(PublicData.getInstance().getCheckedBeaconInDb());

        aroundAdapter.notifyDataSetChanged();
        btWait.setTextColor(Color.rgb(0x99, 0x99, 0x99));
        btAround.setTextColor(Color.rgb(0x99, 0x99, 0x99));
    }

    private void initBluetooth() {
        // TODO Auto-generated method stub
        final BluetoothAdapter blueToothEable = BluetoothAdapter
                .getDefaultAdapter();
        if (!blueToothEable.isEnabled()) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("蓝牙开启")
                    .setMessage("配置需要开启蓝牙").setCancelable(false)
                    .setPositiveButton("开启", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            blueToothEable.enable();
                            iBeaconManager.bind(MainActivity.this);
                        }
                    }).setNegativeButton("退出", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    MainActivity.this.finish();
                }
            }).create().show();
        } else {
            iBeaconManager.setForegroundScanPeriod(800);
            iBeaconManager.bind(this);
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        // if (iBeaconManager.isBound(this)) {
        // iBeaconManager.unBind(this);
        // }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (iBeaconManager != null && !iBeaconManager.isBound(this)) {
            if (aroundBeaconDataList.size() > 0)
                aroundBeaconDataList.clear();
            // 蓝牙dialog
            initBluetooth();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("return", "resultCode" + requestCode);
        if (requestCode == 5 && resultCode == 5) {
            Intent intent = new Intent(MainActivity.this, NetWorkService.class);
            intent.putExtra("ActivityName", MainActivity.class.getName());
            intent.putExtra("ReuqestType", "upload_checked");
            startService(intent);
            Toast.makeText(this, "开始上传...", Toast.LENGTH_SHORT).show();
        } else if (requestCode == 6 && resultCode == 5) {
            Intent intent = new Intent(MainActivity.this, NetWorkService.class);
            intent.putExtra("ActivityName", MainActivity.class.getName());
            intent.putExtra("ReuqestType", "upload_wait");
            startService(intent);
            Toast.makeText(this, "开始上传...", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (iBeaconManager != null && iBeaconManager.isBound(this)) {
            iBeaconManager.unBind(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.upload, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_upload:
                if (PublicData.getInstance().isNetworkAvailable()) {
                    if (PublicData.getInstance().isLogin()) {
                        Intent intent = new Intent(MainActivity.this, NetWorkService.class);
                        intent.putExtra("ActivityName", MainActivity.class.getName());
                        intent.putExtra("ReuqestType", "upload_checked");
                        startService(intent);
                    } else {
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivityForResult(intent, 5);
                    }
                } else {
                    Toast.makeText(this, "当前无网络连接！", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_clearupload:
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("警告")
                        .setMessage("是否重置状态").setCancelable(false)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                PublicData.getInstance().clearAllStatus();
                                switch (showType) {
                                    case WAIT:
                                        onWaitBeaconClick(btWait);
                                        break;
                                    case AROUND:
                                        onAroundBeaconClick(btAround);
                                        break;
                                    case CHECKED:
                                        onCheckedBeaconClick(btChecked);
                                        break;
                                }
                                Toast.makeText(MainActivity.this, "状态已重置", Toast.LENGTH_SHORT);
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create().show();

                return true;
            case R.id.action_unupload:
                if (PublicData.getInstance().isNetworkAvailable()) {
                    if (PublicData.getInstance().isLogin()) {
                        Intent intent = new Intent(MainActivity.this, NetWorkService.class);
                        intent.putExtra("ActivityName", MainActivity.class.getName());
                        intent.putExtra("ReuqestType", "upload_wait");
                        startService(intent);
                    } else {
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivityForResult(intent, 6);
                    }
                } else {
                    Toast.makeText(this, "当前无网络连接！", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_Shelter:
                View v = LayoutInflater.from(this).inflate(R.layout.new_local, null);
                final ListView listView = (ListView) v.findViewById(R.id.shelter_list);
                final EditText editText = (EditText) v.findViewById(R.id.new_shelter);
                final List<String> data = new ArrayList<String>();
                Iterator<String> iterator = PublicData.getInstance().beaconShelter.iterator();
                while (iterator.hasNext()) {
                    data.add(iterator.next());
                }
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, data);
                listView.setAdapter(adapter);
                new AlertDialog.Builder(this)
                        .setTitle("添加新过滤条件(Major值)")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(v)
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String text = editText.getText().toString();
                                adapter.add(text);
                                PublicData.getInstance().beaconShelter.add(text);
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .create().show();
                return true;
            case R.id.clear_Shelter:
                PublicData.getInstance().beaconShelter.clear();
                Toast.makeText(this,"beacon过滤条件已清除！",Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onIBeaconServiceConnect() {
        // TODO Auto-generated method stub
        // 启动Range服务
        iBeaconManager.setRangeNotifier(new RangeNotifier() {

            public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons,
                                                Region region) {
                //if (ProgressBarVisibile) {
                //    handler.sendEmptyMessage(PROGRESSBARGONE);
                // }
                // java.util.Iterator<IBeacon> iterator = iBeacons.iterator();
                // while (iterator.hasNext()) {
                // IBeacon temp = iterator.next();
                // if (beaconDataListA.contains(temp)) {
                // beaconDataListA.set(beaconDataListA.indexOf(temp), temp);
                // handler.sendEmptyMessage(UPDATEUI);
                // } else {
                // beaconDataListA.add(temp);
                // handler.sendEmptyMessage(UPDATEUI);
                // }
                //
                // }

            }

            @Override
            public void onNewBeacons(Collection<IBeacon> iBeacons, Region region) {
                // TODO Auto-generated method stub
                // beaconDataListA.addAll(iBeacons);
                // handler.sendEmptyMessage(UPDATEUI);
                Iterator<IBeacon> iterator = iBeacons.iterator();
                while (iterator.hasNext()) {
                    IBeacon temp = iterator.next();
                    if (!PublicData.getInstance().beaconShelter.contains(String.valueOf(temp.getMajor()))) {
                        if (!aroundBeaconDataList.contains(temp)) {
                            aroundBeaconDataList.add(temp);
                        }
                        handler.sendEmptyMessage(UPDATEUI);
                    }
                }
            }

            @Override
            public void onGoneBeacons(Collection<IBeacon> iBeacons,
                                      Region region) {
                // TODO Auto-generated method stub
                Iterator<IBeacon> iterator = iBeacons.iterator();
                while (iterator.hasNext()) {
                    IBeacon temp = iterator.next();
                    if (aroundBeaconDataList.contains(temp)) {
                        aroundBeaconDataList.remove(temp);
                    }
                    handler.sendEmptyMessage(UPDATEUI);
                }
            }

            @Override
            public void onUpdateBeacon(Collection<IBeacon> iBeacons,
                                       Region region) {
                // TODO Auto-generated method stub
                Iterator<IBeacon> iterator = iBeacons.iterator();
                while (iterator.hasNext()) {
                    IBeacon temp = iterator.next();
                    if (aroundBeaconDataList.contains(temp)) {
                        if(PublicData.getInstance().beaconShelter.contains(String.valueOf(temp.getMajor())))
                            aroundBeaconDataList.remove(temp);
                        else aroundBeaconDataList.set(aroundBeaconDataList.indexOf(temp), temp);
                    }
                    handler.sendEmptyMessage(UPDATEUI);
                }
            }

        });
        iBeaconManager.setMonitorNotifier(new MonitorNotifier() {

            @Override
            public void didExitRegion(Region region) {
                // TODO Auto-generated method stub
            }

            @Override
            public void didEnterRegion(Region region) {
                // TODO Auto-generated method stub

            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                // TODO Auto-generated method stub

            }
        });
        try {
            Region myRegion = new Region("myRangingUniqueId", null, null, null);
            iBeaconManager.startRangingBeaconsInRegion(myRegion);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}