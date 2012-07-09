package com.android.opengl.gameobject.vehicle;

import com.android.opengl.gameobject.base.GameObject;
import com.android.opengl.gameobject.base.Scene;
import com.android.opengl.gameobject.util.geometry.Point3D;
import com.android.opengl.logic.Actions;
import com.android.opengl.logic.Movable;

public abstract class AbstractVehicle extends GameObject implements Movable{

	public AbstractVehicle(Scene parentScene) {
		super(parentScene);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void moveTo(Point3D destination) {
		Actions.moveTo(this, destination);
	}

}
