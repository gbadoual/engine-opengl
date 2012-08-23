package com.android.opengl.gameobject.unit;

import com.android.opengl.R;
import com.android.opengl.gameobject.GameObject;
import com.android.opengl.gameobject.Scene;
import com.android.opengl.gameobject.tools.attacking.SimpleGun;
import com.android.opengl.gameobject.tools.moving.Wheels;
import com.android.opengl.view.control.GLHealthBar;

public class Cube extends GameObject{

	private GLHealthBar healthBar;

	public Cube(Scene parentScene) {
		super(parentScene);
		movingTool = new Wheels(this);
		attackingTool = new SimpleGun(this);
		curHealthLevel = 100;
		healthBar = new GLHealthBar(this);
		healthBar.setVisible(true);
		healthBar.setzOrder(200);
//		parentScene.registerGLView(healthBar, 200);
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
	
	@Override
	public void release() {
		healthBar.release();
		super.release();
	}



}
