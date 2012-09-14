package com.android.opengl.gameobject.building;

import java.util.Random;

import com.android.opengl.Clan;
import com.android.opengl.gameobject.GameObject;
import com.android.opengl.gameobject.Scene;
import com.android.opengl.util.geometry.Point3D;

public abstract class AbstractBuilding extends GameObject {


	
	public AbstractBuilding(Scene parentScene, Clan clan) {
		super(parentScene);
		mClan = clan;
		setHealthLevel(1000);
	}
	
	private final float distance = 80;
	private Random r = new Random();

	
	protected void initBuildedObject(GameObject gameObject){
		gameObject.setPosition(getPosition());
		gameObject.setClan(mClan);
		float x = r.nextFloat() * distance - distance/2;
		float z = r.nextFloat() * distance - distance/2;
		float y = parentScene.getAltitude(x, z);
		gameObject.getMovingTool().moveTo(new Point3D(x, y, z));
	}
	

}
