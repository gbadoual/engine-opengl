package com.android.opengl.gameobject;

import java.util.ArrayList;
import java.util.List;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.util.FloatMath;
import android.util.Log;

import com.android.opengl.Camera;
import com.android.opengl.Camera.ViewportChangeListener;
import com.android.opengl.R;
import com.android.opengl.gameobject.light.PointLight;
import com.android.opengl.shader.SceneShader;
import com.android.opengl.util.GLUtil;
import com.android.opengl.util.MeshQuadNode2D;
import com.android.opengl.util.geometry.Matrix;
import com.android.opengl.util.geometry.Plane;
import com.android.opengl.util.geometry.Point3D;
import com.android.opengl.util.geometry.Rect2D;
import com.android.opengl.util.geometry.Vector3D;
import com.android.opengl.view.control.GLSelectionRegion;

public class Scene extends CommonGameObject implements ViewportChangeListener{

	
//	private static final float MIN_ANGLE = -90;
//	private static final float MAX_ANGLE = 90;
//
//	private static final float MAX_SCALE = 5;
//	private static final float MIN_SCALE = 0.3f;
	
	private static final float SCALING_STEP = 10;
	
	private static final String TAG = Scene.class.getSimpleName();
	
	private List<GameObject> gameObjectList = new ArrayList<GameObject>();
	private List<PointLight> lightList = new ArrayList<PointLight>(); 

	
	private MeshQuadNode2D sceneQuad2D;
	
	
	private Camera mCamera;
	private SkyDome skyDome;
	private SceneShader shader = new SceneShader();

	
	private boolean isRendingFinished = true;
	
	public Scene(Camera camera) {
		super(camera.getContext().getResources());
		mCamera = camera;
		mCamera.setScene(this);
		mCamera.registerViewportChangeListener(this);
		skyDome = new SkyDome(camera);
		lightList.add(new PointLight(new Point3D(-50, 5, 0)));
		lightList.add(new PointLight(new Point3D(40, 5, 40)));

		VboDataHandler vboDataHandler = vboDataHandlerMap.get(getClass().getSimpleName());
		sceneQuad2D = new MeshQuadNode2D(vboDataHandler.vertexData, vboDataHandler.indexData);
		rotate((float)Math.toRadians(45), (float)Math.toRadians(-30), 0);
		GLSelectionRegion glSelectionRegion = new GLSelectionRegion(camera);
		glSelectionRegion.registerSelectionListener(new GLSelectionRegion.RegionSelectionListener() {
			
			@Override
			public void onRegionSelected(Rect2D region) {
				Log.i("tag", "scene: region received (" + region + ")");
				checkRegionIntersection(region);

			}
		});

	}
	

	public void checkRegionIntersection(Rect2D region) {
		Plane[] boundingPlanes = getUnprojectedPlanes(region);
		for(GameObject gameObject: gameObjectList){
			if(gameObject.isVisible()){
				boolean isWithinRect = true;
				for(Plane plane: boundingPlanes){
//					Matrix.multiplyMV(resPosition, 0, gameObject.getModelMatrix(), 0, gameObject.getPosition().asFloatArray(), 0);
					if(plane.getPointPlainPosition(gameObject.getPosition().asFloatArray()) == Plane.RelativePosition.NEGATIVE_HALFPLANE){
						isWithinRect = false;
						break;
					};
				}
				gameObject.setSelected(isWithinRect);
			}else{
				gameObject.setSelected(false);
			}
		}
		
	}


