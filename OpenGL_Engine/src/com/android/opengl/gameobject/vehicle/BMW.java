package com.android.opengl.gameobject.unit.vehicle;

import android.opengl.Matrix;

import com.android.opengl.R;
import com.android.opengl.gameobject.Scene;

public class BMW extends AbstractVehicle{

	public BMW(Scene parentScene) {
		super(parentScene);
	}
	
	@Override
	public void drawFrame() {
		Matrix.rotateM(modelMatrix, 0, -0.5f, 0, 1, 0);
		super.drawFrame();
	}

	@Override
	public int getMeshResource() {
		return R.raw.bmw;
	}
	
	@Override
	public int getTextureResource() {
		// TODO Auto-generated method stub
		return R.raw.bmw_texture;
	}

}
