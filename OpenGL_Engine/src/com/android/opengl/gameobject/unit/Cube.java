package com.android.opengl.gameobject.unit;

import com.android.opengl.R;
import com.android.opengl.gameobject.GameObject;
import com.android.opengl.gameobject.Scene;
import com.android.opengl.gameobject.tools.attacking.SimpleGun;
import com.android.opengl.gameobject.tools.moving.Wheels;
import com.android.opengl.view.control.GLHealthBar;

public class Cube extends GameObject{


	public Cube(Scene parentScene) {
		super(parentScene);
		mMovingTool = new Wheels(this);
		mAttackingTool = new SimpleGun(this);
		setHealthLevel(100);

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
	public int getUnitIconResId() {
		return R.raw.icon_cube;
	}
	



}