	protected Plane[] getUnprojectedPlanes(Rect2D region) {
		float[] pointsToUnproj = new float[3 * 8];
		float left = region.mLeftCoord;
		float top = region.mTopCoord; 
		float right = left + region.mWidth;
		float bottom = top + region.mHeight;
		top = mCamera.getViewportHeight() - top;
		bottom = mCamera.getViewportHeight() - bottom;
		pointsToUnproj[0 * 3 + 0] = left;
		pointsToUnproj[0 * 3 + 1] = top;
		pointsToUnproj[0 * 3 + 2] = 0;
		
		pointsToUnproj[1 * 3 + 0] = left;
		pointsToUnproj[1 * 3 + 1] = top;
		pointsToUnproj[1 * 3 + 2] = 1;
		
		pointsToUnproj[2 * 3 + 0] = right;
		pointsToUnproj[2 * 3 + 1] = top;
		pointsToUnproj[2 * 3 + 2] = 0;
		
		pointsToUnproj[3 * 3 + 0] = right;
		pointsToUnproj[3 * 3 + 1] = top;
		pointsToUnproj[3 * 3 + 2] = 1;
		
		pointsToUnproj[4 * 3 + 0] = right;
		pointsToUnproj[4 * 3 + 1] = bottom;
		pointsToUnproj[4 * 3 + 2] = 0;
		
		pointsToUnproj[5 * 3 + 0] = right;
		pointsToUnproj[5 * 3 + 1] = bottom;
		pointsToUnproj[5 * 3 + 2] = 1;
		
		pointsToUnproj[6 * 3 + 0] = left;
		pointsToUnproj[6 * 3 + 1] = bottom;
		pointsToUnproj[6 * 3 + 2] = 0;
		
		pointsToUnproj[7 * 3 + 0] = left;
		pointsToUnproj[7 * 3 + 1] = bottom;
		pointsToUnproj[7 * 3 + 2] = 1;

		int[] screenDimens = new int[]{0, 0, Scene.this.mCamera.getViewportWidth(), Scene.this.mCamera.getViewportHeight()};
		float[] unprojectedPoints = new float[pointsToUnproj.length];
		GLUtil.glUnproj(pointsToUnproj, getMVMatrix(), getProjectionMatrix(), screenDimens, unprojectedPoints);
		Plane[] boundingPlanes = new Plane[5];
		boundingPlanes[0] = getLeftPlane(unprojectedPoints);
		boundingPlanes[1] = getTopPlane(unprojectedPoints);
		boundingPlanes[2] = getRightPlane(unprojectedPoints);
		boundingPlanes[3] = getBottomPlane(unprojectedPoints);
		boundingPlanes[4] = getNearPlane(unprojectedPoints);
		
		return boundingPlanes;
	}


	private Plane getNearPlane(float[] unprojectedPoints) {
		
		return getPlane(2, 0, 4, unprojectedPoints);
	}


	private Plane getBottomPlane(float[] unprojectedPoints) {
		return getPlane(5, 4, 7, unprojectedPoints);
	}


	private Plane getRightPlane(float[] unprojectedPoints) {
		return getPlane(5, 3, 4, unprojectedPoints);
	}


	private Plane getTopPlane(float[] unprojectedPoints) {
		return getPlane(3, 1, 2, unprojectedPoints);
	}


	private Plane getLeftPlane(float[] unprojectedPoints) {
		return getPlane(1, 7, 0, unprojectedPoints);
	}
	
	private Plane getPlane(int f, int s, int t, float[] unprojectedPoints){
		float x = unprojectedPoints[f * 3 + 0];
		float y = unprojectedPoints[f * 3 + 1];
		float z = unprojectedPoints[f * 3 + 2];
		Point3D pointOnPlane = new Point3D(x, y, z);

		float[] v1 = new float[3];
		v1[0] = unprojectedPoints[s * 3 + 0] - x;
		v1[1] = unprojectedPoints[s * 3 + 1] - y;
		v1[2] = unprojectedPoints[s * 3 + 2] - z;
		
		float[] v2 = new float[3];
		v2[0] = unprojectedPoints[t * 3 + 0] - x;
		v2[1] = unprojectedPoints[t * 3 + 1] - y;
		v2[2] = unprojectedPoints[t * 3 + 2] - z;
		
		Vector3D normal = new Vector3D(Vector3D.vectorProduct(v1, v2));
		return new Plane(pointOnPlane, normal);
	}


	public boolean addGameObject(GameObject gameObject){
		return this.gameObjectList.add(gameObject);
	}

	public boolean removeGameObject(GameObject gameObject){
		boolean res = gameObjectList.remove(gameObject);
		if(gameObject != null){
			gameObject.release();
		}
		return res;
	}
	
