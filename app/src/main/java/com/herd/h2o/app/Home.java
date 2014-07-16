package com.herd.h2o.app;

/**
 * Created by Shane Herd on 6/17/2014.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class Home extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        Button customers = (Button)findViewById(R.id.btnCustomers);
        customers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent viewStores = new Intent(Home.this, ListCustomers.class);
                startActivity(viewStores);
            }
        });

        Button nodes = (Button)findViewById(R.id.btnNodes);
        nodes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent viewNodes = new Intent(Home.this, ListNodes.class);
                startActivity(viewNodes);
            }
        });

        Button runProgram = (Button)findViewById(R.id.btnRunProgram);
        runProgram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent run = new Intent(Home.this, RunProgram.class);
                startActivity(run);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
