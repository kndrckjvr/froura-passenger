package com.froura.develo4.passenger.config;

/**
 * Created by KendrickCosca on 11/27/2017.
 */

public final class TaskConfig {
    public static final String HTTP_HOST = "http://192.168.1.7";
    public static final String DIR_URL = "/froura-web/public/mobile";
    public static final String DIR_ACTION_URL = DIR_URL + "/";
    public static final String REGISTER_USER_URL = HTTP_HOST + DIR_ACTION_URL + "register_user";
    public static final String CREATE_TAXI_FARE_URL = HTTP_HOST + DIR_ACTION_URL + "create_taxi_fare";
    public static final String CHECK_CONNECTION_URL = HTTP_HOST + DIR_ACTION_URL + "connected";
}
