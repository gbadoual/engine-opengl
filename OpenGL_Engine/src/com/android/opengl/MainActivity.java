package com.android.opengl;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.opengl.interaction.remote.IBaseProvider.NewDataReceiveListner;
import com.android.opengl.interaction.remote.bluetooth.BaseBluetoothProvider;
import com.android.opengl.interaction.remote.bluetooth.BaseBluetoothProvider.OnBluetoothDeviceConnectListener;
import com.android.opengl.interaction.remote.bluetooth.BluetoothClientProvider;
import com.android.opengl.interaction.remote.bluetooth.BluetoothServerProvider;
import com.android.opengl.util.Log;
import com.android.opengl.view.WorldView;

public class MainActivity extends Activity {
	
	private static final String BT_NAME_OPEN_GL_ENGINE = "OpenGL Engine";
	private static final String TAG = MainActivity.class.getSimpleName();
	private static final String PREFS_NAME = "uuid_prefs";
	private static final String PREFS_KEY_UUID = "key_uuid";
	private TextView fpsView;
	private WorldView worldView;
	private BluetoothServerProvider mServerProvider;
	private BluetoothClientProvider mClientProvider;
	
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
  
    @Override
    public void onBackPressed() {
    	if(!worldView.onBackPressed()){
    		super.onBackPressed();
    	}
    }
    
    private void testBluetoothConnection() {

    	try {
			mServerProvider = new BluetoothServerProvider(BT_NAME_OPEN_GL_ENGINE, BaseBluetoothProvider.DEFAULT_UUID);
			mClientProvider = null;
			mServerProvider.registerNewDataReceiveListener(new NewDataReceiveListner() {
				
				@Override
				public void onNewDataReceived(final JSONObject newDataJson) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(MainActivity.this, newDataJson.toString(), Toast.LENGTH_SHORT).show();
						}
					});
				}
			});
			mClientProvider.registerNewDataReceiveListener(new NewDataReceiveListner() {
				
				@Override
				public void onNewDataReceived(final JSONObject newDataJson) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(MainActivity.this, newDataJson.toString(), Toast.LENGTH_SHORT).show();
						}
					});
				}
			});
			mServerProvider.resgisterOnBTDeviceConnectListener(new OnBluetoothDeviceConnectListener() {
				
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
			mServerProvider.startServer();
			final List<BluetoothDevice> pairedDevices = mServerProvider.getPairedDevices();
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
						mClientProvider = new BluetoothClientProvider(bluetoothDevice, BaseBluetoothProvider.DEFAULT_UUID);
						mClientProvider.startClient();
//						JSONObject data = new JSONObject();
//						try {
//							data.put("message", "Hello from client!");
//						} catch (JSONException e) {
//							Log.e(TAG, "onClick(): " + e);			
//						}
//						mClientProvider.sendData(data);
						
					} catch (IOException e) {
						Log.e(TAG, "onClick(): " + e);
					} catch (IllegalAccessException e) {
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
    public boolean onTouchEvent(MotionEvent event) {
		JSONObject clientData = new JSONObject();
		JSONObject serverData = new JSONObject();
		try {
			clientData.put("message", "Hello from client!");
			serverData.put("message", "Hi there from server!");
		} catch (JSONException e) {
			Log.e(TAG, "onClick(): " + e);			
		}
		
		if(mClientProvider != null){
			mClientProvider.sendData(clientData);
		}
		if(mServerProvider != null){
			mServerProvider.sendData(serverData);
		}
    	return super.onTouchEvent(event);
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