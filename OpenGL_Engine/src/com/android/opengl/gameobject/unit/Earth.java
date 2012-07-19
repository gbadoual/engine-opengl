package com.android.opengl.gameobject.unit;

import com.android.opengl.R;
import com.android.opengl.gameobject.GameObject;
import com.android.opengl.gameobject.Scene;
import com.android.opengl.gameobject.tools.moving.Wheels;

public class Earth extends GameObject{

	public Earth(Scene parentScene) {
		super(parentScene);
		setMovingTool(new Wheels(this));
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
