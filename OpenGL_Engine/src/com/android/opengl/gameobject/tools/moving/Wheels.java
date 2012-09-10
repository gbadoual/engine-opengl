package com.android.opengl.gameobject.tools.moving;

import com.android.opengl.gameobject.GameObject;
import com.android.opengl.util.geometry.Point3D;

public class Wheels extends MovingTool{
	
	private static float MAX_SPEED = 80;
	

	
	public Wheels(GameObject objectToMove) {
		super(objectToMove);
	}

	@Override
	public float getMaxSpeed() {
		return MAX_SPEED;
	}


	@Override
	protected BaseMovingThread obtainMovingThread() {
		return new BaseMovingThread(objectToMove);
	}







	
	

}
