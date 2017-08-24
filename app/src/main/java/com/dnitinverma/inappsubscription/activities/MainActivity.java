package com.dnitinverma.inappsubscription.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dnitinverma.inappsubscription.R;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button bt_purchased,bt_buy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeUI();
        initializeListeners();
    }

    /*
  * This method is used for initialize all the UI views
  * */
    private void initializeUI() {
        bt_purchased = (Button) findViewById(R.id.bt_purchased);
        bt_buy = (Button) findViewById(R.id.bt_buy);
    }

    /*
  * This method is used for initialize all the click listeners
  * */
    private void initializeListeners() {
        bt_purchased.setOnClickListener(this);
        bt_buy.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.bt_purchased:
                if(isInternetOn()) {
                    Intent intentPurchased = new Intent(MainActivity.this, PurchasedItemActivity.class);
                    startActivity(intentPurchased);
                }
                else
                    Toast.makeText(MainActivity.this,"No Internet Connection",Toast.LENGTH_LONG).show();
                break;
            case R.id.bt_buy:
                Intent intentBuy = new Intent(MainActivity.this,BuyNewItemActivity.class);
                startActivity(intentBuy);
                break;
        }
    }

    /**
     * this method check that is internet connection available or not
     */

    public boolean isInternetOn() {
        boolean val = false;
        ConnectivityManager connec = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // ARE WE CONNECTED TO THE NET
        try {
            if (connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED || connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTING || connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTING || connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED) {
                val = true;
            } else if (connec.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED || connec.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED) {
                val = false;
            }

        } catch (Exception e) {
            val = true;
        }
        return val;
    }
}
