package com.herd.h2o.app;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Shane Herd on 7/2/2014.
 */

public class ListNodes extends ListActivity {
    // Progress Dialog
    private ProgressDialog pDialog;
    int check = 0;

    // php read nodes script
    private static String READ_NODES_URL = "http://192.168.42.1/nodes.php"; //running on pi

    // JSON IDS:
    private static final String TAG_POSTS = "posts";
    private static final String TAG_ID = "id";
    private static final String TAG_ADDRESS = "address";
    private static final String TAG_TYPE = "type";
    private static final String TAG_PARENT = "parent";
    private static final String TAG_ACTIVE = "active";


    // An array of all of our comments
    private JSONArray mNodes = null;
    // manages all of our comments in a list.
    private ArrayList<HashMap<String, String>> mNodesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_nodes);

        Spinner nodeTypes = (Spinner)findViewById(R.id.sNodeType);

        //set up spinners
        //node type spinner (uses string array from strings.xml)
        //possible types are: freakduino, node, valve
        final ArrayAdapter<CharSequence> nodeTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.node_types, android.R.layout.simple_spinner_item);
        nodeTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nodeTypes.setAdapter(nodeTypeAdapter);

        nodeTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == 0) { //freakduino
                    READ_NODES_URL = "http://192.168.42.1/nodes_freakduinos.php";
                }
                else if (position == 1) { //node
                    READ_NODES_URL = "http://192.168.42.1/nodes_nodes.php";
                }
                else if (position == 2) { //valve
                    READ_NODES_URL = "http://192.168.42.1/nodes_valves.php";
                }

                new LoadNodes().execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        nodeTypes.setSelection(2);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // loading the stores via AsyncTask
        check = check + 1;
        if (check > 1) {
            new LoadNodes().execute();
        }
    }

    public void addNode(View v) {
        Intent i = new Intent(ListNodes.this, AddNode.class);
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

        mNodesList = new ArrayList<HashMap<String, String>>();

        JSONParser jParser = new JSONParser();
        JSONObject json = jParser.getJSONFromUrl(READ_NODES_URL);

        try {
            mNodes = json.getJSONArray(TAG_POSTS);

            // looping through all stores according to the json object returned
            for (int i = 0; i < mNodes.length(); i++) {
                JSONObject c = mNodes.getJSONObject(i);

                // gets the content of each tag
                String nodeID = c.getString(TAG_ID);
                String nodeAddress = c.getString(TAG_ADDRESS);
                String nodeType = c.getString(TAG_TYPE);
                String nodeParent = c.getString(TAG_PARENT);
                String nodeActive = c.getString(TAG_ACTIVE);

                // creating new HashMap
                HashMap<String, String> map = new HashMap<String, String>();

                map.put(TAG_ID, nodeID);
                map.put(TAG_ADDRESS, nodeAddress);
                map.put(TAG_TYPE, nodeType);
                map.put(TAG_PARENT, nodeParent);
                map.put(TAG_ACTIVE, nodeActive);

                // adding HashList to ArrayList
                mNodesList.add(map);
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
        ListAdapter adapter = new SimpleAdapter(this, mNodesList,
                R.layout.single_node, new String[] { TAG_ID, TAG_ADDRESS, TAG_TYPE, TAG_PARENT, TAG_ACTIVE },
                new int[] { R.id.tvNodeID, R.id.tvNodeAddress, R.id.tvNodeType, R.id.tvNodeParent, R.id.tvNodeActive });
        setListAdapter(adapter);

        // Optional: when the user clicks a list item we could do something
        ListView lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // This method is triggered if an item is clicked within our list
                Intent editNode = new Intent(ListNodes.this, EditNode.class);
                TextView nodeID = (TextView)view.findViewById(R.id.tvNodeID);
                TextView nodeAddress = (TextView)view.findViewById(R.id.tvNodeAddress);
                TextView nodeType = (TextView)view.findViewById(R.id.tvNodeType);
                TextView nodeParent = (TextView)view.findViewById(R.id.tvNodeParent);
                TextView nodeActive = (TextView)view.findViewById(R.id.tvNodeActive);
                editNode.putExtra("nodeID", nodeID.getText().toString());
                editNode.putExtra("nodeAddress", nodeAddress.getText().toString());
                editNode.putExtra("nodeType", nodeType.getText().toString());
                editNode.putExtra("nodeParent", nodeParent.getText().toString());
                editNode.putExtra("nodeActive", nodeActive.getText().toString());
                startActivity(editNode);
            }
        });
    }

    public class LoadNodes extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ListNodes.this);
            pDialog.setMessage("Loading Nodes...");
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

