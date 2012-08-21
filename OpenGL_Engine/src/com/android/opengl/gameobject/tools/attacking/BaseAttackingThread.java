package com.android.opengl.gameobject.tools.attacking;

import com.android.opengl.gameobject.GameObject;
import com.android.opengl.util.geometry.Vector3D;

public class BaseAttackingThread extends Thread{
	
	private GameObject attackingObject;
	private GameObject objectToAttack;
	
	
	
	
	public BaseAttackingThread(GameObject attackingObject, GameObject objectToAttack) {
		this.attackingObject = attackingObject;
		this.objectToAttack = objectToAttack;
	}

	@Override
	public void run() {
		
		while(!isInterrupted()){
			float distance = getObjectObjectDistance(attackingObject, objectToAttack);
			
		}
	}
	
	private float getObjectObjectDistance(GameObject first, GameObject second){
		Vector3D distanceVector = new Vector3D(first.getPosition(), second.getPosition()).normalize();
		return distanceVector.getLength();
	}

}
