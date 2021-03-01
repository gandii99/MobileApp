package com.example.happyness;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import dmax.dialog.SpotsDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RepostujActivity extends AppCompatActivity implements MyAdapter.OnNoteListener, AdapterView.OnItemSelectedListener {

    private List<String> memeski = new ArrayList();
    private List<String> temp_memeski = new ArrayList();
    private List<String> temp_tytuly = new ArrayList();
    private List<String> tytul = new ArrayList();
    private List<Integer> plusy = new ArrayList();
    String xd = "I tak to kurwa nie działa";
    //private List<String> komentarz = new ArrayList();
    private List<String> kategorie = new ArrayList();
    //private List<String> strony_do_odwiedzenia = new ArrayList();
    private String aktualna_kategoria = "";
    int strona = 1;
    int mem_kolejnosc = 0;
    //int iterator_select_list = 0;
    //int licznik = 0;
    //int lpo = 0;

    //TextView textView;
    RecyclerView recyclerView;
    ImageButton previous_page, next_page, reload;
    EditText number_page;
    TextView network_lost;
    SwipeRefreshLayout swipeRefreshLayout;

    private static final int PERMISSION_REQUEST_CODE = 1000;
    private boolean czy_pobrac_tytyl = false;
    //private boolean czy_odswiezyc = true;
    //boolean pierwszy_link = false;
    //boolean drugi_link = false;
    //int j = 0;
    //int jj = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repostuj);
        recyclerView = findViewById(R.id.recycle_repostuj);
        previous_page = findViewById(R.id.previous_page_repostuj);
        next_page = findViewById(R.id.next_page_repostuj);
        reload = findViewById(R.id.reload_repostuj);
        number_page = findViewById(R.id.number_page_repostuj);
        network_lost = findViewById(R.id.network_lost);

        Spinner spinner = (Spinner) findViewById(R.id.spiner_repostuj);
        spinner.setOnItemSelectedListener(this);
// Create an ArrayAdapter using the string array and a default spinner layout
        kategorie.add("Główna");
        kategorie.add("popularne");
        kategorie.add("poczekalnia");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.spiner_item, kategorie);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh_repostuj);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                haveNetwork();
                if(haveNetwork()) {
                    plusy.clear();
                    tytul.clear();
                    temp_memeski.clear();
                    memeski.clear();
                    connect_server_repostuj(strona, aktualna_kategoria);
                    number_page.setHint(String.valueOf(strona));
                    number_page.setText("");
                    Toast.makeText(RepostujActivity.this, "Reload", Toast.LENGTH_SHORT).show();
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        if(haveNetwork()){
            connect_server_repostuj(1, "");
        }
        reload.setOnClickListener(e->{
            if(haveNetwork()) {
                plusy.clear();
                tytul.clear();
                temp_memeski.clear();
                memeski.clear();
                connect_server_repostuj(strona, aktualna_kategoria);
                number_page.setHint(String.valueOf(strona));
                number_page.setText("");
                Toast.makeText(RepostujActivity.this, "Reload", Toast.LENGTH_SHORT).show();
            }
        });

        previous_page.setOnClickListener(e->{
            if(haveNetwork()) {
                if (strona <= 1 && mem_kolejnosc <= 0) {
                    Toast.makeText(RepostujActivity.this, "This is 1 page!", Toast.LENGTH_SHORT).show();
                } else if(strona >= 1 && mem_kolejnosc > 0){
                    temp_memeski.clear();
                    mem_kolejnosc = mem_kolejnosc-2;
                    if(mem_kolejnosc < 0){
                        mem_kolejnosc = 0;
                    }
                    select_link(memeski.get(mem_kolejnosc));
                }else {
                    tytul.clear();
                    temp_memeski.clear();
                    memeski.clear();
                    strona--;
                    connect_server_repostuj(strona, aktualna_kategoria);
                    number_page.setHint(String.valueOf(strona));
                    number_page.setText("");
                }
            }
        });

        next_page.setOnClickListener(e->{
            if(haveNetwork()) {
                mem_kolejnosc = mem_kolejnosc + 2;
                if(mem_kolejnosc <= tytul.size()-2){
                    //plusy.clear();
                    //tytul.clear();
                    temp_memeski.clear();
                    select_link(memeski.get(mem_kolejnosc));
                }else{
                    plusy.clear();
                    tytul.clear();
                    temp_memeski.clear();
                    memeski.clear();
                    strona++;
                    mem_kolejnosc = 0;
                    connect_server_repostuj(strona, aktualna_kategoria);
                }
                /*
                if (number_page.getText().toString() != null && !number_page.getText().toString().isEmpty()) {
                    strona = Integer.parseInt(number_page.getText().toString());
                } else {
                    strona++;
                }
                 */
                number_page.setHint(String.valueOf(strona));
                number_page.setText("");
            }
        });

    }

    private void connect_server_repostuj(int strona, String kategoria) {
        OkHttpClient client = new OkHttpClient();
        String kat2 = kategoria;
        String address = "https://repostuj.pl";
        System.out.println("===" + kategoria);
        int iterator = 1;
        int how_many = memeski.size();
        if(kategoria.equals("popularne") || kategoria.equals("poczekalnia")){
            kategoria = "/" + kategoria + "/";
        }
        String url = address + kategoria + "?strona=" + strona;
        System.out.println(url + " japko");
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

                    RepostujActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(kat2.equals("poczekalnia")){
                                select_poczekalnia(myResponse);
                            }else if(kat2.equals("popularne")){
                                select_popularne(myResponse);
                            }else {
                                select(myResponse);
                            }
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
//prefetch
//img-fluid

    private void select(String caly_tekst) {
        String mem = "";
        char[] calosc = caly_tekst.toCharArray();
        int i = 0;
        memeski.clear();
        tytul.clear();
        plusy.clear();
        while (i < caly_tekst.length()) {
            if (calosc[i] == 'a' && calosc[i + 1] == ' ' && calosc[i + 2] == 'h' && calosc[i + 3] == 'r' && calosc[i + 4] == 'e' && calosc[i + 5] == 'f' && calosc[i + 6] == '=' && calosc[i + 7] == '"' && calosc[i + 8] == '/' && calosc[i + 9] == 'p' && calosc[i + 10] == 'o' && calosc[i + 11] == 's' && calosc[i + 12] == 't') {
                i = i + 8;
                mem = "https://repostuj.pl";
                while (calosc[i] != '"') {
                    mem = mem + calosc[i];
                    i++;
                }
                if (memeski.size() >= 1 && memeski.get(0).equals(mem)) {
                    // Nwm dlaczego wykrywa mi 2x te same linki xD
                    return;
                }
                memeski.add(mem);
                czy_pobrac_tytyl = true;
                mem = "";

                if (czy_pobrac_tytyl == true) {
                    while (i < caly_tekst.length()) {
                        if (calosc[i] == 'a' && calosc[i + 1] == 'l' && calosc[i + 2] == 't' && calosc[i + 3] == '=') {
                            i = i + 5;
                            break;
                        }
                        i++;
                    }

                    while (calosc[i] != '"') {
                        mem = mem + calosc[i];
                        i++;
                    }
                    if (tytul.size() >= 1 && tytul.get(0).equals(mem)) {
                        return;
                    }
                    tytul.add(mem);
                    plusy.add(1);
                    i++;
                }
            }
            i++;
        }
        select_link(memeski.get(mem_kolejnosc));

    }
    ////
    private void select_poczekalnia(String caly_tekst) {
        String mem = "";
        char[] calosc = caly_tekst.toCharArray();
        int i = 0;
        memeski.clear();
        tytul.clear();
        plusy.clear();
        while (i < caly_tekst.length()) {
            if (calosc[i] == 'a' && calosc[i + 1] == ' ' && calosc[i + 2] == 'h' && calosc[i + 3] == 'r' && calosc[i + 4] == 'e' && calosc[i + 5] == 'f' && calosc[i + 6] == '=' && calosc[i + 7] == '"' && calosc[i + 8] == '/' && calosc[i + 9] == 'p' && calosc[i + 10] == 'o' && calosc[i + 11] == 'c' && calosc[i + 12] == 'z') {
                i = i + 8;
                mem = "https://repostuj.pl";
                while (calosc[i] != '"') {
                    mem = mem + calosc[i];
                    i++;
                }
                if (memeski.size() >= 1 && memeski.get(0).equals(mem)) {
                    // Nwm dlaczego wykrywa mi 2x te same linki xD
                    return;
                }
                memeski.add(mem);
                czy_pobrac_tytyl = true;
                mem = "";

                if (czy_pobrac_tytyl == true) {
                    while (i < caly_tekst.length()) {
                        if (calosc[i] == 'a' && calosc[i + 1] == 'l' && calosc[i + 2] == 't' && calosc[i + 3] == '=') {
                            i = i + 5;
                            break;
                        }
                        i++;
                    }

                    while (calosc[i] != '"') {
                        mem = mem + calosc[i];
                        i++;
                    }
                    if (tytul.size() >= 1 && tytul.get(0).equals(mem)) {
                        return;
                    }
                    tytul.add(mem);
                    plusy.add(1);
                    i++;
                }
            }
            i++;
        }
        select_link(memeski.get(mem_kolejnosc));

        for(String abc : memeski){
            System.out.println(abc + " ===");
        }
        for(String abc : tytul){
            System.out.println(abc + " ===");
        }
        for(String abc : temp_memeski){
            System.out.println(abc + " ===");
        }

    }

    private void select_popularne(String caly_tekst) {
        String mem = "";
        char[] calosc = caly_tekst.toCharArray();
        int i = 0;
        memeski.clear();
        tytul.clear();
        plusy.clear();
        while (i < caly_tekst.length()) {
            if (calosc[i] == 'a' && calosc[i + 1] == ' ' && calosc[i + 2] == 'h' && calosc[i + 3] == 'r' && calosc[i + 4] == 'e' && calosc[i + 5] == 'f' && calosc[i + 6] == '=' && calosc[i + 7] == '"' && calosc[i + 8] == '/' && calosc[i + 9] == 'p' && calosc[i + 10] == 'o' && calosc[i + 11] == 'p' && calosc[i + 12] == 'u') {
                i = i + 8;
                mem = "https://repostuj.pl";
                while (calosc[i] != '"') {
                    mem = mem + calosc[i];
                    i++;
                }
                if (memeski.size() >= 1 && memeski.get(0).equals(mem)) {
                    // Nwm dlaczego wykrywa mi 2x te same linki xD
                    return;
                }
                memeski.add(mem);
                czy_pobrac_tytyl = true;
                mem = "";

                if (czy_pobrac_tytyl == true) {
                    while (i < caly_tekst.length()) {
                        if (calosc[i] == 'a' && calosc[i + 1] == 'l' && calosc[i + 2] == 't' && calosc[i + 3] == '=') {
                            i = i + 5;
                            break;
                        }
                        i++;
                    }

                    while (calosc[i] != '"') {
                        mem = mem + calosc[i];
                        i++;
                    }
                    if (tytul.size() >= 1 && tytul.get(0).equals(mem)) {
                        return;
                    }
                    tytul.add(mem);
                    plusy.add(1);
                    i++;
                }
            }
            i++;
        }
        select_link(memeski.get(mem_kolejnosc));

        for(String abc : memeski){
            System.out.println(abc + " ===");
        }
        for(String abc : tytul){
            System.out.println(abc + " ===");
        }
        for(String abc : temp_memeski){
            System.out.println(abc + " ===");
        }

    }
    //prefetch next
//"img-fluid this
    private void select_link(String url) {
        OkHttpClient client = new OkHttpClient();;
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

                        RepostujActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                boolean zapisz = false;
                                boolean next_link = false;
                                boolean previous_link = false;
                                String mem = "";
                                char[] calosc = myResponse.toCharArray();
                                int i = 0;
                                String next = "", previous = "";
                                while(i < myResponse.length()) {
                                    if (calosc[i] == 'p' && calosc[i + 1] == 'r' && calosc[i + 2] == 'e' && calosc[i + 3] == 'f' && calosc[i + 4] == 'e' && calosc[i + 5] == 't' && calosc[i + 6] == 'c' && calosc[i + 7] == 'h' && calosc[i + 8] == '"') {
                                        i = i + 16;
                                        while (calosc[i] != '"') {
                                            mem = mem + calosc[i];
                                            i++;
                                        }
                                        i++;
                                        next = mem;
                                        mem = "";
                                        next_link = true;
                                    }
                                    if (calosc[i] == '"' && calosc[i + 1] == 'i' && calosc[i + 2] == 'm' && calosc[i + 3] == 'g' && calosc[i + 4] == '-' && calosc[i + 5] == 'f' && calosc[i + 6] == 'l' && calosc[i + 7] == 'u' && calosc[i + 8] == 'i' && calosc[i + 9] == 'd') {
                                        i = i + 17;
                                        while (calosc[i] != '"') {
                                            mem = mem + calosc[i];
                                            i++;
                                        }
                                        i++;
                                        i = myResponse.length();
                                        zapisz = true;
                                        previous_link = true;

                                    }
                                    if(zapisz || i == myResponse.length()-1) {
                                        if (!mem.isEmpty() && mem != null) {
                                            if (!mem.equals("https://repostuj.pl")) {
                                                mem = "https://repostuj.pl" + mem;
                                            }
                                            temp_memeski.add(mem);
                                        }
                                        if (!next.isEmpty() && mem != null) {
                                            /*
                                            if (!next.equals("https://repostuj.pl")) {
                                                next = "https://repostuj.pl" + next;
                                            }
                                             */
                                            temp_memeski.add(next);
                                        }
                                        if(!previous_link && mem_kolejnosc>0 && tytul.size()>mem_kolejnosc && memeski.size()>mem_kolejnosc){
                                            tytul.remove(mem_kolejnosc);
                                            memeski.remove(mem_kolejnosc);
                                            mem_kolejnosc--;
                                            plusy.remove(0);
                                        }
                                        if(!next_link){
                                            tytul.remove(mem_kolejnosc+1);
                                            memeski.remove(mem_kolejnosc+1);
                                            mem_kolejnosc--;
                                            plusy.remove(0);
                                        }
                                        if(temp_memeski.size()>0)
                                        System.out.println(temp_memeski.get(0) + " xxd");
                                        if(temp_memeski.size()>1)
                                        System.out.println(temp_memeski.get(1) + " xxd");
                                        temp_tytuly.clear();
                                        System.out.println(mem_kolejnosc + "heli" + tytul.size());
                                        if(mem_kolejnosc<0 || mem_kolejnosc > tytul.size()-2){
                                            if (previous_link)
                                                temp_tytuly.add(tytul.get(0));
                                            if (next_link)
                                                temp_tytuly.add(tytul.get(0));
                                        }else {
                                            if (previous_link)
                                                temp_tytuly.add(tytul.get(mem_kolejnosc));
                                            if (next_link)
                                                temp_tytuly.add(tytul.get(mem_kolejnosc + 1));
                                        }
                                        putToRecyclerView();
                                    }
                                    mem = "";

                                    i++;
                                }

                            }
                        });
                    }
                }
            });

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
        MyAdapter myAdapter = new MyAdapter(RepostujActivity.this, temp_tytuly, temp_memeski, plusy, RepostujActivity.this);
        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(RepostujActivity.this));
    }

    @Override
    public void onNoteClick(int position) {
        if(ActivityCompat.checkSelfPermission(RepostujActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(RepostujActivity.this, "You should grant permission!", Toast.LENGTH_SHORT).show();
            requestPermissions(new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE }, PERMISSION_REQUEST_CODE);
            return;
        }else{
            AlertDialog dialog = new SpotsDialog(RepostujActivity.this);
            dialog.show();
            dialog.setMessage("Downloading...");

            String fileName = UUID.randomUUID().toString()+".jpg";
            Picasso.get().load(temp_memeski.get(position)).into(new SaveImageHelper(getBaseContext(), dialog, getApplicationContext().getContentResolver(), tytul.get(position), "Desc"));
        }
        Toast.makeText(this,tytul.get(position), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();

        if(haveNetwork()) {
            mem_kolejnosc = 0;
            strona = 1;
            tytul.clear();
            temp_memeski.clear();
            temp_tytuly.clear();
            memeski.clear();
            plusy.clear();
            if (item.equals("Główna")) {
                connect_server_repostuj(1, "");
                aktualna_kategoria = "";

            } else {
                aktualna_kategoria = kategorie.get(position);
                connect_server_repostuj(1, kategorie.get(position));
            }
            number_page.setHint(String.valueOf(strona));
            number_page.setText("");
        }


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}