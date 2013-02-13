package com.android.opengl.interaction.remote;

import java.io.IOException;


public interface IBaseServerProvider extends IBaseProvider{

	public void startServer() throws IOException;
	public void stopServer();
	
}
