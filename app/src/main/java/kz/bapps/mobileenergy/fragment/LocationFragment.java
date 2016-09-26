package kz.bapps.mobileenergy.fragment;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kz.bapps.mobileenergy.MobileEnergy;
import kz.bapps.mobileenergy.R;
import kz.bapps.mobileenergy.adapter.LocationRecyclerViewAdapter;
import kz.bapps.mobileenergy.draw.DividerItemDecoration;
import kz.bapps.mobileenergy.model.Location;
import kz.bapps.mobileenergy.service.LoadLocationService;

/**
 * A fragment representing a list of Items.
 * <p>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class LocationFragment extends Fragment implements
        LocationListener, GoogleMap.OnMarkerClickListener {

    private static final String TAG = "LocationFragment";
    final private float MAP_ZOOM = 14;

    private OnListFragmentInteractionListener mListener;
    private List<Location> locations = new ArrayList<>();
    MapView mMapView;
    private GoogleMap googleMap;
    private RecyclerView recyclerView;
    private Button btnMap;
    private Button btnLocations;
    private LatLng mapLatLng;
    private Map<Marker, Location> haspMap = new HashMap<>();
    private android.location.Location myLocation;

    private boolean showMap = false;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LocationFragment() {
    }

    public static LocationFragment newInstance() {
        LocationFragment fragment = new LocationFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location_list, container, false);



        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationManager locationManager = (LocationManager) getActivity()
                    .getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, false);

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 1f, this);
            myLocation = locationManager.getLastKnownLocation(provider);

            if (myLocation == null) {
                myLocation = new android.location.Location(provider);
                myLocation.setLatitude(43.228999);
                myLocation.setLongitude(76.906483);
            }
        }



        btnMap = (Button) view.findViewById(R.id.btn_maps);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tabSelect(true);
            }
        });

        btnLocations = (Button) view.findViewById(R.id.btn_locations);
        btnLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tabSelect(false);
            }
        });


        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // For showing a move to my location button
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }

                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);

                // For zooming automatically to the location of the marker
                setMyLocation();
                googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                    @Override
                    public boolean onMyLocationButtonClick() {
                        setMyLocation();
                        return true;
                    }
                });

                googleMap.setOnMarkerClickListener(LocationFragment.this);
            }
        });

        // Set the adapter
        Context context = view.getContext();
        recyclerView = (RecyclerView) view.findViewById(R.id.list);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        recyclerView.addItemDecoration(
                new DividerItemDecoration(getActivity(), R.drawable.list_item));

        tabSelect(showMap);


        // resize map
        View toolbar = getActivity().findViewById(R.id.toolbar);
        mMapView.setPadding(0,0,0,toolbar.getHeight());
        recyclerView.setPadding(0,0,0,toolbar.getHeight());
        startGetLocations();
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        getActivity().registerReceiver(brGetLocations,
                new IntentFilter(LoadLocationService.BROADCAST_RECEIVER));
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        getActivity().unregisterReceiver(brGetLocations);
        super.onPause();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!MobileEnergy.isGpsEnabled(context)) {
            MobileEnergy.displayPromptForEnablingGPS(context);
        }

        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;

            if (mapLatLng == null && myLocation != null) {
                mapLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            }

            for (Location location : locations) {
                Log.d(TAG,location.getName() + " message");
            }

        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        Location location = haspMap.get(marker);

        if(mListener != null && location != null) {
            mListener.openDetail(location);
        }

        return false;
    }


    public interface OnListFragmentInteractionListener {
        void openDetail(Location location);
    }


    private void tabSelect(boolean showMap) {

        this.showMap = showMap;

        if(showMap) {
            mMapView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            btnMap.setBackgroundColor(Color.argb(255,0,152,70));
            btnLocations.setBackgroundColor(Color.argb(255,185,185,185));
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            mMapView.setVisibility(View.GONE);
            btnLocations.setBackgroundColor(Color.argb(255,0,152,70));
            btnMap.setBackgroundColor(Color.argb(255,185,185,185));
        }
    }


    /**
     *      BROADCASTRECEIVER GET LOCATIONS
     */
    BroadcastReceiver brGetLocations = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String json = intent.getStringExtra(LoadLocationService.EXTRA_DATA);

            locations = MobileEnergy.getInstance(getActivity()).getGson()
                    .fromJson(json,
                            new TypeToken<List<Location>>() {
                            }.getType());

            recyclerView.setAdapter(new LocationRecyclerViewAdapter(locations, mListener));

            if (showMap) {
                Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 102, 102, false);
                BitmapDescriptor icon = BitmapDescriptorFactory
                        .fromBitmap(resizedBitmap);

                for (Location location : locations) {

                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(new LatLng(location.getLat(), location.getLng()))
                            .title(location.getName())
                            .icon(icon)
                            .snippet(location.getAbout());

                    Marker marker = googleMap.addMarker(markerOptions);

                    haspMap.put(marker, location);
                }
            }

        }
    };


    /**  УСТАНОВИТЬ МОЕ МЕСТОПОЛОЖЕНИЕ
     * =================================================================== */
    private void setMyLocation() {

        if (myLocation != null) {
            mapLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        }

        CameraPosition cameraPosition = new CameraPosition.Builder().target(mapLatLng).zoom(MAP_ZOOM).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }



    private void startGetLocations() {
        if (getActivity() == null) return;
        if (MobileEnergy.isNetworkAvailable(getActivity())) {

            ContentValues params = new ContentValues();

            if(myLocation != null) {
                params.put("lat",Double.toString(myLocation.getLatitude()));
                params.put("lng",Double.toString(myLocation.getLongitude()));
            } else {
                params.put("lat","43.228999");
                params.put("lng","76.906483");
            }

            LoadLocationService.startActionGetAll(getActivity(),params);
        }
    }

    @Override
    public void onLocationChanged(android.location.Location location) {
        myLocation = location;
        startGetLocations();
//        if(showMap) {
//            setMyLocation();
//        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        startGetLocations();
    }

    @Override
    public void onProviderDisabled(String s) {
        MobileEnergy.displayPromptForEnablingGPS(getActivity());
    }
}
