package com.ucy.ecu.gui.aid;

import androidx.appcompat.widget.LinearLayoutCompat;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class LogActivity extends Activity {

    BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_log);

        mReceiver=new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("messCh");
        registerReceiver(mReceiver, filter);

        refreshList();

    }

    public void onDestroy(){
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }


    public void refreshList(){
        LinearLayout ll = (LinearLayout)findViewById(R.id.logview);
        ArrayList<String> messages = new ArrayList<>(AndroidAutoPlugin.logmess);
        ll.removeAllViews();

        for(String mess : messages){
            TextView tx = new TextView(this);
            tx.setText(mess);
            tx.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                    LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
            tx.setPadding(20, 20, 20, 20);

            ll.addView(tx);
        }



    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshList();
        }
    }


}