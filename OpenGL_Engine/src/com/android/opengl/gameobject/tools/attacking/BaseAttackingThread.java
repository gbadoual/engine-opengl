package com.android.opengl.gameobject.tools.attacking;

import android.util.Log;

import com.android.opengl.gameobject.GameObject;
import com.android.opengl.gameobject.PositionChangeListener;
import com.android.opengl.gameobject.tools.moving.BaseMovingThread;
import com.android.opengl.util.geometry.Point3D;
import com.android.opengl.util.geometry.Vector3D;

public class BaseAttackingThread extends Thread{
	
	private static final String TAG = BaseAttackingThread.class.getSimpleName();
	private GameObject attackingObject;
	private GameObject objectToAttack;
	
	
	
	public BaseAttackingThread(GameObject attackingObject, GameObject objectToAttack) {
		this.attackingObject = attackingObject;
		this.objectToAttack = objectToAttack;
	}

	@Override
	public void run() {
//		objectToAttack.registerPositionListener(positionChangeListener);
		AttackingTool objectAttackingTool = attackingObject.getAttackingTool();
		attackingObject.getMovingTool().cancelMove();
		while(!isInterrupted()){
			float distance = getSquaredDistance(attackingObject, objectToAttack);
			Log.i("taggg", "distance = " + distance);
			if(distance <= objectAttackingTool.mAttackingRadius){
				attackingObject.getMovingTool().cancelMove();
				objectAttackingTool.fire(objectToAttack);
				if(!objectToAttack.getParentScene().containsGameObject(objectToAttack)){
					interrupt();
				}

			} else {
//				objectAttackingTool.cancelAttack();
				if(!attackingObject.getMovingTool().isMoving()){
<<<<<<< .mine
					attackingObject.moveToAttack(objectToAttack.getPosition());
=======
					Log.i("taggg", "moving for attack");
					attackingObject.moveForAttackTo(objectToAttack.getPosition());
>>>>>>> .r84
				}
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				interrupt();
			}

//			Vector3D destVector = new Vector3D(attackingObject.getPosition(), lastApponentPos);
//			float attackingRadius = attackingObject.getAttackingTool().attackingRadius;
//			float len = destVector.getLength() - attackingRadius;
			
//			attackingObject.getMovingTool().beginFollowingObject(objectToAttack, attackingRadius, 0);

//			while(attackingObject.getMovingTool().isMoving()){
//				if(objectToAttack.getMovingTool().isMoving()){
//					
//				}
//			}
//			if(distance > attackingRadius){
//				followObjectToAttack();
//				if(isInterrupted()){
//					return;
//				}
//			}
			
		}
		Log.i("taggg", "attacking thread was interrupted");
	}
	
	@Override
	public void interrupt() {
		super.interrupt();
		objectToAttack.unregisterPositionListener(positionChangeListener);
	}

	private float getSquaredDistance(GameObject first, GameObject second){
		return Point3D.getSquaredDistance(first.getPosition(), second.getPosition());
	}
	
	
	public GameObject getObjectToAttack() {
		return objectToAttack;
	}

	public void setObjectToAttack(GameObject objectToAttack) {
		this.objectToAttack = objectToAttack;
	}


	private PositionChangeListener positionChangeListener = new PositionChangeListener() {
		// This callback can be executed on GLThread. Need to be careful
		@Override
		public void onPositionChanged(float x, float y, float z) {
			Log.i(TAG, "apponent's position has changed: " + new Point3D(x, y, z));
//			if(objectToAttack.get)
//			objectToAttack.getMovingTool()
		}
	};

}
