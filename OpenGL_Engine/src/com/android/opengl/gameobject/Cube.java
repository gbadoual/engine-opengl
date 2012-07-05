package com.android.opengl.gameobject;

import com.android.opengl.R;
import com.android.opengl.gameobject.base.GameObject;
import com.android.opengl.gameobject.base.Scene;
import com.android.opengl.gameobject.util.geometry.Point3D;
import com.android.opengl.logic.Actions;
import com.android.opengl.logic.Movable;

public class Cube extends GameObject implements Movable{
		

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
	
	@Override
	public int getTextureResource() {
		return R.raw.smile;
	}

	@Override
	public void moveTo(Point3D destination) {
		Actions.moveTo(this, destination);
		
	}

}
