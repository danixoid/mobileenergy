package kz.bapps.mobileenergy;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import kz.bapps.mobileenergy.model.Location;

public class DetailActivity extends FragmentActivity implements
        OnMapReadyCallback {

    final static private String TAG = "DetailActivity";
    final static public String EXTRA_LOCATION = "kz.bapps.mobileenergy.details.LOCATION";

    private GoogleMap mMap;
    private Location location;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);

        location = (Location) getIntent().getSerializableExtra(EXTRA_LOCATION);

        if(location != null) {

            TextView tvName = (TextView) findViewById(R.id.name);
            TextView tvAddress = (TextView) findViewById(R.id.address);
            TextView tvDistance = (TextView) findViewById(R.id.distance);
            TextView tvSpots = (TextView) findViewById(R.id.spots);
            TextView tvAbout = (TextView) findViewById(R.id.about);
            ImageView imgLogo = (ImageView) findViewById(R.id.imgLogo);

            tvName.setText(location.getName());
            tvAddress.setText(location.getAddress());
            tvAbout.setText(location.getAbout());

            @SuppressLint("DefaultLocale")
            String distance = String.format("%.3f", location.getDistance());

            tvDistance.setText(getString(R.string.distance) + ": "
                    + distance);
            tvSpots.setText(getString(R.string.spots) + ": "
                    + Integer.toString(location.getSpots()));

            Picasso.with(this)
                    .load(JSONParser.URL_ROOT + "location/"
                            + Integer.toString(location.getId()) + "?photo=logo")
                    .placeholder(R.drawable.logo) // optional
                    .error(R.drawable.logo)
                    .into(imgLogo);

        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.logo);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 64, 64, false);
        BitmapDescriptor icon = BitmapDescriptorFactory
                .fromBitmap(resizedBitmap);

        LatLng myPlace = new LatLng(location.getLat(), location.getLng());

        final Marker marker = mMap.addMarker(new MarkerOptions()
                .position(myPlace)
                .title(location.getName())
                .icon(icon)
                .snippet(location.getAbout()));

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {

                LayoutInflater inflater = getLayoutInflater();
                ImageView imagePhoto = (ImageView) inflater.inflate(R.layout.gallery_item, null);

                Picasso.with(inflater.getContext())
                        .load(JSONParser.URL_ROOT + "location/"
                                + Integer.toString(location.getId()) + "?photo=image")
                        .placeholder(android.R.drawable.sym_def_app_icon) // optional
                        .error(android.R.drawable.sym_def_app_icon)
                        .into(imagePhoto);

                return imagePhoto;
            }

            @Override
            public View getInfoContents(Marker arg0) {
                return null;
            }
        });

        CameraPosition cameraPosition = new CameraPosition.Builder().target(myPlace).zoom(16).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

}
