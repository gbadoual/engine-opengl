package com.android.opengl.gameobject.unit;

import com.android.opengl.R;
import com.android.opengl.gameobject.GameObject;
import com.android.opengl.gameobject.Scene;
import com.android.opengl.gameobject.tools.moving.Wheels;
import com.android.opengl.gameobject.util.geometry.Point3D;
import com.android.opengl.logic.Actions;

public class Cube extends GameObject{
		

	public Cube(Scene parentScene) {
		super(parentScene);
		movingTool = new Wheels(this);
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
	
	@Override
	public int getTextureResource() {
		return R.raw.smile;
	}



}
