package com.example.internetspeedtester.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.internetspeedtester.service.DataService;


public class DataReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (!DataService.service_status) {
            context.startService(new Intent(context, DataService.class));
        }
    }
}
