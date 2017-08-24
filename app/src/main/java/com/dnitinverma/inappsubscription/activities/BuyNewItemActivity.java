package com.dnitinverma.inappsubscription.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.dnitinverma.inappsubscription.R;

import com.dnitinverma.inappsubscription.AppProperties;
import com.dnitinverma.inappsubscription.adapters.BuyItemsAdapter;
import com.dnitinverma.inappsubscription.inappbilling.utils.IabHelper;
import com.dnitinverma.inappsubscription.inappbilling.utils.IabResult;
import com.dnitinverma.inappsubscription.models.Product;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class BuyNewItemActivity extends AppCompatActivity {
    private boolean blnBind;
    private IInAppBillingService mService;
    private IabHelper mHelper;
    public ListView lv_buy_item;
    private ArrayList<Product> productArrayList;
    private ArrayList<String> skuList;
    private Bundle skuDetails;
    private Bundle querySkus;
    private Button bt_get;

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
        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        blnBind = bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_item);
        String base64EncodedPublicKey = AppProperties.BASE_64_KEY;
        // Create the helper, passing it our context and the public key to verify signatures with
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(true);
        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    // complain("Problem setting up in-app billing: " + result);
                    return;
                }
                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;
            }
        });
        initializeUI();
    }

    /*
    * This method is used for initialize all the UI views
    * */
    private void initializeUI() {
        lv_buy_item = (ListView) findViewById(R.id.lv_items);
        bt_get = (Button) findViewById(R.id.bt_get);
        bt_get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInternetOn())
                    new getGoogleAvailablePacks().execute();
                else {
                    finish();
                    Toast.makeText(BuyNewItemActivity.this, "No Internet Connection", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /*
    * This Asynctask is used for fetch all the products on the basis of SKU Id to buy.
    * */
    class getGoogleAvailablePacks extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            if (!isFinishing()) {
                progressDialog = new ProgressDialog(BuyNewItemActivity.this);
                progressDialog.show();
            }
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            int response = -1;
            try {
                getListOfProduct(response);
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
                if (productArrayList != null && productArrayList.size() > 0) {
                    BuyItemsAdapter adapter = new BuyItemsAdapter(BuyNewItemActivity.this, productArrayList);
                    lv_buy_item.setAdapter(adapter);
                }
            }
        }
    }

    /*
    * This method is used for buy a product on the basis of product ID.
    * */
    public void buyItem(Product product, int pos) {
        if (!blnBind) return;
        if (mService == null) return;
        ArrayList<String> sku_list = new ArrayList<String>();
        sku_list.add(product.getProductId());
        Bundle querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", sku_list);
        Bundle skuDetails;
        try {
            skuDetails = mService.getSkuDetails(3, getPackageName(), "subs", querySkus);
        } catch (RemoteException e) {
            e.printStackTrace();
            return;
        }
        int response = skuDetails.getInt("RESPONSE_CODE");
        if (response != 0) return;

        ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");

        if (responseList.size() == 0) return;

        for (String thisResponse : responseList) {
            try {
                JSONObject object = new JSONObject(thisResponse);
                String productId = object.getString("productId");

                if (!productId.equals(product.getProductId())) continue;

                Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), product.getProductId(), "subs", getEmail(BuyNewItemActivity.this));
                response = buyIntentBundle.getInt("RESPONSE_CODE");
                if (response != 0) continue;

                PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                startIntentSenderForResult(pendingIntent.getIntentSender(), 10, new Intent(), 0, 0, 0);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        * This method is used to fetch all the products.
        * */
    private void getListOfProduct(int response) {
        skuList = getListOfPacksFromServer();
        querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
        try {
            skuDetails = mService.getSkuDetails(3, getPackageName(), "subs", querySkus);
            response = skuDetails.getInt("RESPONSE_CODE");
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if (response == 0) {
            Log.e("InAppBillingActivity", "response (0 Ok) : " + response);
        } else {
            Log.e("InAppBillingActivity", "response not Ok: " + response);
            return;
        }

        ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");
        productArrayList = getGooglePacks(responseList);
    }

    /*
    * This method is used for parsing the response of new available products List.
    * */
    private ArrayList getGooglePacks(ArrayList responseList) {
        productArrayList = new ArrayList<>();
        for (int i = 0; i < responseList.size(); i++) {
            Product product = new Product();
            try {
                JSONObject jsonObject = new JSONObject((String) responseList.get(i));
                product.setPrice(jsonObject.getString("price"));
                product.setProductId(jsonObject.getString("productId"));
                product.setPrice_currency_code(jsonObject.getString("price_currency_code"));
                product.setTitle(jsonObject.getString("title"));
                product.setDescription(jsonObject.getString("description"));
                product.setType(jsonObject.getString("type"));
                product.setPrice_amount_micros(jsonObject.getString("price_amount_micros"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            productArrayList.add(product);
        }
        return productArrayList;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            try {
                int responseCode = -1;
                String purchaseData = null;
                if (data != null) {
                    responseCode = data.getIntExtra("RESPONSE_CODE", 0);
                    purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
                    String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
                }
                // If item is purchased successfully
                if (resultCode == RESULT_OK || responseCode == 0) {
                    try {
                        JSONObject jsonPurchaseData = new JSONObject(purchaseData);
                        String sku = "", purchaseToken = "";
                        if (!jsonPurchaseData.isNull("productId"))
                            sku = jsonPurchaseData.getString("productId");
                        if (!jsonPurchaseData.isNull("purchaseToken"))
                            purchaseToken = jsonPurchaseData.getString("purchaseToken");
                    } catch (JSONException e) {
                        Toast.makeText(BuyNewItemActivity.this, "Failed to purchase.", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
                // If item is already purchased
                else if ( responseCode == 7) {
                    Toast.makeText(BuyNewItemActivity.this, "Already Purchased", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // this method is used for describe the sku id of subscription product.
    private ArrayList<String> getListOfPacksFromServer() {
        ArrayList<String> packList = new ArrayList<String> ();
        if(packList.isEmpty()){
            packList.add("weekly_pack");
            packList.add("monthly_pack");
        }
        return packList;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mService != null) {
            unbindService(mServiceConn);
        }

    }


    /*
        * This method is used for fetch system email id.
        * */
        public String getEmail(Context context) {
            AccountManager accountManager = AccountManager.get(context);
            Account account = getAccount(accountManager);

            if (account == null) {
                return null;
            } else {
                return account.name;
            }
        }

        private Account getAccount(AccountManager accountManager) {
            Account[] accounts = accountManager.getAccountsByType("com.google");
            Account account;
            if (accounts.length > 0) {
                account = accounts[0];
            } else {
                account = null;
            }
            return account;
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
