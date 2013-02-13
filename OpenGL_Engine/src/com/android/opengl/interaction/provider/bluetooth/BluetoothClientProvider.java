package com.android.opengl.interaction.provider.bluetooth;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.JSONObject;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.android.opengl.interaction.provider.IBaseClientProvider;
import com.android.opengl.util.Log;

public class BluetoothClientProvider extends BaseBluetoothProvider implements IBaseClientProvider{
	
	private static final String TAG = BluetoothClientProvider.class.getSimpleName();
	private BluetoothSocket mBluetoothClientSocket;
	private BluetoothDevice mBluetoothDevice;
	private UUID mUuid;

	public BluetoothClientProvider(BluetoothDevice bluetoothDevice, UUID uuid) throws IllegalAccessException{
		super(TAG);
		mBluetoothDevice = bluetoothDevice;
		mUuid = uuid;
	}
	

	@Override
	public void sendData(JSONObject data) {
		
		if (mBluetoothClientSocket == null || !mBluetoothClientSocket.isConnected()){
			Log.w(TAG, "sendData(): BluetoothClientSocket is not connected. Can't send data");
			return;
		}
		try {
			OutputStream outputStream = mBluetoothClientSocket.getOutputStream();
			PrintWriter printWriter = new PrintWriter(outputStream);
			printWriter.println(data.toString());
			printWriter.flush();
		} catch (IOException e) {
			Log.e(TAG, "sendData(): " + e);
		}
	}

	@Override
	public void startClient() throws IOException{
		stopClient();
		mBluetoothClientSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(mUuid);
		mBluetoothAdapter.cancelDiscovery();
		mBluetoothClientSocket.connect();
		startListningData();
	}

	@Override
	public void stopClient() {
		if(mBluetoothClientSocket != null){
			try {
				mBluetoothClientSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "stopClient(): " + e);
			}
		}		
		mBluetoothClientSocket = null;
		stopListningData();
	}


	public void startListningData() {
		if(mDataListenerThread != null){
			Log.i(TAG, "startListningData(): client is already listning for new data");
			return;
		}
		if(mBluetoothClientSocket != null){
			List<BluetoothSocket> bluetoothSocketList = new ArrayList<BluetoothSocket>();
			bluetoothSocketList.add(mBluetoothClientSocket);
			startListningData(bluetoothSocketList);
		}
	}


}
