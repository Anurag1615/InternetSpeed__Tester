package com.example.internetspeedtester;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.internetspeedtester.fragment.MonthFragment;
import com.example.internetspeedtester.service.DataService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!DataService.service_status) {
            startService(new Intent(this, DataService.class));
        }


        MonthFragment fragment = new MonthFragment();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();



    }

    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menux, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setting: {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("All Data Will Be Clear!").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @SuppressLint("WrongConstant")
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences sp_today = getApplicationContext().getSharedPreferences(DataService.TODAY_DATA, 0);
                        SharedPreferences sp_month = getApplicationContext().getSharedPreferences(DataService.MONTH_DATA, 0);
                        SharedPreferences.Editor editor = sp_today.edit();
                        SharedPreferences.Editor edito2 = sp_month.edit();
                        editor.clear();
                        edito2.clear();
                        editor.apply();
                        edito2.apply();



                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.setTitle("Do You Want To Reset Data?");
                alert.show();

            }



        }
        return super.onOptionsItemSelected(item);
    }



    }


