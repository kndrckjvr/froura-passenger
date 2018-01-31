package com.froura.develo4.passenger.config;

/**
 * Created by KendrickCosca on 11/27/2017.
 */

public final class TaskConfig {
    public static final String HTTP_HOST = "http://192.168.1.9";
    public static final String DIR_URL = "/froura-web/mobile";
//    public static final String HTTP_HOST = "";
//    public static final String DIR_URL = "";
    public static final String DIR_ACTION_URL = DIR_URL + "/";
    public static final String CHECK_USER_URL = HTTP_HOST + DIR_ACTION_URL + "check_user";
    //public static final String DISTANCE_MATRIX_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";
    public static final String DISTANCE_MATRIX_URL = "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=40.6655101,-73.89188969999998&destinations=40.6905615%2C-73.9976592&key=AIzaSyAxECLx1YGEGZkvS8bvohJcZDDnwWd087w";
}
