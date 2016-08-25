package kz.bapps.mobileenergy.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import kz.bapps.mobileenergy.MobileEnergy;
import kz.bapps.mobileenergy.R;
import kz.bapps.mobileenergy.draw.DividerItemDecoration;
import kz.bapps.mobileenergy.model.Location;

/**
 * A fragment representing a list of Items.
 * <p>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class LocationFragment extends Fragment {

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

                Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.logo);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 64, 64, false);
                BitmapDescriptor icon = BitmapDescriptorFactory
                        .fromBitmap(resizedBitmap);

                for(Location location : locations) {

                    MarkerOptions marker = new MarkerOptions()
                            .position(new LatLng(location.getLat(),location.getLng()))
                            .title(location.getName())
                            .icon(icon)
                            .snippet(location.getAbout());

                    googleMap.addMarker(marker);
                }

                // For zooming automatically to the location of the marker
                setMyLocation();
            }
        });

        // Set the adapter
        Context context = view.getContext();
        recyclerView = (RecyclerView) view.findViewById(R.id.list);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        recyclerView.setAdapter(new LocationRecyclerViewAdapter(locations, mListener));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(getActivity(), R.drawable.list_item));

        tabSelect(showMap);


        // resize map
        View toolbar = getActivity().findViewById(R.id.toolbar);
        mMapView.setPadding(0,0,0,toolbar.getHeight());

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
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
            locations = mListener.onGetLocations();

            myLocation = mListener.getMyLocation();
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


    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Location item);
        List<Location> onGetLocations();
        android.location.Location getMyLocation();
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


    /**  УСТАНОВИТЬ МОЕ МЕСТОПОЛОЖЕНИЕ
     * =================================================================== */
    private void setMyLocation() {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(mapLatLng).zoom(MAP_ZOOM).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

}
