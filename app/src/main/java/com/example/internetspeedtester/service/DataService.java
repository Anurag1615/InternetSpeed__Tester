package com.example.internetspeedtester.service;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.example.internetspeedtester.MainActivity;
import com.example.internetspeedtester.R;
import com.example.internetspeedtester.utils.NetworkUtil;
import com.example.internetspeedtester.utils.RetrieveData;
import com.example.internetspeedtester.utils.StoredData;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

//import android.support.v7.app.Notification;

public class DataService extends Service {
    public static final String MONTH_DATA = "monthdata";
    public static final String TODAY_DATA = "todaydata";
    public static boolean notification_status = true;
    public static boolean service_status = false;
    static NotificationManager notificationManager;
    Thread dataThread;
    int nid = 5000;
    Notification.Builder notificationBuilder;

    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint("WrongConstant")
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sp_day = getSharedPreferences(TODAY_DATA, 0);
        if (!sp_day.contains("today_date")) {
            Editor editor_day = sp_day.edit();
            editor_day.putString("today_date", new SimpleDateFormat("MMM dd, yyyy").format(Calendar.getInstance().getTime()));
            editor_day.apply();
        }
        if (!service_status) {
            service_status = true;
            this.dataThread = new Thread(new MyThreadClass(startId));
            this.dataThread.setName("showNotification");
            this.dataThread.start();
            if (!StoredData.isSetData) {
                StoredData.setZero();
            }
        }
        return 1;
    }

    public void onDestroy() {
        super.onDestroy();
        service_status = false;
    }

    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void getData() {
        String network_status = NetworkUtil.getConnectivityStatusString(getApplicationContext());
        List<Long> allData = RetrieveData.findData();
        Long mDownload = allData.get(0);
        Long mUpload = allData.get(1);
        long receiveData = mDownload + mUpload;
        storedData(mDownload, mUpload);
        if (notification_status) {
            showNotification(receiveData);
        }
        long wifiData = 0;
        long mobileData = 0;
        long totalData;
        if (network_status.equals("wifi_enabled")) {
            totalData = receiveData;
            wifiData = receiveData;
        } else if (network_status.equals("mobile_enabled")) {
            totalData = receiveData;
            mobileData = receiveData;
        }
        Calendar ca = Calendar.getInstance();
        String tDate = new SimpleDateFormat("MMM dd, yyyy").format(ca.getTime());
        SharedPreferences sp_day = getSharedPreferences(TODAY_DATA, 0);
        String saved_date = sp_day.getString("today_date", "empty");
        if (saved_date.equals(tDate)) {
            long saved_mobileData = sp_day.getLong("MOBILE_DATA", 0);
            long saved_wifiData = sp_day.getLong("WIFI_DATA", 0);
            Editor day_editor = sp_day.edit();
            day_editor.putLong("MOBILE_DATA", mobileData + saved_mobileData);
            day_editor.putLong("WIFI_DATA", wifiData + saved_wifiData);
            day_editor.apply();
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("WIFI_DATA", sp_day.getLong("WIFI_DATA", 0));
            jsonObject.put("MOBILE_DATA", sp_day.getLong("MOBILE_DATA", 0));
            Editor month_editor = getSharedPreferences(MONTH_DATA, 0).edit();
            month_editor.putString(saved_date, jsonObject.toString());
            month_editor.apply();
            Editor day_editor = getSharedPreferences(TODAY_DATA, 0).edit();
            day_editor.clear();
            day_editor.putString("today_date", tDate);
            day_editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("WrongConstant")
    public void showNotification(long receiveData) {
        List<String> connStatus = NetworkUtil.getConnectivityInfo(getApplicationContext());
        notificationManager = (NotificationManager) getSystemService("notification");
        Boolean notification_state = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("notification_state", true);
        String wifi_mobile_details = getWifiMobileData();
        String network_name;

        if ((connStatus.get(0)).equals("wifi_enabled")) {
            network_name = (connStatus.get(1)) + " " + (connStatus.get(2));
        } else if ((connStatus.get(0)).equals("mobile_enabled")) {
            network_name = connStatus.get(1);
        } else {
            network_name = "";
        }
        DecimalFormat df = new DecimalFormat("#.##");
        String speed;
        if (receiveData < PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) {
            speed = "Speed " + ((int) receiveData) + " B/s" + " " + network_name;
        } else if (receiveData < 1048576) {
            speed = "Speed " + (((int) receiveData) / 1024) + " KB/s" + " " + network_name;
        } else {
            speed = "Speed " + df.format(((double) receiveData) / 1048576.0d) + " MB/s" + " " + network_name;
        }

        notificationBuilder = new Notification.Builder(this);
        if (receiveData < PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) {
            try {
                notificationBuilder.setSmallIcon(R.drawable.wkb000);
            } catch (Exception e) {
                Log.e("NotificationHandler", "Error creating notification for speed " + receiveData);
                return;
            }
        } else if (receiveData < 1048576) {
            Drawable myIcon = getResources().getDrawable(R.drawable.wkb000 + ((int) (receiveData / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID)));
            notificationBuilder.setSmallIcon(R.drawable.wkb000 + ((int) (receiveData / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID)));
            if (receiveData >= 1022976) {
                notificationBuilder.setSmallIcon(R.drawable.wmb010 + ((int) (receiveData / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID)));
            }
        } else if (receiveData <= 10485760) {
            notificationBuilder.setSmallIcon(R.drawable.wkb990 + ((int) (0.5d + ((double) (10.0f * (((float) receiveData) / 1048576.0f))))));
        } else if (receiveData <= 103809024) {
            notificationBuilder.setSmallIcon(R.drawable.wmb090 + ((int) (0.5d + ((double) (((float) receiveData) / 1048576.0f)))));
        } else {
            notificationBuilder.setSmallIcon(R.drawable.wmb190);
        }


      //  notificationBuilder.setSmallIcon(R.drawable.wkb000);
        notificationBuilder.setContentTitle(speed)
                .setContentText(wifi_mobile_details)
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))
               // .setAutoCancel(true);
        .setOngoing(true);
        try {
            if (notification_state) {
                notificationManager.notify(this.nid, notificationBuilder.build());
            } else {
                notificationManager.cancel(this.nid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public String getWifiMobileData() {
        String wifi_today;
        String mobile_today;
        SharedPreferences sp_day = getSharedPreferences(TODAY_DATA, 0);
        long saved_mobileData = sp_day.getLong("MOBILE_DATA", 0);
        long saved_wifiData = sp_day.getLong("WIFI_DATA", 0);
        DecimalFormat df = new DecimalFormat("#.##");
        double wifi_data = ((double) saved_wifiData) / 1048576.0d;
        double mobile_data = ((double) saved_mobileData) / 1048576.0d;
        if (wifi_data < 1024.0d) {
            wifi_today = "Wifi: " + df.format(wifi_data) + "MB  ";
        } else {
            wifi_today = "Wifi: " + df.format(wifi_data / 1024.0d) + "GB  ";
        }
        if (mobile_data < 1024.0d) {
            mobile_today = " Mobile: " + df.format(mobile_data) + "MB";
        } else {
            mobile_today = " Mobile: " + df.format(mobile_data / 1024.0d) + "GB";
        }
        return wifi_today + mobile_today;
    }

    public void storedData(Long mDownload, Long mUpload) {
        StoredData.downloadSpeed = mDownload;
        StoredData.uploadSpeed = mUpload;
        if (StoredData.isSetData) {
            StoredData.downloadList.remove(0);
            StoredData.uploadList.remove(0);
            StoredData.downloadList.add(mDownload);
            StoredData.uploadList.add(mUpload);
        }
        StringBuilder append = new StringBuilder().append("test ");
      //  Log.e("storeddata", append.append(String.valueOf(StoredData.downloadList.size())).toString());
    }

    private final class MyThreadClass implements Runnable {
        int service_id;

        MyThreadClass(int service_id) {
            this.service_id = service_id;
        }

        public void run() {
            int i = 0;
            synchronized (this) {
                while (DataService.this.dataThread.getName().equals("showNotification")) {
                    DataService.this.getData();
                    try {
                        wait(1000);
                        i++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

