package com.android.opengl.interaction.provider;

import java.io.IOException;


public interface IBaseServerProvider extends IBaseProvider{

	public void startServer() throws IOException;
	public void stopServer();
	
}
