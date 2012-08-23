package com.android.opengl.gameobject.tools.attacking;

import com.android.opengl.gameobject.GameObject;
import com.android.opengl.gameobject.PositionChangeListener;

public abstract class AttackingTool{
	
	protected static String TAG;
	protected static final float MIN_ATTACKING_RADIUS = 100;
	
	protected float mAttackingRadius;
	protected float mDamage;
	protected GameObject mAttackingObject;
	protected BaseAttackingThread mAttackingThread;
	
	
	public AttackingTool(GameObject attackingObject, float attackingRadius, float damage) {
		TAG = getClass().getSimpleName();
		this.mAttackingObject = attackingObject;
		mAttackingRadius = attackingRadius;
		mDamage = damage;
	}

	
	public abstract void attack(GameObject gameObjectToAttack);

	
	public void cancelPrevAttack(){
		if(mAttackingThread != null){
			mAttackingThread.interrupt();
		}
		
	}

	public float getAttackingRadius() {
		return mAttackingRadius;
	}

	protected void beginAttack(BaseAttackingThread baseAttackingThread) {
		cancelPrevAttack();
		this.mAttackingThread = baseAttackingThread;
		baseAttackingThread.start();
	}


	public void fire(GameObject objectToAttack) {
		objectToAttack.decreaseLife(mDamage);
	}


}
