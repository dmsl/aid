package com.example.androidauto;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceActivity;

import java.util.Arrays;
import java.util.Comparator;


public class SettingsActivity extends PreferenceActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            onCreatePreferenceActivity();
        } else {
            onCreatePreferenceFragment();
           // initItemSelection();
        }
    }

    /**
     * Wraps legacy {@link #onCreate(Bundle)} code for Android < 3 (i.e. API lvl
     * < 11).
     */
    @SuppressWarnings("deprecation")
    private void onCreatePreferenceActivity() {
        addPreferencesFromResource(R.xml.settings);
        initItemSelection();
    }

    /**
     * Wraps {@link #onCreate(Bundle)} code for Android >= 3 (i.e. API lvl >=
     * 11).
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void onCreatePreferenceFragment() {
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new MyPreferenceFragment())
                .commit();
    }


    /**
     * Initialize selection of data items to be published
     * from list of data items currently known
     */
    void initItemSelection()
    {
        // get collected data items from set of known data items
        CharSequence[] items = new CharSequence[AndroidAutoPlugin.mKnownItems.size()];
        items = AndroidAutoPlugin.mKnownItems.toArray(items);
        // sort array to make it readable
        Arrays.sort(items, new Comparator<CharSequence>()
        {
            @Override
            public int compare(CharSequence o1, CharSequence o2)
            {
                return o1.toString().compareTo(o2.toString());
            }
        });
        if(items.length != 0){
            // assign list to preference selection
            MultiSelectListPreference pref = (MultiSelectListPreference)findPreference(AndroidAutoPlugin.ITEMS_SELECTED);

            pref.setEntries(items);
            pref.setEntryValues(items);
        }

    }
}