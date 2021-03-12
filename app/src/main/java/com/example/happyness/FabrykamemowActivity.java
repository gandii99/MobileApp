package com.example.happyness;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dmax.dialog.SpotsDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FabrykamemowActivity extends AppCompatActivity implements MyAdapter.OnNoteListener{

    private List<String> memeski = new ArrayList();
    private List<String> tytul = new ArrayList();
    private List<Integer> plusy = new ArrayList();
    private List<Integer> scrolowanie = new ArrayList();
    int strona = 1;

    TextView textView;
    RecyclerView recyclerView;
    ImageButton previous_page, next_page, reload;
    EditText number_page;
    TextView network_lost;
    SwipeRefreshLayout swipeRefreshLayout;

    private static final int PERMISSION_REQUEST_CODE = 1000;
    private boolean czy_pobrac_tytyl = false;
    private boolean czy_odswiezyc = true;
    private boolean skonczone_pierwszy_raz = true;
    private boolean skonczone = true;
    private boolean nie_chce_mi_sie_tego_robic_wiec_robie_bezsensowna_zmienna = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fabrykamemow);

        recyclerView = findViewById(R.id.recycle_fabrykamemow);
        previous_page = findViewById(R.id.previous_page_fabrykamemow);
        next_page = findViewById(R.id.next_page_fabrykamemow);
        reload = findViewById(R.id.reload_fabrykamemow);
        number_page = findViewById(R.id.number_page_fabrykamemow);
        network_lost = findViewById(R.id.network_lost);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh_fabrykamemow);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                haveNetwork();
                if(haveNetwork()) {
                    tytul.clear();
                    memeski.clear();
                    plusy.clear();
                    Toast.makeText(FabrykamemowActivity.this, "Reload", Toast.LENGTH_SHORT).show();
                    connect_server_fabrykamemow(strona);
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        if(haveNetwork()){
            connect_server_fabrykamemow(1);
        }
        reload.setOnClickListener(e->{
            if(haveNetwork()) {
                recyclerView.stopScroll();
                tytul.clear();
                memeski.clear();
                plusy.clear();
                Toast.makeText(FabrykamemowActivity.this, "Reload", Toast.LENGTH_SHORT).show();
                connect_server_fabrykamemow(strona);
            }
        });

        previous_page.setOnClickListener(e->{
            if(haveNetwork()) {
                recyclerView.stopScroll();
                if (strona <= 1) {
                    Toast.makeText(FabrykamemowActivity.this, "This is 1 page!", Toast.LENGTH_SHORT).show();
                } else {
                    tytul.clear();
                    memeski.clear();
                    plusy.clear();
                    strona--;
                    connect_server_fabrykamemow(strona);
                    number_page.setHint(String.valueOf(strona));
                    number_page.setText("");
                }
            }
        });

        next_page.setOnClickListener(e->{
            if(haveNetwork()) {
                recyclerView.stopScroll();
                if (number_page.getText().toString() != null && !number_page.getText().toString().isEmpty()) {
                    strona = Integer.parseInt(number_page.getText().toString());
                } else {
                    strona++;
                }
                tytul.clear();
                memeski.clear();
                plusy.clear();
                connect_server_fabrykamemow(strona);
                number_page.setHint(String.valueOf(strona));
                number_page.setText("");
            }
        });

    }


    private void connect_server_fabrykamemow(int strona) {
        OkHttpClient client = new OkHttpClient();
        String address = "https://fabrykamemow.pl/page/";
        int iterator = 1;
        // for (int j = ((10*strona)-9); j <= (10*strona); j++){
        int how_many = memeski.size();
        // String url = address + j;
        String url = address + strona;
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String myResponse = response.body().string();

                    FabrykamemowActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            select(myResponse);
                            scrolowanie.add(memeski.size());
                        }
                    });
                }
            }
        });
/*
        try {
            Thread.sleep(300);
        }
        catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

 */

    }

    private void select(String caly_tekst) {
        if (skonczone == true || skonczone_pierwszy_raz == true){
            String mem = "";
            char[] calosc = caly_tekst.toCharArray();
            int i = 0;
            while (i < caly_tekst.length()) {

                if (calosc[i] == 's' && calosc[i + 1] == 'r' && calosc[i + 2] == 'c' && calosc[i + 3] == '=' && calosc[i + 4] == '"' && calosc[i + 5] == '/' && calosc[i + 6] == 'u' && calosc[i + 7] == 'i') {
                    i = i + 5;
                    while (calosc[i] != '"') {
                        mem = mem + calosc[i];
                        i++;
                    }
                    if (mem.contains("uimages")) {
                        memeski.add("https://fabrykamemow.pl"+mem);
                        czy_pobrac_tytyl = true;
                    } else {
                        czy_pobrac_tytyl = false;
                    }
                    mem = "";

                    if (czy_pobrac_tytyl == true) {

                        while (i < caly_tekst.length()) {
                            if (calosc[i] == 'a' && calosc[i + 1] == 'l' && calosc[i + 2] == 't' && calosc[i + 3] == '=') {
                                break;
                            }
                            i++;
                        }
                        i = i + 5;

                        while (calosc[i] != '"') {
                            mem = mem + calosc[i];
                            i++;
                        }
                        tytul.add(mem);
                        plusy.add(1);
                        mem = "";
                    }
                }
                i++;
            }
            skonczone_pierwszy_raz = false;
            skonczone = true;
            putToRecyclerView();
        }
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
                    have_MobileData = true;
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

    private void putToRecyclerView(){
        MyAdapter myAdapter = new MyAdapter(FabrykamemowActivity.this, tytul, memeski, plusy, FabrykamemowActivity.this);
        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(FabrykamemowActivity.this));
    }

    @Override
    public void onNoteClick(int position) {
        if(ActivityCompat.checkSelfPermission(FabrykamemowActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(FabrykamemowActivity.this, "You should grant permission!", Toast.LENGTH_SHORT).show();
            requestPermissions(new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE }, PERMISSION_REQUEST_CODE);
            return;
        }else{
            AlertDialog dialog = new SpotsDialog(FabrykamemowActivity.this);
            dialog.show();
            dialog.setMessage("Downloading...");

            String fileName = UUID.randomUUID().toString()+".jpg";
            Picasso.get().load(memeski.get(position)).into(new SaveImageHelper(getBaseContext(), dialog, getApplicationContext().getContentResolver(), tytul.get(position), "Desc"));
        }
        Toast.makeText(this,tytul.get(position), Toast.LENGTH_SHORT).show();
    }

}
