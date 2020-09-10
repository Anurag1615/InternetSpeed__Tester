package com.example.internetspeedtester.fragment;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.internetspeedtester.R;
import com.example.internetspeedtester.adapter.DataAdapter;
import com.example.internetspeedtester.service.DataService;
import com.example.internetspeedtester.utils.DataInfo;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

//import androidx.fragment.app.Fragment;




public class MonthFragment extends Fragment {
    static final String GIGABYTE = " GB";
    static final String MEGABYTE = " MB";
    private final SimpleDateFormat SDF = new SimpleDateFormat("MMM dd, yyyy");
    private DataAdapter dataAdapter;
    private Thread dataUpdate;
    private final DecimalFormat df = new DecimalFormat("#.##");
    private TextView mTotal;
    List<DataInfo> monthData;
    private RecyclerView recList;
    private TextView tTotal;
    private String today_date = null;
    private double today_mobile = 0.0d;
    private double today_wifi = 0.0d;
    private double total_mobile;
    private double total_wifi;
    private Handler vHandler = new Handler();
    private TextView wTotal;
   // private AdView mAdView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_month, container, false);
      //  mAdView = (AdView) rootView.findViewById(R.id.adView);
       // AdRequest adRequest = new AdRequest.Builder().build();
     //   mAdView.loadAd(adRequest);
        wTotal = (TextView) rootView.findViewById(R.id.id_wifi);
        mTotal = (TextView) rootView.findViewById(R.id.id_mobile);
        tTotal = (TextView) rootView.findViewById(R.id.id_total);
        recList = (RecyclerView) rootView.findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.getItemAnimator().setChangeDuration(0);
        monthData = createList(30);
       dataAdapter = new DataAdapter(getActivity(), monthData);
        //dataAdapter = new DataAdapter(getActivity(), monthData);
        recList.setAdapter(dataAdapter);
        totalData();
        clearExtraData();
        liveData();
        return rootView;
    }

    public void liveData() {
        dataUpdate = new Thread(new Runnable() {
            public void run() {
                while (!dataUpdate.getName().equals("stopped")) {
                    final String temp_today = SDF.format(Calendar.getInstance().getTime());
                    vHandler.post(new Runnable() {
                        public void run() {
                            if (temp_today.equals(today_date)) {
                                monthData.set(0, todayData());
                                dataAdapter.notifyItemChanged(0);
                                Log.e("datechange", temp_today);
                            } else {
                                today_wifi = 0.0d;
                                today_mobile = 0.0d;
                                monthData = createList(30);
                                dataAdapter.itemdata = monthData;
                                dataAdapter.notifyDataSetChanged();
                                monthData.set(0, todayData());
                                dataAdapter.notifyItemChanged(0);
                            }
                            totalData();
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        dataUpdate.setName("started");
        dataUpdate.start();
    }

    @SuppressLint("WrongConstant")
    public List<DataInfo> createList(int size) {
        List<DataInfo> result = new ArrayList();
        total_wifi = 0.0d;
        total_mobile = 0.0d;
        String wifi = "0";
        String mobile = "0";
        String total = "0";
        SharedPreferences sp_month = getActivity().getSharedPreferences(DataService.MONTH_DATA, 0);
        for (int i = 1; i <= size; i++) {
            if (i == 1) {
                result.add(todayData());
            } else {
                Calendar calendar = Calendar.getInstance();
                calendar.add(5, 1 - i);
                String mDate = SDF.format(calendar.getTime());
                List<String> allData = new ArrayList();
                if (sp_month.contains(mDate)) {
                    try {
                        JSONObject jSONObject = new JSONObject(sp_month.getString(mDate, null));
                        wifi = jSONObject.getString("WIFI_DATA");
                        double wTemp = ((double) Long.parseLong(wifi)) / 1048576.0d;
                        double mTemp = ((double) Long.parseLong(jSONObject.getString("MOBILE_DATA"))) / 1048576.0d;
                        allData = dataFormate(wTemp, mTemp, wTemp + mTemp);
                        total_wifi += wTemp;
                        total_mobile += mTemp;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    allData = dataFormate(0.0d, 0.0d, 0.0d);
                }
                DataInfo dataInfo = new DataInfo();
                dataInfo.date = mDate;
                dataInfo.wifi = allData.get(0);
                dataInfo.mobile = allData.get(1);
                dataInfo.total = allData.get(2);
                result.add(dataInfo);
            }
        }
        return result;
    }

    public DataInfo todayData() {
        List<DataInfo> listToday = new ArrayList();
        today_date = SDF.format(Calendar.getInstance().getTime());
        double wTemp = 0.0d;
        double mTemp = 0.0d;
        try {
            SharedPreferences sp = getActivity().getSharedPreferences(DataService.TODAY_DATA, 0);
            wTemp = ((double) sp.getLong("WIFI_DATA", 0)) / 1048576.0d;
            mTemp = ((double) sp.getLong("MOBILE_DATA", 0)) / 1048576.0d;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("crashed", "hello");
        }
        List<String> allData = dataFormate(wTemp, mTemp, wTemp + mTemp);
        total_wifi += wTemp - today_wifi;
        total_mobile += mTemp - today_mobile;
        today_wifi = wTemp;
        today_mobile = mTemp;
        DataInfo dataInfo = new DataInfo();
        dataInfo.date = "Today";
        dataInfo.wifi =  allData.get(0);
        dataInfo.mobile =  allData.get(1);
        dataInfo.total =  allData.get(2);
        listToday.add(dataInfo);
        return dataInfo;
    }

    public void totalData() {
        List<String> total = dataFormate(total_wifi, total_mobile, total_wifi + total_mobile);
        wTotal.setText(total.get(0));
        mTotal.setText(total.get(1));
        tTotal.setText( total.get(2));
    }

    public List<String> dataFormate(double wifi, double mobile, double total) {
        List<String> allData = new ArrayList();
        if (wifi < 1024.0d) {
            allData.add(df.format(wifi) + MEGABYTE);
        } else {
            allData.add(df.format(wifi / 1024.0d) + GIGABYTE);
        }
        if (mobile < 1024.0d) {
            allData.add(df.format(mobile) + MEGABYTE);
        } else {
            allData.add(df.format(mobile / 1024.0d) + GIGABYTE);
        }
        if (total < 1024.0d) {
            allData.add(df.format(total) + MEGABYTE);
        } else {
            allData.add(df.format(total / 1024.0d) + GIGABYTE);
        }
        return allData;
    }

    void clearExtraData() {
        SharedPreferences sp_month = getActivity().getSharedPreferences(DataService.MONTH_DATA, 0);
        Editor editor = sp_month.edit();
        for (int i = 40; i <= 1000; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(5, 1 - i);
            String mDate = SDF.format(calendar.getTime());
            if (sp_month.contains(mDate)) {
                editor.remove(mDate);
            }
        }
        editor.apply();
    }

    public void sharedPref() {
        Editor editor = getActivity().getSharedPreferences(DataService.MONTH_DATA, 0).edit();
        editor.clear();
        for (int i = 1; i <= 30; i++) {
            if (i % 2 != 1) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(5, 1 - i);
                try {
                    String tDate = SDF.format(calendar.getTime());
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("WIFI_DATA", 10000906 * i);
                    jsonObject.put("MOBILE_DATA", 40005002 * i);
                    editor.putString(tDate, jsonObject.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        editor.apply();
    }

    public void onPause() {
        super.onPause();
        dataUpdate.setName("stopped");
        Log.e("astatus", "onPause");
    }

    public void onResume() {
        super.onResume();
        DataService.notification_status = true;
        dataUpdate.setName("started");
        Log.e("astatus", "onResume");
        Log.e("astatus", dataUpdate.getState().toString());
        if (!dataUpdate.isAlive()) {
            liveData();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        Log.e("astatus", "onDestroy");
    }

    public void onStart() {
        super.onStart();
        Log.e("astatus", "onStart");
    }

    public void onStop() {
        super.onStop();
        Log.e("astatus", "onStop");
    }

    @SuppressLint("WrongConstant")
    private boolean isOnline() {
        try {
            return ((ConnectivityManager) getActivity().getSystemService("connectivity")).getActiveNetworkInfo().isConnected();
        } catch (Exception e) {
            return false;
        }
    }
}
