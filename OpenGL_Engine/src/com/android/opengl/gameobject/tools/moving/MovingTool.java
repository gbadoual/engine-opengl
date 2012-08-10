package com.android.opengl.gameobject.tools.moving;

import android.util.Log;

import com.android.opengl.gameobject.GameObject;
import com.android.opengl.util.geometry.Point3D;
import com.android.opengl.util.geometry.Vector3D;

public abstract class MovingTool {
	
	protected BaseMovingThread movingThread;
	
	
	
	public abstract void moveTo(Point3D destination);
	public abstract void stop();
	public abstract float getMaxSpeed();
	
	
	



}
