package com.android.opengl.gameobject.tools.moving;

import com.android.opengl.gameobject.util.geometry.Point3D;

public interface MovingTool {
	
	public void moveTo(Point3D destination);
	public void stop();
	

}
