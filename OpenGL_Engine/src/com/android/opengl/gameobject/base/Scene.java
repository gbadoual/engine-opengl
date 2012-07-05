package com.android.opengl.gameobject.base;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.android.opengl.R;
import com.android.opengl.gameobject.util.MeshQuadNode2D;
import com.android.opengl.gameobject.util.geometry.Vector3D;
import com.android.opengl.logic.Movable;

public class Scene extends CommonGameObject{

	
	private static final float MIN_ANGLE = -90;
	private static final float MAX_ANGLE = 90;
	private static final String TAG = Scene.class.getSimpleName();
	
	private List<GameObject> gameObjectList = new ArrayList<GameObject>();
	
	private MeshQuadNode2D sceneQuad2D;
	
	
	protected float[] projectionMatrix = new float[16];
	private float[] vpMatrix = new float[16];
	
	private boolean isRendingFinished = true;
	
	public Scene(Context context, int programHandle, float[] projectionMatrix) {
		super(programHandle, context.getResources());
		this.projectionMatrix = projectionMatrix;
		setupModelMatrix(modelMatrix);
		VboDataHandler vboDataHandler = vboDataHandlerMap.get(getClass().getSimpleName());
		sceneQuad2D = new MeshQuadNode2D(vboDataHandler.vertexData, vboDataHandler.indexData);
//		setCenterXYZ(modelMatrix[12],modelMatrix[13], modelMatrix[14]);
		rotate(60, -30, 0);
	}
	

	public boolean addGameObject(GameObject gameObject){
		return this.gameObjectList.add(gameObject);
	}

	public boolean removeGameObject(GameObject gameObject){
		return this.gameObjectList.remove(gameObject);
	}
	
	public void clearGameObjectList(){
		gameObjectList.clear();
	}
	
	@Override
	public void drawFrame() {
		isRendingFinished = false;
//		rotate(0, -0.5f, 0);
		mvpMatrix = vpMatrix;
		super.drawFrame();
//		localDraw();

//		for(GameObject gameObject: gameObjectList){
//			gameObject.drawFrame();
//		}
	}
	
