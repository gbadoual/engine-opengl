package com.android.opengl.gameobject.tools.moving;

import com.android.opengl.gameobject.GameObject;
import com.android.opengl.util.geometry.Point3D;

public abstract class MovingTool {
	
	protected BaseMovingThread movingThread;
	
	protected GameObject objectToMove;
	
	public MovingTool(GameObject objectToMove) {
		this.objectToMove = objectToMove;
	}
	public abstract void moveTo(Point3D destination);
	public abstract void stop();
	public abstract float getMaxSpeed();
	
	
	



}
