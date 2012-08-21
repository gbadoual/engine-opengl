package com.android.opengl.gameobject.tools.moving;

import com.android.opengl.gameobject.GameObject;
import com.android.opengl.util.geometry.Point3D;

public class Wheels extends MovingTool{
	
	private static float MAX_SPEED = 80;
	

	
	public Wheels(GameObject objectToMove) {
		super(objectToMove);
	}
	

	@Override
	public void moveTo(Point3D destination) {
		stop();
		movingThread = new BaseMovingThread(objectToMove, destination);
		movingThread.start();
	}

	@Override
	public void stop() {
		if(movingThread != null){
			movingThread.interrupt();
		}
		
	}


	@Override
	public float getMaxSpeed() {
		return MAX_SPEED;
	}







	
	

}
