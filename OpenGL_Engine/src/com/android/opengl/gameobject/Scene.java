package com.android.opengl.gameobject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES20;
import android.util.Log;

import com.android.opengl.Camera;
import com.android.opengl.R;
import com.android.opengl.shader.CommonShader;
import com.android.opengl.util.GLUtil;
import com.android.opengl.util.MeshQuadNode2D;
import com.android.opengl.util.geometry.Matrix;
import com.android.opengl.util.geometry.Point3D;
import com.android.opengl.util.geometry.Vector3D;
import com.android.opengl.view.control.GLButton;
import com.android.opengl.view.control.GLGridLayout;
import com.android.opengl.view.control.GLView;

public class Scene extends CommonGameObject{

	
	private static final float MIN_ANGLE = -90;
	private static final float MAX_ANGLE = 90;

	private static final float MAX_SCALE = 5;
	private static final float MIN_SCALE = 0.3f;
	
	private static final float SCALING_STEP = 10;
	
	private static final String TAG = Scene.class.getSimpleName();
	
	private List<GameObject> gameObjectList = new ArrayList<GameObject>();
	
	private MeshQuadNode2D sceneQuad2D;
	
	
//	protected float[] projectionMatrix = new float[16];
	protected Camera camera;

	
	private boolean isRendingFinished = true;
	private float[] positionXYZ = new float[4];
	
//	private Context context;

	
	public Scene(Camera camera) {
		super(new CommonShader(), camera.getContext().getResources());
//		this.context = context;
		this.camera = camera;
		this.camera.setScene(this);

		glGridLayout = new GLGridLayout(camera, 5, 10, 30, 0);
		
				
		GLView child = new GLButton(camera, 50, 20, 30, 10);
		GLView child1 = new GLButton(camera, 5, 2, 10, 10);
		GLView child2 = new GLButton(camera, 5, 2, 10, 10);
		GLView child3 = new GLButton(camera, 5, 2, 10, 5);
		GLView child4 = new GLButton(camera, 5, 2, 10, 5);
		GLView child5 = new GLButton(camera, 5, 2, 10, 5);
		GLView child6 = new GLButton(camera, 5, 2, 10, 5);
		child.setBackground(R.raw.bubble_background);
		child.setOnTapListener(new GLView.OnTapListener() {
			
			@Override
			public void onTap(GLView glView) {
				Log.i("tag", "another glview tapped: " + glView);
			}
		});
		glGridLayout.addChild(child1);
		glGridLayout.addChild(child2);
		glGridLayout.addChild(child3);
		glGridLayout.addChild(child4);
		glGridLayout.addChild(child5);
		glGridLayout.addChild(child6);
		glGridLayout.addChild(child);
		glGridLayout.setOnTapListener(new GLView.OnTapListener() {
			
			@Override
			public void onTap(GLView glView) {
				Log.i("tag", "glview tapped: " + glView);
			}
		});
		VboDataHandler vboDataHandler = vboDataHandlerMap.get(getClass().getSimpleName());
		sceneQuad2D = new MeshQuadNode2D(vboDataHandler.vertexData, vboDataHandler.indexData);
		rotate((float)Math.toRadians(45), (float)Math.toRadians(-30), 0);
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
		Matrix.multiplyMM(mvMatrix, 0, camera.getViewMatrix(), 0, modelMatrix, 0);//mvMatrix = viewMatrix;
		Matrix.multiplyMM(mvpMatrix, 0, camera.getProjectionMatrix(), 0, mvMatrix, 0);//mvMatrix = viewMatrix;
		super.drawFrame();
//		localDraw();

		for(GameObject gameObject: gameObjectList){
			gameObject.drawFrame();
		}
		glGridLayout.onDraw();
		isRendingFinished = true;
	}
	
	
	private void localDraw(){
		GLES20.glUseProgram(getShader().programHandle);

			
		GLES20.glUniform1f(shader.isSelectedHandle, isSelected?1:0);
        GLES20.glUniformMatrix4fv(shader.mvpMatrixHandle, 1, false, mvpMatrix, 0);

// using VBOs		

		VboDataHandler vboDataHandler = vboDataHandlerMap.get(getClass().getSimpleName());

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
	    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, shader.textureDataHandler);
	    GLES20.glUniform1i(shader.textureHandle, 0);

	    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboDataHandler.vboTextureCoordHandle);
		GLES20.glEnableVertexAttribArray(shader.textureCoordHandle);
		GLES20.glVertexAttribPointer(shader.textureCoordHandle, GLUtil.TEXTURE_SIZE, GLES20.GL_FLOAT, false, 0, 0);

		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboDataHandler.vboVertexHandle);
		GLES20.glEnableVertexAttribArray(shader.positionHandle);
		GLES20.glVertexAttribPointer(shader.positionHandle, GLUtil.VERTEX_SIZE, GLES20.GL_FLOAT, false, 0, 0);
		
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboDataHandler.vboNormalHandle);
		GLES20.glEnableVertexAttribArray(shader.normalHandle);
		GLES20.glVertexAttribPointer(shader.normalHandle, GLUtil.NORMAL_SIZE, GLES20.GL_FLOAT, false, 0, 0);

		
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
	


	
	public void scale(float scaleFactor) {
		float distacne = SCALING_STEP * (1 - 1/scaleFactor);
		camera.moveForward(distacne);
		notifyMVPMatrixChanged();
//		if(Math.abs(1 - scaleFactor) < EPSILON){
//			scaleFactor = 1;
//		}
//		scale *= scaleFactor;
//		float borderedScale = Math.min(MAX_SCALE, Math.max(MIN_SCALE, scale));
//		if(scale == borderedScale){
//			Matrix.scaleM(modelMatrix, 0, scaleFactor, scaleFactor, scaleFactor);
//			float[] newposition = getPosition().asFloatArray();
//			float dx = newposition[0] * scaleFactor - newposition[0];
//			float dz = newposition[2] * scaleFactor - newposition[2];
//			newposition[0] = newposition[0] + dx;
//			newposition[2] = newposition[2] + dz;
//
//			Log.i("tag", "scaleFactor = " + scaleFactor);
//			Log.i("tag", "newposition = " + newposition[0] + ", " + newposition[1] + ", " + newposition[2]);
//			setPosition(newposition);
//			notifyMVPMatrixChanged();
//		}
//		scale = borderedScale;
	}

	@Override
	public void setPosition(float centerX, float centerY, float centerZ) {
		super.setPosition(centerX, centerY, centerZ);
		positionXYZ = getPosition().asFloatArray();
		notifyMVPMatrixChanged();
	}

	@Override
	public void setPosition(float[] newCenterXYZ) {
		super.setPosition(newCenterXYZ);
		positionXYZ = getPosition().asFloatArray();
		notifyMVPMatrixChanged();
	}
	
	
	@Override
	public void rotate(float dx, float dy, float dz){
		rotate(new float[]{dx, dy, dz});
	}
	
	@Override
	public void rotate(float[] dAngleXYZ) {
		camera.rotate(dAngleXYZ[0], dAngleXYZ[1], dAngleXYZ[2]);
		notifyMVPMatrixChanged();
	}

	
	public void notifyMVPMatrixChanged(){
		Matrix.multiplyMM(mvMatrix, 0, camera.getViewMatrix(), 0, modelMatrix, 0);
		Matrix.multiplyMM(mvpMatrix, 0, camera.getProjectionMatrix(), 0, mvMatrix, 0);
	}
	private GLGridLayout glGridLayout;

	public void notifyViewportChanged(int width, int height) {
		notifyMVPMatrixChanged();
		glGridLayout.invalidate();
	}

	public void setViewMatrix(float[] viewMatrix) {
		camera.setViewMatrix(viewMatrix);
		notifyMVPMatrixChanged();
	}


	
	public float[] getProjectionMatrix() {
		return camera.getProjectionMatrix();
	}


	public float[] getViewMatrix() {
		return camera.getViewMatrix();
	}


	public boolean isRendingFinished() {
		return isRendingFinished;
	}
	
	List<GameObject> selectedObjectToMove = new ArrayList<GameObject>();
	
	public void checkObjectRayIntersection(Vector3D ray) {
		boolean isAnyGameObgectSelected = false;
		selectedObjectToMove.clear();		
		for(GameObject gameObject: gameObjectList){
			if(gameObject.isSelected()){
				selectedObjectToMove.add(gameObject);
			};
		}
		for(GameObject gameObject: gameObjectList){
			isAnyGameObgectSelected |= gameObject.checkObjectRayIntersection(ray);
		}
		if(!isAnyGameObgectSelected){
			//find intersection point with scene
			long time = System.currentTimeMillis();
			boolean res = sceneQuad2D.intersectionTest(ray);
			time = System.currentTimeMillis() - time;
			Log.d(TAG, "scene intersection test time = " + time/1000.0f +" sec.");
			if(res){
				Log.d(TAG, "intersection point with scene: " + ray.getTargetPoint());
				onObjectTap(ray.getTargetPoint());
			} 
		}
	}
	
	public void onObjectTap(Point3D point) {
		for(GameObject gameObject: selectedObjectToMove){
			Log.d(TAG, "moving " +selectedObjectToMove.getClass().getSimpleName());
			gameObject.moveTo(point);					
		}
	}

	public float getAltitude(float x, float z) {
		
		return sceneQuad2D.getAltitude(x, z);
	}

	@Override
	public int getMeshResource() {
		return R.raw.landscape;
	}


	@Override
	public void release() {
		super.release();
		Log.d(TAG, "deinit");
		for(GameObject gameObject: gameObjectList){
			gameObject.release();
		}
		vboDataHandlerMap.clear();
		if(meshLoader != null){
			meshLoader.release();
		}
	}
	
	public float[] getMVMatrix(){
		return mvMatrix;
	}
	public float[] getMVPMatrix(){
		return mvpMatrix;
	}
	
	public Resources getResources(){
		return resources;
	}


	public void translate(float dx, float dz) {
//		camera.translate(dx, dz);
		float[] position = getPosition().asFloatArray();
		float[] rotation = camera.getAngleXYZ();
		float sinY = (float)Math.sin(rotation[1]);
		float cosY = (float)Math.cos(rotation[1]);
		position[0] = position[0] + -dx * cosY + dz * sinY;
		position[2] = position[2] + -dx * sinY - dz * cosY;
		setPosition(position);
	}


//	public Context getContext() {
//		return context;
//	}
//
//
//	public void setContext(Context context) {
//		this.context = context;
//	}


	public GLView getGlView() {
		return glGridLayout;
	}








}
