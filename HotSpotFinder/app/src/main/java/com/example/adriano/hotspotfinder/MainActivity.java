package com.example.adriano.hotspotfinder;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LocationListener{
    private LocationManager locationManager;
    private static DBClass db;
    private ListView wifiDeviceList;
    private WifiManager mainWifi;
    private List<ScanResult> wifiList;
    private Context context;
    private final int MY_PERMISSIONS_REQUEST =1 ;
    private final long MINTIME=10;
    private final float MINDISTANCE=100;
    private MainActivity.WifiReceiver receiverWifi;
    private double currentLatitude;
    private double currentLongitude;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String themeName = pref.getString("theme", "0");
        if (themeName.equals("0")) {
            setTheme(R.style.AppTheme_NoActionBar);
        }
        else {
            setTheme(R.style.AppTheme_Dark_NoActionBar);
        }
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        db = new DBClass(this);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        locationManager=(LocationManager) getSystemService(LOCATION_SERVICE);

        wifiDeviceList=(ListView)findViewById(R.id.listView);
        wifiDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                View promptView = inflater.inflate(R.layout.dialog_wifi, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setView(promptView);

                final TextView ssid_et = promptView.findViewById(R.id.ssid);
                final EditText pass_et = promptView.findViewById(R.id.password);
                final String capabilities = wifiList.get(position).capabilities;
                if (!(capabilities.contains("WPA2") || capabilities.contains("WPA") || capabilities.contains("WPA")) ){
                    pass_et.setVisibility(view.INVISIBLE);
                }
                ssid_et.setText(wifiList.get(position).SSID);

                alertDialogBuilder
                        .setCancelable(true)
                        .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String ssid = "\"" + wifiList.get(position).SSID + "\"";
                                String password = "\""+ pass_et.getText().toString() +"\"";

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    connectToHost(getApplicationContext(), ssid, password, capabilities);
                                }
                                else{
                                    connectToHost2(getApplicationContext(), ssid, password);
                                }
                            }
                        })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                })
                        .create()
                        .show();
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent i = new Intent(this, PreferencesActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.map) {
            listWifi fragment = new listWifi();
            FragmentManager man=getFragmentManager();
            FragmentTransaction transaction=man.beginTransaction();
            transaction.replace(R.id.listWifi, fragment);
            transaction.addToBackStack("wifi list");
            transaction.commit();
        }
        if (id == R.id.settings) {
            Intent i = new Intent(this, PreferencesActivity.class);
            startActivity(i);
        }

        if (id == R.id.reset) {

            new AlertDialog.Builder(this)
                    .setTitle("Reset")
                    .setMessage("Delete all data?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            db.reset();
                            Toast.makeText(MainActivity.this, "Reset", Toast.LENGTH_SHORT).show();
                        }})
                    .setNegativeButton(android.R.string.no, null).show();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void connectToHost(Context context,String host,String password, String capabilities ){
        mainWifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration wc=new WifiConfiguration();

        if (capabilities.contains("WPA2") || capabilities.contains("WPA") ) {
            wc.SSID= host;
            wc.preSharedKey =  password;
        }
        else if (capabilities.contains("WEP")) {
            wc.wepKeys[0] = password ;
            wc.wepTxKeyIndex = 0;
            wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        }
        else {
            //assume open network
            wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }
        int netId=mainWifi.addNetwork(wc);
        if (netId > -1) {
            mainWifi.enableNetwork(netId, true);
        }
    }

    public void connectToHost2(Context context,String host,String password){
        mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration wc=new WifiConfiguration();

        wc.SSID= host;
        wc.preSharedKey =  password;
        wc.status = WifiConfiguration.Status.ENABLED;
        wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA); // For WPA
        wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN); // For WPA2
        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);

        int netId=mainWifi.addNetwork(wc);
        if (netId > -1) {
            mainWifi.enableNetwork(netId, true);
        }
    }

    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            String action = intent.getAction();
            if(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)){
                wifiList = mainWifi.getScanResults();
                String encType;

                final ArrayList<ItemListObject> deviceList = new ArrayList<>();

                for(int i = 0; i < wifiList.size(); i++){

                    ScanResult elem = wifiList.get(i);
                    if (!elem.SSID.equals("")){
                        ItemListObject item = new ItemListObject();
                        item.setSsid(elem.SSID);
                        item.setType(elem.capabilities);
                        item.setSignal(elem.level);
                        deviceList.add(item);

                        int level = WifiManager.calculateSignalLevel(elem.level, 5);
                        if (elem.capabilities.contains("WPA2") || elem.capabilities.contains("WPA") || elem.capabilities.contains("WEP")){
                            encType = "protected";
                        }else {
                            encType = "open";
                        }
                        db.insertNewConnection(elem.SSID, elem.BSSID, encType, level, elem.frequency, currentLatitude, currentLongitude ) ;
                    }
                }

                ArrayAdapter<ItemListObject> adapter = new ArrayAdapter<ItemListObject>(MainActivity.this, android.R.layout.simple_list_item_2, android.R.id.text1, deviceList) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView text1 =  view.findViewById(android.R.id.text1);
                        TextView text2 =  view.findViewById(android.R.id.text2);

                        text1.setText(deviceList.get(position).getSsid());
                        text2.setText(deviceList.get(position).getType() + " Level: " + deviceList.get(position).getSignal());
                        return view;
                    }
                };
                wifiDeviceList.setAdapter(adapter);
            }

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(MainActivity.this, "permission granted", Toast.LENGTH_SHORT).show();
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MINTIME,MINDISTANCE,this);
            }else{
                Toast.makeText(MainActivity.this, "permission not granted", Toast.LENGTH_SHORT).show();
            }

        }
    }

    public static DBClass getDB() {
        return db;
    }

    @Override
    public void onLocationChanged(Location location) {

        progressBar.setVisibility(View.GONE);
        Toast t= Toast.makeText(this,"GPS available", Toast.LENGTH_SHORT);
        t.show();
        currentLatitude=location.getLatitude();
        currentLongitude=location.getLongitude();
        receiverWifi = new MainActivity.WifiReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(receiverWifi, intentFilter);
        mainWifi.startScan();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onStop() {
        super.onStop();
        if (receiverWifi != null)
            unregisterReceiver(receiverWifi);
        locationManager.removeUpdates(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MINTIME,MINDISTANCE,this);
            }
            else{
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST);
            }

        }else {
            if (!((LocationManager) context.getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                new AlertDialog.Builder(this)
                        .setTitle("Activate GPS")
                        .setMessage("Do you want to go to settings?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }else{
                //gps is enabled
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MINTIME,MINDISTANCE,this);
            }
        }

        mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!mainWifi.isWifiEnabled())
        {
            Toast.makeText(getApplicationContext(), "Turning WiFi on", Toast.LENGTH_LONG).show();
            mainWifi.setWifiEnabled(true);
        }
    }

}
