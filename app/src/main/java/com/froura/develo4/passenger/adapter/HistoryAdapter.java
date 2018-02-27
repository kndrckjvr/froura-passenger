package com.froura.develo4.passenger.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.froura.develo4.passenger.R;
import com.froura.develo4.passenger.object.HistoryObject;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by KendrickAndrew on 26/02/2018.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private Context context;
    private HistoryAdapterListener listener;
    public static ArrayList<HistoryObject> historyList = new ArrayList<>();

    public interface HistoryAdapterListener {
        public void onHistoryClick(ArrayList<HistoryObject> resultList, int position);
    }

    public HistoryAdapter(Context context, HistoryAdapterListener listener) {
        this.context = context;
        this.listener = listener;
        getUserHistory();
    }

    private void getUserHistory() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("history/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                historyList.clear();
                for(DataSnapshot historyIds : dataSnapshot.getChildren()) {
                    String history_id = historyIds.getKey();
                    String driver_id = "";
                    String pickup_name = "";
                    LatLng pickup_location = new LatLng(0,0);
                    String dropoff_name = "";
                    LatLng dropoff_location = new LatLng(0,0);
                    int driver_rating = 0;
                    int timestamp = 0;
                    double lat = 0;
                    double lng = 0;
                    for(DataSnapshot userHistory : historyIds.getChildren()) {
                        switch (userHistory.getKey()) {
                            case "driver_id":
                                driver_id = userHistory.getValue().toString();
                                break;
                            case "dropoff":
                                for(DataSnapshot dropoff: userHistory.getChildren()) {
                                    switch (dropoff.getKey()) {
                                        case "name":
                                            dropoff_name = dropoff.getValue().toString();
                                            break;
                                        case "lat":
                                            lat = Double.parseDouble(dropoff.getValue().toString());
                                            break;
                                        case "lng":
                                            lng = Double.parseDouble(dropoff.getValue().toString());
                                            break;
                                    }
                                }
                                dropoff_location = new LatLng(lat, lng);
                                break;
                            case "pickup":
                                for(DataSnapshot pickup : userHistory.getChildren()) {
                                    switch (pickup.getKey()) {
                                        case "name":
                                            pickup_name = pickup.getValue().toString();
                                            break;
                                        case "lat":
                                            lat = Double.parseDouble(pickup.getValue().toString());
                                            break;
                                        case "lng":
                                            lng = Double.parseDouble(pickup.getValue().toString());
                                            break;
                                    }
                                }
                                pickup_location = new LatLng(lat, lng);
                                break;
                            case "driver_rating":
                                driver_rating = Integer.parseInt(userHistory.getValue().toString());
                                break;
                            case "timestamp":
                                timestamp = Integer.parseInt(userHistory.getValue().toString());
                                break;
                        }
                    }
                    historyList.add(new HistoryObject(history_id, driver_id, dropoff_name, pickup_name, pickup_location, dropoff_location, driver_rating, timestamp));
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = layoutInflater.inflate(R.layout.user_adapter, parent, false);
        ViewHolder mPredictionHolder = new ViewHolder(convertView);
        return mPredictionHolder;
    }

    @Override
    public void onBindViewHolder(HistoryAdapter.ViewHolder holder, final int position) {
        HistoryObject currentHistory = historyList.get(position);
        holder.pickup_txt_vw.setSelected(true);
        holder.dropoff_txt_vw.setSelected(true);
        holder.pickup_txt_vw.setText(currentHistory.getPickupName());
        holder.dropoff_txt_vw.setText(currentHistory.getDropoffName());
        holder.date_txt_vw.setText(currentHistory.getTimestamp());
        holder.rating_txt_vw.setText(currentHistory.getDriver_rating());
        holder.row_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onHistoryClick(historyList, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView pickup_txt_vw;
        public TextView dropoff_txt_vw;
        public TextView date_txt_vw;
        public TextView rating_txt_vw;
        public LinearLayout row_layout;
        public ViewHolder(View itemView) {
            super(itemView);
            pickup_txt_vw = itemView.findViewById(R.id.pickup_txt_vw);
            dropoff_txt_vw = itemView.findViewById(R.id.dropoff_txt_vw);
            date_txt_vw = itemView.findViewById(R.id.date_txt_vw);
            rating_txt_vw = itemView.findViewById(R.id.rating_txt_vw);
            row_layout = itemView.findViewById(R.id.row_layout);
        }
    }
}
