package com.p.xunjian.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SdDbHelper extends SQLiteOpenHelper{
    //数据库文件在SD卡中,此时必须用全限定名!! 默认路径在/data/data/databases/testDBinSD.sqlite, 但私密数据外界看不到-除非root。  
    private static final String SQL_NAME = "/sdcard/xunjian/NEWBEACONDEPOY902.db";//数据库名称。//Environment.getExternalStorageDirectory().getPath() + "testDBinSD.sqlite";//

    //构造方法  
    public SdDbHelper(Context context) {
        super(context, SQL_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //建表
        String createBeaconTable = "CREATE TABLE [beacon] (" +
                "[mac_id] varchar(40) NOT NULL,  " +
                "[uuid] varchar(40) NOT NULL,  " +
                "[major] varchar(11) NOT NULL,  " +
                "[minor] varchar(11) NOT NULL,  " +
                "[building] varchar(40) NOT NULL, " +
                "[floor] varchar(40) NOT NULL,  " +
                "[coord_x] varchar(10) NOT NULL,  " +
                "[coord_y] varchar(10) NOT NULL,  " +
                "[coverage] varchar(40) ,  " +
                "[power] varchar(20) ," +
                "[frequency] varchar(20) ,  " +
                "[temperaturefrequency] varchar(20) ,  " +
                "[lightfrequency] varchar(20) ,  " +
                "[type] varchar(40) ,  " +
                "[firm] varchar(100) ,  " +
                "[create_id] varchar(20) ,  " +
                "[company_id] varchar(20) ,  " +
                "[accelerate] varchar(20) ,  " +
                "[create_time] varchar(20) , " +
                "[last_modify_id] varchar(20) ,  " +
                "[last_modify_time] varchar(20)," +
                "[address] varchar(1000),"+
                "[status] varchar(20)"+
                " )";
        db.execSQL(createBeaconTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}  