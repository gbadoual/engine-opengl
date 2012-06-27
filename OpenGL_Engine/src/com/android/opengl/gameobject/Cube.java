package com.android.opengl.gameobject;

import android.opengl.Matrix;

import com.android.opengl.R;
import com.android.opengl.gameobject.base.GameObject;
import com.android.opengl.gameobject.base.Scene;

public class Cube extends GameObject{
	

	public Cube(Scene parentScene) {
		super(parentScene);
	}

	@Override
	public void drawFrame() {
//		Matrix.rotateM(modelMatrix, 0, 0.5f, 0.5f, 1f, 1f);
		super.drawFrame();

	}

	@Override
	public int getMeshResource() {
		return R.raw.cube;
	}

}
