package com.android.opengl;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.opengl.interaction.BluetoothInteractionProvider;
import com.android.opengl.interaction.BluetoothInteractionProvider.OnBluetoothDeviceConnectListener;
import com.android.opengl.util.Log;
import com.android.opengl.view.WorldView;

public class MainActivity extends Activity {
	
	private static final String BT_NAME_OPEN_GL_ENGINE = "OpenGL Engine";
	private static final String TAG = MainActivity.class.getSimpleName();
	private static final String PREFS_NAME = "uuid_prefs";
	private static final String PREFS_KEY_UUID = "key_uuid";
	private TextView fpsView;
	private WorldView worldView;
	private BluetoothInteractionProvider mProvider;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        fpsView = (TextView)findViewById(R.id.fps_view);
        worldView = (WorldView)findViewById(R.id.world_view);
        worldView.setFpsView(fpsView);
//        testBluetoothConnection();
    }
  
    //any random valid string. Should be the same across participating devices
    private UUID mUuid = UUID.fromString("550e8400-e29b-41d4-a716-446655446543");

    private void testBluetoothConnection() {

    	try {
			mProvider = new BluetoothInteractionProvider();
			mProvider.resgisterOnBTDeviceConnectListener(new OnBluetoothDeviceConnectListener() {
				
				@Override
				public void onBluetoothDeviceConnected(BluetoothSocket bluetoothSocket) {
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							Toast.makeText(MainActivity.this, "new client device connected to server", Toast.LENGTH_SHORT).show();
						}
					});
					
				}
			});
			mProvider.startServer(BT_NAME_OPEN_GL_ENGINE, mUuid);
			final List<BluetoothDevice> pairedDevices = mProvider.getPairedDevices();
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			ListAdapter adapter = new ArrayAdapter<BluetoothDevice>(this, android.R.layout.simple_list_item_1, pairedDevices){
				@Override
				public View getView(int position, View convertView,ViewGroup parent) {
					TextView textView = (TextView)super.getView(position, convertView, parent);
					textView.setText(pairedDevices.get(position).getName());
					textView.setTextColor(Color.BLACK);
					return textView;
				}
			};
			builder.setAdapter(adapter , new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					BluetoothDevice bluetoothDevice = pairedDevices.get(which);
					try {
						mProvider.startClient(bluetoothDevice, mUuid);
						
					} catch (IOException e) {
						Log.e(TAG, "onClick(): " + e);
					}
				}
			});
			builder.setNegativeButton("Cancel", null);
			builder.create().show();
		} catch (IllegalAccessException e) {
			Log.e(TAG, e.toString());
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
		
	}

	@Override
    protected void onResume() {
    	super.onResume();
    	worldView.onResume();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	worldView.onPause();
    }
    

}