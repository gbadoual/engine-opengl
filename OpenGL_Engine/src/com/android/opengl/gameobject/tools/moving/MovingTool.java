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
	protected Point3D curDestination;
	private MovingToolState curState;
	private List<MovingToolStateListener> toolStateListeners = new ArrayList<MovingTool.MovingToolStateListener>();
	
	
	public MovingTool(GameObject objectToMove) {
		this.objectToMove = objectToMove;
	}

	
	public boolean isMoving(){
		return movingThread != null && movingThread.isAlive();
	}


	
<<<<<<< .mine
	public void moveTo(Point3D destination){
		objectToMove.getAttackingTool().cancelAttack();
		cancelMove();
		curDestination = destination;
		beginMove();
	};

	public void moveToAttack(Point3D destination) {
		cancelMove();
		curDestination = destination;
		beginMove();
	}
	
	protected void beginMove(){
		this.movingThread = obtainMovingThread();
		if(this.movingThread != null){
			this.movingThread.setDestination(curDestination);
			this.movingThread.start();
		}
	}

	protected abstract BaseMovingThread obtainMovingThread();

=======
	public void moveTo(Point3D destination){
		objectToMove.getAttackingTool().cancelAttack();
		beginMove(new BaseMovingThread(objectToMove, destination));
	};
	
	public void moveForAttackTo(Point3D destination) {
		beginMove(new BaseMovingThread(objectToMove, destination));
	}
	
	protected void beginMove(BaseMovingThread baseMovingThread){
		cancelMove();
		this.movingThread = baseMovingThread;
		this.movingThread.start();
		
	}

>>>>>>> .r84

	public void cancelMove() {
		if(movingThread != null){
			movingThread.interruptCanceled();
			movingThread = null;
		}
		curDestination = null;
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
