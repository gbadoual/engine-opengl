package com.android.opengl.gameobject.unit;

import com.android.opengl.R;
import com.android.opengl.gameobject.GameObject;
import com.android.opengl.gameobject.GLScene;
import com.android.opengl.gameobject.tools.moving.Wheels;

public class Earth extends GameObject{

	public Earth(GLScene parentScene) {
		super(parentScene);
		setMovingTool(new Wheels(this));
		setHealthLevel(500);
	}

	@Override
	public int getMeshResource() {
		return R.raw.earth_data;
	}
	
	@Override
	public void onDrawFrame() {
		rotate(0, 0.01f, 0);
		super.onDrawFrame();
	}

	@Override
	public int getUnitIconResId() {
		return R.raw.icon_earth;
	}

}
