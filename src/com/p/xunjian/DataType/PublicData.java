package com.p.xunjian.DataType;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.telephony.TelephonyManager;
import com.lef.scanner.IBeacon;
import com.p.xunjian.R;
import com.p.xunjian.Util.AssetsDatabaseManager;
import com.p.xunjian.Util.DataUtil;
import com.p.xunjian.Util.SdDbHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by p on 2014/12/16.
 */
public class PublicData extends Application {

    private static PublicData myself;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    private String user;

    public String getPsw() {
        return psw;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    String key;
    public void setPsw(String psw) {
        this.psw = psw;
    }

    private String psw;
    private String ip;
    public MessageDigest md5_encriptor = null;
    public boolean isHas_save_ip() {
        return has_save_ip;
    }

    public void setHas_save_ip(boolean has_save_ip) {
        this.has_save_ip = has_save_ip;
    }

    private boolean has_save_ip;
    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    private String port;
    public static PublicData getInstance() {
        return myself;
    }

    public DataUtil du;
    private ConcurrentHashMap<String, Handler> handlerHashMap = new ConcurrentHashMap<String, Handler>();

    public ConcurrentHashMap<String, Handler> getHandlerHashMap() {
        return handlerHashMap;
    }

    //数据库中待巡检的beacon
    public ArrayList<IBeacon> waitBeaconDataList = new ArrayList<IBeacon>();
    public ArrayList<DBIbeancon> dbBeaconDataList = new ArrayList<DBIbeancon>();
    public HashSet<String> dbBeaconMacSet = new HashSet<String>();
    public ConcurrentHashMap<String, String> beaconAddressMap = new ConcurrentHashMap<String, String>();
    public ConcurrentHashMap<String,DBIbeancon> dbBeaconMap = new ConcurrentHashMap<String, DBIbeancon>();

    //标记巡检到的beacon
    public ArrayList<IBeacon> checkedBeaconDataList = new ArrayList<IBeacon>();
    public HashSet<String> uploadBeaconSet = new HashSet<String>();
    public HashSet<String> checkBeaconSet = new HashSet<String>();

    //检测到的beacon
    public ArrayList<IBeacon> touchedBeaconDataList = new ArrayList<IBeacon>();

    @Override
    public void onCreate() {
        myself = this;
        super.onCreate();
        du = new DataUtil(this, this.getString(R.string.unupload_dbname), null, 1);
        user = "A01001";
        try {
            md5_encriptor = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        getUnuploadBeaconDataInDb();
        getUploadedBeaconDataInDb();
        getDeployedBeaconInAssestsDb();

    }

    public String getMd5(String data){

        md5_encriptor.reset();
        byte[] data_byte = null;
        data_byte = data.getBytes();

        byte[] hash_data = md5_encriptor.digest(data_byte);
        StringBuilder md5StrBuff = new StringBuilder();

        for (byte aHash_data : hash_data) {
            if (Integer.toHexString(0xFF & aHash_data).length() == 1)
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & aHash_data));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & aHash_data));
        }

        return md5StrBuff.toString();
    }
    public String getBeaconAddress(IBeacon beacon) {
        if (beacon instanceof DBIbeancon) {
            return ((DBIbeancon) beacon).getAddress();
        } else {
            return beaconAddressMap.get(beacon.getBluetoothAddress()) == null ? "未知" : beaconAddressMap.get(beacon.getBluetoothAddress());
        }
    }
    public ArrayList<DBIbeancon> getWaitBeaconData(){
        ArrayList<DBIbeancon> dbIbeancons = new ArrayList<DBIbeancon>();
        for(IBeacon iBeacon : waitBeaconDataList){
            DBIbeancon dbIbeancon = (DBIbeancon)iBeacon;
            dbIbeancons.add(dbIbeancon);
        }
        return dbIbeancons;
    }
    public void saveBeaconAddress(IBeacon iBeacon,String x,String y,String ad){
        String sql = String.format("update unupbeacon set coord_x='%s',coord_y='%s',address='%s' where mac_id='%s'",x,y,ad,iBeacon.getBluetoothAddress());
        SQLiteDatabase db = du.getReadableDatabase();

        try {
            db.execSQL(sql);
        }catch (SQLException e){
            e.printStackTrace();
        }finally {

            db.close();
        }
    }
    /**
     */
    public void getDeployedBeaconInAssestsDb() {
        /*AssetsDatabaseManager.initManager(myself);
        // 获取管理对象，因为数据库需要通过管理对象才能够获取
        AssetsDatabaseManager mg = AssetsDatabaseManager.getManager();
        // 通过管理对象获取数据库
        SQLiteDatabase db1 = mg.getDatabase("NEWBEACONDEPOY902.db");*/
        SdDbHelper dbHelper = new SdDbHelper(this);
        SQLiteDatabase db1 = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
             cursor = db1.rawQuery(getString(R.string.get_db_beacon)+" where status='已巡检'", null);
            if (cursor != null) {
                while (cursor.moveToNext()) {//直到返回false说明表中到了数据末尾
                    DBIbeancon ibeacon = new DBIbeancon();
                    ibeacon.setMac(cursor.getString(0));
                    ibeacon.setMajor(cursor.getString(1));
                    ibeacon.setMinor(cursor.getString(2));
                    ibeacon.setBuilding(cursor.getString(3));
                    ibeacon.setFloor(cursor.getString(4));
                    ibeacon.setAddress(cursor.getString(5));
                    ibeacon.setCoordx(cursor.getString(6));
                    ibeacon.setCoordy(cursor.getString(7));
                    beaconAddressMap.put(cursor.getString(0), cursor.getString(5));
                    if(!checkBeaconSet.contains(ibeacon.getBluetoothAddress()) && !uploadBeaconSet.contains(ibeacon.getBluetoothAddress()))
                        waitBeaconDataList.add(ibeacon);
                    if(uploadBeaconSet.contains(ibeacon.getBluetoothAddress())){
                        checkedBeaconDataList.add(ibeacon);
                    }
                    dbBeaconDataList.add(ibeacon);
                    dbBeaconMacSet.add(cursor.getString(0));
                    dbBeaconMap.put(ibeacon.getBluetoothAddress(),ibeacon);
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            cursor.close();
            db1.close();
        }
    }

    /**
     * 将一个beancon 添加到 带巡检列表中
     * @param mac
     */
    public void add2WaitList(String mac){
        DBIbeancon beacon = dbBeaconMap.get(mac);
        if(beacon != null)
            waitBeaconDataList.add(beacon);
    }
    public void removeFromWaitList(String mac){
        IBeacon beacon = dbBeaconMap.get(mac);
        if(beacon != null)
            waitBeaconDataList.remove(beacon);
    }
    /**
     * 程序启动后，从数据库中读取上次保存已巡检的beacon状态，
     */
    public void getUnuploadBeaconDataInDb() {
        SQLiteDatabase db = du.getReadableDatabase();
        Cursor cursor = null;
        try {
             cursor = db.rawQuery(getString(R.string.select_unupload_beacon), null);
            if (cursor != null) {
                while (cursor.moveToNext()) {//直到返回false说明表中到了数据末尾
                    DBIbeancon ibeacon = new DBIbeancon();
                    ibeacon.setMac(cursor.getString(cursor.getColumnIndex("mac_id")));
                    ibeacon.setMajor(cursor.getString(cursor.getColumnIndex("major")));
                    ibeacon.setMinor(cursor.getString(cursor.getColumnIndex("minor")));
                    ibeacon.setRssi(cursor.getString(cursor.getColumnIndex("rssi")));
                    checkedBeaconDataList.add(ibeacon);
                    checkBeaconSet.add(ibeacon.getBluetoothAddress());
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            cursor.close();
            db.close();
        }

    }

    /**
     * 清楚巡检当前巡检的状态
     * 将删除数据库中记录的已巡检，已上传数据
     * 重置UI数据
     */
    public void clearAllStatus() {
        String uploaded_sql = "delete  from uploaded";
        String unupload_sql = "delete  from unupbeacon";

        checkBeaconSet.clear();
        checkedBeaconDataList.clear();
        uploadBeaconSet.clear();
        waitBeaconDataList.clear();
        waitBeaconDataList.addAll(dbBeaconDataList);
        SQLiteDatabase db = du.getReadableDatabase();
        try {
            db.execSQL(uploaded_sql);
            db.execSQL(unupload_sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }

    }

    /**
     * 获取存在数据中已上传的beacon信息
     */
    public void getUploadedBeaconDataInDb() {
        SQLiteDatabase db = du.getReadableDatabase();
        Cursor cursor = db.rawQuery(getString(R.string.select_uploaded_beacon), null);
        if (cursor != null) {
            while (cursor.moveToNext()) {//直到返回false说明表中到了数据末尾
                uploadBeaconSet.add(cursor.getString(0));
            }
        }
        cursor.close();
        db.close();
    }

    public boolean saveUploadedBeacon2Db(String sql) {
        SQLiteDatabase db = du.getReadableDatabase();
        boolean result = true;
        try {
            db.execSQL(sql);
        } catch (SQLException e) {
            result = false;
        } finally {
            db.close();
        }
        return result;
    }

    public boolean saveCheckBeacon2Db(IBeacon iBeacon) {
        boolean result = true;
        SQLiteDatabase db = du.getReadableDatabase();
        String sql;
        // area text,type text,time text,val text
        String beaconType;
        if (dbBeaconMacSet.contains(iBeacon.getBluetoothAddress()))
            beaconType = "our";
        else beaconType = "other";
        String coordx="",coordy="",address="",building ="",floor = "";
        if(dbBeaconMacSet.contains(iBeacon.getBluetoothAddress())){
            DBIbeancon dbIbeancon = dbBeaconMap.get(iBeacon.getBluetoothAddress());
            if(dbIbeancon != null) {
                coordx = dbIbeancon.getCoordx();
                coordy = dbIbeancon.getCoordy();
                address = dbIbeancon.getAddress();
                building = dbIbeancon.getBuilding();
                floor = dbIbeancon.getFloor();
            }
        }
        sql = "insert into unupbeacon(isour,mac_id,uuid,major,minor,rssi,coord_x,coord_y,address,building,floor) values('";
        sql += beaconType + "','" + iBeacon.getBluetoothAddress() + "','" + iBeacon.getProximityUuid()
                + "','" + iBeacon.getMajor() + "','" + iBeacon.getMinor() + "','" + iBeacon.getRssi()
                + "','"+coordx+"','"+coordy+"','"+address+"','"+building+"','"+floor+"')";

        try {
            db.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            result = false;
        } finally {
            db.close();
        }
        return result;
    }

    public ArrayList<DBIbeancon> getCheckedBeaconInDb() {
        ArrayList<DBIbeancon> beacons = new ArrayList<DBIbeancon>();
        SQLiteDatabase db = du.getReadableDatabase();
        String sql;
        Cursor cursor = null;
        // area text,type text,time text,val text
        String beaconType;

        sql = "select * from unupbeacon";
        try {
            cursor = db.rawQuery(sql, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {//直到返回false说明表中到了数据末尾
                    DBIbeancon ibeacon = new DBIbeancon();
                    ibeacon.setMac(cursor.getString(cursor.getColumnIndex("mac_id")));
                    ibeacon.setMajor(cursor.getString(cursor.getColumnIndex("major")));
                    ibeacon.setMinor(cursor.getString(cursor.getColumnIndex("minor")));
                    ibeacon.setIsour(cursor.getString(cursor.getColumnIndex("isour")));
                    ibeacon.setRssi(cursor.getString(cursor.getColumnIndex("rssi")));
                    ibeacon.setUuid(cursor.getString(cursor.getColumnIndex("uuid")));
                    ibeacon.setBuilding(cursor.getString(cursor.getColumnIndex("building")));
                    ibeacon.setCoordx(cursor.getString(cursor.getColumnIndex("coord_x")));
                    ibeacon.setCoordy(cursor.getString(cursor.getColumnIndex("coord_y")));
                    ibeacon.setAddress(cursor.getString(cursor.getColumnIndex("address")));
                    beacons.add(ibeacon);
                }
            }
        } catch (SQLException e) {
            beacons = null;
        } finally {
            db.close();
        }
        return beacons;
    }

    public boolean deleteCheckBeacon2Db(IBeacon iBeacon) {
        boolean result = true;
        SQLiteDatabase db = du.getReadableDatabase();
        String sql;
        // area text,type text,time text,val text
        String beaconType;

        sql = "delete from unupbeacon where mac_id = '" + iBeacon.getBluetoothAddress() + "'";
        try {
            db.execSQL(sql);
        } catch (SQLException e) {
            result = false;
        } finally {
            db.close();
        }
        return result;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
        } else {
            //如果仅仅是用来判断网络连接
            //则可以使用 cm.getActiveNetworkInfo().isAvailable();
            NetworkInfo[] info = cm.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String getImei() {
        return ((TelephonyManager) getSystemService(TELEPHONY_SERVICE))
                .getDeviceId();
    }
}
