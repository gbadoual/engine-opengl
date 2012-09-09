package com.android.opengl.gameobject.tools.attacking;

import android.util.Log;

import com.android.opengl.gameobject.GameObject;

public class SimpleGun extends AttackingTool{
	
<<<<<<< .mine
	public static float ATTACKING_RADIUS_SQUARE = 20 * 20;
	
=======
	public static float ATTACKING_RADIUS = 10;
>>>>>>> .r84
	public static float DAMAGE_VALUE = 10;
	
	public SimpleGun(GameObject attackingObject) {
		super(attackingObject, ATTACKING_RADIUS_SQUARE, DAMAGE_VALUE);
	}

	@Override
	public void attack(GameObject gameObjectToAttack) {
//		if(mAttackingRadius < MIN_ATTACKING_RADIUS){
//			Log.i(TAG, "Can not attack. Attacking radius is less than minimum possible (" +MIN_ATTACKING_RADIUS+")");
//			return;
//		}
		if(mAttackingObject == gameObjectToAttack){
			Log.i(TAG, "Object can't attack itself");
			return;
		}
		beginAttack(new BaseAttackingThread(mAttackingObject, gameObjectToAttack));		
	}
	

}
