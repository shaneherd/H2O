package com.herd.h2o.app;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Shane Herd on 6/17/2014.
 */

public class ListCustomers extends ListActivity{
    // Progress Dialog
    private ProgressDialog pDialog;

    // php read stores script
    //private static final String READ_CUSTOMERS_URL = "http://10.37.152.140:1337/h2o/customers.php";
    private static final String READ_CUSTOMERS_URL = "http://192.168.0.253:1337/h2o/customers.php"; //running from laptop
    //private static final String READ_STORES_URL = "http://www.h2o.com/h2o/comments.php";  //running from a real server

    // JSON IDS:
    private static final String TAG_POSTS = "posts";
    private static final String TAG_VALVEID = "valveID";
    private static final String TAG_FIRSTNAME = "firstName";
    private static final String TAG_LASTNAME = "lastName";
    private static final String TAG_SERVICESTARTDATE = "serviceStartDate";
    private static final String TAG_LITERSPERDAY = "litersPerDay";
    private static final String TAG_PRICEPERLITER = "pricePerLiter";


    // An array of all of our comments
    private JSONArray mCustomers = null;
    // manages all of our comments in a list.
    private ArrayList<HashMap<String, String>> mCustomersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_customers);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // loading the stores via AsyncTask
        new LoadCustomers().execute();
    }

    public void addCustomer(View v) {
        Intent i = new Intent(ListCustomers.this, AddCustomer.class);
        startActivity(i);
    }

    /**
     * Retrieves recent post data from the server.
     */
    public void updateJSONdata() {

        // Instantiate the arraylist to contain all the JSON data.
        // we are going to use a bunch of key-value pairs, referring
        // to the json element name, and the content, for example,
        // storeName as the tag, and "BYUI" as the content

        mCustomersList = new ArrayList<HashMap<String, String>>();

        JSONParser jParser = new JSONParser();
        JSONObject json = jParser.getJSONFromUrl(READ_CUSTOMERS_URL);

        try {
            mCustomers = json.getJSONArray(TAG_POSTS);

            // looping through all stores according to the json object returned
            for (int i = 0; i < mCustomers.length(); i++) {
                JSONObject c = mCustomers.getJSONObject(i);

                // gets the content of each tag
                String valveID = c.getString(TAG_VALVEID);
                String storeName = c.getString(TAG_FIRSTNAME);
                String storeLocation = c.getString(TAG_LASTNAME);
                String serviceStartDate = c.getString(TAG_SERVICESTARTDATE);
                String litersPerDay = c.getString(TAG_LITERSPERDAY);
                String pricePerLiter = c.getString(TAG_PRICEPERLITER);

                // creating new HashMap
                HashMap<String, String> map = new HashMap<String, String>();

                map.put(TAG_VALVEID, valveID);
                map.put(TAG_FIRSTNAME, storeName);
                map.put(TAG_LASTNAME, storeLocation);
                map.put(TAG_SERVICESTARTDATE, serviceStartDate);
                map.put(TAG_LITERSPERDAY, litersPerDay);
                map.put(TAG_PRICEPERLITER, pricePerLiter);

                // adding HashList to ArrayList
                mCustomersList.add(map);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inserts the parsed data into the listview.
     */
    private void updateList() {
        //For a ListActivity we need to set the List Adapter, and in order to do
        //that, we need to create a ListAdapter.  This SimpleAdapter
        //will utilize our updated Hashmapped ArrayList,
        //use our single_comment xml template for each item in our list,
        //and place the appropriate info from the list to the
        //correct GUI id.  Order is important here.
        ListAdapter adapter = new SimpleAdapter(this, mCustomersList,
                R.layout.single_customer, new String[] { TAG_VALVEID, TAG_FIRSTNAME, TAG_LASTNAME, TAG_SERVICESTARTDATE, TAG_LITERSPERDAY, TAG_PRICEPERLITER },
                new int[] { R.id.valveID, R.id.firstname, R.id.lastname, R.id.serviceStartDate, R.id.litersPerDay, R.id.pricePerLiter });
        setListAdapter(adapter);

        // Optional: when the user clicks a list item we could do something
        ListView lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // This method is triggered if an item is clicked within our list
            }
        });
    }

    public class LoadCustomers extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ListCustomers.this);
            pDialog.setMessage("Loading Customers...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            updateJSONdata();
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            pDialog.dismiss();
            updateList();
        }
    }
}

