package com.android.opengl.interaction.user;

import com.android.opengl.gameobject.GLScene;
import com.android.opengl.interaction.remote.IBaseClientProvider;

public class ClientUser extends BaseRemoteUser{

	private IBaseClientProvider mBaseClientProvider; 
	
	public ClientUser(GLScene scene , IBaseClientProvider baseClientProvider) {
		super(scene);
		mBaseClientProvider = baseClientProvider;
	}

}
