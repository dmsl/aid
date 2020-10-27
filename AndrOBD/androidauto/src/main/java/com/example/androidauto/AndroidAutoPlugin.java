package com.example.androidauto;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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

public class AndroidAutoPlugin
        extends Plugin
        implements Plugin.ConfigurationHandler,
        Plugin.ActionHandler,
        Plugin.DataReceiver,
        SharedPreferences.OnSharedPreferenceChangeListener
{
    static final PluginInfo myInfo = new PluginInfo("AndroidAuto",
            AndroidAutoPlugin.class,
            "Show AndrOBD measurements ",
            "Copyright (C)",
            "GPLV3+", //check the lisence
            "https://github.com/fr3ts0n/AndrOBD"
    );

    /**
     * Preference keys
     */
    static final String MQTT_PREFIX = "mqtt_prefix";
    static final String UPDATE_PERIOD = "update_period";
    static final String MQTT_PROTOCOL = "mqtt_protocol";
    static final String MQTT_HOSTNAME = "mqtt_hostname";
    static final String MQTT_PORT = "mqtt_port";
    static final String MQTT_USERNAME = "mqtt_username";
    static final String MQTT_PASSWORD = "mqtt_password";
    static final String MQTT_CLIENTID = "mqtt_clientid";
    static final String MQTT_RETAIN = "mqtt_retain";
    static final String MQTT_QOS = "mqtt_qos";
    static final String ITEMS_SELECTED = "data_items";
    static final String ITEMS_KNOWN = "known_items";

    /**
     * The data collection
     */
    static final HashMap<String, String> valueMap = new HashMap<>();

    SharedPreferences prefs;
    String mqtt_prefix = "";


    /**
     * set of items which are known to the plugin
     */
    protected static HashSet<String> mKnownItems = new HashSet<>();
    /**
     * set of items to be published
     */
    HashSet<String> mSelectedItems = new HashSet<>();
    /**
     * MQTT client id
     */
    String mClientId = null;
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
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.registerOnSharedPreferenceChangeListener(this);
        // get all shared preference values
        onSharedPreferenceChanged(prefs, null);
        Log.d("AutoPluginLALALA",""+mSelectedItems.size());
        String[] selectedItems = new String[mSelectedItems.size()];
        mSelectedItems.toArray(selectedItems);
        Log.d("AutoPluginLALALA","Selected items array "+selectedItems.length);
        Intent mIntent = new Intent("RefChild");
        mIntent.putExtra("selected",selectedItems);
//        Bundle selected = new Bundle();
//        selected.putStringArray("selected_items",selectedItems);
//        mIntent.putExtras(selected);
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
    @Override
    public PluginInfo getPluginInfo()
    {
        return myInfo;
    }

    /**
     * Perform intended action of the plugin
     */
    @Override
    public void performAction()
    {

        // Nothing to be sent - finished!
        if (valueMap.isEmpty()) { return; }

        Boolean insertedValues = false;
        synchronized (valueMap)
        {
            // loop through data items
            for (Map.Entry<String, String> entry : valueMap.entrySet())
            {
                // get topic and value
                String topic = entry.getKey();
                String value = entry.getValue();
                float val;
                try{
                    val = Float.parseFloat(value);
                    Log.d("Values","Insert "+topic+" "+value);
                    db.insertValue(topic,Float.parseFloat(value));
                    insertedValues = true;
                }catch (Exception e){
                    Log.d("AndroidAuto Module","Value could not be parsed");
                }
            }
        }
        if(insertedValues) {
//            CharSequence[] selectedItems = new CharSequence[mSelectedItems.size()];
//            selectedItems = mSelectedItems.toArray(selectedItems);
            String[] selectedItems = new String[mSelectedItems.size()];
            mSelectedItems.toArray(selectedItems);
            Intent mIntent = new Intent("RefChild");
            mIntent.putExtra("selected",selectedItems);
//            Bundle selected = new Bundle();
//            selected.putCharSequenceArray("selected_items",selectedItems);
//            mIntent.putExtras(selected);
            sendBroadcast(mIntent);
        }
    }

    /**
     * Handle configuration request.
     * Perform plugin configuration
     */
    @Override
    public void performConfigure()
    {
        Intent cfgIntent = new Intent(getApplicationContext(), SettingsActivity.class);
        cfgIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(cfgIntent);
    }

    /**
     * Handle data list update.
     *
     * @param csvString CSV data string in format key;value
     */
    @Override
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
    @Override
    public void onDataUpdate(String key, String value)
    {
        synchronized (valueMap)
        {
            // add value if it is to be published ...
//            if (mSelectedItems.size() == 0 || mSelectedItems.contains(key))
//            {
                valueMap.put(key, value);
//            }
        }
    }
}
