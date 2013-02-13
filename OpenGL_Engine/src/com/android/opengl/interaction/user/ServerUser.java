package com.android.opengl.interaction.user;

import com.android.opengl.gameobject.GLScene;
import com.android.opengl.interaction.remote.IBaseServerProvider;

public class ServerUser extends BaseRemoteUser{
	
	private IBaseServerProvider mBaseServerProvider;

	public ServerUser(GLScene scene, IBaseServerProvider baseServerProvider) {
		super(scene);
		mBaseServerProvider = baseServerProvider;
	}
	

}
