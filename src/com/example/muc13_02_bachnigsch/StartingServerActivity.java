package com.example.muc13_02_bachnigsch;

import java.io.IOException;
import java.util.UUID;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author Martin Bach
 * @author Maximilian Nigsch
 * 
 */
public class StartingServerActivity extends Activity {

    private final int REQUEST_DISCOVERABLE_BT = 23;
    private TextView statusText;
    private BluetoothAdapter mBTAdapter;
    private BluetoothServerSocket mBTServerSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_starting_server);
	// Show the Up button in the action bar.
	setupActionBar();

	statusText = (TextView) findViewById(R.id.serverStatus);

	/************************
	 * BT stuff
	 ***********************/

	// Get bluetooth adapter
	mBTAdapter = BluetoothAdapter.getDefaultAdapter();
	if (mBTAdapter == null) {
	    // device does not support bluetooth
	    Toast toast = Toast.makeText(getApplicationContext(),
		    "Bluetooth not supported!", Toast.LENGTH_LONG);
	    toast.show();
	    finish();
	}

	// since we're starting a server, the device should be discoverable
	// using standard duration = 120s
	Intent discoverableIntent = new Intent(
		BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
	startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE_BT);

	// TODO: register boradcast receiver for being notified when leaving
	// discoverability mode
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	    getActionBar().setDisplayHomeAsUpEnabled(true);
	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.starting_server, menu);
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case android.R.id.home:
	    // This ID represents the Home or Up button. In the case of this
	    // activity, the Up button is shown. Use NavUtils to allow users
	    // to navigate up one level in the application structure. For
	    // more details, see the Navigation pattern on Android Design:
	    //
	    // http://developer.android.com/design/patterns/navigation.html#up-vs-back
	    //
	    NavUtils.navigateUpFromSameTask(this);
	    return true;
	}
	return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	// call super-method first
	super.onActivityResult(requestCode, resultCode, data);

	if (requestCode == REQUEST_DISCOVERABLE_BT) {
	    if (resultCode == RESULT_CANCELED) {
		Toast toast = Toast.makeText(getApplicationContext(),
			"Discoverability necessary in order to start a server",
			Toast.LENGTH_LONG);
		toast.show();
		finish();
	    } else {
		Toast toast = Toast.makeText(getApplicationContext(),
			"Starting server...", Toast.LENGTH_LONG);
		toast.show();

		statusText.setText("Starting server...");

		new ServerTask().execute();
	    }
	}
    }

    /**
     * Class for handling BT-Server
     * 
     * creates new socket and waits for a connecting device
     * 
     */
    private class ServerTask extends AsyncTask<Void, Void, Void> {

	@Override
	protected void onPreExecute() {
	    // call super-method
	    super.onPreExecute();

	    statusText.setText("Initiating server!");
	}

	@Override
	protected Void doInBackground(Void... params) {

	    System.out.println("GOOO");
	    
	    // Use a temporary object that is later assigned to mmServerSocket,
	    // because mmServerSocket is final
	    BluetoothServerSocket tmp = null;
	    try {
		// MY_UUID is the app's UUID string, also used by the client
		// code
		tmp = mBTAdapter
			.listenUsingRfcommWithServiceRecord(
				"MUCubigame",
				UUID.fromString("4080ad8d-8ba2-4846-8803-a3206a8975be"));
		System.out.println(UUID.fromString("4080ad8d-8ba2-4846-8803-a3206a8975be"));
	    } catch (IOException e) {
		System.out.println("Failed to start server");
	    }
	    mBTServerSocket = tmp;

	    BluetoothSocket socket = null;
	    // Keep listening until exception occurs or a socket is returned
	    while (true) {
		try {
		    socket = mBTServerSocket.accept();
		} catch (IOException e) {
		    break;
		}
		// If a connection was accepted
		if (socket != null) {
		    // Do work to manage the connection (in a separate thread)
		    // TODO: implement
		    try {
			mBTServerSocket.close();
		    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		    break;
		}
	    }
	    
	    System.out.println("DONE");

	    return null;
	}

	@Override
	protected void onPostExecute(Void result) {
	    // call super-method
	    super.onPostExecute(result);

	    statusText.setText("Connecting established!");
	    Toast toast = Toast.makeText(getApplicationContext(),
		    "Connection established!", Toast.LENGTH_LONG);
	    toast.show();
	}

	@Override
	protected void onCancelled() {
	    // call super-method
	    super.onCancelled();

	    try {
		mBTServerSocket.close();
	    } catch (IOException e) {
		//
	    }
	}

    }

}
