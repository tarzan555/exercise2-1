package com.example.muc13_02_bachnigsch;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

/**
 * Activity for listing of available MUCubigame BT server
 * 
 * Shows a list of available Bluetooth Server. Therefore uses Bluetooth service...
 * 
 * @author Martin Bach
 * @author Maximilian Nigsch
 *
 */
public class ListBTServerActivity extends Activity {

	private BluetoothAdapter mBTAdapter;
	private final int REQUEST_ENABLE_BT = 17; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_btserver);
		
		// Get bluetooth adapter
		mBTAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBTAdapter == null) {
			// device does not support bluetooth
			Toast toast = Toast.makeText(getApplicationContext(), "Bluetooth not supported!", Toast.LENGTH_LONG);
			toast.show();
			finish();
		}
		
		// Enable BT through system settings
		if(!mBTAdapter.isEnabled()) {
			Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
		}
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list_btserver, menu);
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// call super-method
		super.onActivityResult(requestCode, resultCode, data);
		
		// check requestCode
		if(requestCode == REQUEST_ENABLE_BT) {
			if(resultCode == RESULT_CANCELED) {
				Toast toast = Toast.makeText(getApplicationContext(), "UbiGame only playable via bluetooth", Toast.LENGTH_LONG);
				toast.show();
				finish();
			} else if(resultCode == RESULT_OK){
				// Bluetooth activated
				Toast toast = Toast.makeText(getApplicationContext(), "Thanks for activating bluetooth", Toast.LENGTH_LONG);
				toast.show();
				finish();
			}
		}
	}

}
