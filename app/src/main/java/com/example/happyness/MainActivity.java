package com.example.happyness;

import android.app.ActionBar;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity {

    ImageView imageView_jbzd;
    ImageView imageView_chamsko;
    ImageView imageView_fabrykamemow;
    ImageView imageView_kwejk;
    ImageView imageView_repostuj;
    TextView network_lost;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(actionBar.getDisplayOptions()
                | ActionBar.DISPLAY_SHOW_CUSTOM);
        ImageView imageView = new ImageView(actionBar.getThemedContext());
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setImageResource(R.drawable.ic_launcher_x50);
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT, Gravity.LEFT
                | Gravity.CENTER_VERTICAL);
        layoutParams.rightMargin = 40;
        imageView.setLayoutParams(layoutParams);
        actionBar.setCustomView(imageView);
        getSupportActionBar().setTitle("Happyness");
        setContentView(R.layout.activity_main);

        imageView_jbzd = findViewById(R.id.home_jbzd);
        imageView_chamsko = findViewById(R.id.home_chamsko);
        imageView_fabrykamemow = findViewById(R.id.home_fabrykamemow);
        imageView_kwejk = findViewById(R.id.home_kwejk);
        imageView_repostuj = findViewById(R.id.home_repostuj);
        network_lost = findViewById(R.id.network_lost);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                haveNetwork();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        haveNetwork();
            imageView_jbzd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(haveNetwork()) {
                        Intent intent = new Intent(MainActivity.this, JbzdActivity.class);
                        startActivity(intent);
                    }
                }
            });

            imageView_chamsko.setOnClickListener(e -> {
                if(haveNetwork()) {
                    Intent intent = new Intent(this, ChamskoActivity.class);
                    startActivity(intent);
                }
            });

            imageView_fabrykamemow.setOnClickListener(e -> {
                if(haveNetwork()) {
                    Intent intent = new Intent(this, FabrykamemowActivity.class);
                    startActivity(intent);
                }
            });

            imageView_kwejk.setOnClickListener(e -> {
                if(haveNetwork()) {
                    Intent intent = new Intent(this, KwejkActivity.class);
                    startActivity(intent);
                }
            });
/*
        imageView_repostuj.setOnClickListener(e -> {
            if(haveNetwork()) {
                Intent intent = new Intent(this, RepostujActivity.class);
                startActivity(intent);
            }
        });

 */

    }

    private boolean haveNetwork(){
        boolean have_WIFI = false;
        boolean have_MobileData = false;

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfos=connectivityManager.getAllNetworkInfo();

        for(NetworkInfo info:networkInfos){
            if(info.getTypeName().equalsIgnoreCase("WIFI")){
                if(info.isConnected()) {
                    have_WIFI = true;
                }
            }
            if(info.getTypeName().equalsIgnoreCase("MOBILE")){
                if(info.isConnected()) {
                    have_WIFI = true;
                }
            }
        }

        if(have_WIFI || have_MobileData){
            network_lost.setVisibility(View.INVISIBLE);
        }else{
            network_lost.setVisibility(View.VISIBLE);
        }

        return have_MobileData || have_WIFI;
    }
}