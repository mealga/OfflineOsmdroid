package com.carnavi.offlineosm;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.File;

import static android.Manifest.permission.*;

public class MainActivity extends AppCompatActivity {

    private static int REQUEST_WRITE_STORAGE = 1;

    Context context;

    MapView map;

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
        File osmdroid = new File(Environment.getExternalStorageDirectory().getPath(), "osmdroid");
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
        map.setUseDataConnection(false); //optional, but a good way to prevent loading from the network and test your zip loading.

        // 시작시 보이는 위치 설정
        IMapController mapController = map.getController();
        mapController.setZoom(19);
        GeoPoint startPoint = new GeoPoint(44.6256, 11.3810);
        mapController.setCenter(startPoint);
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
}
