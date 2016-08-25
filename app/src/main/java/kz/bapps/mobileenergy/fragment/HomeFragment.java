package kz.bapps.mobileenergy.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kz.bapps.mobileenergy.MobileEnergy;
import kz.bapps.mobileenergy.R;
import kz.bapps.mobileenergy.service.LoadLocationService;

public class HomeFragment extends Fragment {

    private OnFragmentInteractionListener mListener;


    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
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
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_home, container, false);

        ContentValues params = new ContentValues();
        params.put("lat","43.228999");
        params.put("lng","76.906483");

        if (MobileEnergy.isNetworkAvailable(getActivity())) {
            LoadLocationService.startActionGetAll(getActivity(),params);
        } else {

        }

        return mView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!MobileEnergy.isGpsEnabled(context)) {
            MobileEnergy.displayPromptForEnablingGPS(context);
        }

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void nextFragment(Fragment fragment);
    }

}
