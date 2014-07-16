package com.herd.h2o.app;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Shane Herd on 7/15/2014.
 */

public class RunProgram extends ListActivity {
    // Progress Dialog
    private ProgressDialog pDialog;

    private static RunProgram instance;
    private boolean ready;
    private boolean first = true;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    // php read customers script
    private static final String READ_STATUS_URL = "http://192.168.42.1/status.php"; //running on pi
    private static final String UPDATE_STATUS_URL = "http://192.168.42.1/updatestatus.php"; //running on pi
    private static final String GET_STATUS_URL = "http://192.168.42.1/getstatus.php"; //running on pi

    // JSON IDS:
    private static final String TAG_POSTS = "posts";
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_STATUS = "status";

    //ids
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private int idToSetActive;


    // An array of all of our comments
    private JSONArray mStatus = null;
    // manages all of our comments in a list.
    private ArrayList<HashMap<String, String>> mStatusList;

    //used for determining which state we are in
    private boolean initialize = false;
    private boolean start = false;
    private boolean stop = false;

    private Button action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.run_program);
        instance = this;

        action = (Button)findViewById(R.id.btnAction);
        action.setText("Initialize");
        initialize = true;
        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (initialize) {
                    initialize = false;
                    start = true;
                    idToSetActive = 1;
                    //the button text is changed to start after the program is done initializing
                }
                else if(start) {
                    start = false;
                    stop = true;
                    idToSetActive = 3;
                    action.setText("Stop");
                }
                else if (stop) {
                    stop = false;
                    initialize = true;
                    idToSetActive = 4;
                    action.setText("Initialize");
                }

                new UpdateStatus().execute();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // loading the stores via AsyncTask
        new LoadStatus().execute();
    }

    /**
     * Retrieves recent post data from the server.
     */
    public void updateJSONdata() {

        // Instantiate the arraylist to contain all the JSON data.
        // we are going to use a bunch of key-value pairs, referring
        // to the json element name, and the content, for example,
        // storeName as the tag, and "BYUI" as the content

        mStatusList = new ArrayList<HashMap<String, String>>();

        JSONParser jParser = new JSONParser();
        JSONObject json = jParser.getJSONFromUrl(READ_STATUS_URL);

        try {
            mStatus = json.getJSONArray(TAG_POSTS);

            // looping through all status according to the json object returned
            for (int i = 0; i < mStatus.length(); i++) {
                JSONObject c = mStatus.getJSONObject(i);

                // gets the content of each tag
                String id = c.getString(TAG_ID);
                String name = c.getString(TAG_NAME);
                String status = c.getString(TAG_STATUS);

                // creating new HashMap
                HashMap<String, String> map = new HashMap<String, String>();

                map.put(TAG_ID, id);
                map.put(TAG_NAME, name);
                map.put(TAG_STATUS, status);

                // adding HashList to ArrayList
                mStatusList.add(map);
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
        ListAdapter adapter = new SimpleAdapter(this, mStatusList,
                R.layout.single_status, new String[] { TAG_ID, TAG_NAME, TAG_STATUS },
                new int[] { R.id.tvStatusID, R.id.tvStatusName, R.id.tvStatusStatus });
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

    public class LoadStatus extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RunProgram.this);
            pDialog.setMessage("Loading Status...");
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

    class UpdateStatus extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //set up the progress dialog to show that the node is trying to be added
            pDialog = new ProgressDialog(RunProgram.this);
            pDialog.setMessage("Updating Status...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            // Check for success tag
            int success;

            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("id", String.valueOf(idToSetActive)));

                Log.d("request!", "starting");

                //Posting user data to script
                JSONObject json = jsonParser.makeHttpRequest(
                        UPDATE_STATUS_URL, "POST", params);

                // full json response
                Log.d("Update Status Attempt", json.toString());

                // json success element
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Status Updated!", json.toString());
                    return json.getString(TAG_MESSAGE);
                }else{
                    Log.d("Update Status Failure!", json.getString(TAG_MESSAGE));
                    return json.getString(TAG_MESSAGE);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once the node has been added
            pDialog.dismiss();
            if (file_url != null){
                Toast.makeText(RunProgram.this, file_url, Toast.LENGTH_LONG).show();
            }
            if (idToSetActive == 1) {
                new GetReadyStatus().execute();
            }
            else {
                instance.onResume();
            }
        }
    }

    public class GetReadyStatus extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (first) {
                pDialog = new ProgressDialog(RunProgram.this);
                pDialog.setMessage("Initializing...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(true);
                pDialog.show();
                first = false;
            }

        }

        @Override
        protected Boolean doInBackground(Void... arg0) {

            updateJSONdataStatus();
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (!ready) {
                new GetReadyStatus().execute();
            }
            else {
                pDialog.dismiss();
                action.setText("Start");
                instance.onResume();
            }
        }
    }

    public void updateJSONdataStatus() {
        JSONParser jParser = new JSONParser();
        JSONObject json = jParser.getJSONFromUrl(GET_STATUS_URL);

        try {
            mStatus = json.getJSONArray(TAG_POSTS);

            // looping through all status according to the json object returned
            for (int i = 0; i < mStatus.length(); i++) {
                JSONObject c = mStatus.getJSONObject(i);

                // gets the content of each tag
                String status = c.getString(TAG_STATUS);

                if (status.equals("1")) {
                    ready = true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
