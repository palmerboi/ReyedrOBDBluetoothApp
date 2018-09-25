package com.example.reube.reyedrobdbluetoothapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class ConnectorSelectionActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connector_selection);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.connectors_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    // Called when the user taps the proceed button
    public void proceed(View view) {
        Intent intent = new Intent(this, BluetoothConnectionActivity.class);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        String selection = spinner.getSelectedItem().toString();
        if(!selection.equalsIgnoreCase("Harley Davidson/6-pin")) {
            Toast.makeText(this, "Not currently supported!" , Toast.LENGTH_LONG).show();
        } else {
            intent.putExtra("ConnectorSelection", selection);
            startActivity(intent);
        }
    }
}
