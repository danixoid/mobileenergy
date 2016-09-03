package kz.bapps.mobileenergy.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DecimalFormat;

import kz.bapps.mobileenergy.MainActivity;
import kz.bapps.mobileenergy.R;

public class HomeFragment extends Fragment {


    private TextView tvAlert;
    private TextView tvInfo;
    private View mView;
    private ImageView imgView;
    private ImageView imgLogo;
    Animation animBlink;
    Animation animZoom;

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

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.activity_splash, container, false);

        tvAlert = (TextView) mView.findViewById(R.id.alert);
        tvInfo = (TextView) mView.findViewById(R.id.info);


        imgView = (ImageView) mView.findViewById(R.id.imgGrad1);
        imgLogo = (ImageView) mView.findViewById(R.id.imageView);

        // load the animation
        animBlink = AnimationUtils.loadAnimation(mView.getContext().getApplicationContext(), R.anim.blink);
        animZoom = AnimationUtils.loadAnimation(mView.getContext().getApplicationContext(), R.anim.zoom_out);

        imgView.setAnimation(animBlink);

        return mView;
    }

    public class ProgressBarAnimation extends Animation {
        private ProgressBar progressBar;
        private float from;
        private float  to;

        public ProgressBarAnimation(ProgressBar progressBar, float from, float to) {
            super();
            this.progressBar = progressBar;
            this.from = from;
            this.to = to;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            float value = from + (to - from) * interpolatedTime;
            progressBar.setProgress((int) value);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mBatInfoReceiver,
                new IntentFilter(new IntentFilter(Intent.ACTION_BATTERY_CHANGED)));
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(mBatInfoReceiver);
        super.onPause();
    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;
            int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryState = ((float) level / (float) scale) * 100.0f;

            if(isCharging) {

                imgLogo.setAnimation(animZoom);

                if (batteryState >= 100) {
                    tvInfo.setText(R.string.high_battery);
                } else if (usbCharge) {
                    tvInfo.setText(R.string.usb_charge);
                } else if (acCharge) {
                    tvInfo.setText(R.string.ac_charge);
                }
            } else {
                imgLogo.clearAnimation();
                tvInfo.setText("");
            }

            // Error checking that probably isn't needed but I added just in case.
            if(level > -1 && scale > -1) {
                DecimalFormat df = new DecimalFormat("#.##");
                tvAlert.setText(df.format(batteryState) + "%");

                final Drawable drawable;
                if(batteryState < 20) {
                    if(!isCharging) {
                        tvInfo.setText(R.string.low_battery);
                    }

                    drawable = ContextCompat.getDrawable(getActivity(), R.drawable.battery_low);
                } else if(batteryState < 80) {
                    drawable = ContextCompat.getDrawable(getActivity(), R.drawable.battery_mid);
                } else {
                    drawable = ContextCompat.getDrawable(getActivity(), R.drawable.battery_high);
                }

                ProgressBar progressBattery = (ProgressBar) mView.findViewById(R.id.progress_battery);
                progressBattery.setProgressDrawable(drawable);
                ProgressBarAnimation anim = new ProgressBarAnimation(progressBattery, 0, batteryState);
                anim.setDuration(1000);
                progressBattery.startAnimation(anim);

            } else {
                tvAlert.setText("Батарея не распознана");
            }
        }
    };

}
