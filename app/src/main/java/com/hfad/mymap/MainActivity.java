package com.hfad.mymap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.SyncStateContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import com.baidu.mapapi.map.MyLocationConfiguration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.model.LatLng;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    private MapView mMapView = null;
    private BaiduMap mBaiduMap;
    private LocationService locationService;
    public LocationClient mLocationClient = null;
    private String mLatitudeStr, mLongitudeStr, province;
    private EditText j, w;
    private static final String TAG = "MainActivity";


    private BDLocationListener mListener = new BDLocationListener() {
        int count = 0;

        @Override
        public void onReceiveLocation(BDLocation location) {
            mBaiduMap = mMapView.getMap();
            mBaiduMap.setMyLocationEnabled(true);
            LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
            MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.setMapStatus(mapStatusUpdate);
            //4、设置地图缩放为15
            mapStatusUpdate = MapStatusUpdateFactory.zoomTo(20);
            mBaiduMap.setMapStatus(mapStatusUpdate);
            mBaiduMap.animateMapStatus( mapStatusUpdate);
            String addr = location.getAddrStr();    //获取详细地址信息
            String country = location.getCountry();    //获取国家
            String province = location.getProvince();    //获取省份
            String city = location.getCity();    //获取城市
            String district = location.getDistrict();    //获取区县
            String street = location.getStreet();    //获取街道信息
            mLatitudeStr = Double.toString(location.getLatitude());
            mLongitudeStr = Double.toString(location.getLongitude());
            Log.d(TAG, "onReceiveLocation: 纬度" + mLatitudeStr + "    经度 " + mLongitudeStr);
            count++;
            province = location.getProvince();
            Log.d(TAG, "省份" + province);
            Toast.makeText(getApplicationContext(), "纬度" + mLatitudeStr + "经度" + mLongitudeStr + "省份" + province + "\n" + addr + street, Toast.LENGTH_SHORT).show();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
            String time = df.format(new Date());
            ToWeb(mLatitudeStr,mLongitudeStr,time,addr);
          if (location == null || mMapView == null){
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
           /*if (!"".equals(province) && locationService != null) {
                locationService.stop();
            }*/
        }

    };
    private static final int BAIDU_READ_PHONE_STATE = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();

        //普通地图 ,mBaiduMap是地图控制器对象
       mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        //卫星地图
       //mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
        //空白地图
        //case :mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NONE);
        //开启交通图
        // case :mBaiduMap.setTrafficEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getApplicationContext().checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, BAIDU_READ_PHONE_STATE);
            }
            if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, BAIDU_READ_PHONE_STATE);
            }
            if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            }
            if (getApplicationContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
            if (getApplicationContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
            }
        }

        locationService = new LocationService(getApplicationContext());
        locationService.registerListener(mListener);
        //注册监听
        locationService.setLocationOption(locationService.getDefaultLocationClientOption());
        Button button = findViewById(R.id.but1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isopen()) {
                    Toast.makeText(MainActivity.this, "GPS没开", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "开了", Toast.LENGTH_SHORT).show();
                }
                mBaiduMap.setMyLocationEnabled(true);
                locationService.start();

            }
        });
    }
        private boolean isopen () {
            LocationManager locationManager = (LocationManager) getApplicationContext().
                    getSystemService(Context.LOCATION_SERVICE);
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

        @Override
        protected void onResume () {
            mMapView.onResume();
            super.onResume();
            //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        }
        @Override
        protected void onPause () {
            mMapView.onPause();
            super.onPause();
            //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理

        }
        @Override
        protected void onDestroy () {
            mBaiduMap.setMyLocationEnabled(false);
            mMapView.onDestroy();
            mMapView = null;
            super.onDestroy();
        }

        void ToWeb(final String positionxs,final String positionys,final String time,final String positionChinese){
            final Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    // 反复尝试连接，直到连接成功后退出循环
                    while (!Thread.interrupted()) {
                        try {
                            Thread.sleep(1000);  // 每隔0.1秒尝试连接
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // 2.设置好IP/端口/数据库名/用户名/密码等必要的连接信息
                        String ip = "47.107.142.103";
                        int port = 3306;
                        String dbName = "tesr";
                        String url = "jdbc:mysql://" + ip + ":" + port
                                + "/" + dbName; // 构建连接mysql的字符串
                        String user = "tesr";
                        String password = "1002";
                        String TAG = "0";
                        String posx = "'" + positionxs + "'";
                        String posy = "'" + positionys + "'";
                        String positionChineseInpu ="'" +positionChinese + "'";
                        // 3.连接JDBC
                        try {
                            Connection conn = DriverManager.getConnection(url, user, password);
                            Log.i(TAG, "远程连接成功!");
                            String sql = "INSERT INTO gpsData(latitionx,latitiony,name,positionChinese)"
                                    + " VALUES (" +positionxs+ ","+posy+", "+"'"+ time+"'" +","+positionChineseInpu+ ")";  // 插入数据的sql语句

                            Statement statement = conn.createStatement();
                            int count = statement.executeUpdate(sql);
                            Log.i(TAG,"向gpsData表中加入" + count + "条数据" + positionChineseInpu + "oo" + positionChinese);
                            //Toast.makeText(getApplicationContext(),"向gpsData中加入1条数据",Toast.LENGTH_SHORT).show();
                            conn.close();
                            return;
                        } catch (SQLException e) {
                            Log.e(TAG, "远程连接失败!");
                        }
                    }
                }
            });
            thread.start();
        }


    }

