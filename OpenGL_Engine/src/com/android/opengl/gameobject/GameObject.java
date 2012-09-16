package com.android.opengl.gameobject;

import java.util.ArrayList;
import java.util.List;

import android.opengl.GLES20;
import android.util.Log;

import com.android.opengl.Clan;
import com.android.opengl.gameobject.CommonGameObject.VboDataHandler;
import com.android.opengl.gameobject.tools.attacking.AttackingTool;
import com.android.opengl.gameobject.tools.attacking.EmptyAttackingTool;
import com.android.opengl.gameobject.tools.moving.EmptyMovingTool;
import com.android.opengl.gameobject.tools.moving.MovingTool;
import com.android.opengl.shader.ObjectShader;
import com.android.opengl.util.GLUtil;
import com.android.opengl.util.ObjectOuterCube;
import com.android.opengl.util.geometry.Matrix;
import com.android.opengl.util.geometry.Point3D;
import com.android.opengl.util.geometry.Vector3D;
import com.android.opengl.view.control.GLHealthBar;

abstract public class GameObject extends CommonGameObject{

	private String TAG;
	
	
	protected Scene parentScene;
	protected ObjectOuterCube outerCube;
	protected ObjectShader shader = new ObjectShader();
	
	protected float curSpeed;
	
	private GLHealthBar healthBar;

	protected float healthLevel;
	private float maxHealthLevel;
	
	protected boolean isSelected;


	
	protected MovingTool movingTool;
	protected AttackingTool attackingTool;
	private List<PositionChangeListener> positionListenerList = new ArrayList<PositionChangeListener>();


	private boolean isAlive;


	private boolean isVisible = true;
	
	public GameObject(Scene parentScene) {
		super(parentScene.getResources());
		isAlive = true;
		vboDataHandlerMap.get(getClass().getSimpleName()).indexData = null;
		TAG = getClass().getSimpleName();
		setHealthLevel(100);
		this.parentScene = parentScene;
		this.parentScene.addGameObject(this);
		outerCube = new ObjectOuterCube(this);
		movingTool = new EmptyMovingTool(this);
		attackingTool = new EmptyAttackingTool(this);
		healthBar = new GLHealthBar(this);
		healthBar.setVisible(true);
	}

	
	public void onDrawFrame() {
		if(parentScene.isRendingFinished()){
			throw new IllegalStateException("Scene shoud be rendered before rendering object of this scene");
		}
        Matrix.multiplyMM(mvpMatrix, 0, parentScene.getMVPMatrix(), 0, modelMatrix, 0);
        Matrix.multiplyMM(mvMatrix, 0, parentScene.getMVMatrix(), 0, modelMatrix, 0);
        
        openGLDraw();
		
	}


	private void openGLDraw() {
		VboDataHandler vboDataHandler = vboDataHandlerMap.get(getClass().getSimpleName());
		GLES20.glUseProgram(shader.programHandle);
		GLES20.glUniform1f(shader.isSelectedHandle, isSelected()?1:0);
		GLES20.glUniform4fv(shader.clanColorHandle, 1, mClan.getColor(), 0);
		GLES20.glUniform1f(shader.lightCountHandle, parentScene.getLightListSize());
		GLES20.glUniform3fv(shader.lightPositionHandle, parentScene.getLightListSize(), parentScene.lightListToFloatArray(), 0);
        GLES20.glUniformMatrix4fv(shader.mvpMatrixHandle, 1, false, mvpMatrix, 0);
        GLES20.glUniformMatrix4fv(shader.mvMatrixHandle, 1, false, mvMatrix, 0);

        GLUtil.passBufferToShader(vboDataHandler.vboTextureCoordHandle, shader.textureCoordHandle, GLUtil.TEXTURE_SIZE);
		GLUtil.passBufferToShader(vboDataHandler.vboVertexHandle, shader.positionHandle, GLUtil.VERTEX_SIZE_3D);
		GLUtil.passBufferToShader(vboDataHandler.vboNormalHandle, shader.normalHandle, GLUtil.NORMAL_SIZE);

		for(int i = 0; i < 1; i++){
		    GLUtil.passTextureToShader(vboDataHandler.textureDataHandler, shader.textureHandle);
		    GLES20.glUniform1f(shader.instanceIdHandle, i);
			GLUtil.drawElements(vboDataHandler.vboIndexHandle, vboDataHandler.indexDataLength);
		}
	}


	public Scene getParentScene() {
		return parentScene;
	}


	public ObjectOuterCube getOuterCube() {
		return outerCube;
	}
	
	public boolean checkObjectRayIntersection(Vector3D vector) {
		setSelected(outerCube.isIntersected(vector));
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
		attackingTool.cancelAttack();
		healthBar.release();
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
		healthLevel-= damage;
		if(healthLevel <= 0 && isAlive){
			destroy();
		}
	}
	
	public float getHealthLevel(){
		return healthLevel;
	}


	private void destroy() {
		isAlive = false;
		//TODO "synchronized" is just workaround
		synchronized (parentScene) {
			parentScene.removeGameObject(this);
		}
	}


	public float getMaxHealthLevel() {
		return maxHealthLevel;
	}


	protected void setHealthLevel(float maxHealthLevel) {
		this.maxHealthLevel = maxHealthLevel;
		healthLevel = maxHealthLevel;
	}


	public Clan getClan() {
		return mClan;
	}


	public void setClan(Clan mClan) {
		this.mClan = mClan;
	}

	public void moveToAttack(Point3D destination) {
		movingTool.moveToAttack(destination);
	}


	public boolean isAlive() {
		return isAlive;
	}

//TODO implement object-camera clipping
	public boolean isVisible() {
		return isVisible;
	}


	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	public boolean isSelected() {
		return isSelected;
	}
	
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	


}
