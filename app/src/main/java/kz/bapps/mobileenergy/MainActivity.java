package kz.bapps.mobileenergy;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import kz.bapps.mobileenergy.fragment.HomeFragment;
import kz.bapps.mobileenergy.fragment.LocationFragment;
import kz.bapps.mobileenergy.fragment.WebFragment;
import kz.bapps.mobileenergy.model.Location;
import kz.bapps.mobileenergy.service.LoadLocationService;

public class MainActivity extends AppCompatActivity implements
        HomeFragment.OnFragmentInteractionListener,
        LocationFragment.OnListFragmentInteractionListener,
        WebFragment.OnFragmentInteractionListener {

    final static private String TAG = "MainActivity";

    public android.location.Location myLocation;
    private List<Location> locations = new ArrayList<>();
    private ImageButton btnHome;
    private ImageButton btnLocs;
    private ImageButton btnShop;
    private ImageButton btnMore;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        /**
         *
         */
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        /**
         *      МЕСТОПОЛОЖЕНИЕ
         */

        myLocation = locationManager.getLastKnownLocation(provider);

        if (myLocation == null) {
            myLocation = new android.location.Location(provider);
            myLocation.setLatitude(50.41664123535156);
            myLocation.setLongitude(80.26166534423828);
        }

        title = (TextView) findViewById(R.id.text_view_title);

        btnHome = (ImageButton) findViewById(R.id.btn_home);
        btnLocs = (ImageButton) findViewById(R.id.btn_locs);
        btnShop = (ImageButton) findViewById(R.id.btn_shop);
        btnMore = (ImageButton) findViewById(R.id.btn_more);

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextFragment(HomeFragment.newInstance());
            }
        });

        btnLocs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextFragment(LocationFragment.newInstance());
            }
        });

        btnShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextFragment(WebFragment.newInstance());
            }
        });

        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //nextFragment(ShopFragment.newInstance());
            }
        });

        nextFragment(HomeFragment.newInstance());
    }


    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(brGetLocations,
                new IntentFilter(LoadLocationService.BROADCAST_RECEIVER));
    }

    @Override
    public void onPause() {
        unregisterReceiver(brGetLocations);
        super.onPause();
    }

    @Override
    public void nextFragment(Fragment fragment) {

        btnHome.setImageResource(R.drawable.ic_explore_gray_24dp);
        btnLocs.setImageResource(R.drawable.ic_place_gray_24dp);
        btnShop.setImageResource(R.drawable.ic_card_travel_gray_24dp);
        btnMore.setImageResource(R.drawable.ic_more_horiz_gray_24dp);

        if(fragment instanceof HomeFragment) {
            title.setText(R.string.home);
            btnHome.setImageResource(R.drawable.ic_explore_green_24dp);
        } else if(fragment instanceof LocationFragment) {
            title.setText(R.string.maps);
            btnLocs.setImageResource(R.drawable.ic_place_green_24dp);
        } else if(fragment instanceof WebFragment) {
            title.setText(R.string.shop);
            btnShop.setImageResource(R.drawable.ic_card_travel_green_24dp);
        }/* else if(fragment instanceof LocationFragment) {
            title.setText(R.string.more);
            btnLocs.setImageResource(R.drawable.ic_place_green_24dp);
        }*/

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.layout_main,fragment)
                .addToBackStack(TAG)
                .commit();
    }


    @Override
    public void onListFragmentInteraction(Location item) {
        nextFragment(LocationFragment.newInstance());
    }

    @Override
    public android.location.Location getMyLocation() {
        return myLocation;
    }

    @Override
    public List<Location> onGetLocations() {
        return locations;
    }


    /**
     *      BROADCASTRECEIVER GET LOCATIONS
     */
    BroadcastReceiver brGetLocations = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String json = intent.getStringExtra(LoadLocationService.EXTRA_DATA);

            Log.d(TAG,json);
            locations = MobileEnergy.getInstance(MainActivity.this).getGson()
                    .fromJson(json,
                            new TypeToken<List<Location>>() {
                            }.getType());

            nextFragment(LocationFragment.newInstance());
        }
    };

}