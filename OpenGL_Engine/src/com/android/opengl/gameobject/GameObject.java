package com.android.opengl.gameobject;

import java.util.ArrayList;
import java.util.List;

import android.opengl.GLES20;
import android.util.Log;

import com.android.opengl.Clan;
import com.android.opengl.gameobject.tools.attacking.AttackingTool;
import com.android.opengl.gameobject.tools.attacking.EmptyAttackingTool;
import com.android.opengl.gameobject.tools.moving.EmptyMovingTool;
import com.android.opengl.gameobject.tools.moving.MovingTool;
import com.android.opengl.shader.ObjectShader;
import com.android.opengl.util.GLUtil;
import com.android.opengl.util.ObjectOuterCube;
import com.android.opengl.util.ShaderManager;
import com.android.opengl.util.geometry.Matrix;
import com.android.opengl.util.geometry.Point3D;
import com.android.opengl.util.geometry.Vector3D;
import com.android.opengl.view.control.GLHealthBar;
import com.android.opengl.view.control.GLUnitIcon;

abstract public class GameObject extends CommonGameObject{

	private String TAG;
	
	
	protected Scene mParentScene;
	protected ObjectOuterCube mOuterCube;
	protected ObjectShader mShader = ShaderManager.getInstance().getShader(ObjectShader.class);
	
	protected float mCurSpeed;
	
	protected GLHealthBar mHealthBar;
	private GLUnitIcon mUnitIcon;

	protected float mHhealthLevel;
	private float mMaxHealthLevel;
	
	protected boolean mIsSelected;


	
	protected MovingTool mMovingTool;
	protected AttackingTool mAttackingTool;
	private List<PositionChangeListener> mPositionListenerList = new ArrayList<PositionChangeListener>();


	private boolean mIsAlive;


	private boolean mIsVisible = true;
	
	public GameObject(Scene parentScene) {
		super(parentScene.getResources());
		mIsAlive = true;
		vboDataHandlerMap.get(getClass().getSimpleName()).indexData = null;
		TAG = getClass().getSimpleName();
		setHealthLevel(100);
		mParentScene = parentScene;
		mParentScene.addGameObject(this);
		mOuterCube = new ObjectOuterCube(this);
		mMovingTool = new EmptyMovingTool(this);
		mAttackingTool = new EmptyAttackingTool(this);
		mHealthBar = new GLHealthBar(this);
		mUnitIcon = new GLUnitIcon(this);
		parentScene.getCamera().unregisterGLView(mUnitIcon);
	}

	
	public void onDrawFrame() {
		if(mParentScene.isRendingFinished()){
			throw new IllegalStateException("Scene shoud be rendered before rendering object of this scene");
		}
        Matrix.multiplyMM(mvpMatrix, 0, mParentScene.getMVPMatrix(), 0, modelMatrix, 0);
        Matrix.multiplyMM(mvMatrix, 0, mParentScene.getMVMatrix(), 0, modelMatrix, 0);
        openGLDraw();
	}


	private void openGLDraw() {
		VboDataHandler vboDataHandler = vboDataHandlerMap.get(getClass().getSimpleName());
		GLES20.glUseProgram(mShader.programHandle);
		GLES20.glUniform1f(mShader.isSelectedHandle, isSelected()?1:0);
		GLES20.glUniform4fv(mShader.clanColorHandle, 1, mClan.getColor(), 0);
		GLES20.glUniform1f(mShader.lightCountHandle, mParentScene.getLightListSize());
		GLES20.glUniform3fv(mShader.lightPositionHandle, mParentScene.getLightListSize(), mParentScene.lightListToFloatArray(), 0);
        GLES20.glUniformMatrix4fv(mShader.mvpMatrixHandle, 1, false, mvpMatrix, 0);
        GLES20.glUniformMatrix4fv(mShader.mvMatrixHandle, 1, false, mvMatrix, 0);

        GLUtil.passBufferToShader(vboDataHandler.vboTextureCoordHandle, mShader.textureCoordHandle, GLUtil.TEXTURE_SIZE);
		GLUtil.passBufferToShader(vboDataHandler.vboVertexHandle, mShader.positionHandle, GLUtil.VERTEX_SIZE_3D);
		GLUtil.passBufferToShader(vboDataHandler.vboNormalHandle, mShader.normalHandle, GLUtil.NORMAL_SIZE);

		for(int i = 0; i < 1; i++){
		    GLUtil.passTextureToShader(vboDataHandler.textureDataHandler, mShader.textureHandle);
		    GLES20.glUniform1f(mShader.instanceIdHandle, i);
			GLUtil.drawElements(vboDataHandler.vboIndexHandle, vboDataHandler.indexDataLength);
		}
	}


