package com.carnavi.offlineosm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import static android.Manifest.permission.*;

public class MainActivity extends AppCompatActivity {

    private static int REQUEST_WRITE_STORAGE = 1;

    Context context;

    MapView map;

    Timer timer;
    TimerTask timerTask;
    double latitude;
    double longitude;
    int cnt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        ActivityCompat.requestPermissions(this,
//                new String[]{WRITE_EXTERNAL_STORAGE},
//                REQUEST_WRITE_STORAGE);
        ActivityCompat.requestPermissions(this,
                new String[]{READ_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION}, 1);

        context = this;
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        File osmdroid = new File(Environment.getExternalStorageDirectory().getPath(), "/Download/osmdroid");
        File tiles = new File(osmdroid.getPath(), "tiles");

        //Log
        Log.d("File", "osmdroid getAbsolutePath : " + osmdroid.getAbsolutePath());
        Log.d("File", "osmdroid getPath : " + osmdroid.getPath());

        if (osmdroid.isDirectory()) {
            Log.d("File", "Osmdroid isDirectory");
        }

        //inflate and create the map
        setContentView(R.layout.activity_main);

        Configuration.getInstance().setOsmdroidBasePath(osmdroid);
        Configuration.getInstance().setOsmdroidTileCache(tiles);


        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        map.setUseDataConnection(false);

        // 시작시 보이는 위치 설정
        IMapController mapController = map.getController();
        mapController.setZoom(19.0);
        GeoPoint startPoint = new GeoPoint(44.6256, 11.3810);
        mapController.setCenter(startPoint);

        movePoint();
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }

    public void movePoint() {
        latitude = 44.6256;
        longitude = 11.3810;

        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (cnt < 10) {
                    GeoPoint point = new GeoPoint(latitude += 0.0001, longitude += 0.0001);
                    handler.obtainMessage(1, point).sendToTarget();
                    cnt++;
                } else {
                    timer.cancel();
                    timerTask.cancel();
                }
            }
        };
        timer.schedule(timerTask, 0, 1000);
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what) {
                case 1:
                    GeoPoint point = (GeoPoint) msg.obj;
                    map.getController().setCenter(point);
                    Marker marker = new Marker(map);
                    marker.setPosition(point);
                    map.getOverlays().clear();
                    map.getOverlays().add(marker);
                    map.invalidate();
                    break;
            }
        }
    };
}
