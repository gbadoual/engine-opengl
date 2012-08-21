package com.android.opengl.gameobject.tools.attacking;

import android.util.Log;

import com.android.opengl.gameobject.GameObject;

public class SimpleGun extends AttackingTool{
	
	public static float ATTACKING_RADIUS = 1000;
	
	public SimpleGun(GameObject attackingObject) {
		super(attackingObject);
		attackingRadius = ATTACKING_RADIUS;
	}

	@Override
	public void attack(GameObject gameObjectToAttack) {
		if(attackingRadius < MIN_ATTACKING_RADIUS){
			Log.i(TAG, "Can not attack. Attacking radius is less than minimum possible (" +MIN_ATTACKING_RADIUS+")");
			return;
		}
		beginAttack(gameObjectToAttack);		
	}
	
	@Override
	public void beginAttack(GameObject gameObjectToAttack) {
		super.beginAttack(gameObjectToAttack);
		attackingThread = new BaseAttackingThread(attackingObject, gameObjectToAttack);
		attackingThread.start();
	}

}
