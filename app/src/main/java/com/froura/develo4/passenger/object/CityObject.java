package com.froura.develo4.passenger.object;

/**
 * Created by KendrickAndrew on 01/04/2018.
 */

public class CityObject {
    private int id;
    private String name;
    private int state_id;
    private String price;

    public CityObject(int id, String name, int state_id, String price) {
        this.id = id;
        this.name = name;
        this.state_id = state_id;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getState_id() {
        return state_id;
    }

    public String getPrice() {
        return price;
    }
}
