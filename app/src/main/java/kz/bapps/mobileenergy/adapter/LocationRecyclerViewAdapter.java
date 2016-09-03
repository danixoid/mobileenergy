package kz.bapps.mobileenergy.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import kz.bapps.mobileenergy.JSONParser;
import kz.bapps.mobileenergy.R;
import kz.bapps.mobileenergy.fragment.LocationFragment.OnListFragmentInteractionListener;
import kz.bapps.mobileenergy.model.Location;

import java.util.List;

public class LocationRecyclerViewAdapter extends RecyclerView.Adapter<LocationRecyclerViewAdapter.ViewHolder> {

    private final List<Location> mValues;
    private final OnListFragmentInteractionListener mListener;

    public LocationRecyclerViewAdapter(List<Location> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_location_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        Context context = holder.mView.getContext();


        holder.mItem = mValues.get(position);
        holder.mNameView.setText(mValues.get(position).getName());
        holder.mAddressView.setText(mValues.get(position).getAddress());

        Picasso.with(context)
                .load(JSONParser.URL_ROOT + "location/"
                        + Integer.toString(holder.mItem.getId()) + "?photo=logo")
                .placeholder(R.drawable.logo) // optional
                .error(R.drawable.logo)
                .into(holder.mLogoView);


        @SuppressLint("DefaultLocale")
        String distance = holder.mItem.getDistance() >= 1
                ? String.format("%.2f", holder.mItem.getDistance()) + "км"
                : String.format("%.0f", holder.mItem.getDistance() * 1000) + "м";

        holder.mDistanceView.setText(context.getString(R.string.distance) + ": "
                + distance);
        holder.mSpotsView.setText(context.getString(R.string.spots) + ": "
                + Integer.toString(holder.mItem.getSpots()));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.openDetail(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNameView;
        public final TextView mAddressView;
        public final TextView mDistanceView;
        public final TextView mSpotsView;
        public final ImageView mLogoView;
        public Location mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.name);
            mAddressView = (TextView) view.findViewById(R.id.address);
            mDistanceView = (TextView) view.findViewById(R.id.distance);
            mSpotsView = (TextView) view.findViewById(R.id.spots);
            mLogoView = (ImageView) view.findViewById(R.id.imgLogo);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mAddressView.getText() + "'";
        }
    }
}
