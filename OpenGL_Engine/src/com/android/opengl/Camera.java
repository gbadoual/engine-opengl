package com.android.opengl;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;
import android.view.MotionEvent;

import com.android.opengl.gameobject.GLScene;
import com.android.opengl.listener.listenerholder.TouchEventListenerHolder;
import com.android.opengl.listener.listenerholder.ViewportChangeListenerHolder;
import com.android.opengl.util.geometry.Matrix;
import com.android.opengl.util.geometry.Point3D;
import com.android.opengl.util.geometry.Rect2D;
import com.android.opengl.view.EngineRenderer;
import com.android.opengl.view.Touchable;
import com.android.opengl.view.WorldView;
import com.android.opengl.view.control.GLIconGridLayout;
import com.android.opengl.view.control.GLSelectionRegion;
import com.android.opengl.view.control.GLTextView;
import com.android.opengl.view.control.GLView;

public class Camera implements Touchable{
	
	private static final int MATRIX_X_OFFSET = 12;
	private static final int MATRIX_Y_OFFSET = 13;
	private static final int MATRIX_Z_OFFSET = 14;
	
	private static final float CAMERA_MIN_DISTANCE = -80;
	private static final float CAMERA_MAX_DISTANCE = -5;
	private static final String TAG = Camera.class.getSimpleName();
	
	
	private float[] viewMatrix = new float[16];
	private float[] projectionMatrix = new float[16];
	private float[] vpMatrix = new float[16];
	
	private int mViewportWidth;
	private int mViewportHeight;
	
	private float aspectRatio;

	public static float screenToWorldRatioX;
	public static float screenToWorldRatioY;

	public static float percentToWorldRatioX;
	public static float percentToWorldRatioY;
	public static float percentToScreenRatio;
	public static float screenToPercentRatio;
	
	
	private float angleX;
	private float angleY;
	private float angleZ;
	
	private WorldView worldView;
//	private List<Renderable> mRenderableList = new ArrayList<Renderable>();
	private GLScene mScene;
	private List<GLView> glViewList = new ArrayList<GLView>();
//	private List<ViewportChangeListener> viewportChangeListenerHolder = new ArrayList<ViewportChangeListener>();

	private ViewportChangeListenerHolder viewportChangeListenerHolder = new ViewportChangeListenerHolder();
	private TouchEventListenerHolder touchEventListenerHolder = new TouchEventListenerHolder();

	
	public Camera(WorldView worldView) {
		this.worldView = worldView;
		worldView.registerToucheble(this, 10000);
		initViewMatrix(viewMatrix);
		setViewport(worldView.getMeasuredWidth(), worldView.getMeasuredHeight());
//		initControls();
	}
	
	GLTextView mGLTextView;
	GLIconGridLayout glUnitIconLayout;
	public void initControls() {
		clearCamera();
		mGLTextView = new GLTextView(this, "Airmole_Antique", 26);//setText("9:ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]");
		mGLTextView.setText("... =)");
		new Thread(){
			public void run() {
				String originalText = "1231435623";//"I love Aljonka! :*";//"qazw34rfgy678ijnmko90";
				final StringBuilder stepText = new StringBuilder();
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				for(int i = 0; i < originalText.length(); ++i){
					stepText.append(originalText.charAt(i));
					runOnGLThread(new Runnable() {
						public void run() {
							mGLTextView.setText(stepText.toString());
							
						}
					});
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} 
			};
		}.start();

		
		glUnitIconLayout = new GLIconGridLayout(this, 2, 2, 15, 0);
		glUnitIconLayout.setSpacing(1, 1);
		
		glUnitIconLayout.setVisible(false);
		GLSelectionRegion glSelectionRegion = new GLSelectionRegion(this);
		glSelectionRegion.registerSelectionListener(new GLSelectionRegion.RegionSelectionListener() {
			
			@Override
			public void onRegionSelected(Rect2D region) {
				if(mScene != null){
					Log.i("tag", "scene: region received (" + region + ")");
					mScene.checkRegionIntersection(region);
				}
			}
		});
		
	}
	
	public void notifySelectedObjectsChanged(){
		glUnitIconLayout.removeChildren();
		glUnitIconLayout.addUnitListToGrid(mScene.getSelectedObjects());
		if(!glUnitIconLayout.getChildren().isEmpty()){
			glUnitIconLayout.setVisible(true);
		} else{
			glUnitIconLayout.setVisible(false);
		}
	}

