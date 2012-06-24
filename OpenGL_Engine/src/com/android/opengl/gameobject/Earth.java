package com.android.opengl.gameobject;

import com.android.opengl.R;
import com.android.opengl.gameobject.base.GameObject;
import com.android.opengl.gameobject.base.Scene;

public class Earth extends GameObject{

	public Earth(Scene parentScene) {
		super(parentScene);
	}

	@Override
	public int getMeshResource() {
		return R.raw.earth;
	}
	
	@Override
	public void drawFrame() {
		rotate(0, 0.1f, 0);
		super.drawFrame();
	}

}
