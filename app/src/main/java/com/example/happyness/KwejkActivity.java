package com.example.happyness;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
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

public class KwejkActivity extends AppCompatActivity implements MyAdapter.OnNoteListener, AdapterView.OnItemSelectedListener{

    private List<String> memeski = new ArrayList();
    private List<String> tytul = new ArrayList();
    private List<Integer> plusy = new ArrayList();
    private List<Integer> scrolowanie = new ArrayList();
    private List<String> kategorie = new ArrayList();
    private String aktualna_kategoria = "";
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
    private boolean first_connect = true;
    private int current_page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kwejk);

        recyclerView = findViewById(R.id.recycle_kwejk);
        previous_page = findViewById(R.id.previous_page_kwejk);
        next_page = findViewById(R.id.next_page_kwejk);
        reload = findViewById(R.id.reload_kwejk);
        number_page = findViewById(R.id.number_page_kwejk);
        network_lost = findViewById(R.id.network_lost);
        Spinner spinner = (Spinner) findViewById(R.id.spiner_kwejk);
        spinner.setOnItemSelectedListener(this);
        kategorie.add("Główna");
        kategorie.add("oczekujace");
        kategorie.add("top/12h");
        kategorie.add("top/24h");
        kategorie.add("top/48h");
        kategorie.add("Humor");
        kategorie.add("Filmiki");
        kategorie.add("Meme");
        kategorie.add("Komiksy");
        kategorie.add("Galeria");
        kategorie.add("Ciekawostki");
        kategorie.add("Polityka");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.spiner_item, kategorie);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh_kwejk);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                haveNetwork();
                if(haveNetwork()) {
                    tytul.clear();
                    memeski.clear();
                    plusy.clear();
                    connect_server_kwejk(strona, "");
                    number_page.setHint(String.valueOf(strona));
                    number_page.setText("");
                    Toast.makeText(KwejkActivity.this, "Reload", Toast.LENGTH_SHORT).show();
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        if(haveNetwork()){
            connect_server_kwejk(1, "");
        }

        reload.setOnClickListener(e->{
            if(haveNetwork()) {
                tytul.clear();
                memeski.clear();
                plusy.clear();
                connect_server_kwejk(strona, "");
                number_page.setHint(String.valueOf(strona));
                number_page.setText("");
                Toast.makeText(KwejkActivity.this, "Reload", Toast.LENGTH_SHORT).show();
            }
        });

        previous_page.setOnClickListener(e->{
            if(haveNetwork()) {
                recyclerView.stopScroll();
                if (strona >= current_page) {
                    Toast.makeText(KwejkActivity.this, "This is first page!", Toast.LENGTH_SHORT).show();
                } else {
                    tytul.clear();
                    memeski.clear();
                    plusy.clear();
                    strona++;
                    System.out.println(strona + " =============");
                    connect_server_kwejk(strona, aktualna_kategoria);
                    number_page.setHint(String.valueOf(strona));
                    number_page.setText("");
                }
            }
        });

        next_page.setOnClickListener(e->{
            if(haveNetwork()) {
                recyclerView.stopScroll();
                if (number_page.getText().toString() != null && !number_page.getText().toString().isEmpty() ) {
                    strona = Integer.parseInt(number_page.getText().toString());
                } else {
                    strona--;
                }
                tytul.clear();
                memeski.clear();
                plusy.clear();
                connect_server_kwejk(strona, aktualna_kategoria);
                number_page.setHint(String.valueOf(strona));
                number_page.setText("");
            }
        });

    }

    private void connect_server_kwejk(int strona, String kategoria) {
        OkHttpClient client = new OkHttpClient();
        String address = "https://kwejk.pl/";

        if(kategoria.equals("oczekujace") || kategoria.equals("top/12h") || kategoria.equals("top/24h") || kategoria.equals("top/48h")){
            address = address + kategoria + "/";
        }else if(!kategoria.equals("") ){
            address = address + "kategoria/" + kategoria + "/";
        }else{
            address = address;
        }
        if(!first_connect) {
            address = address + "strona/" + strona;
        }
        int iterator = 1;
        int how_many = memeski.size();
        Request request = new Request.Builder()
                .url(address)
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

                    KwejkActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            select(myResponse);
                            scrolowanie.add(memeski.size());
                            first_connect = false;
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

                if (calosc[i] == '"' && calosc[i + 1] == ' ' && calosc[i + 2] == 's' && calosc[i + 3] == 'r' && calosc[i + 4] == 'c' && calosc[i + 5] == '=' && calosc[i + 6] == '"' ) {
                    i = i + 7;
                    while (calosc[i] != '"') {
                        mem = mem + calosc[i];
                        i++;
                    }
                    if (mem.contains("i1") && mem.contains("obrazki")) {
                        memeski.add(mem);
                        czy_pobrac_tytyl = true;
                    } else {
                        czy_pobrac_tytyl = false;
                    }
                    mem = "";

                    if (czy_pobrac_tytyl == true) {
                        while (i < caly_tekst.length()) {
                            if (calosc[i] == 'a' && calosc[i + 1] == 'l' && calosc[i + 2] == 't' && calosc[i + 3] == '=') {
                                i = i + 5;
                                while (calosc[i] != '"') {
                                    mem = mem + calosc[i];
                                    i++;
                                }
                                tytul.add(mem);
                                mem = "";
                                break;
                            }
                            i++;
                        }
                        int j = i;
                        if(j == caly_tekst.length()){
                            j = j - 11;
                        }
                        //data-vote-up
                        while(j > 0){
                            if (calosc[j] == 'd' && calosc[j + 1] == 'a' && calosc[j + 2] == 't' && calosc[j + 3] == 'a' && calosc[j + 4] == '-' && calosc[j + 5] == 'v' && calosc[j + 6] == 'o' && calosc[j + 7] == 't' && calosc[j + 8] == 'e' && calosc[j + 9] == '-' && calosc[j + 10] == 'u' && calosc[j + 11] == 'p' ) {
                                j = j + 14;
                                while (calosc[j] != '"') {
                                    mem = mem + calosc[j];
                                    j++;
                                }
                                plusy.add(Integer.parseInt(mem));
                                mem = "";
                                break;
                            }
                            j--;
                        }
                    }
                }

                if (first_connect && calosc[i] == 'c' && calosc[i + 1] == 'u' && calosc[i + 2] == 'r' && calosc[i + 3] == 'r' && calosc[i + 4] == 'e' && calosc[i + 5] == 'n' && calosc[i + 6] == 't' && calosc[i + 7] == '"') {
                    //i=i+7;
                    i=i+11;
                    /*while(calosc[i] != '0' && calosc[i] != '1' && calosc[i] != '2' && calosc[i] != '3' && calosc[i] != '4' && calosc[i] != '5' && calosc[i] != '6' && calosc[i] != '7' && calosc[i] != '8' && calosc[i] != '9' ){
                        i++;
                    }

                     */
                    while(calosc[i]!= '>' ){
                        i++;
                    }
                    i++;
                    String temp_number = "";
                    while(calosc[i]!='<'/*calosc[i] == '0' || calosc[i] == '1' || calosc[i] == '2' && calosc[i] == '3' || calosc[i] == '4' || calosc[i] == '5' || calosc[i] == '6' || calosc[i] == '7' || calosc[i] == '8' || calosc[i] == '9' */){
                        temp_number = temp_number + calosc[i];
                        i++;
                    }
                    current_page = strona = Integer.parseInt(temp_number);
                    number_page.setHint(temp_number);
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
        MyAdapter myAdapter = new MyAdapter(KwejkActivity.this, tytul, memeski, plusy, KwejkActivity.this);
        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(KwejkActivity.this));
    }

    @Override
    public void onNoteClick(int position) {
        if(ActivityCompat.checkSelfPermission(KwejkActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(KwejkActivity.this, "You should grant permission!", Toast.LENGTH_SHORT).show();
            requestPermissions(new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE }, PERMISSION_REQUEST_CODE);
            return;
        }else{
            AlertDialog dialog = new SpotsDialog(KwejkActivity.this);
            dialog.show();
            dialog.setMessage("Downloading...");

            String fileName = UUID.randomUUID().toString()+".jpg";
            Picasso.get().load(memeski.get(position)).into(new SaveImageHelper(getBaseContext(), dialog, getApplicationContext().getContentResolver(), tytul.get(position), "Desc"));
        }
        Toast.makeText(this,tytul.get(position), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();

        if(haveNetwork()) {
            tytul.clear();
            memeski.clear();
            plusy.clear();
            first_connect = true;
            if (item.equals("Główna")) {
                connect_server_kwejk(1, "");
                aktualna_kategoria = "";

            } else {
                aktualna_kategoria = kategorie.get(position);
                connect_server_kwejk(1, kategorie.get(position));
            }
            number_page.setHint(String.valueOf(strona));
            number_page.setText("");
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
