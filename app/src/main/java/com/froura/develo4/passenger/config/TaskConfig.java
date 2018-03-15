package com.froura.develo4.passenger.config;

/**
 * Created by KendrickCosca on 11/27/2017.
 */

public final class TaskConfig {
    public static final String HTTP_HOST = "http://frourataxi.com";
    public static final String DIR_URL = "/mobile";
    public static final String DIR_ACTION_URL = DIR_URL + "/";
    public static final String REGISTER_USER_URL = HTTP_HOST + DIR_ACTION_URL + "register_user";
    public static final String CREATE_TAXI_FARE_URL = HTTP_HOST + DIR_ACTION_URL + "create_taxi_fare";
    public static String CURRENT_TOKEN = "";
}
