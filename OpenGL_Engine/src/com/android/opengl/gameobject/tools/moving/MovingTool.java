package com.android.opengl.gameobject.tools.moving;

import java.util.ArrayList;
import java.util.List;

import com.android.opengl.gameobject.GameObject;
import com.android.opengl.util.geometry.Point3D;

public abstract class MovingTool{
	
	public static enum MovingToolState{
		IDLE,
		MOVING,
		ARRIVED,
		MOVEMENT_IMPOSSIBLE,
		CANCELED
	}
	

	protected BaseMovingThread movingThread;
	
	protected GameObject objectToMove;
	private MovingToolState curState;
	private List<MovingToolStateListener> toolStateListeners = new ArrayList<MovingTool.MovingToolStateListener>();
	
	
	public MovingTool(GameObject objectToMove) {
		this.objectToMove = objectToMove;
	}

	
	public boolean isMoving(){
		return movingThread != null && movingThread.isAlive();
	}


	
	public void moveTo(Point3D destination){
		objectToMove.getAttackingTool().cancelAttack();
		beginMove(destination);
	};

	public void moveToAttack(Point3D destination) {
		beginMove(destination);
	}
	
	protected void beginMove(Point3D destination){
		cancelMove();
		this.movingThread = obtainMovingThread();
		if(this.movingThread != null){
			this.movingThread.setDestination(destination);
			this.movingThread.start();
		}
	}

	protected abstract BaseMovingThread obtainMovingThread();


	public void cancelMove() {
		if(movingThread != null){
			movingThread.interruptCanceled();
			movingThread = null;
		}
	}
	public abstract float getMaxSpeed();
	
	
	public MovingToolState getCurState() {
		return curState;
	}


	public void setCurState(MovingToolState curState) {
		if(this.curState != curState){
			this.curState = curState;
			notifyToolStateListeners();
		}
	}
	
	private void notifyToolStateListeners() {
		for(MovingToolStateListener stateListener: toolStateListeners){
			stateListener.onToolStateChanged(this.curState);
		}
	}


	public void registerStateListener(MovingToolStateListener stateListener){
		toolStateListeners.add(stateListener);
	}
	public void unregisterStateChangeListener(MovingToolStateListener stateListener){
		toolStateListeners.remove(stateListener);
	}


	public static interface MovingToolStateListener{
		public void onToolStateChanged(MovingToolState newState);
	}




}
