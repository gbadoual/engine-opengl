package com.android.opengl.gameobject.unit.vehicle;

import com.android.opengl.R;
import com.android.opengl.gameobject.Scene;
import com.android.opengl.gameobject.tools.moving.Wheels;

public class BMW extends AbstractVehicle{

	public BMW(Scene parentScene) {
		super(parentScene);
		setMovingTool(new Wheels(this));
	}
	
	@Override
	public void onDrawFrame() {
		rotate(0, -0.001f, 0);
		super.onDrawFrame();
	}

	@Override
	public int getMeshResource() {
		return R.raw.bmw;
	}
	
	@Override
	public int getTextureResource() {
		// TODO Auto-generated method stub
		return R.raw.smile;
	}

}
