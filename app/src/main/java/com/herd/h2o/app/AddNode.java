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

public class AddNode extends Activity implements View.OnClickListener {

    private EditText nodeAddress, nodeParent, nodeActive;
    private Spinner nodeType;
    private Button mSubmit;

    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    //php add a node script
    //private static final String ADD_NODE_URL = "http://127.0.0.1:1337/h2o_php/H2O_PHP/addnode.php"; //running from laptop at school
    //private static final String ADD_NODE_URL = "http://192.168.0.253:1337/h2o_php/H2O_PHP/addnode.php"; //running from laptop at home
    private static final String ADD_NODE_URL = "http://192.168.42.1/addnode.php"; //running on pi

    //ids
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_node);

        //initialize components
        nodeAddress = (EditText)findViewById(R.id.etNodeAddress);
        nodeType = (Spinner)findViewById(R.id.sNodeType);
        mSubmit = (Button)findViewById(R.id.submit);

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

        /*
        //Set up for the spinners
        //reads in a file and stores content in an array list
        //array list is then used to populate the spinner adapter
        final ArrayList<String> freakduinoAddresses = new ArrayList<String>();  //list of all of the armors in file
        //File filename = new File(weaponPath, "weaponsMainHand.txt");
        //FileInputStream is = null;
        //BufferedReader br = null;
        //try
        //{
        //    is = new FileInputStream(filename);
        //    DataInputStream in = new DataInputStream(is);
        //    br = new BufferedReader(new InputStreamReader(in));
        //}
        //catch (FileNotFoundException e)	{e.printStackTrace();}

        freakduinoAddresses.add("");
        //read the file and get each line individually
        //try {
        //    String line = "";

            //while not the end of the file
            //while ((line = br.readLine()) != null)
            //{
                //add the name to the array list
                //mainHand.add(line);
                //line = br.readLine();
            //}
        //} catch (IOException e) {e.printStackTrace();}

        final ArrayAdapter<String> freakduinoAddressesAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, freakduinoAddresses);
        freakduinoAddressesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nodeType.setAdapter(freakduinoAddressesAdapter);
        */

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

                    //clear any input in the text box
                    nodeAddress.setText("");
                }
                else if (position == 1) { //node
                    //set the length to 4
                    InputFilter[] FilterArray = new InputFilter[1];
                    FilterArray[0] = new InputFilter.LengthFilter(4);
                    nodeAddress.setFilters(FilterArray);

                    //clear any input in the text box
                    nodeAddress.setText("");
                }
                else if (position == 2) { //valve
                    //set the length to 6
                    InputFilter[] FilterArray = new InputFilter[1];
                    FilterArray[0] = new InputFilter.LengthFilter(6);
                    nodeAddress.setFilters(FilterArray);

                    //clear any input in the text box
                    nodeAddress.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
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
            pDialog = new ProgressDialog(AddNode.this);
            pDialog.setMessage("Adding Node...");
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
                params.add(new BasicNameValuePair("address", node_address));
                params.add(new BasicNameValuePair("type", node_type));
                params.add(new BasicNameValuePair("parent", node_parent));
                params.add(new BasicNameValuePair("active" , "0"));

                Log.d("request!", "starting");

                //Posting user data to script
                JSONObject json = jsonParser.makeHttpRequest(
                        ADD_NODE_URL, "POST", params);

                // full json response
                Log.d("Add Node Attempt", json.toString());

                // json success element
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Node Added!", json.toString());
                    finish();
                    return json.getString(TAG_MESSAGE);
                }else{
                    Log.d("Add Node Failure!", json.getString(TAG_MESSAGE));
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
                Toast.makeText(AddNode.this, file_url, Toast.LENGTH_LONG).show();
            }
        }
    }
}
