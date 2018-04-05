package com.froura.develo4.passenger.object;

/**
 * Created by KendrickAndrew on 01/04/2018.
 */

public class StateObject {
    private int id;
    private String name;

    public StateObject(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
