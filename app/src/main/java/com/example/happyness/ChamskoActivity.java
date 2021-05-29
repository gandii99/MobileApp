package com.example.happyness;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class ChamskoActivity extends AppCompatActivity implements MyAdapter.OnNoteListener, AdapterView.OnItemSelectedListener{

    private List<String> memeski = new ArrayList();
    private List<String> tytul = new ArrayList();
    private List<Integer> scrolowanie = new ArrayList();
    private List<Integer> plusy = new ArrayList();
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
    private boolean nie_chce_mi_sie_tego_robic_wiec_robie_bezsensowna_zmienna = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("");
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(actionBar.getDisplayOptions()
                | ActionBar.DISPLAY_SHOW_CUSTOM);
        ImageView imageView = new ImageView(actionBar.getThemedContext());
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setImageResource(R.drawable.chamsko_x50);
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT, Gravity.RIGHT
                | Gravity.CENTER_VERTICAL);
        layoutParams.rightMargin = 40;
        imageView.setLayoutParams(layoutParams);
        actionBar.setCustomView(imageView);
        setContentView(R.layout.activity_chamsko);

        recyclerView = findViewById(R.id.recycle_chamsko);
        previous_page = findViewById(R.id.previous_page_chamsko);
        next_page = findViewById(R.id.next_page_chamsko);
        reload = findViewById(R.id.reload_chamsko);
        number_page = findViewById(R.id.number_page_chamsko);
        network_lost = findViewById(R.id.network_lost);
        Spinner spinner = (Spinner) findViewById(R.id.spiner_chamsko);
        spinner.setOnItemSelectedListener(this);
        kategorie.add("Główna");
        kategorie.add("Poczekalnia");
        //kategorie.add("Losowy");
        kategorie.add("top/ocena/tydzien");
        kategorie.add("top/ocena/miesiac");
        kategorie.add("top/ocena/ostatni_miesiac");
        kategorie.add("top/ocena/ostatnie_3_miesiace");
        kategorie.add("top");


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.spiner_item, kategorie);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh_chamsko);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                haveNetwork();
                if(haveNetwork()) {
                    tytul.clear();
                    memeski.clear();
                    plusy.clear();
                    connect_server_chamsko(strona, aktualna_kategoria);
                    number_page.setHint(String.valueOf(strona));
                    number_page.setText("");
                    Toast.makeText(ChamskoActivity.this, "Reload", Toast.LENGTH_SHORT).show();
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        if(haveNetwork()){
            connect_server_chamsko(1, "");
        }
            reload.setOnClickListener(e -> {
                if(haveNetwork()) {
                    tytul.clear();
                    memeski.clear();
                    plusy.clear();
                    connect_server_chamsko(strona, aktualna_kategoria);
                    number_page.setHint(String.valueOf(strona));
                    number_page.setText("");
                    Toast.makeText(ChamskoActivity.this, "Reload", Toast.LENGTH_SHORT).show();
                }
            });

            previous_page.setOnClickListener(e -> {
                if(haveNetwork()) {
                    recyclerView.stopScroll();
                    if (strona <= 1) {
                        Toast.makeText(ChamskoActivity.this, "This is 1 page!", Toast.LENGTH_SHORT).show();
                    } else {
                        tytul.clear();
                        memeski.clear();
                        plusy.clear();
                        strona--;
                        connect_server_chamsko(strona, aktualna_kategoria);
                        number_page.setHint(String.valueOf(strona));
                        number_page.setText("");
                    }
                }
            });

            next_page.setOnClickListener(e -> {
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
                    connect_server_chamsko(strona, aktualna_kategoria);
                    number_page.setHint(String.valueOf(strona));
                    number_page.setText("");
                }
            });

    }

    private void connect_server_chamsko(int strona, String kategoria) {
        OkHttpClient client = new OkHttpClient();
        String address = "https://chamsko.pl/";
        if(kategoria.equals("Poczekalnia") || kategoria.equals("top") ){
            address = address + kategoria + "/";
        }else if(!kategoria.equals("") ){
            address = address + kategoria + "/";
        }else{
            address = address + "page/";
        }
        System.out.println(address + " =====================");

        int iterator = 1;
        int how_many = memeski.size();
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

                    ChamskoActivity.this.runOnUiThread(new Runnable() {
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

                if (calosc[i] == 's' && calosc[i + 1] == 'r' && calosc[i + 2] == 'c' && calosc[i + 3] == '=' && calosc[i + 4] == '"' && calosc[i + 5] == '/' && calosc[i + 6] == 'd') {
                    i = i + 5;
                    while (calosc[i] != '"') {
                        mem = mem + calosc[i];
                        i++;
                    }
                    if (mem.contains("demot") ) {
                        memeski.add("https://chamsko.pl" + mem);
                        czy_pobrac_tytyl = true;
                    } else {
                        czy_pobrac_tytyl = false;
                    }
                    mem = "";

                    if (czy_pobrac_tytyl == true) {
                        int j = i;
                        while (j >= 0) {
                            if (calosc[j] == '<' && calosc[j + 1] == '/' && calosc[j + 2] == 'h' && calosc[j + 3] == '2' && calosc[j + 4] == '>') {
                                break;
                            }
                            j--;
                        }
                        j--;
                        while (calosc[j] != '>') {
                            j--;
                        }
                        j++;

                        while (calosc[j] != '<') {
                            mem = mem + calosc[j];
                            j++;
                        }

                        tytul.add(mem);
                        mem = "";

                        while(i < caly_tekst.length()) {
                            if (calosc[i] == 'r' && calosc[i + 1] == 'a' && calosc[i + 2] == 't' && calosc[i + 3] == 'i' && calosc[i + 4] == 'n' && calosc[i + 5] == 'g' && calosc[i + 6] == '_' && calosc[i + 7] == 'c' && calosc[i + 8] == 'o' && calosc[i + 9] == 'u' && calosc[i + 10] == 'n' && calosc[i + 11] == 't' ) {
                                i = i + 14;
                                while (calosc[i] != '"') {
                                    mem = mem + calosc[i];
                                    i++;
                                }
                                plusy.add(Integer.parseInt(mem));
                                mem = "";
                                break;
                            }
                            i++;
                        }

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

            if(have_WIFI || have_MobileData){
                network_lost.setVisibility(View.INVISIBLE);
            }else{
                network_lost.setVisibility(View.VISIBLE);
            }
        }

        return have_MobileData || have_WIFI;
    }

    private void putToRecyclerView(){
        MyAdapter myAdapter = new MyAdapter(ChamskoActivity.this, tytul, memeski, plusy, ChamskoActivity.this);
        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ChamskoActivity.this));
    }

    @Override
    public void onNoteClick(int position) {
        if(ActivityCompat.checkSelfPermission(ChamskoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(ChamskoActivity.this, "You should grant permission!", Toast.LENGTH_SHORT).show();
            requestPermissions(new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE }, PERMISSION_REQUEST_CODE);
            return;
        }else{
            AlertDialog dialog = new SpotsDialog(ChamskoActivity.this);
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
            strona = 1;
            tytul.clear();
            memeski.clear();
            plusy.clear();
            if (item.equals("Główna")) {
                if(!nie_chce_mi_sie_tego_robic_wiec_robie_bezsensowna_zmienna)
                    connect_server_chamsko(1, "");
                aktualna_kategoria = "";
            } else {
                aktualna_kategoria = kategorie.get(position);
                connect_server_chamsko(1, kategorie.get(position));
            }
            number_page.setHint(String.valueOf(strona));
            number_page.setText("");
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
