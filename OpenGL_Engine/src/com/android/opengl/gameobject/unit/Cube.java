package com.android.opengl.gameobject.unit;

import com.android.opengl.R;
import com.android.opengl.gameobject.GameObject;
import com.android.opengl.gameobject.Scene;
import com.android.opengl.gameobject.tools.moving.Wheels;

public class Cube extends GameObject{
		

	public Cube(Scene parentScene) {
		super(parentScene);
		movingTool = new Wheels(this);
	}

	@Override
	public void drawFrame() {
		super.drawFrame();

	}

	@Override
	public int getMeshResource() {
		return R.raw.cube_data;
	}
	
	@Override
	public int getTextureResource() {
		return R.raw.smile;
	}



}
