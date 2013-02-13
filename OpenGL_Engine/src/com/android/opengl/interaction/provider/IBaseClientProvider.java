package com.android.opengl.interaction.provider;

import java.io.IOException;


public interface IBaseClientProvider extends IBaseProvider{

	public void startClient() throws IOException;
	public void stopClient();
	
}
