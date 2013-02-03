package com.android.opengl.interaction;

import org.json.JSONObject;

public interface BaseInteractionProvider {

	//TODO unify starting params for bluetooth and http client/server
//	public void startServer();
//	public void startClient();
	public void startListningData(OnNewDataListner onNewDataListner);
	public void stopListningData(OnNewDataListner onNewDataListner);
	public void sendData(JSONObject jsonObject);
//	public void stopServer();
//	public void stopClient();
	
	public static interface OnNewDataListner{
		public void onNewDataReceived(JSONObject newDataJson);
	}

}
