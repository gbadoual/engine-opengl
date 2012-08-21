package com.android.opengl.gameobject.tools.attacking;

import com.android.opengl.gameobject.GameObject;

public abstract class AttackingTool {
	
	protected static String TAG;
	protected static final float MIN_ATTACKING_RADIUS = 100;
	
	protected float attackingRadius;
	protected GameObject attackingObject;
	protected BaseAttackingThread attackingThread;
	
	
	public AttackingTool(GameObject attackingObject) {
		TAG = getClass().getSimpleName();
		this.attackingObject = attackingObject;
	}

	
	public abstract void attack(GameObject gameObjectToAttack);

	
	public void prepareAttack(){
		if(attackingThread != null){
			attackingThread.interrupt();
		}
	}

	public float getAttackingRadius() {
		return attackingRadius;
	}

	public void beginAttack(GameObject gameObjectToAttack) {
		prepareAttack();
	}


}
