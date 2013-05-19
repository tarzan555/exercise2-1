package com.example.muc13_02_bachnigsch;

import java.io.IOException;
import java.util.UUID;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.muc13_02_bachnigsch.bt.ConnectedThread;

/**
 * 
 * @author Martin Bach
 * @author Maximilian Nigsch
 * 
 */
public class StartingServerActivity extends Activity {
    // static field for UUID of this app
    public static final String GAMEUUID = "4080ad8d-8ba2-4846-8803-a3206a8975be";

    private final int REQUEST_DISCOVERABLE_BT = 23;

    private TextView statusText;
    private BluetoothAdapter mBTAdapter;
    private BluetoothServerSocket mBTServerSocket;

    /**
     * BroadcastReceiver for checking scan-modes of BT-Device in order to notify
     * user as soon as device is no longer discoverable
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	@Override
	public void onReceive(Context context, Intent intent) {
	    String action = intent.getAction();

	    if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
		int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,
			-1);
		if (BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE != mode) {
		    // device is no longer discoverable
		    statusText.setText("Device is no longer discoverable");
		}
	    }

	}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_starting_server);
	// Show the Up button in the action bar.
	setupActionBar();

	statusText = (TextView) findViewById(R.id.serverStatus);

	// register BroadcastReceiver
	IntentFilter filter = new IntentFilter(
		BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
	registerReceiver(mReceiver, filter);

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
	// using duration = 300s
	Intent discoverableIntent = new Intent(
		BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
	discoverableIntent.putExtra(
		BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
	startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE_BT);
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
	// class-name for debug output
	private static final String TAG = "ServerTask";

	@Override
	protected void onPreExecute() {
	    // call super-method
	    super.onPreExecute();

	    statusText.setText("Initiating server!");
	}

	@Override
	protected Void doInBackground(Void... params) {
	    // Use a temporary object that is later assigned to mmServerSocket,
	    // because mmServerSocket is final
	    BluetoothServerSocket tmp = null;
	    try {
		// MY_UUID is the app's UUID string, also used by the client
		// code
		tmp = mBTAdapter.listenUsingRfcommWithServiceRecord(
			"MUCubigame", UUID.fromString(GAMEUUID));
	    } catch (IOException e) {
		Log.e(TAG,"Failed to start server");
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
		    ConnectedThread ct = new ConnectedThread(socket);
		    Log.v(TAG, "Trying to send something...");
		    ct.write(new String("HalliHallo"));
		    // TODO: implement
		    
		    try {
			mBTServerSocket.close();
		    } catch (IOException e) {
			e.printStackTrace();
		    }
		    break;
		}
	    }

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
