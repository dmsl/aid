package com.ucy.ecu.gui.aid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.media.MediaBrowserServiceCompat;

import java.util.List;


public class MyMediaBrowserService extends MediaBrowserServiceCompat
                                    implements SharedPreferences.OnSharedPreferenceChangeListener {


    private MediaSessionCompat mSession;
    public static final String inten="Refresh";
    /** Declares that ContentStyle is supported */
    public static final String CONTENT_STYLE_SUPPORTED =
            "android.media.browse.CONTENT_STYLE_SUPPORTED";
    static SharedPreferences prefs;

    /**
     * Bundle extra indicating the presentation hint for playable media items.
     */
    public static final String CONTENT_STYLE_PLAYABLE_HINT =
            "android.media.browse.CONTENT_STYLE_PLAYABLE_HINT";

    /**
     * Bundle extra indicating the presentation hint for browsable media items.
     */
    public static final String CONTENT_STYLE_BROWSABLE_HINT =
            "android.media.browse.CONTENT_STYLE_BROWSABLE_HINT";

    /**
     * Specifies the corresponding items should be presented as lists.
     */
    public static final int CONTENT_STYLE_LIST_ITEM_HINT_VALUE = 1;

    /**
     * Specifies that the corresponding items should be presented as grids.
     */
    public static final int CONTENT_STYLE_GRID_ITEM_HINT_VALUE = 2;
    BroadcastReceiver mReceiver;

    String[] mSelectedItems;

    public void onCreate(){
        super.onCreate();
        mSession = new MediaSessionCompat(this, "MusicService");
        MediaSessionCompat.Token sessionToken = mSession.getSessionToken();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // register for later changes
        prefs.registerOnSharedPreferenceChangeListener(this);
        setSessionToken(mSession.getSessionToken());
        mReceiver=new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("RefChild");
        registerReceiver(mReceiver, filter);
        //register Callback


    }


    public void onDestroy(){
        if(mSession.isActive()){
            mSession.setActive(false);
            mSession.release();
        }
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        Bundle extras=new Bundle();
        extras.putBoolean(CONTENT_STYLE_SUPPORTED,true);
        extras.putInt(CONTENT_STYLE_BROWSABLE_HINT,CONTENT_STYLE_GRID_ITEM_HINT_VALUE);
        extras.putInt(CONTENT_STYLE_PLAYABLE_HINT,CONTENT_STYLE_LIST_ITEM_HINT_VALUE);
        return new BrowserRoot("/",extras);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onLoadChildren(final String parentMediaId,
                               final Result<List<MediaBrowserCompat.MediaItem>> result) {

        result.detach();
        MusicLibrary ms = new MusicLibrary();
        ms.init();
        if(parentMediaId.equals("/")){
            result.sendResult(ms.getMedia("/"));
        }
        else{
            result.sendResult(ms.update(this,parentMediaId,mSelectedItems));
        }

        //result.sendResult(ls);
        //ms.createMediaMetadata(getApplicationContext());

        //result.sendResult(ms.getMediaItems());

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }



    // use this as an inner class like here or as a top-level class
    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if((intent!= null)&&(intent.getExtras()!=null)){
                String[] sl = intent.getStringArrayExtra("selected");

                if(sl !=null)
                    mSelectedItems = sl;

            }
            notifyChildrenChanged("/");
            notifyChildrenChanged("_ALL_ITEMS_");
            notifyChildrenChanged("_ITEMS_");
        }

    }

}
