package com.android.opengl.gameobject.tools.attacking;

import android.util.Log;

import com.android.opengl.gameobject.GameObject;
import com.android.opengl.gameobject.tools.moving.BaseMovingThread;
import com.android.opengl.util.geometry.Point3D;
import com.android.opengl.util.geometry.Vector3D;

public class BaseAttackingThread extends Thread{
	
	private static final String TAG = BaseAttackingThread.class.getSimpleName();
	private GameObject attackingObject;
	private GameObject objectToAttack;
	
	private Point3D lastApponentPos;
	
	
	public BaseAttackingThread(GameObject attackingObject, GameObject objectToAttack) {
		this.attackingObject = attackingObject;
		this.objectToAttack = objectToAttack;
		this.lastApponentPos = objectToAttack.getPosition();
	}

	@Override
	public void run() {
		
		while(!isInterrupted()){
			float distance = getDistance(attackingObject, objectToAttack);
			Vector3D destVector = new Vector3D(attackingObject.getPosition(), lastApponentPos);
			float attackingRadius = attackingObject.getAttackingTool().attackingRadius;
			float len = destVector.getLength() - attackingRadius;
			
			attackingObject.getMovingTool().beginFollowingObject(objectToAttack, attackingRadius, 0);

			while(attackingObject.getMovingTool().isMoving()){
				if(objectToAttack.getMovingTool().isMoving()){
					
				}
			}
//			if(distance > attackingRadius){
//				followObjectToAttack();
//				if(isInterrupted()){
//					return;
//				}
//			}
			
		}
	}
	

	private float getDistance(GameObject first, GameObject second){
		Vector3D distanceVector = new Vector3D(first.getPosition(), second.getPosition()).normalize();
		return distanceVector.getLength();
	}

}
