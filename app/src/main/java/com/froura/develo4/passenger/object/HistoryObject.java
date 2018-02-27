package com.froura.develo4.passenger.object;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by KendrickAndrew on 26/02/2018.
 */

public class HistoryObject {

    private String history_id;
    private String driver_id;
    private String dropoffName;
    private String pickupName;
    private LatLng pickupLoc;
    private LatLng dropoffLoc;
    private int driver_rating;
    private int timestamp;

    public HistoryObject(String history_id, String driver_id, String dropoffName, String pickupName, LatLng pickupLoc, LatLng dropoffLoc, int driver_rating, int timestamp) {
        this.history_id = history_id;
        this.driver_id = driver_id;
        this.dropoffName = dropoffName;
        this.pickupName = pickupName;
        this.pickupLoc = pickupLoc;
        this.dropoffLoc = dropoffLoc;
        this.driver_rating = driver_rating;
        this.timestamp = timestamp;
    }

    public String getHistory_id() {
        return history_id;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public String getDropoffName() {
        return dropoffName;
    }

    public String getPickupName() {
        return pickupName;
    }

    public LatLng getPickupLoc() {
        return pickupLoc;
    }

    public LatLng getDropoffLoc() {
        return dropoffLoc;
    }

    public int getDriver_rating() {
        return driver_rating;
    }

    public int getTimestamp() {
        return timestamp;
    }
}
