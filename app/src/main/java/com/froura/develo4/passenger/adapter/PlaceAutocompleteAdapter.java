package com.froura.develo4.passenger.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.froura.develo4.passenger.R;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import java.util.List;

/**
 * Created by KendrickAndrew on 24/01/2018.
 */

public class PlaceAutocompleteAdapter extends RecyclerView.Adapter<PlaceAutocompleteAdapter.ViewHolder> {
    private List<PlaceAutocomplete> mResultList;
    private Context mContext;

    @Override
    public PlaceAutocompleteAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = layoutInflater.inflate(R.layout.search_adapter, parent, false);
        ViewHolder mPredictionHolder = new ViewHolder(convertView);
        return mPredictionHolder;
    }

    @Override
    public void onBindViewHolder(PlaceAutocompleteAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return this.mResultList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView placesTxtVw;

        public ViewHolder(View v) {
            super(v);
            placesTxtVw = v.findViewById(R.id.placesTxtVw);
        }
    }
}
