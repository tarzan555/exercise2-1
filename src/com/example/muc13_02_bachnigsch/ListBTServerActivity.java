package com.example.muc13_02_bachnigsch;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.example.muc13_02_bachnigsch.bt.ConnectedThread;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Activity for listing of available MUCubigame BT server
 * 
 * Shows a list of available Bluetooth Server. Therefore uses Bluetooth
 * service...
 * 
 * @author Martin Bach
 * @author Maximilian Nigsch
 * 
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
public class ListBTServerActivity extends Activity {
    // Class-Name for debug output
    private static final String TAG = "ListBTServerActivity";
    
    public static ConnectedThread ct = null;

    private BluetoothAdapter mBTAdapter;
    private final int REQUEST_ENABLE_BT = 17;
    ArrayAdapter<String> mArrayAdapter;
    // List containing all discovered BT-devices
    List<BluetoothDevice> mDiscoveredDevices = new LinkedList<BluetoothDevice>();
    // List containing BT-Devices running UbiGame
    List<BluetoothDevice> mBTDevices = new LinkedList<BluetoothDevice>();
    BluetoothSocket mmSocket;

    /**
     * BroadcastReceiver handling Bluetooth-Broadcasts:
     * 	ACTION_FOUND: adds devices to list of discovered devices 
     * 	ACTION_DISCOVERY_FINISHED: starts fetching of UUIDs 
     * 	ACTION_UUID: compares UUIDs to GameUUID
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	private static final String TAG = "ListBTServerActivity-BroadcastReceiver";

	// TODO: remove
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
	public void onReceive(Context context, Intent intent) {
	    String action = intent.getAction();

	    // When discovery finds a device
	    // add it to list of discovered devices
	    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
		// Get the BluetoothDevice object from the Intent
		BluetoothDevice device = intent
			.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

		Log.v(TAG,
			"Found " + device.getName() + " ["
				+ device.getAddress() + "]");

		if (!mDiscoveredDevices.contains(device))
		    mDiscoveredDevices.add(device);
	    }

	    // after discovery finished => start scanning for UUIDs
	    if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
		for (BluetoothDevice device : mDiscoveredDevices)
		    // TODO: implement for API levels lower than 15
		    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
			Log.v(TAG,
				"Trying to fetch UUIDs from "
					+ device.getName());
			device.fetchUuidsWithSdp();
		    }
	    }

	    // receiving array of uuids
	    // checking for game-uuid
	    // adding appropriate devices to game-devices list
	    if (BluetoothDevice.ACTION_UUID.equals(action)) {
		BluetoothDevice device = intent
			.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		Parcelable[] uuidExtra = intent
			.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);

		Log.v(TAG, "Received UUIDs from " + device.getName() + " => "
			+ uuidExtra);

		for (int i = 0; uuidExtra != null && i < uuidExtra.length; i++) {
		    if (uuidExtra[i].toString().equals(
			    StartingServerActivity.GAMEUUID)) {

			if (!mBTDevices.contains(device)) {
			    mArrayAdapter.add(device.getName() + "\n"
				    + device.getAddress());

			    // add bluetooth device to list
			    mBTDevices.add(device);
			}

		    }

		}
	    }
	}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_list_btserver);
	// Show the Up button in the action bar.
	setupActionBar();

	Log.v(TAG, "Starting Activity");

	mArrayAdapter = new ArrayAdapter<String>(this,
		android.R.layout.simple_list_item_1);
	ListView mlistServerView = (ListView) findViewById(R.id.listServerView);
	mlistServerView.setAdapter(mArrayAdapter);

	mlistServerView
		.setOnItemClickListener(new AdapterView.OnItemClickListener() {

		    @Override
		    public void onItemClick(AdapterView<?> arg0, View arg1,
			    int position, long id) {

			BluetoothDevice device = mBTDevices.get((int) id);
			new ConnectTask().execute(device);

		    }
		});

	// Register the BroadcastReceiver
	IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
	filter.addAction(BluetoothDevice.ACTION_UUID);
	filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
	registerReceiver(mReceiver, filter);

	/************************
	 * BT stuff
	 ***********************/

	Log.v(TAG, "Trying to get BT-Adapter");

