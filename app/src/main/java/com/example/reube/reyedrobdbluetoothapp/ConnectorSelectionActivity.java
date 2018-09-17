package com.example.reube.reyedrobdbluetoothapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class ConnectorSelectionActivity extends Activity {
    public static final String EXTRA_SELECTION = "com.example.myfirstapp.SELECTION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connector_selection);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.connectors_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }

    // Called when the user taps the send button
    public void proceed(View view) {
        Intent intent = new Intent(this, BluetoothConnectionActivity.class);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        String selection = spinner.getSelectedItem().toString();
        if(selection.isEmpty()) {
            Toast.makeText(this, "Select a manufacturer/connector to continue" , Toast.LENGTH_LONG).show();
        } else {
            intent.putExtra(EXTRA_SELECTION, selection);
            startActivity(intent);
        }
    }
}
