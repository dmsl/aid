package com.fr3ts0n.ecu.gui.androbd;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import android.os.IBinder;
import android.util.Log;

import com.fr3ts0n.androbd.plugin.Plugin;
import com.fr3ts0n.androbd.plugin.PluginInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * AndrOBD AndroidAuto plugin
 * <p>
 * Show AndrOBD measurements to AndroidAuto
 */

public class AndroidAutoPlugin extends Service implements
        SharedPreferences.OnSharedPreferenceChangeListener
{
    static final PluginInfo myInfo = new PluginInfo("AndroidAuto",
            AndroidAutoPlugin.class,
            "Show Android OBD measurements  on Android Auto screen",
            "Copyright (C)",
            "GPLV3+", //check the lisence
            "https://github.com/dmsl/androidauto"
    );

    /**
     * Preference keys
     */
    static final String UPDATE_PERIOD = "update_period";
    static final String ITEMS_SELECTED = "selection_items";
    static final String ITEMS_KNOWN = "known_items";

    /**
     * The data collection
     */
    static final HashMap<String, String> valueMap = new HashMap<>();

    SharedPreferences prefs;


    /**
     * set of items which are known to the plugin
     */
    protected static HashSet<String> mKnownItems = new HashSet<>();
    /**
     * set of items to be published
     */
    protected HashSet<String> mSelectedItems = new HashSet<>();
    /**
     * Period between getting updates
     */
    int update_period = 5;
    final DBHelper db = new DBHelper(this);
    /**
     * Get preference int value
     *
     * @param key          preference key name
     * @param defaultValue numeric default value
     * @return preference int value
     */
    public static int getPrefsInt(SharedPreferences prefs, String key, int defaultValue)
    {
        int result = defaultValue;

        try
        {
            result = Integer.valueOf(prefs.getString(key, String.valueOf(defaultValue)));
        }
        catch (Exception ex)
        {
            // log error message
            Log.e("Prefs", String.format("Preference '%s'(%d): %s", key, result, ex.toString()));
        }
        return result;
    }

    /**
     * Working thread for cyclic publishing updates
     */
    Thread updateThread = new Thread()
    {
        public void run()
        {
            Log.i("Thread", "started");
            try
            {
                while (!interrupted())
                {
                    sleep(update_period * 1000);
                    Log.i("Thread", "Publish data");
                    performAction();
                }
            }
            catch (InterruptedException ignored)
            {
            }
            Log.i("Thread", "finished");
        }
    };

    @Override
    public void onCreate()
    {
        super.onCreate();
        // get preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        // get all shared preference values
        this.onSharedPreferenceChanged(prefs, null);
        Log.d("pluginpref",prefs.getAll().toString());
        String[] selectedItems = new String[mSelectedItems.size()];
        mSelectedItems.toArray(selectedItems);
        Intent mIntent = new Intent("RefChild");
        mIntent.putExtra("selected",selectedItems);
        sendBroadcast(mIntent);
        updateThread.start();
    }

    @Override
    public void onDestroy()
    {
        // interrupt cyclic thread
        updateThread.interrupt();

        // forget about settings changes
        prefs.unregisterOnSharedPreferenceChangeListener(this);

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Called when a shared preference is changed, added, or removed. This
     * may be called even if a preference is set to its existing value.
     * <p>
     * <p>This callback will be run on your main thread.
     *
     * @param sharedPreferences The {@link SharedPreferences} that received
     *                          the change.
     * @param key               The key of the preference that was changed, added, or
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {

        if (key == null || UPDATE_PERIOD.equals(key))
        { update_period = getPrefsInt(sharedPreferences, UPDATE_PERIOD, 30); }

        if (key == null || ITEMS_SELECTED.equals(key))
        {
            mSelectedItems =
                    (HashSet<String>) sharedPreferences.getStringSet(ITEMS_SELECTED, mSelectedItems);

        }

        if (key == null || ITEMS_KNOWN.equals(key))
        {
            mKnownItems =
                    (HashSet<String>) sharedPreferences.getStringSet(ITEMS_KNOWN, mKnownItems);
        }
    }

    /**
     * get own plugin info
     */
    public PluginInfo getPluginInfo()
    {
        return myInfo;
    }

    /**
     * Perform intended action of the plugin
     */
    public void performAction()
    {

        // Nothing to be sent - finished!
//        if (valueMap.isEmpty()) { return; }
//
//        Boolean insertedValues = false;
//        synchronized (valueMap)
//        {
//            // loop through data items
//            for (Map.Entry<String, String> entry : valueMap.entrySet())
//            {
//                // get topic and value
//                String topic = entry.getKey();
//                String value = entry.getValue();
//                try{
//                    db.insertValue(topic, Float.parseFloat(value));
//                    insertedValues = true;
//                }catch (Exception e){
//                    Log.d("AndroidAuto Module","Value could not be parsed");
//                }
//            }
//        }
        if(true) {
            String[] selectedItems = new String[mSelectedItems.size()];
            mSelectedItems.toArray(selectedItems);
            Log.d("auto","the selected are"+mSelectedItems.toString());
            Intent mIntent = new Intent("RefChild");
            mIntent.putExtra("selected",selectedItems);
            sendBroadcast(mIntent);
        }
    }

    /**
     * Handle configuration request.
     * Perform plugin configuration
     */
    public void performConfigure()
    {
        Intent cfgIntent = new Intent(getApplicationContext(), PluginSettingsActivity.class);
        cfgIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(cfgIntent);
    }

    /**
     * Handle data list update.
     *
     * @param csvString CSV data string in format key;value
     */
    public void onDataListUpdate(String csvString)
    {
        // append unknown items to list of known items
        //noinspection SynchronizeOnNonFinalField
        synchronized (mKnownItems)
        {
            for (String csvLine : csvString.split("\n"))
            {
                String[] fields = csvLine.split(";");
                if (fields.length > 0)
                {
                    mKnownItems.add(fields[0]);
                }
            }
            // store known items as preference
            prefs.edit().putStringSet(ITEMS_KNOWN, mKnownItems).apply();
        }
        // clear the map of received values
        synchronized (valueMap)
        {
            valueMap.clear();
        }
    }

    /**
     * Handle data update.
     *
     * @param key   Key of data change
     * @param value New value of data change
     */
    public void onDataUpdate(String key, String value)
    {
        synchronized (valueMap)
        {
            valueMap.put(key, value);
        }
    }
}
