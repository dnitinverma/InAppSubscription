package com.dnitinverma.inappsubscription.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.android.vending.billing.IInAppBillingService;
import com.dnitinverma.inappsubscription.AppProperties;
import com.dnitinverma.inappsubscription.R;
import com.dnitinverma.inappsubscription.adapters.PurchasedItemsAdapter;
import com.dnitinverma.inappsubscription.inappbilling.utils.IabHelper;
import com.dnitinverma.inappsubscription.inappbilling.utils.IabResult;
import com.dnitinverma.inappsubscription.models.PurchasedItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class PurchasedItemActivity extends AppCompatActivity {
    private boolean blnBind;
    private IInAppBillingService mService;
    private IabHelper mHelper;
    private ListView lv_items;
    private  ArrayList<PurchasedItem> purchasedArrayList;

    //////////////////////////////service ////////////////////////////////////
    private ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };
/////////////////////////////////////////////////////////////////////////

    @Override
    protected void onStart() {
        Intent serviceIntent =new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        blnBind=bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchased_item);
        initializeUI();
        String base64EncodedPublicKey = AppProperties.BASE_64_KEY;
        // Create the helper, passing it our context and the public key to verify signatures with
        //Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(true);
        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        //Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                // Log.d(TAG, "Setup finished.");
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    // complain("Problem setting up in-app billing: " + result);
                    return;
                }
                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;
            }
        });
        new getPurchasedItems().execute();
    }

    private void initializeUI() {
        lv_items = (ListView) findViewById(R.id.lv_items);
    }

    /*
    * This Asynctask is used for fetch all the purchased products details.
    * */
    class getPurchasedItems extends AsyncTask<String,String,String> {
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            if(!isFinishing()) {
                progressDialog = new ProgressDialog(PurchasedItemActivity.this);
                progressDialog.show();
            }
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                purchasedItems();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (!isFinishing()) {
                progressDialog.cancel();
                super.onPostExecute(result);
                    if (purchasedArrayList!=null && purchasedArrayList.size()>0) {
                        PurchasedItemsAdapter adapter = new PurchasedItemsAdapter(PurchasedItemActivity.this,purchasedArrayList);
                        lv_items.setAdapter(adapter);
                    }
                    else {
                        AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(PurchasedItemActivity.this);
                        alertDialogBuilder.setTitle("No Packs");
                        alertDialogBuilder.setMessage("Sorry you have not purchased any product");
                        alertDialogBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).create().show();
                    }
                }
            }
        }

    /*
    * This method is used for fetch all the purchased products.
    * */
    private void purchasedItems(){
        if (!blnBind) return;
        if (mService == null) return;


        Bundle ownedItems;
        try {
            ownedItems = mService.getPurchases(3, getPackageName(), "subs", null);
        } catch (RemoteException e) {
            e.printStackTrace();

            return;
        }

        int response = ownedItems.getInt("RESPONSE_CODE");
        if(response!=0)return;

        if (response != 0) return;
        ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
        purchasedArrayList = parsePurchasedPacks(purchaseDataList);
    }

    /*
    * This method is used for parsing the response of purchased products List.
    * */
    private ArrayList parsePurchasedPacks(ArrayList responseList)
    {
        purchasedArrayList = new ArrayList<>();
        for(int i=0;i<responseList.size();i++)
        {
            PurchasedItem product = new PurchasedItem();
            try {
                JSONObject jsonObject = new JSONObject((String) responseList.get(i));
                product.setPurchaseTime(jsonObject.getString("purchaseTime"));
                product.setProductId(jsonObject.getString("productId"));
                product.setPurchaseToken(jsonObject.getString("purchaseToken"));
                product.setPackageName(jsonObject.getString("packageName"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            purchasedArrayList.add(product);
        }
        return purchasedArrayList;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mService != null) {
            unbindService(mServiceConn);
        }

    }

}