	public Scene getParentScene() {
		return mParentScene;
	}


	public ObjectOuterCube getOuterCube() {
		return mOuterCube;
	}
	
	public boolean checkObjectRayIntersection(Vector3D vector) {
		setSelected(mOuterCube.isIntersected(vector));
		return mIsSelected;
	}
	
	


	public void incPosition(float x, float z) {
		if(mParentScene == null){
			Log.w(TAG, "setPosition(x, z): parentScene is null");
			return;
		}
		float y = mParentScene.getAltitude(getPosX(), getPosZ());
		setPosition(getPosX() + x, y, getPosZ() + z);
	}

	public void setPosition(float x, float z) {
		if(mParentScene == null){
			Log.w(TAG, "setPosition(x, z): parentScene is null");
			return;
		}
		float y = mParentScene.getAltitude(x, z);
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
		mMovingTool.moveTo(destination);
	}
	
	public float getCurSpeed() {
		return mCurSpeed;
	}


	public MovingTool getMovingTool() {
		return mMovingTool;
	}


	public void setMovingTool(MovingTool movingTool) {
		this.mMovingTool = movingTool;
	}


	public AttackingTool getAttackingTool() {
		return mAttackingTool;
	}


	public void setAttackingTool(AttackingTool attackingTool) {
		this.mAttackingTool = attackingTool;
	}


	public float getMaxSpeed() {
		return mMovingTool.getMaxSpeed();
	}
	
	
	@Override
	public void release() {
		super.release();
		mMovingTool.cancelMove();
		mAttackingTool.cancelAttack();
		mHealthBar.release();
		mUnitIcon.release();
	}


	public boolean registerPositionListener(PositionChangeListener positionChangeListener){
		if(positionChangeListener != this && !mPositionListenerList.contains(positionChangeListener)){
			return mPositionListenerList.add(positionChangeListener);
		}
		return false;
	}
	
	public boolean unregisterPositionListener(PositionChangeListener movingToolStateListener){
		return mPositionListenerList.remove(movingToolStateListener);
	}
	
	private void notifyPositionChanged() {
		for(PositionChangeListener listener: mPositionListenerList){
			listener.onPositionChanged(getPosX(), getPosY(), getPosZ());
		}
	}


	public void decreaseLife(float damage) {
		mHhealthLevel-= damage;
		if(mHhealthLevel <= 0 && mIsAlive){
			destroy();
		}
	}
	
	public float getHealthLevel(){
		return mHhealthLevel;
	}


	private void destroy() {
		mIsAlive = false;
		mParentScene.removeGameObject(this);
	}


	public float getMaxHealthLevel() {
		return mMaxHealthLevel;
	}


	protected void setHealthLevel(float maxHealthLevel) {
		this.mMaxHealthLevel = maxHealthLevel;
		mHhealthLevel = maxHealthLevel;
	}


	public Clan getClan() {
		return mClan;
	}


	public void setClan(Clan mClan) {
		this.mClan = mClan;
	}

	public void moveToAttack(Point3D destination) {
		mMovingTool.moveToAttack(destination);
	}


	public boolean isAlive() {
		return mIsAlive;
	}

//TODO implement object-camera clipping
	public boolean isVisible() {
		return mIsVisible;
	}


	public void setVisible(boolean isVisible) {
		this.mIsVisible = isVisible;
	}

	public boolean isSelected() {
		return mIsSelected;
	}
	
	public void setSelected(boolean isSelected) {
		this.mIsSelected = isSelected;
	}
	
	public abstract int getUnitIconResId();


	public GLUnitIcon getUnitIconView(){
		return mUnitIcon;
	}
	public void setUnitIconResId(int unitIconResId) {
		mUnitIcon.setBackground(unitIconResId);
	}


}
