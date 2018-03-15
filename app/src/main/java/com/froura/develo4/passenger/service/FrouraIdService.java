package com.froura.develo4.passenger.service;

import com.froura.develo4.passenger.config.TaskConfig;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by KendrickAndrew on 15/03/2018.
 */

public class FrouraIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        TaskConfig.CURRENT_TOKEN = FirebaseInstanceId.getInstance().getToken();
    }
}
