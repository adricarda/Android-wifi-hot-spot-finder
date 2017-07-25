package com.example.adriano.hotspotfinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;


public class PreferencesActivity extends PreferenceActivity{

    private SharedPreferences SP;

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
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();

        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    }

    public static class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener  {
        @Override
        public void onCreate(final Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
           if (key.equals("theme")) {
               getActivity().finish();
               Intent i = new Intent(getActivity(), MainActivity.class);
               i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
               startActivity(i);
           }

        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

    }



}