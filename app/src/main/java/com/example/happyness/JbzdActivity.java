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
import java.util.List;
import java.util.UUID;

import dmax.dialog.SpotsDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class JbzdActivity extends AppCompatActivity implements MyAdapter.OnNoteListener, AdapterView.OnItemSelectedListener {

    private List<String> memeski = new ArrayList();
    private List<String> tytul = new ArrayList();
    private List<Integer> plusy = new ArrayList();
    private List<String> komentarz = new ArrayList();
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
    private boolean skonczone_pierwszy_raz = true;
    private boolean skonczone = true;
    private boolean nie_chce_mi_sie_tego_robic_wiec_robie_bezsensowna_zmienna = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jbzd);
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
        kategorie.add("oczekujace");
        kategorie.add("top/miesiac");
        kategorie.add("top/tydzien");
        kategorie.add("top/dzien");
        kategorie.add("Motoryzacja");
        kategorie.add("Humor");
        kategorie.add("Polityka");
        kategorie.add("Dowcipy");
        kategorie.add("Pasty");
        kategorie.add("Czarny-humor");
        kategorie.add("Gry");
        kategorie.add("Pytanie");
        kategorie.add("Sport");
        kategorie.add("Hobby");
        kategorie.add("Filmy");
        kategorie.add("Ciekawostki");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.spiner_item, kategorie);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh_jbzd);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                haveNetwork();
                if(haveNetwork()) {
                    tytul.clear();
                    memeski.clear();
                    plusy.clear();
                    connect_server_jbzd(strona, "");
                    number_page.setHint(String.valueOf(strona));
                    number_page.setText("");
                    Toast.makeText(JbzdActivity.this, "Reload", Toast.LENGTH_SHORT).show();
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        if(haveNetwork()){
            connect_server_jbzd(1, "");
        }
        reload.setOnClickListener(e->{
            if(haveNetwork()) {
                tytul.clear();
                memeski.clear();
                plusy.clear();
                connect_server_jbzd(strona, "");
                number_page.setHint(String.valueOf(strona));
                number_page.setText("");
                Toast.makeText(JbzdActivity.this, "Reload", Toast.LENGTH_SHORT).show();
            }
        });

        previous_page.setOnClickListener(e->{
            if(haveNetwork()) {
                if (strona <= 1) {
                    Toast.makeText(JbzdActivity.this, "This is 1 page!", Toast.LENGTH_SHORT).show();
                } else {
                    tytul.clear();
                    memeski.clear();
                    plusy.clear();
                    strona--;
                    connect_server_jbzd(strona, aktualna_kategoria);
                    number_page.setHint(String.valueOf(strona));
                    number_page.setText("");
                }
            }
        });

        next_page.setOnClickListener(e->{
            if(haveNetwork()) {
                if (number_page.getText().toString() != null && !number_page.getText().toString().isEmpty()) {
                    strona = Integer.parseInt(number_page.getText().toString());
                } else {
                    strona++;
                }
                tytul.clear();
                memeski.clear();
                connect_server_jbzd(strona, aktualna_kategoria);
                number_page.setHint(String.valueOf(strona));
                number_page.setText("");
            }
        });

        //pringKeyHash(); //sharedPreferences
    }

    private void connect_server_jbzd(int strona, String kategoria) {
        OkHttpClient client = new OkHttpClient();
        String address = "https://jbzd.com.pl/";
        if(kategoria.equals("oczekujace") || kategoria.equals("top/miesiac") || kategoria.equals("top/tydzien") || kategoria.equals("top/dzien")){
            address = address + kategoria + "/";
        }else if(!kategoria.equals("") ){
            address = address + "/kategoria/" + kategoria + "/";
        }else{
            address = address + "str/";
        }
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

                        JbzdActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                select(myResponse);
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

                if (calosc[i] == 'i' && calosc[i + 1] == 'm' && calosc[i + 2] == 'g' && calosc[i + 3] == ' ' && calosc[i + 4] == 's' && calosc[i + 5] == 'r' && calosc[i + 6] == 'c' && calosc[i + 7] == '=') {
                    i = i + 9;
                    while (calosc[i] != '"') {
                        mem = mem + calosc[i];
                        i++;
                    }
                    if (mem.contains("i1") && !mem.contains("small")) {
                        memeski.add(mem);
                        czy_pobrac_tytyl = true;
                    } else {
                        czy_pobrac_tytyl = false;
                    }
                    mem = "";

                    if (czy_pobrac_tytyl == true) {

                        int j = i;
                        while (j >= 0) {
                            if (calosc[j] == '<' && calosc[j + 1] == 'a' && calosc[j + 2] == ' ' && calosc[j + 3] == 'h' && calosc[j + 4] == 'r') {
                                j = j + 9;
                                break;
                            }
                            j--;
                        }

                        while (calosc[j] != '"') {
                            mem = mem + calosc[j];
                            j++;
                        }
                        komentarz.add(mem);
                        mem = "";
                        j++;


                        while (i < caly_tekst.length()) {
                            if (calosc[i] == 'a' && calosc[i + 1] == 'l' && calosc[i + 2] == 't' && calosc[i + 3] == '=') {
                                i = i + 5;
                                while (calosc[i] != '"') {
                                    mem = mem + calosc[i];
                                    i++;
                                }
                                tytul.add(mem);
                                mem = "";
                            }
                            if (calosc[i] == 's' && calosc[i + 1] == 'c' && calosc[i + 2] == 'o' && calosc[i + 3] == 'r' && calosc[i + 4] == 'e' && calosc[i + 5] == '=' && calosc[i + 6] == '"') {
                                i = i + 7;
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

    private void pringKeyHash(){
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.example.jbzd",
                    PackageManager.GET_SIGNATURES);
            for(Signature signature : info.signatures){
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));

            }


        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
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
        MyAdapter myAdapter = new MyAdapter(JbzdActivity.this, tytul, memeski, plusy, JbzdActivity.this);
        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(JbzdActivity.this));
    }

    @Override
    public void onNoteClick(int position) {
        if(ActivityCompat.checkSelfPermission(JbzdActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(JbzdActivity.this, "You should grant permission!", Toast.LENGTH_SHORT).show();
            requestPermissions(new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE }, PERMISSION_REQUEST_CODE);
            return;
        }else{
            AlertDialog dialog = new SpotsDialog(JbzdActivity.this);
            dialog.show();
            dialog.setMessage("Downloading...");
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
            if (item.equals("Główna")) {
                aktualna_kategoria = "";
                if(!nie_chce_mi_sie_tego_robic_wiec_robie_bezsensowna_zmienna)
                    connect_server_jbzd(1, "");
                nie_chce_mi_sie_tego_robic_wiec_robie_bezsensowna_zmienna = false;

            } else {
                aktualna_kategoria = kategorie.get(position);
                connect_server_jbzd(1, kategorie.get(position));
            }
            number_page.setHint(String.valueOf(strona));
            number_page.setText("");
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
