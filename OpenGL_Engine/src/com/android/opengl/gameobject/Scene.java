package com.android.opengl.gameobject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.res.Resources;
import android.util.FloatMath;
import android.util.Log;

import com.android.opengl.Camera;
import com.android.opengl.R;
import com.android.opengl.shader.CommonShader;
import com.android.opengl.util.MeshQuadNode2D;
import com.android.opengl.util.geometry.Matrix;
import com.android.opengl.util.geometry.Point3D;
import com.android.opengl.util.geometry.Vector3D;
import com.android.opengl.view.control.GLView;

public class Scene extends CommonGameObject{

	
//	private static final float MIN_ANGLE = -90;
//	private static final float MAX_ANGLE = 90;
//
//	private static final float MAX_SCALE = 5;
//	private static final float MIN_SCALE = 0.3f;
	
	private static final float SCALING_STEP = 10;
	
	private static final String TAG = Scene.class.getSimpleName();
	
	private List<GameObject> gameObjectList = new ArrayList<GameObject>();
	private List<GLView> glViewList = new ArrayList<GLView>();
	
	private MeshQuadNode2D sceneQuad2D;
	
	
	private Camera camera;

	
	private boolean isRendingFinished = true;
	
	public Scene(Camera camera) {
		super(new CommonShader(), camera.getContext().getResources());
		this.camera = camera;
		this.camera.setScene(this);
		skyBox = new SkyDome(camera);

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
	
	private SkyDome skyBox;
	
	@Override
	public void drawFrame() {
		isRendingFinished = false;
		skyBox.onDrawFrame();
//		rotate(0, -0.5f, 0);
		Matrix.multiplyMM(mvMatrix, 0, camera.getViewMatrix(), 0, modelMatrix, 0);//mvMatrix = viewMatrix;
		Matrix.multiplyMM(mvpMatrix, 0, camera.getProjectionMatrix(), 0, mvMatrix, 0);//mvMatrix = viewMatrix;
		super.drawFrame();
//		localDraw();

		for(GameObject gameObject: gameObjectList){
			gameObject.drawFrame();
		}
		for(GLView glView: glViewList){
			glView.onDraw();
		}
		
		isRendingFinished = true;
	}
	
	public boolean registerGLView(GLView glView, int zOrder){
		boolean res = getCamera().registerTouchable(glView, zOrder);
		if(!res){
			Log.w(TAG, "registerGLView(): unable to register glView");
			return false;
		}
		glView.setzOrder(zOrder);
		res = glViewList.add(glView);
		if(!res){
			Log.w(TAG, "registerGLView(): unable to register glView");
			getCamera().unregisterTouchable(glView);
			return false;
		}
		Collections.sort(glViewList, new GLView.GLViewComparator());
		return res;
	}
	
	public boolean unregisterGLView(GLView glView){
		getCamera().unregisterTouchable(glView);
		return glViewList.remove(glView);
	}
	
	public void scale(float scaleFactor) {
		float distacne = SCALING_STEP * (1 - 1/scaleFactor);
		camera.moveForward(distacne);
		notifyMVPMatrixChanged();
	}

	@Override
	public void setPosition(float centerX, float centerY, float centerZ) {
		super.setPosition(centerX, centerY, centerZ);
		notifyMVPMatrixChanged();
	}

	@Override
	public void setPosition(float[] newCenterXYZ) {
		super.setPosition(newCenterXYZ);
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
		Matrix.multiplyMM(mvMatrix, 0, camera.getVpMatrix(), 0, modelMatrix, 0);
	}

	public void notifyViewportChanged(int width, int height) {
		notifyMVPMatrixChanged();
		for(GLView glView: glViewList){
			glView.invalidate();
		}
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
		List<GameObject> prevSelectedObj = new ArrayList<GameObject>(selectedObjectToMove);

		for(GameObject gameObject: gameObjectList){
			isAnyGameObgectSelected |= gameObject.checkObjectRayIntersection(ray);
			if(gameObject.isSelected() && !prevSelectedObj.isEmpty()){
				//Begin attack
				for(GameObject attackingObj: prevSelectedObj){
					attackingObj.getAttackingTool().attack(gameObject);
				}
			}
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
		return R.raw.scene;
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
		float sinY = FloatMath.sin(rotation[1]);
		float cosY = FloatMath.cos(rotation[1]);
		position[0] = position[0] + -dx * cosY + dz * sinY;
		position[2] = position[2] + -dx * sinY - dz * cosY;
		setPosition(position);
	}




	public Camera getCamera() {
		return camera;
	}


	public void setCamera(Camera camera) {
		this.camera = camera;
	}


	public boolean containsGameObject(GameObject objectToAttack) {
		return gameObjectList.contains(objectToAttack);
	}


	public boolean containsGLView(GLView glView) {
		return glViewList.contains(glView);
	}


}
