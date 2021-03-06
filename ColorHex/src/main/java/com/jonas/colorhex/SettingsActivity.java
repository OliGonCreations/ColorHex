package com.jonas.colorhex;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

public class SettingsActivity extends PreferenceActivity {

    public final static String pref_clipboard = "pref_clipboard";
    public final static String pref_delete_db_fav = "pref_delete_db_fav";
    public final static String pref_delete_db_rec = "pref_delete_db_rec";
    public final static String pref_license = "pref_license";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        menu.findItem(R.id.action_favorite).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

        ListPreference lpClipboard;
        SharedPreferences sp;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            sp.registerOnSharedPreferenceChangeListener(this);
            lpClipboard = (ListPreference) findPreference(pref_clipboard);
            lpClipboard.setSummary(lpClipboard.getEntry());
        }


        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference.getKey().equals(pref_delete_db_fav)) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(getString(R.string.dialog_delete_fav))
                        .setPositiveButton(getString(R.string.dialog_action_delete), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                new DatabaseHandler(getActivity()).deleteFavorites();
                            }
                        }).setNegativeButton(getString(android.R.string.cancel), null).show();
            } else if (preference.getKey().equals(pref_delete_db_rec)) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(getString(R.string.dialog_delete_rec))
                        .setPositiveButton(getString(R.string.dialog_action_delete), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                new DatabaseHandler(getActivity()).deleteRecents();
                            }
                        }).setNegativeButton(getString(android.R.string.cancel), null).show();
            } else if (preference.getKey().equals(pref_license)) {
                DialogFragment newFragment = new LicenseDialogFragment();
                newFragment.show(getFragmentManager(), "license");
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
            if (key.equals(pref_clipboard))
                lpClipboard.setSummary(lpClipboard.getEntry());
        }
    }

    public static class LicenseDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(inflater.inflate(R.layout.dialog_license, null))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            getDialog().dismiss();
                        }
                    });
            return builder.create();
        }
    }
}
