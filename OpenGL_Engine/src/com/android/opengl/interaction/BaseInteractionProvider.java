package com.android.opengl.interaction;

import org.json.JSONObject;

public interface BaseInteractionProvider {

	//TODO unify starting params for bluetooth and http client/server
//	public void startServer();
//	public void startClient();
	public JSONObject readData();
	public void writeData(JSONObject jsonObject);
	public void stopServer();
	public void stopClient();

}