	private void localDraw(){
		GLES20.glUseProgram(programHandle);

			
			GLES20.glUniform1f(isSelectedHandle, isSelected?1:0);
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

// using VBOs		

		VboDataHandler vboDataHandler = vboDataHandlerMap.get(getClass().getSimpleName());

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
	    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, vboDataHandler.textureDataHandler);
	    GLES20.glUniform1i(vboDataHandler.textureUniformHandle, 0);

	    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboDataHandler.vboTextureHandle);
		GLES20.glEnableVertexAttribArray(vboDataHandler.textureCoordHandle);
		GLES20.glVertexAttribPointer(vboDataHandler.textureCoordHandle, TEXTURE_ELEMENT_SIZE, GLES20.GL_FLOAT, false, 0, 0);

		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboDataHandler.vboVertexHandle);
		GLES20.glEnableVertexAttribArray(positionHandle);
		GLES20.glVertexAttribPointer(positionHandle, VERTEX_ELEMENT_SIZE, GLES20.GL_FLOAT, false, 0, 0);
		
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboDataHandler.vboNormalHandle);
		GLES20.glEnableVertexAttribArray(normalHandle);
		GLES20.glVertexAttribPointer(normalHandle, NORMAL_ELEMENT_SIZE, GLES20.GL_FLOAT, false, 0, 0);

		
		IntBuffer indexBuffer;
		ArrayList<Integer> indexList = (ArrayList<Integer>) sceneQuad2D.getIndexToDrawList().clone();
		int[] indexData = new int[indexList.size()];
		for(int i = 0; i < indexData.length; ++i){
			if(indexList == null){
				return;
			}
			indexData[i] = indexList.get(i);
		}

			indexBuffer = ByteBuffer.allocateDirect(indexData.length * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
			indexBuffer.put(indexData).position(0);
				
			
			GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, vboDataHandler.vboIndexHandle);
			GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * 4, indexBuffer, GLES20.GL_DYNAMIC_DRAW);

			GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, vboDataHandler.vboIndexHandle);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexData.length, GLES20.GL_UNSIGNED_INT, 0);
			
			
			indexBuffer.limit(0);
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
			GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
				
			GLES20.glUseProgram(0);
	}
	
	
	
	@Override
	public void rotate(float dx, float dy, float dz){
		rotate(new float[]{dx, dy, dz});
	}
	
	@Override
	public void rotate(float[] dAngleXYZ) {
		angleXYZ[0] = (angleXYZ[0] + dAngleXYZ[0])%360;
		angleXYZ[1] = (angleXYZ[1] + dAngleXYZ[1])%360;
		angleXYZ[2] = (angleXYZ[2] + dAngleXYZ[2])%360;
		if(angleXYZ[0]<MIN_ANGLE ){
			angleXYZ[0] = MIN_ANGLE;
		}
		if(angleXYZ[0]>MAX_ANGLE ){
			angleXYZ[0] = MAX_ANGLE;
		}
		float [] localViewMatrix = new float[16];
		Matrix.setIdentityM(localViewMatrix, 0);
		setupModelMatrix(localViewMatrix);
		if(angleXYZ[0] != 0) Matrix.rotateM(localViewMatrix, 0, angleXYZ[0], 1, 0, 0);
		if(angleXYZ[1] != 0) Matrix.rotateM(localViewMatrix, 0, angleXYZ[1], 0, 1, 0);
		if(angleXYZ[2] != 0) Matrix.rotateM(localViewMatrix, 0, angleXYZ[2], 0, 0, 1);
		this.setModelMatrix(localViewMatrix);
	}
	
	@Override
	public void setPosition(float centerX, float centerY, float centerZ) {
		super.setPosition(centerX, centerY, centerZ);
		notifyVPMatrixChanged();
	}
	
	@Override
	public void setPosition(float[] newCenterXYZ) {
		super.setPosition(newCenterXYZ);
		notifyVPMatrixChanged();
	}
	
	private void setupModelMatrix(float[] localViewMatrix) {

		final float eyeX = 0.0f;
		final float eyeY = 0.0f;
		final float eyeZ = 18.5f;

		// We are looking toward the distance
		final float lookX = 0.0f;
		final float lookY = 0.0f;
		final float lookZ = 5.0f;

		// Set our up vector. This is where our head would be pointing were we holding the camera.
		final float upX = 0.0f;
		final float upY = 1.0f;
		final float upZ = 0.0f;

		// Set the view matrix. This matrix can be said to represent the camera position.
		// NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
		// view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
		
		Matrix.setLookAtM(localViewMatrix , 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
	}

	
	public void notifyVPMatrixChanged(){
			Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, modelMatrix, 0);
	}

	public void setProjectionMatrix(float[] projectionMatrix) {
		this.projectionMatrix = projectionMatrix;
		notifyVPMatrixChanged();
	}

	@Override
	public void setModelMatrix(float[] modelMatrix) {
		super.setModelMatrix(modelMatrix);
		notifyVPMatrixChanged();
	}


	
	public float[] getProjectionMatrix() {
		return projectionMatrix;
	}



	public float[] getVpMatrix() {
			return vpMatrix;
	}

	public void setRendingFinished(boolean isRendingFinished) {
		this.isRendingFinished = isRendingFinished;
	}

	public boolean isRendingFinished() {
		return isRendingFinished;
	}
	
	public void setIsSelected(Vector3D ray) {
		boolean isAnyGameObgectSelected = false;
		Movable selectedObjectToMove = null;
		for(GameObject gameObject: gameObjectList){
			if(gameObject.isSelected() && gameObject instanceof Movable){
				selectedObjectToMove = (Movable)gameObject;
				break;
			};
		}
		for(GameObject gameObject: gameObjectList){
			isAnyGameObgectSelected |= gameObject.setIsSelected(ray);
		}
		if(!isAnyGameObgectSelected){
			//find intersection point with scene
			long time = System.currentTimeMillis();
			boolean res = sceneQuad2D.intersectionTest(ray);
			time = System.currentTimeMillis() - time;
			if(res){
				Log.d(TAG, "intersection point with scene: " + ray.getTargetPoint()+", time = " + time/1000.0f +" sec.");
				if(selectedObjectToMove != null){
					Log.d(TAG, "moving " +selectedObjectToMove.getClass().getSimpleName());
					selectedObjectToMove.moveTo(ray.getTargetPoint());					
				}
			} else{
				Log.d(TAG, "no intersection with scene detected, time = " + time/1000.0f +" sec.");
			}
		}
	}


	@Override
	public int getMeshResource() {
		return R.raw.scene;
	}


	public void deinit() {
		Log.d(TAG, "deinit");
		vboDataHandlerMap.clear();		
	}
	
	public Resources getResources(){
		return resources;
	}

}
