package com.carnavi.offlineosm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.cachemanager.CacheManager;
import org.osmdroid.tileprovider.modules.SqliteArchiveTileWriter;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private static int REQUEST_WRITE_STORAGE = 1;

    Context context;

    MapView map;

    Timer timer;
    TimerTask timerTask;
    double latitude;
    double longitude;
    int cnt = 0;

    CacheManager mgr;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this,
                new String[]{WRITE_EXTERNAL_STORAGE},
                REQUEST_WRITE_STORAGE);
        ActivityCompat.requestPermissions(this,
                new String[]{READ_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION}, 1);

        context = this;
        File osmdroid = new File(Environment.getExternalStorageDirectory().getPath(), "Download/osmdroid");
        File tiles = new File(osmdroid.getPath(), "tiles");
        Configuration.getInstance().setOsmdroidBasePath(osmdroid);
        Configuration.getInstance().setOsmdroidTileCache(tiles);

        //Log
        Log.d("File", "osmdroid getAbsolutePath : " + osmdroid.getAbsolutePath());
        Log.d("File", "osmdroid getPath : " + osmdroid.getPath());

        if (osmdroid.isDirectory()) {
            Log.d("File", "Osmdroid isDirectory");
        }

        //inflate and create the map
        setContentView(R.layout.activity_main);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
//        map.setUseDataConnection(false);

        //sqlwriter
        String output = osmdroid.getAbsolutePath() + File.separator + "test" + ".sqlite";
        SqliteArchiveTileWriter writer = null;
        try {
            writer = new SqliteArchiveTileWriter(output);
            Log.d("output", "output : " + output);

        } catch (Exception e) {
            e.printStackTrace();
        }

        mgr = new CacheManager(map, writer);
        map.addMapListener(new DelayedMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                //download tile cache
//                BoundingBox bb = map.getBoundingBox();
//                int currentZoomLevel = (int) map.getZoomLevelDouble();
//                if (currentZoomLevel > 19) {
//                    currentZoomLevel = 19;
//                }
//                mgr.downloadAreaAsyncNoUI(context, bb, currentZoomLevel, currentZoomLevel, new CacheManager.CacheManagerCallback() {
//                    @Override
//                    public void onTaskComplete() {
//                        Toast.makeText(context, "Download complete!!", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void updateProgress(int progress, int currentZoomLevel, int zoomMin, int zoomMax) {
//
//                    }
//
//                    @Override
//                    public void downloadStarted() {
//
//                    }
//
//                    @Override
//                    public void setPossibleTilesInArea(int total) {
//
//                    }
//
//                    @Override
//                    public void onTaskFailed(int errors) {
//
//                    }
//                });

                Log.d("zoom", "onScroll zoom : " + map.getZoomLevelDouble());

                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                map.invalidate();
                Log.d("zoom", "onZoom zoom : " + map.getZoomLevelDouble());
                return false;
            }
        }, 100));

        // 시작시 보이는 위치 설정
        IMapController mapController = map.getController();
        mapController.setZoom(18.0);
        GeoPoint startPoint = new GeoPoint(44.6256, 11.3810);
        mapController.setCenter(startPoint);

        //offline tile to cache
        //download tile cache
//        mgr = new CacheManager(map);
        BoundingBox bb = new BoundingBox(44.637507, 11.394768, 44.597256, 11.369250);
        mgr.downloadAreaAsync(context, bb, 18, 18, new CacheManager.CacheManagerCallback() {
            @Override
            public void onTaskComplete() {
                Toast.makeText(context, "Download complete!!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void updateProgress(int progress, int currentZoomLevel, int zoomMin, int zoomMax) {

            }

            @Override
            public void downloadStarted() {

            }

            @Override
            public void setPossibleTilesInArea(int total) {

            }

            @Override
            public void onTaskFailed(int errors) {

            }
        });

//        movePoint();
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
