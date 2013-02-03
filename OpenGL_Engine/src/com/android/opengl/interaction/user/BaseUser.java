package com.android.opengl.interaction.user;

import com.android.opengl.gameobject.GLScene;

public abstract class BaseUser {
	private GLScene mScene;
	
	public BaseUser(GLScene scene) {
		mScene = scene;
		mScene.addUser(this);
	}

}
