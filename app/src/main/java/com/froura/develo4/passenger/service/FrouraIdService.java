package com.froura.develo4.passenger.service;

import android.content.ContentValues;

import com.froura.develo4.passenger.config.TaskConfig;
import com.froura.develo4.passenger.tasks.SuperTask;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by KendrickAndrew on 15/03/2018.
 */

public class FrouraIdService extends FirebaseInstanceIdService implements SuperTask.TaskListener {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        TaskConfig.CURRENT_TOKEN = FirebaseInstanceId.getInstance().getToken();
        SuperTask.execute(this, TaskConfig.REGISTER_USER_URL, "refresh_token");
    }

    @Override
    public void onTaskRespond(String json, String id) {

    }

    @Override
    public ContentValues setRequestValues(ContentValues contentValues, String id) {
        contentValues.put("token", TaskConfig.CURRENT_TOKEN);
        contentValues.put("uid", FirebaseAuth.getInstance().getUid());
        return contentValues;
    }
}
