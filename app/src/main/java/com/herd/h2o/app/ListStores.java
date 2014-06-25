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
 * Created by Shane Herd on 6/3/2014.
 */

public class ListStores extends ListActivity {
    // Progress Dialog
    private ProgressDialog pDialog;

    // php read stores script
    //private static final String READ_STORES_URL = "http://10.37.152.140:1337/h2o/stores.php"; //running from laptop at school
    //private static final String READ_STORES_URL = "http://192.168.0.253:1337/h2o/stores.php"; //running from laptop at home
    private static final String READ_STORES_URL = "http://192.168.42.1/comments.php";  //running on pi

    // JSON IDS:
    private static final String TAG_POSTS = "posts";
    private static final String TAG_STORENAME = "name";
    private static final String TAG_LOCATION = "location";

    // An array of all of our comments
    private JSONArray mStores = null;
    // manages all of our comments in a list.
    private ArrayList<HashMap<String, String>> mStoresList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_stores);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // loading the stores via AsyncTask
        new LoadStores().execute();
    }

    public void addStore(View v) {
        Intent i = new Intent(ListStores.this, AddStore.class);
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

        mStoresList = new ArrayList<HashMap<String, String>>();

        JSONParser jParser = new JSONParser();
        JSONObject json = jParser.getJSONFromUrl(READ_STORES_URL);

        try {
            mStores = json.getJSONArray(TAG_POSTS);

            // looping through all stores according to the json object returned
            for (int i = 0; i < mStores.length(); i++) {
                JSONObject c = mStores.getJSONObject(i);

                // gets the content of each tag
                String storeName = c.getString(TAG_STORENAME);
                String storeLocation = c.getString(TAG_LOCATION);

                // creating new HashMap
                HashMap<String, String> map = new HashMap<String, String>();

                map.put(TAG_STORENAME, storeName);
                map.put(TAG_LOCATION, storeLocation);

                // adding HashList to ArrayList
                mStoresList.add(map);
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
        ListAdapter adapter = new SimpleAdapter(this, mStoresList,
                R.layout.single_store, new String[] { TAG_STORENAME, TAG_LOCATION },
                new int[] { R.id.storename, R.id.storelocation });
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

    public class LoadStores extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ListStores.this);
            pDialog.setMessage("Loading Stores...");
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
