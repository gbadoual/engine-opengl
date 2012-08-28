package com.android.opengl.gameobject;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.android.opengl.gameobject.tools.attacking.AttackingTool;
import com.android.opengl.gameobject.tools.attacking.EmptyAttackingTool;
import com.android.opengl.gameobject.tools.moving.EmptyMovingTool;
import com.android.opengl.gameobject.tools.moving.MovingTool;
import com.android.opengl.util.ObjectOuterCube;
import com.android.opengl.util.geometry.Matrix;
import com.android.opengl.util.geometry.Point3D;
import com.android.opengl.util.geometry.Vector3D;

abstract public class GameObject extends CommonGameObject{

	private String TAG;
	
	
	protected Scene parentScene;
	protected ObjectOuterCube outerCube;
	
	protected float curSpeed;
	protected float curHealthLevel;
	protected MovingTool movingTool;
	protected AttackingTool attackingTool;
	private List<PositionChangeListener> positionListenerList = new ArrayList<PositionChangeListener>();
	
	public GameObject(Scene parentScene) {
		super(parentScene.getShader(), parentScene.getResources());
		TAG = getClass().getSimpleName();
		this.parentScene = parentScene;
		this.parentScene.addGameObject(this);
		outerCube = new ObjectOuterCube(this);
		movingTool = new EmptyMovingTool(this);
		attackingTool = new EmptyAttackingTool(this);
	}

	
	public void onDrawFrame() {
		if(parentScene.isRendingFinished()){
			throw new IllegalStateException("Scene shoud be rendered before rendering object of this scene");
		}
        Matrix.multiplyMM(mvpMatrix, 0, parentScene.getMVPMatrix(), 0, modelMatrix, 0);
        Matrix.multiplyMM(mvMatrix, 0, parentScene.getMVMatrix(), 0, modelMatrix, 0);

		super.onDrawFrame();
		
	}


	public Scene getParentScene() {
		return parentScene;
	}


	public ObjectOuterCube getOuterCube() {
		return outerCube;
	}
	
	public boolean checkObjectRayIntersection(Vector3D vector) {
		isSelected = outerCube.isIntersected(vector);
		onObjectFocusChanged(isSelected);
		return isSelected;
	}
	
	


	public void incPosition(float x, float z) {
		if(parentScene == null){
			Log.w(TAG, "setPosition(x, z): parentScene is null");
			return;
		}
		float y = parentScene.getAltitude(getPosX(), getPosZ());
		setPosition(getPosX() + x, y, getPosZ() + z);
	}

	public void setPosition(float x, float z) {
		if(parentScene == null){
			Log.w(TAG, "setPosition(x, z): parentScene is null");
			return;
		}
		float y = parentScene.getAltitude(x, z);
		setPosition(x, y, z);
	}
	
	@Override
	public void setPosition(float x, float y, float z) {
		if(x != modelMatrix[Matrix.POS_X_OFFSET] ||
		   y != modelMatrix[Matrix.POS_Y_OFFSET] ||
		   z != modelMatrix[Matrix.POS_Z_OFFSET]){
			super.setPosition(x, y, z);
			notifyPositionChanged();
		}
	}
	
	@Override
	public void setPosition(float[] position) {
		if(position[0] != modelMatrix[Matrix.POS_X_OFFSET] ||
		   position[1] != modelMatrix[Matrix.POS_Y_OFFSET] ||
		   position[2] != modelMatrix[Matrix.POS_Z_OFFSET]){
			super.setPosition(position);
			notifyPositionChanged();
		}
	}

	@Override
	public void setPosition(Point3D position) {
		if(position.x != modelMatrix[Matrix.POS_X_OFFSET] ||
		   position.y != modelMatrix[Matrix.POS_Y_OFFSET] ||
		   position.z != modelMatrix[Matrix.POS_Z_OFFSET]){
		   super.setPosition(position);
		   notifyPositionChanged();
		}
	}



	
	
	public void moveTo(Point3D destination) {
		movingTool.moveTo(destination);
	}
	
	public float getCurSpeed() {
		return curSpeed;
	}


	public MovingTool getMovingTool() {
		return movingTool;
	}


	public void setMovingTool(MovingTool movingTool) {
		this.movingTool = movingTool;
	}


	public AttackingTool getAttackingTool() {
		return attackingTool;
	}


	public void setAttackingTool(AttackingTool attackingTool) {
		this.attackingTool = attackingTool;
	}


	public float getMaxSpeed() {
		return movingTool.getMaxSpeed();
	}
	
	
	@Override
	public void release() {
		super.release();
		movingTool.cancelMove();
	}


	public boolean registerPositionListener(PositionChangeListener positionChangeListener){
		if(positionChangeListener != this && !positionListenerList.contains(positionChangeListener)){
			return positionListenerList.add(positionChangeListener);
		}
		return false;
	}
	
	public boolean unregisterPositionListener(PositionChangeListener movingToolStateListener){
		return positionListenerList.remove(movingToolStateListener);
	}
	
	private void notifyPositionChanged() {
		for(PositionChangeListener listener: positionListenerList){
			listener.onPositionChanged(getPosX(), getPosY(), getPosZ());
		}
	}


	public void decreaseLife(float damage) {
		curHealthLevel-= damage;
		if(curHealthLevel <= 0){
			destroy();
		}
	}


	private void destroy() {
		parentScene.removeGameObject(this);
	}




	
	
	
	

	

}
