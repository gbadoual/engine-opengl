package com.android.opengl.gameobject;

import android.util.Log;

import com.android.opengl.gameobject.tools.attacking.AttackingTool;
import com.android.opengl.gameobject.tools.attacking.EmptyAttackingTool;
import com.android.opengl.gameobject.tools.moving.EmptyMovingTool;
import com.android.opengl.gameobject.tools.moving.MovingTool;
import com.android.opengl.gameobject.util.ObjectOuterCube;
import com.android.opengl.gameobject.util.geometry.Matrix;
import com.android.opengl.gameobject.util.geometry.Point3D;
import com.android.opengl.gameobject.util.geometry.Vector3D;

abstract public class GameObject extends CommonGameObject{

	private String TAG;
	
	
	protected Scene parentScene;
	protected ObjectOuterCube outerCube;
	
	protected float curSpeed;
	protected float curHealthLevel;
	protected MovingTool movingTool;
	protected AttackingTool attackingTool;
	
	public GameObject(Scene parentScene) {
		super(parentScene.shader, parentScene.getResources());
		TAG = getClass().getSimpleName();
		this.parentScene = parentScene;
		this.parentScene.addGameObject(this);
		outerCube = new ObjectOuterCube(this);
		movingTool = new EmptyMovingTool();
		attackingTool = new EmptyAttackingTool();
	}

	
	public void drawFrame() {
		if(parentScene.isRendingFinished()){
			throw new IllegalStateException("Scene shoud be rendered before rendering object of this scene");
		}
        Matrix.multiplyMM(mvpMatrix, 0, parentScene.getMVPMatrix(), 0, modelMatrix, 0);
        Matrix.multiplyMM(mvMatrix, 0, parentScene.getMVMatrix(), 0, modelMatrix, 0);

		super.drawFrame();
		
	}


	public Scene getParentScene() {
		return parentScene;
	}


	public ObjectOuterCube getOuterCube() {
		return outerCube;
	}
	
	public boolean setIsSelected(Vector3D vector) {
		return isSelected = outerCube.isIntersected(vector);
	}
	
	
	public void incPosition(float x, float z) {
		if(parentScene == null){
			Log.w(TAG, "setPosition(x, z): parentScene is null");
			return;
		}
		Point3D position = getPosition();
		float y = parentScene.getAltitude(position.x, position.z);
		setPosition(position.x + x, y, position.z + z);
	}

	public void setPosition(float x, float z) {
		if(parentScene == null){
			Log.w(TAG, "setPosition(x, z): parentScene is null");
			return;
		}
		float y = parentScene.getAltitude(x, z);
		setPosition(x, y, z);
	}

	
	public void setAltitude(float alt){
		modelMatrix[13] = alt;
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
		movingTool.stop();
	}



	

	

}
