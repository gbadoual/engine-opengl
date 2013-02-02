package com.android.opengl.interaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.json.JSONObject;

import com.android.opengl.util.Log;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class BluetoothInteractionProvider implements BaseInteractionProvider{
	
	private static final long DEFAULT_DEVICE_DISCOVERY_TIMEOUT = 15 * 1000;
	private static final String TAG = BluetoothInteractionProvider.class.getSimpleName();
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothSocket mBluetoothClientSocket;
	private List<BluetoothDevice> mDiscoveredDevices = new ArrayList<BluetoothDevice>();
	private BluetoothServerThread mServerThread;
	private List<OnBluetoothDeviceConnectListener> mBluetoothDeviceConnectListeners = new ArrayList<BluetoothInteractionProvider.OnBluetoothDeviceConnectListener>(); 

	public BluetoothInteractionProvider() throws IllegalAccessException{
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter == null){
			throw new IllegalAccessException("The device doesn't suppor Bluetooth");
		}
	}
	
	public List<BluetoothDevice> discoverDevices(Context context){
		return discoverDevices(context, DEFAULT_DEVICE_DISCOVERY_TIMEOUT);	
	}
	
	public List<BluetoothDevice> discoverDevices(Context context, long timout){
		mDiscoveredDevices.clear();
		registerDiscoveryFoundReceiver(context);
		mBluetoothAdapter.startDiscovery();
		try {
			Thread.sleep(timout);
		} catch (InterruptedException e) {
		}
		mBluetoothAdapter.cancelDiscovery();
		unregisterDiscoveryFoundReceiver(context);
		return mDiscoveredDevices;
	}
	
	private void unregisterDiscoveryFoundReceiver(Context context) {
		context.unregisterReceiver(mDiscoveryFoundReceiver);
	}

	private void registerDiscoveryFoundReceiver(Context context) {
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		context.registerReceiver(mDiscoveryFoundReceiver, filter);		
	}

	public List<BluetoothDevice> getPairedDevices(){
		List<BluetoothDevice> bluetoothDevicesList = new ArrayList<BluetoothDevice>(mBluetoothAdapter.getBondedDevices());	
		return bluetoothDevicesList;
	}
	
	public Intent getEnableBluetoothRequestIntent(){
		return new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	}
	
	public Intent getEnableDiscoverablyModeIntent(){
		return new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
	}
	
	public Intent getEnableDiscoverablyModeIntent(int discoverableDuration){
		Intent discoverableIntent = getEnableDiscoverablyModeIntent();
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, discoverableDuration);
		return discoverableIntent;
	}
	
	
	private BroadcastReceiver mDiscoveryFoundReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(BluetoothDevice.ACTION_FOUND.equals(intent.getAction())){
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				mDiscoveredDevices.add(device);
			}
		}
	};
	
	
	public void resgisterOnBTDeviceConnectListener(OnBluetoothDeviceConnectListener listener){
		mBluetoothDeviceConnectListeners.add(listener);
	}
	public void unResgisterOnBTDeviceConnectListener(OnBluetoothDeviceConnectListener listener){
		mBluetoothDeviceConnectListeners.remove(listener);
	}
	
	private class BluetoothServerThread extends Thread{
		
		private BluetoothServerSocket mBluetoothServerSocket;
		private String mName;
		private UUID mUuid;
		
		
		public BluetoothServerThread(String name, UUID uuid) throws IOException {
			mName = name;
			mUuid = uuid;
		}
		
		@Override
		public void run() {
			while(!isInterrupted()){
				try {
					mBluetoothServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(mName, mUuid);
					BluetoothSocket bluetoothSocket = mBluetoothServerSocket.accept();
					notifyDeviceConnected(bluetoothSocket);
					closeServerSocket(mBluetoothServerSocket);
					
				} catch (IOException e) {
					Log.e(TAG, "run(): closing server socket" + e);
				}finally{
					closeServerSocket(mBluetoothServerSocket);
				} 
			}
			closeServerSocket(mBluetoothServerSocket);
		}

		private void closeServerSocket(BluetoothServerSocket mBluetoothServerSocket) {
			try {
				mBluetoothServerSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "closeServerSocket(): " + e);				
			}
		}
	}

	
	public static interface OnBluetoothDeviceConnectListener{
		public void onBluetoothDeviceConnected(BluetoothSocket bluetoothSocket);
	}


	public void notifyDeviceConnected(BluetoothSocket bluetoothSocket) {
		for(OnBluetoothDeviceConnectListener listener : mBluetoothDeviceConnectListeners){
			listener.onBluetoothDeviceConnected(bluetoothSocket);
		}		
	}


	@Override
	public JSONObject readData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeData(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		
	}

	public void startServer(String name, UUID uuid) throws IOException{
		if(mServerThread != null){
			throw new IllegalStateException("Serevr is already started. Use stopServer() before starting it again");
		}
		mServerThread = new BluetoothServerThread(name, uuid);
		mServerThread.start();
	}
	
	public void stopServer(){
		if(mServerThread != null){
			mServerThread.interrupt();
		}
		mServerThread = null;
	}
	
	public void startClient(BluetoothDevice bluetoothDevice, UUID uuid) throws IOException{
		stopClient();
		mBluetoothClientSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
		mBluetoothAdapter.cancelDiscovery();
		mBluetoothClientSocket.connect();
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
	}

	@Override
	public void startServer() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startClient() {
		// TODO Auto-generated method stub
		
	}

}
