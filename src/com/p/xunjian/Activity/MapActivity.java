package com.p.xunjian.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.lxr.overflot.OverlayIcon;
import com.lxr.overflot.OverlayLayout;
import com.lz.map.FloatView;
import com.p.xunjian.DataType.PublicData;
import com.p.xunjian.R;
import com.wxq.draw.DrawConstants;
import com.wxq.draw.DrawDBTool;
import com.wxq.draw.MapControler;
import com.wxq.draw.MapLayout;

import java.io.File;
import java.util.Map;
import java.util.Vector;

/**
 * Created by p on 2015/1/29.
 */
public class MapActivity extends Activity {
    MapControler mapLayout;

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getCoordx() {
        return coordx;
    }

    public void setCoordx(String coordx) {
        this.coordx = coordx;
    }

    public String getCoordy() {
        return coordy;
    }

    public void setCoordy(String coordy) {
        this.coordy = coordy;
    }

    String building, floor, coordx, coordy;
    float newcx;

    public float getNewcy() {
        return newcy;
    }

    public void setNewcy(float newcy) {
        this.newcy = newcy;
    }

    public float getNewcx() {
        return newcx;
    }

    public void setNewcx(float newcx) {
        this.newcx = newcx;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    String mac;
    float newcy;
    public String getNewBuilding() {
        return newBuilding;
    }

    public void setNewBuilding(String newBuilding) {
        this.newBuilding = newBuilding;
    }

    String newBuilding;

    public String getNewFloor() {
        return newFloor;
    }

    public void setNewFloor(String newFloor) {
        this.newFloor = newFloor;
    }

    String newFloor;
    OverlayIcon overlaypointer;

    public String getBeaconDescription() {
        return beaconDescription;
    }

    public void setBeaconDescription(String beaconDescription) {
        this.beaconDescription = beaconDescription;
    }

    String beaconDescription;
    private OverlayLayout overlaylayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle("Beacon的位置");

        int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        if (actionBarTitleId > 0) {
            TextView title = (TextView) findViewById(actionBarTitleId);
            if (title != null) {
                title.setTextColor(Color.WHITE);
            }
        }
        getActionBar().setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.backcolor_norock));
        setContentView(R.layout.map_activity);

        Intent intent = getIntent();
        mapLayout = (MapControler) findViewById(R.id.mapLayout);
        if (mapLayout == null || !mapLayout.isSuccess()) {
            finish();
        }
        if (intent.hasExtra("mac")){
            setMac(intent.getStringExtra("mac"));
        }
        if (intent.hasExtra("nolocation")) {
            Button tb = (Button) findViewById(R.id.bt_show);
            tb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MapActivity.this, "无此beacon位置信息。", Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (intent.hasExtra("building") && intent.hasExtra("floor")
                && intent.hasExtra("coordx") && intent.hasExtra("coordy")) {
            setBuilding(intent.getStringExtra("building"));
            setFloor(intent.getStringExtra("floor"));
            setCoordx(intent.getStringExtra("coordx"));
            setCoordy(intent.getStringExtra("coordy"));
            try {
                setNewcx(Float.valueOf(getCoordx()));
                setNewcy(Float.valueOf(getCoordy()));
            }catch (Exception ignored){

            }
            Log.d(getClass().getName(), building);
            Log.d(getClass().getName(), floor);
        }
        initNewLocationView();
    }

    private void initNewLocationView() {
        overlaypointer = new OverlayIcon(mapLayout, R.drawable.loc_pointer,
                0.8f, false, -1);
        overlaylayout = new OverlayLayout(mapLayout,
                R.layout.navigation_overlay_modify, 2);
        Button btn = (Button) overlaylayout.layoutView
                .findViewById(R.id.bt_locates);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mapLayout.getFloatView().clear();
                Log.d(getClass().getName(), "draw a circle");
                setNewBuilding(mapLayout.getDbfile());
                setNewFloor(mapLayout.getFloor());
                setNewcx(overlaylayout.MapCoord.x);
                setNewcy(overlaylayout.MapCoord.y);
                drawLocation(overlaylayout.MapCoord.x, overlaylayout.MapCoord.y);

            }
        });
        // 点击地图显示事件
        mapLayout.setMapclicklistener(new MapControler.IMapClickListener() {

            @Override
            public void mapClicked(float arg0, float arg1) {
                // TODO Auto-generated method stub
                overlaypointer.pinAtMapWithScreenCoord(new PointF(arg0, arg1));
                overlaylayout.pinAtMapWithScreenCoord(new PointF(arg0, arg1));
            }

            @Override
            public void mapChanged() {
                // TODO Auto-generated method stub
                // String newdb=mapLayout.getDbfile();
                // if(MallDBpath!=null && MallDBpath.equals(newdb))
                // return ;
                // MallDBpath=newdb;
                // DrawDBTool dbTool = new DrawDBTool(MainActivity.this);
                // dbTool.setDBName(MallDBpath);
                // mallName = dbTool.getMallName();
                // setTitle(mallName);
            }

            @Override
            public void floorChanged() {
                // TODO Auto-generated method stub

            }
        });
    }

    private void drawLocation(float x, float y) {
        Paint paintEmptyB = new Paint();
        paintEmptyB.setStyle(Paint.Style.FILL);
        paintEmptyB.setColor(Color.BLUE);
        paintEmptyB.setAlpha(20);

        Paint paintEmptyc = new Paint();
        paintEmptyc.setStyle(Paint.Style.FILL);
        paintEmptyc.setColor(Color.BLACK);


        Paint paintCricleB = new Paint();
        paintCricleB.setStyle(Paint.Style.STROKE);
        paintCricleB.setColor(Color.BLUE);

        mapLayout.getFloatView().addCircle("ha",
                x,
                y,
                5,
                paintCricleB);

        mapLayout.getFloatView()
                .addCircle("hah",
                        x,
                        y,
                        5,
                        paintEmptyB);
        mapLayout.getFloatView()
                .addCircle("h",
                        x,
                        y,
                        1,
                        paintEmptyc);
    }

    private void drawLocation(String x, String y) {
        Paint paintEmptyB = new Paint();
        paintEmptyB.setStyle(Paint.Style.FILL);
        paintEmptyB.setColor(Color.BLUE);
        paintEmptyB.setAlpha(20);

        Paint paintEmptyc = new Paint();
        paintEmptyc.setStyle(Paint.Style.FILL);
        paintEmptyc.setColor(Color.BLACK);


        Paint paintCricleB = new Paint();
        paintCricleB.setStyle(Paint.Style.STROKE);
        paintCricleB.setColor(Color.BLUE);

        try{
            mapLayout.getFloatView().addCircle("ha",
                    Float.valueOf(x),
                    Float.valueOf(y),
                    5,
                    paintCricleB);

            mapLayout.getFloatView()
                    .addCircle("hah",
                            Float.valueOf(x),
                            Float.valueOf(y),
                            5,
                            paintEmptyB);
            mapLayout.getFloatView()
                    .addCircle("h",
                            Float.valueOf(x),
                            Float.valueOf(y),
                            1,
                            paintEmptyc);
        }catch (Exception e){
            Toast.makeText(this,"无该beacon位置信息",Toast.LENGTH_SHORT).show();
        }
    }

    public void onChangeMap(View v) {
        if(getBuilding() == null || getFloor() == null){
            new AlertDialog.Builder(this)
                    .setTitle("警告")
                    .setMessage("该beacon尚未标记地图位置")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .create().show();
        }else{
            try {
                if(getBuilding().length() > 0 && getFloor().length() > 0) {
                    mapLayout.changedbmap(getBuilding(), getFloor());
                    drawLocation(coordx, coordy);
                }else{
                    Toast.makeText(this,"无此beacon位置信息",Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e){
                Toast.makeText(this,"无此beacon位置信息",Toast.LENGTH_SHORT).show();
            }

            /*FloatView fView=mapLayout.getFloatView();
            Paint p=new Paint();
            p.setColor(Color.RED);
            p.setStyle(Paint.Style.FILL_AND_STROKE);
            fView.addCircle("hah", Float.valueOf(coordx), Float.valueOf(coordy), 1, p);
            */

        }
    }

    public void onSaveMap(View v) {
        boolean result = PublicData.getInstance().saveLocation(getMac(),getNewBuilding(),getNewFloor(),getBeaconDescription(),getNewcx(),getNewcy());
        if(result){
            Log.d(getClass().getName(),"保存成功");
            Toast.makeText(this,"保存新位置信息成功",Toast.LENGTH_SHORT).show();
        }else {
            Log.d(getClass().getName(),"保存失败");
            Toast.makeText(this,"保存新位置信息失败",Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            new AlertDialog.Builder(this)
                    .setTitle("警告")
                    .setMessage("是否返回?")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MapActivity.this.finish();
                        }
                    })
                    .create().show();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
    public void onDescription(View v) {
        final EditText ncx = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("输入备注")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(ncx)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strncx = ncx.getText().toString();
                        setBeaconDescription(strncx);
                    }
                })
                .create().show();
    }
}