	public void clearGameObjectList(){
		gameObjectList.clear();
	}
	
	
	private float a = 0.1f;
	@Override
	public void onDrawFrame() {
		isRendingFinished = false;
		skyDome.onDrawFrame();
		Matrix.multiplyMM(mvMatrix, 0, mCamera.getViewMatrix(), 0, modelMatrix, 0);//mvMatrix = viewMatrix;
		Matrix.multiplyMM(mvpMatrix, 0, mCamera.getProjectionMatrix(), 0, mvMatrix, 0);//mvMatrix = viewMatrix;

		//		localDraw();
		openGLDraw();

		//TODO "synchronized" is just workaround
		synchronized (this) {
			for(GameObject gameObject: gameObjectList){
				gameObject.onDrawFrame();
			}
		}

        GLES20.glUseProgram(0);
        lightList.get(0).getPosition().incXYZ(a, 0, 0);
        if(Math.abs(lightList.get(0).getPosition().x) > 50){
        	a = -a;
        }
		
		isRendingFinished = true;
	}
	

	
	private void openGLDraw() {
		VboDataHandler vboDataHandler = vboDataHandlerMap.get(getClass().getSimpleName());
		GLES20.glUseProgram(shader.programHandle);
        GLES20.glUniformMatrix4fv(shader.mvpMatrixHandle, 1, false, mvpMatrix, 0);
        GLES20.glUniformMatrix4fv(shader.mvMatrixHandle, 1, false, mvMatrix, 0);
		GLES20.glUniform1f(shader.lightCountHandle, lightList.size());

		GLES20.glUniform3fv(shader.lightPositionHandle, lightList.size(), lightListToFloatArray(), 0);

        GLUtil.passBufferToShader(vboDataHandler.vboTextureCoordHandle, shader.textureCoordHandle, GLUtil.TEXTURE_SIZE);
		GLUtil.passBufferToShader(vboDataHandler.vboVertexHandle, shader.positionHandle, GLUtil.VERTEX_SIZE_3D);
		GLUtil.passBufferToShader(vboDataHandler.vboNormalHandle, shader.normalHandle, GLUtil.NORMAL_SIZE);

		for(int i = 0; i < 1; i++){
		    GLUtil.passTextureToShader(vboDataHandler.textureDataHandler, shader.textureHandle);
		    GLES20.glUniform1f(shader.instanceIdHandle, i);
			GLUtil.drawElements(vboDataHandler.vboIndexHandle, vboDataHandler.indexDataLength);
		}
	}


	public void scale(float scaleFactor) {
		float distacne = SCALING_STEP * (1 - 1/scaleFactor);
		mCamera.moveForward(distacne);
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
		mCamera.rotate(dAngleXYZ[0], dAngleXYZ[1], dAngleXYZ[2]);
		notifyMVPMatrixChanged();
	}

	
	public void notifyMVPMatrixChanged(){
		Matrix.multiplyMM(mvMatrix, 0, mCamera.getVpMatrix(), 0, modelMatrix, 0);
	}

	public void setViewMatrix(float[] viewMatrix) {
		mCamera.setViewMatrix(viewMatrix);
		notifyMVPMatrixChanged();
	}


	
	public float[] getProjectionMatrix() {
		return mCamera.getProjectionMatrix();
	}


	public float[] getViewMatrix() {
		return mCamera.getViewMatrix();
	}


	public boolean isRendingFinished() {
		return isRendingFinished;
	}
	
	List<GameObject> selectedObjectToMove = new ArrayList<GameObject>();
	
	//TODO "synchronized" is just workaround
	public synchronized void checkObjectRayIntersection(Vector3D ray) {
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
		Log.d(TAG, "release");
		if(mCamera != null){
			mCamera.unregisterViewportChangeListener(this);
		}
		for(GameObject gameObject: gameObjectList){
			gameObject.release();
		}

		vboDataHandlerMap.clear();
		if(meshLoader != null){
			meshLoader.release();
		}
		super.release();
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
		float[] position = getPosition().asFloatArray().clone();
		float[] rotation = mCamera.getAngleXYZ();
		float sinY = FloatMath.sin(rotation[1]);
		float cosY = FloatMath.cos(rotation[1]);
		position[0] = position[0] + -dx * cosY + dz * sinY;
		position[2] = position[2] + -dx * sinY - dz * cosY;
		setPosition(position);
	}




	public Camera getCamera() {
		return mCamera;
	}


	public void setCamera(Camera camera) {
		this.mCamera = camera;
	}

	public float[] lightListToFloatArray(){
		float[] lights = new float[lightList.size() * 3];
		float[] res = new float[4];
		for(int i = 0 ; i < lightList.size(); ++i){
			Matrix.multiplyMV(res, 0, mvMatrix, 0, lightList.get(i).getPosition().asFloatArray(), 0);
			lights[i * 3 + 0] = res[0];
			lights[i * 3 + 1] = res[1];
			lights[i * 3 + 2] = res[2];
		}
		return lights;		
	}

	public int getLightListSize() {
		return lightList.size();
	}


	public void onWorldUpdate() {
		
	}


	@Override
	public void onViewportChanged(Rect2D newViewportRect) {
		notifyMVPMatrixChanged();
	}




}