	// Get bluetooth adapter
	mBTAdapter = BluetoothAdapter.getDefaultAdapter();
	if (mBTAdapter == null) {
	    // device does not support bluetooth
	    Toast toast = Toast.makeText(getApplicationContext(),
		    "Bluetooth not supported!", Toast.LENGTH_LONG);
	    toast.show();
	    finish();
	}

	// Enable BT through system settings
	if (!mBTAdapter.isEnabled()) {
	    Log.v(TAG, "Enabling BT");
	    Intent enableBTIntent = new Intent(
		    BluetoothAdapter.ACTION_REQUEST_ENABLE);
	    startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
	} else {
	    // BT is already enabled
	    // start device Discovery
	    if (!mBTAdapter.startDiscovery()) {
		Log.v(TAG, "Failed to start BT Discovery");
		Toast toast = Toast.makeText(getApplicationContext(),
			"Failed to start BT Discovery", Toast.LENGTH_LONG);
		toast.show();
		finish();
	    } else {
		Log.v(TAG, "BT-Discovery started");
		Toast toast = Toast.makeText(getApplicationContext(),
			"Searching for BT-Devices...", Toast.LENGTH_LONG);
		toast.show();
	    }
	}

    }

    @Override
    protected void onDestroy() {
	super.onDestroy();
	// unregister
	unregisterReceiver(mReceiver);
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
	// call super-method
	super.onActivityResult(requestCode, resultCode, data);

	// check requestCode
	if (requestCode == REQUEST_ENABLE_BT) {
	    if (resultCode == RESULT_CANCELED) {
		Toast toast = Toast.makeText(getApplicationContext(),
			"UbiGame only playable via bluetooth",
			Toast.LENGTH_LONG);
		toast.show();
		finish();
	    } else if (resultCode == RESULT_OK) {
		// Bluetooth activated
		Toast toast = Toast.makeText(getApplicationContext(),
			"Thanks for activating bluetooth", Toast.LENGTH_LONG);
		toast.show();

		// start device Discovery
		if (!mBTAdapter.startDiscovery()) {
		    Toast toast2 = Toast.makeText(getApplicationContext(),
			    "Failed to start BT Discovery", Toast.LENGTH_LONG);
		    toast2.show();
		    finish();
		}
	    }
	}
    }

    // start game activity
    public void callGameActivity() {
	Intent intent = new Intent(this, GameActivity.class);
	startActivity(intent);
    }

    /**
     * 'startServer'-button's onclicked-method
     * 
     * @param view
     */
    public void startServer(View view) {
	Intent intent = new Intent(this, StartingServerActivity.class);
	startActivity(intent);
    }

    private class ConnectTask extends AsyncTask<BluetoothDevice, Void, Void> {
	@Override
	protected void onPostExecute(Void result) {
	    // call super-method
	    super.onPostExecute(result);

	    Toast toast = Toast.makeText(getApplicationContext(),
		    "Connection established!", Toast.LENGTH_LONG);
	    toast.show();
	}

	@Override
	protected Void doInBackground(BluetoothDevice... devices) {

	    assert (devices.length == 1);

	    BluetoothDevice device = devices[0];
	    BluetoothSocket tmp = null;

	    try {
		// MY_UUID is the app's UUID string, also used by the server
		// code
		tmp = device.createRfcommSocketToServiceRecord(UUID
			.fromString(StartingServerActivity.GAMEUUID));
	    } catch (IOException e) {
	    }
	    mmSocket = tmp;

	    // Cancel discovery because it will slow down the connection
	    mBTAdapter.cancelDiscovery();

	    try {
		// Connect the device through the socket. This will block
		// until it succeeds or throws an exception
		mmSocket.connect();
	    } catch (IOException connectException) {
		// Unable to connect; close the socket and get out
		try {
		    mmSocket.close();
		} catch (IOException closeException) {
		}
		return null;
	    }

	    // manage the connection (in a separate thread)
	    ct = new ConnectedThread(mmSocket);
	    
	    // let the games begin :-)
	    callGameActivity();

	    return null;
	}

	@Override
	protected void onCancelled() {
	    // call super-method
	    super.onCancelled();

	    try {
		mmSocket.close();
	    } catch (IOException e) {
		//
	    }
	}

    }

}
