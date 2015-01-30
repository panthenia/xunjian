package com.p.xunjian.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.lz.map.FloatView;
import com.p.xunjian.R;
import com.wxq.draw.MapControler;
import com.wxq.draw.MapLayout;

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

    String building,floor,coordx,coordy;
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
        setContentView(R.layout.map_activity);

        Intent intent = getIntent();
        if(intent.hasExtra("building") && intent.hasExtra("floor")
            &&intent.hasExtra("coordx") && intent.hasExtra("coordy")){
            setBuilding(intent.getStringExtra("building"));
            setFloor(intent.getStringExtra("floor"));
            setCoordx(intent.getStringExtra("coordx"));
            setCoordy(intent.getStringExtra("coordy"));
            mapLayout = (MapControler) findViewById(R.id.mapLayout);
            if (mapLayout == null || !mapLayout.isSuccess()){
                finish();
            }
            Log.d(getClass().getName(),building);
            Log.d(getClass().getName(),floor);

        }else{
            new AlertDialog.Builder(this).setTitle("无此Beacon位置")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).create().show();
        }
    }
    public void onChangeMap(View v){
        mapLayout.changedbmap(getBuilding(), getFloor());
        /*FloatView fView=mapLayout.getFloatView();
        Paint p=new Paint();
        p.setColor(Color.RED);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        fView.addCircle("hah", Float.valueOf(coordx), Float.valueOf(coordy), 1, p);
        */
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
                Float.valueOf(coordx),
                Float.valueOf(coordy),
                5,
                paintCricleB);

        mapLayout.getFloatView()
                .addCircle("hah",
                        Float.valueOf(coordx),
                        Float.valueOf(coordy),
                        5,
                        paintEmptyB);
        mapLayout.getFloatView()
                .addCircle("h",
                        Float.valueOf(coordx),
                        Float.valueOf(coordy),
                        1,
                        paintEmptyc);
    }
}