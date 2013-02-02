package com.android.opengl.gameobject.tools.attacking;

import android.util.Log;

import com.android.opengl.gameobject.GameObject;

public abstract class AttackingTool{
	
	protected static String TAG;
//	protected static final float MIN_ATTACKING_RADIUS = 100;
	
	protected float mAttackingRadiusSquared;
	protected float mDamage;
	protected GameObject mAttackingObject;
	protected BaseAttackingThread mAttackingThread;
	
	
	public AttackingTool(GameObject attackingObject, float attackingRadius, float damage) {
		TAG = AttackingTool.class.getSimpleName() + ": " + getClass().getSimpleName();
		this.mAttackingObject = attackingObject;
		mAttackingRadiusSquared = attackingRadius * attackingRadius;
		mDamage = damage;
	}

	
	public void attack(GameObject gameObjectToAttack){
		if(gameObjectToAttack == null){
			Log.i(TAG, "incoming gameObjectToAttack is null. Skipping the attack");
			return;
		}
		if(mAttackingObject == gameObjectToAttack){
			Log.i(TAG, "Object can't attack itself");
			return;
		}
		beginAttack(gameObjectToAttack);				
	};
	public abstract BaseAttackingThread obtainAttackingThread();

	
	public void cancelAttack(){
		if(mAttackingThread != null){
			mAttackingThread.interrupt();
			mAttackingThread = null;
			Log.i(TAG, "Attack was cancelled: " + this);
		}
		
	}

	public float getAttackingRadius() {
		return mAttackingRadiusSquared;
	}

	protected void beginAttack(GameObject gameObjectToAttack) {
		cancelAttack();
//		if(gameObjectToAttack.getClan() == mAttackingObject.getClan()){
//			Log.w(TAG, "No friendly fire");
//			return;
//		}
		mAttackingThread = obtainAttackingThread();
		if(mAttackingThread != null){
			Log.i(TAG, "Begin attack: " + mAttackingObject.getClass().getSimpleName() + " -> " + gameObjectToAttack.getClass().getSimpleName());
			mAttackingThread.setObjectToAttack(gameObjectToAttack);
			mAttackingThread.start();
		} else {
			Log.i(TAG, "Attacking thread is null. The object cannot attack anybody");
		}
	}


	public void fire(final GameObject objectToAttack) {
		objectToAttack.getParentScene().getCamera().runOnGLThread(new Runnable() {
			
			@Override
			public void run() {
				objectToAttack.decreaseLife(mDamage);
			}
		});
	}


}
