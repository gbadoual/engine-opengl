package com.android.opengl.gameobject.tools.moving;

import com.android.opengl.gameobject.GameObject;
import com.android.opengl.gameobject.util.geometry.Point3D;
import com.android.opengl.logic.Actions;

public class Wheels implements MovingTool{
	
	private static float MAX_SPEED = 120;
	
	private GameObject objectToMove;
	
	public Wheels(GameObject objectToMove) {
		this.objectToMove = objectToMove; 
	}
	

	@Override
	public void moveTo(Point3D destination) {
		Actions.moveTo(objectToMove, destination);
	}

	@Override
	public void stop() {
		
	}




	
	

}