	private float[] calculateProjectionMatrix(int width, int height) {
		float ratio = (float) width / height;
		float left = -1;
		float right = 1;
		float bottom = -1;
		float top = 1;
		float near = 1;
		float far = 205;
		if(ratio > 1){
			top = 1/ratio;
			bottom = -1/ratio;
		} else{
			left = -ratio;
			right = ratio;
		}
		float [] projectionMatrix = new float[16];
		Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far);
		return projectionMatrix;
	}
	
	
	public void rotate(float dx, float dy, float dz){
		Point3D position = getPosition();
		angleX = (angleX + dx) % 360;
		angleY = (angleY + dy) % 360;
		angleZ = (angleZ + dz) % 360;
		float[] locaViewMatrix = new float[16];
		Matrix.setIdentityM(locaViewMatrix, 0);
		locaViewMatrix[12] = position.x;
		locaViewMatrix[13] = position.y;
		locaViewMatrix[14] = position.z;
		Matrix.rotateRad(locaViewMatrix, angleX, angleY, angleZ);
		viewMatrix = locaViewMatrix;
		setPosition(position);
	}
	
	public void moveForward(float distance){
		viewMatrix[MATRIX_Z_OFFSET] += distance;
		viewMatrix[MATRIX_Z_OFFSET] = Math.min(CAMERA_MAX_DISTANCE, Math.max(CAMERA_MIN_DISTANCE, viewMatrix[MATRIX_Z_OFFSET]));
		notifyVPMatrixChanged();
	}
	
	public void translate(float dx, float dz){
		float[] position = getPosition().asFloatArray().clone();
		float sinX = (float)Math.sin(angleX);
		float cosX = 1 - sinX * sinX;
		position[0] += -dx;
		position[1] += dz * sinX;
		position[2] += -dz * cosX;
		setPosition(position[0], position[1], position[2]);
	}
	

	public Point3D getPosition(){
		return new Point3D(viewMatrix[MATRIX_X_OFFSET], 
						   viewMatrix[MATRIX_Y_OFFSET], 
						   viewMatrix[MATRIX_Z_OFFSET]);
	}
	
	public void setPosition(Point3D position){
		setPosition(position.x, position.y, position.z);
	}
	
	public void setPosition(float x, float y, float z){
		viewMatrix[MATRIX_X_OFFSET] = x; 
		viewMatrix[MATRIX_Y_OFFSET] = y; 
		viewMatrix[MATRIX_Z_OFFSET] = z;
		notifyVPMatrixChanged();
	}
	
	public float[] getViewMatrix() {
		return viewMatrix;
	}

	public void setViewMatrix(float[] viewMatrix) {
		this.viewMatrix = viewMatrix;
		notifyVPMatrixChanged();
	}	
	
	private void initViewMatrix(float[] viewatrix) {

		final float eyeX = 0.0f;
		final float eyeY = 0.0f;
		final float eyeZ = 18.5f;

		// We are looking toward the distance
		final float lookX = 0.0f;
		final float lookY = 0.0f;
		final float lookZ = 1.0f;

		// Set our up vector. This is where our head would be pointing were we holding the camera.
		final float upX = 0.0f;
		final float upY = 1.0f;
		final float upZ = 0.0f;

		// Set the view matrix. This matrix can be said to represent the camera position.
		// NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
		// view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
		
		Matrix.setLookAtM(viewatrix , 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
	}



	public float[] getAngleXYZ() {
		// TODO Auto-generated method stub
		return new float[]{angleX, angleY, angleZ};
	}

	public float[] getProjectionMatrix() {
		return projectionMatrix;
	}

	public void setProjectionMatrix(float[] projectionMatrix) {
		this.projectionMatrix = projectionMatrix;
		notifyVPMatrixChanged();
	}

	public void setViewport(int width, int height) {
		Log.i(TAG, "viewport changed: new w/h = " + width + "/" + height);
		mViewportWidth = width;
		mViewportHeight = height;
		initRatioCoeffitients();
		GLES20.glViewport(0, 0, width, height);
		Rect2D newViewportRect = new Rect2D(0, 0, width, height);
		projectionMatrix = calculateProjectionMatrix(mViewportWidth, mViewportHeight);
		notifyVPMatrixChanged();
		viewportChangeListenerHolder.notifyListeners(newViewportRect);
//		for(ViewportChangeListener listener: viewportChangeListenerHolder){
//			listener.onViewportChanged(newViewportRect);
//		}
//		if(scene != null){
//			scene.notifyViewportChanged(width, height);
//		}
//		for(GLView glView: glViewList){
//			glView.invalidate();
//		}
	}

	private void initRatioCoeffitients() {
		aspectRatio = ((float)mViewportWidth)/mViewportHeight;
		percentToScreenRatio = Math.max(mViewportWidth, mViewportHeight) / 100f;
		screenToPercentRatio = 1 / percentToScreenRatio;
		screenToWorldRatioX = 2.0f / mViewportWidth;
		screenToWorldRatioY = 2.0f / mViewportHeight;
		if(aspectRatio > 1){
			percentToWorldRatioX = 2.0f / 100;
			percentToWorldRatioY = 2.0f * (aspectRatio/100);
		} else {
			percentToWorldRatioX = 2.0f / (100 * aspectRatio);
			percentToWorldRatioY = 2.0f / 100;
			screenToWorldRatioX = 1;
			screenToWorldRatioY = 1;
		}
		
		//this coefs should be recalculated here. Notification should be send to the listeners  
//		protected float percentToWorldRatioX;
//		protected float percentToWorldRatioY;
//
//		protected float percentToScreenRatio;
//		protected float screenToPercentRatio;
	}

	public int getViewportHeight() {
		return mViewportHeight;
	}

	public int getViewportWidth() {
		return mViewportWidth;
	}

	public Context getContext() {
		return worldView.getContext();
	}

	public GLScene getScene() {
		return mScene;
	}

	public void setScene(GLScene scene) {
		if(mScene != null){
			Log.w(TAG, "setScene: scene was alredy set");
		}
		mScene = scene;
	}

	public float[] getVpMatrix() {
		return vpMatrix;
	}
	
	
	private void notifyVPMatrixChanged(){
		Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
	}
	
	public boolean registerTouchable(Touchable touchable, int zOrder){
		return worldView.registerToucheble(touchable, zOrder);
	};
	public boolean unregisterTouchable(Touchable touchable){
		return worldView.unregisterToucheble(touchable);
	}
	
	public boolean registerGLView(GLView glView, int zOrder){

		boolean res = registerTouchable(glView, zOrder);
		if(!res){
			Log.w(TAG, "registerGLView(): unable to register glView");
			return false;
		}
		glView.setzOrder(zOrder);
		res = glViewList.add(glView);
		if(!res){
			Log.w(TAG, "registerGLView(): unable to register glView");
			unregisterTouchable(glView);
			return false;
		}
		Collections.sort(glViewList, new GLView.GLViewComparator());
		return res;
	}
	
	public boolean unregisterGLView(GLView glView){
		boolean res = unregisterTouchable(glView);
		res |= glViewList.remove(glView);
		return res;
	}
	public boolean containsGLView(GLView glView) {
		return glViewList.contains(glView);
	}
	

	public void onWorldUpdate(){
		mScene.onWorldUpdate();
	}
	
	public void onDrawFrame() {
		mScene.onDrawFrame();
		for(GLView glView: glViewList){
			glView.onDrawFrame();
		}		
	};
	
	public void release(){
		clearCamera();
		if(mScene != null){
			mScene.release();
			mScene = null;
		}
		viewportChangeListenerHolder.clear();
		worldView.unregisterToucheble(this);
	}

	public void notifyGLViewzOrderChanged() {
		Collections.sort(glViewList, new GLView.GLViewComparator());
	}

	public float getWidthToHeightRatio() {
		return aspectRatio;
	}

	public void clearCamera(){
		while(!glViewList.isEmpty()){
			GLView glView = glViewList.remove(0);
			glView.release();
			unregisterGLView(glView);
		}
	}

//	public boolean registerViewportChangeListener(ViewportChangeListener listener){
//		return viewportChangeListenerList.add(listener);
//	}
//
//	public boolean unregisterViewportChangeListener(ViewportChangeListener listener){
//		return viewportChangeListenerList.remove(listener);
//	}
	

	public void runOnGLThread(Runnable runnable) {
		worldView.queueEvent(runnable);		
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		touchEventListenerHolder.notifyListeners(event);
		return true;
	}

	@Override
	public Rect2D getBoundariesRectInPixel() {
		// TODO Auto-generated method stub
		return new Rect2D(0, 0, mViewportWidth, mViewportHeight);
	}

	public ViewportChangeListenerHolder getViewportChangeListenerHolder() {
		return viewportChangeListenerHolder;
	}

	public TouchEventListenerHolder getTouchEventListenerHolder() {
		return touchEventListenerHolder;
	}
	
	public EngineRenderer getEngineRenderer(){
		return worldView.getEngineRenderer();
	}

}
