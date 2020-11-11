package com.ucy.ecu.gui.aid;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.fr3ts0n.ecu.EcuDataItem;
import com.fr3ts0n.ecu.EcuDataPv;
import com.ucy.ecu.gui.aid.R;
import com.fr3ts0n.ecu.prot.obd.ObdProt;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Vector;

public class MyPreferenceFragment extends PreferenceFragment {
    Vector<EcuDataItem> items;
    SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.plugin_settings);
        prefs = getPreferenceManager().getSharedPreferences();
        setupPidSelection();
        initItemSelection();
    }

    void setupPidSelection()
    {
        // collect data items for selection
        items = ObdProt.dataItems.getSvcDataItems(ObdProt.OBD_SVC_DATA);
        HashSet <String> known_items = new HashSet<>();
        // loop through data items
        for (EcuDataItem currItem : items)
        {
            String item = currItem.pv.get(EcuDataPv.FID_MNEMONIC).toString();
            if(!item.contains("PID")){
                item = item.replace('_',' ');
                item = Character.toUpperCase(item.charAt(0)) + item.substring(1);
                known_items.add(item);
            }
        }
        prefs.edit().putStringSet(AndroidAutoPlugin.ITEMS_KNOWN, known_items).apply();

    }

    /**
     * Initialize selection of data items to be published
     * from list of data items currently known
     */
    void initItemSelection()
    {
        // get collected data items from set of known data items
        CharSequence[] items = new CharSequence[MainActivity.mKnownItems.size()];
        items = MainActivity.mKnownItems.toArray(items);
        // sort array to make it readable
        Arrays.sort(items, new Comparator<CharSequence>()
        {
            @Override
            public int compare(CharSequence o1, CharSequence o2)
            {
                return o1.toString().compareTo(o2.toString());
            }
        });

        // assign list to preference selection
        MultiSelectListPreference pref = (MultiSelectListPreference)findPreference(AndroidAutoPlugin.ITEMS_SELECTED);
        pref.setEntries(items);
        pref.setEntryValues(items);
    }


}
