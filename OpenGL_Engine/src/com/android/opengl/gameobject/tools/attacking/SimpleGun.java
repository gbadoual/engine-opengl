package com.android.opengl.gameobject.tools.attacking;

import com.android.opengl.gameobject.GameObject;

public class SimpleGun extends AttackingTool{
 
	public static float ATTACKING_RADIUS = 10;
	
	public static float DAMAGE_VALUE = 10;
	
	public SimpleGun(GameObject attackingObject) {
		super(attackingObject, ATTACKING_RADIUS, DAMAGE_VALUE);
	}

	@Override
	public BaseAttackingThread obtainAttackingThread() {
		return new BaseAttackingThread(mAttackingObject);
	}
	

}
