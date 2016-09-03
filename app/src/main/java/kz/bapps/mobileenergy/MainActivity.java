package kz.bapps.mobileenergy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import kz.bapps.mobileenergy.fragment.HomeFragment;
import kz.bapps.mobileenergy.fragment.LocationFragment;
import kz.bapps.mobileenergy.fragment.WebFragment;
import kz.bapps.mobileenergy.model.Location;

public class MainActivity extends AppCompatActivity implements
        LocationFragment.OnListFragmentInteractionListener,
        WebFragment.OnFragmentInteractionListener {

    final static private String TAG = "MainActivity";

    private ImageButton btnHome;
    private ImageButton btnLocs;
    private ImageButton btnShop;
    private ImageButton btnSite;
    private ImageButton btnMore;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);


        if (!MobileEnergy.isGpsEnabled(this)) {
            MobileEnergy.displayPromptForEnablingGPS(this);
        }

        /**
         *
         */
        title = (TextView) findViewById(R.id.text_view_title);

        btnHome = (ImageButton) findViewById(R.id.btn_home);
        btnLocs = (ImageButton) findViewById(R.id.btn_locs);
        btnSite = (ImageButton) findViewById(R.id.btn_site);
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

        btnSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextFragment(WebFragment.newInstance());
            }
        });

        btnShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //nextFragment(WebFragment.newInstance());
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

    public void nextFragment(Fragment fragment) {

        btnHome.setImageResource(R.drawable.ic_flash_gray);
        btnLocs.setImageResource(R.drawable.ic_marker_gray);
        btnSite.setImageResource(R.drawable.ic_public_gray_24dp);
        btnShop.setImageResource(R.drawable.ic_shop_gray);
        btnMore.setImageResource(R.drawable.ic_more_gray);

        if(fragment instanceof HomeFragment) {
            title.setText(R.string.app_name);
            btnHome.setImageResource(R.drawable.ic_flash_green);
        } else if(fragment instanceof LocationFragment) {
            title.setText(R.string.locations);
            btnLocs.setImageResource(R.drawable.ic_marker_green);
        } else if(fragment instanceof WebFragment) {
            title.setText(R.string.site);
            btnSite.setImageResource(R.drawable.ic_public_green_24dp);
        }/* else if(fragment instanceof WebFragment) {
            title.setText(R.string.shop);
            btnShop.setImageResource(R.drawable.ic_shop_green);
        } else if(fragment instanceof LocationFragment) {
            title.setText(R.string.more);
            btnLocs.setImageResource(R.drawable.ic_more_green);
        }*/

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.layout_main,fragment)
//                .addToBackStack(TAG)
                .commit();
    }

    @Override
    public void openDetail(Location location) {

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_LOCATION,location);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Fragment myFragment = getVisibleFragment();

        if(myFragment != null && myFragment instanceof HomeFragment) {
            super.onBackPressed();
        } else {
            nextFragment(HomeFragment.newInstance());
        }
    }

    public Fragment getVisibleFragment(){
        FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if(fragments != null){
            for(Fragment fragment : fragments){
                if(fragment != null && fragment.isVisible())
                    return fragment;
            }
        }
        return null;
    }

}
