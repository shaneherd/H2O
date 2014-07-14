package com.herd.h2o.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shane Herd on 7/14/2014.
 */

public class EditNode extends Activity implements View.OnClickListener {
    private String nodeIDString, nodeAddressString, nodeTypeString, nodeParentString, nodeActiveString = "";

    private EditText nodeAddress, nodeActive;
    private Spinner nodeType;
    private Button mSubmit;
    int check = 0;

    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    //php add a node script
    private static final String UPDATE_NODE_URL = "http://192.168.42.1/updatenode.php"; //running on pi

    //ids
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_node);

        Bundle extras = getIntent().getExtras();  //this is the extra stuff that was passed from the previous activity
        if (extras != null) {
            nodeIDString = extras.getString("nodeID");
            nodeAddressString = extras.getString("nodeAddress");
            nodeTypeString = extras.getString("nodeType");
            nodeParentString = extras.getString("nodeParent");
            nodeActiveString = extras.getString("nodeActive");
        }

        //initialize components
        nodeAddress = (EditText) findViewById(R.id.etNodeAddress);
        nodeType = (Spinner) findViewById(R.id.sNodeType);
        nodeActive = (EditText) findViewById(R.id.etNodeActive);
        mSubmit = (Button) findViewById(R.id.submit);

        //set the button event
        mSubmit.setOnClickListener(this);

        //limit address length to 2 characters
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(2);
        nodeAddress.setFilters(FilterArray);

        //set up spinners
        //node type spinner (uses string array from strings.xml)
        //possible types are: freakduino, node, valve
        final ArrayAdapter<CharSequence> nodeTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.node_types, android.R.layout.simple_spinner_item);
        nodeTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nodeType.setAdapter(nodeTypeAdapter);

        //when the node type is changed, set the length of the address
        //freakduino: length = 2
        //node: length = 4
        //valve: length = 6
        nodeType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == 0) { //freakduino
                    //set the length to 2
                    InputFilter[] FilterArray = new InputFilter[1];
                    FilterArray[0] = new InputFilter.LengthFilter(2);
                    nodeAddress.setFilters(FilterArray);

                    //if not the initial, clear any input in the text box
                    check = check + 1;
                    if (check > 1) {
                        nodeAddress.setText("");
                    }
                    else {
                        nodeAddress.setText(nodeAddressString);
                    }
                } else if (position == 1) { //node
                    //set the length to 4
                    InputFilter[] FilterArray = new InputFilter[1];
                    FilterArray[0] = new InputFilter.LengthFilter(4);
                    nodeAddress.setFilters(FilterArray);

                    //if not the initial, clear any input in the text box
                    check = check + 1;
                    if (check > 1) {
                        nodeAddress.setText("");
                    }
                    else {
                        nodeAddress.setText(nodeAddressString);
                    }
                } else if (position == 2) { //valve
                    //set the length to 6
                    InputFilter[] FilterArray = new InputFilter[1];
                    FilterArray[0] = new InputFilter.LengthFilter(6);
                    nodeAddress.setFilters(FilterArray);

                    //if not the initial, clear any input in the text box
                    check = check + 1;
                    if (check > 1) {
                        nodeAddress.setText("");
                    }
                    else {
                        nodeAddress.setText(nodeAddressString);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });


        if (nodeTypeString.equals("2")) {
            nodeType.setSelection(0);
        }
        else if (nodeTypeString.equals("3")) {
            nodeType.setSelection(1);
        }
        else if (nodeTypeString.equals("4")) {
            nodeType.setSelection(2);
        }


        nodeActive.setText(nodeActiveString);
    }

    //whenever the submit button is pressed, perform the specified action
    @Override
    public void onClick(View v) {
        //add the new node to the database
        new AddNewNode().execute();
    }

    class AddNewNode extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //set up the progress dialog to show that the node is trying to be added
            pDialog = new ProgressDialog(EditNode.this);
            pDialog.setMessage("Editing Node...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            // Check for success tag
            int success;

            //determine parameters for insert
            String node_address = nodeAddress.getText().toString();
            String node_type = nodeType.getSelectedItem().toString();
            String node_parent;
            String node_active = nodeActive.getText().toString();

            //set the parent address based off of the
            if (node_type.equals("Valve")) {
                node_type = "4";
                node_parent = node_address.substring(0, node_address.length()-2) + "00";
            }
            else if (node_type.equals("Node")) {
                node_type = "3";
                node_parent = node_address.substring(0, node_address.length()-2) + "0000";
                node_address = node_address + "00";
            }
            else {
                node_type = "2";
                node_parent = "000000";
                node_address = node_address + "0000";
            }

            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("valveID", nodeIDString));
                params.add(new BasicNameValuePair("address", node_address));
                params.add(new BasicNameValuePair("type", node_type));
                params.add(new BasicNameValuePair("parent", node_parent));
                params.add(new BasicNameValuePair("oldAddress", nodeAddressString));

                Log.d("request!", "starting");

                //Posting user data to script
                JSONObject json = jsonParser.makeHttpRequest(
                        UPDATE_NODE_URL, "POST", params);

                // full json response
                Log.d("Update Node Attempt", json.toString());

                // json success element
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Node Updated!", json.toString());
                    finish();
                    return json.getString(TAG_MESSAGE);
                }else{
                    Log.d("Update Node Failure!", json.getString(TAG_MESSAGE));
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
                Toast.makeText(EditNode.this, file_url, Toast.LENGTH_LONG).show();
            }
        }
    }
}