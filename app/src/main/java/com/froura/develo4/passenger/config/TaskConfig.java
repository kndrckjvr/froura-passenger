package com.froura.develo4.passenger.config;

/**
 * Created by KendrickCosca on 11/27/2017.
 */

public final class TaskConfig {
    public static final String HTTP_HOST = "http://frourataxi.com";
    public static final String DIR_URL = "/mobile";
    public static final String DIR_ACTION_URL = DIR_URL + "/";
    public static final String CHECK_CONNECTION_URL = HTTP_HOST + DIR_ACTION_URL + "check_connect";
    public static final String REGISTER_USER_URL = HTTP_HOST + DIR_ACTION_URL + "register_user";
    public static final String CREATE_TAXI_FARE_URL = HTTP_HOST + DIR_ACTION_URL + "create_taxi_fare";
    public static final String SEND_NOTIFICATION = HTTP_HOST + DIR_ACTION_URL + "send_notification";
    public static final String SIGNOUT_URL = HTTP_HOST + DIR_ACTION_URL + "sign_out";
    public static final String SEND_FEEDBACK = HTTP_HOST + DIR_ACTION_URL + "send_feedback";
    public static final String CHECK_TARIFF_URL = HTTP_HOST + DIR_ACTION_URL + "check_tariff";
    public static final String RESERVATION_URL = HTTP_HOST + "/execute/addreserve";
    public static final String RESERVATION_LIST_URL = HTTP_HOST + DIR_ACTION_URL + "get_reservations";
    public static final String TAG = "FROURA_LOG_TAG";
}
