package com.android.opengl.view;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;

import com.android.opengl.Camera;
import com.android.opengl.Clan;
import com.android.opengl.gameobject.CommonGameObject;
import com.android.opengl.gameobject.GLScene;
import com.android.opengl.gameobject.building.MainBase;
import com.android.opengl.util.geometry.Point3D;
import com.android.opengl.util.geometry.Rect2D;
import com.android.opengl.util.geometry.Vector3D;
import com.android.opengl.view.state.EngineState;
import com.android.opengl.view.state.GameInProgressState;
import com.android.opengl.view.state.LoadingLevelState;
import com.android.opengl.view.state.MainScreenState;

public class EngineRenderer implements Renderer, Touchable{

	
	private EngineState mCurrentEngineState;
	
	private LoadingLevelState mLoadingLevelState;
	private GameInProgressState mGameInProgressState;
	private MainScreenState mMainScreenState;
	
	private Camera mCamera;
	
	
	private Rect2D mViewBoundaries;	



	private static final String TAG = EngineRenderer.class.getSimpleName();
	private long currentFrame = 0;
	private Handler callbackHandler;
//	private List<GameObject> gameObjectList = new ArrayList<GameObject>();
//	private Cube cube1;
//	private Cube cube2;
//	private BMW bmw1;
//	private MainBase mainBase;
////	private GameObject bmw2;
//	private Earth earth;
	private GLScene scene;



	private int fps;
	private long prevTime;
	private WorldView worldView;
	private float[] resXYZ0 = new float[4];
	private float[] resXYZ1 = new float[4];
	

	public EngineRenderer(WorldView worldView, Handler handler) {
		this.worldView = worldView;
		this.callbackHandler = handler;
		this.mViewBoundaries = new Rect2D(worldView.getLeft(), worldView.getTop(), worldView.getWidth(), worldView.getHeight());
		initStates();
	}


	private void initStates() {
		mLoadingLevelState = new LoadingLevelState(this);
		mGameInProgressState = new GameInProgressState(this);
		mMainScreenState = new MainScreenState(this, (Activity)worldView.getContext());
	}


	@Override
	public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
		mCamera = new Camera(worldView);
//		mCurrentEngineState = mLoadingLevelState;
//		mCurrentEngineState.loadLevel();
		mCurrentEngineState = mMainScreenState;
		mCurrentEngineState.showMainScreen();

	}


	@Override
	public void onSurfaceChanged(GL10 arg0, int width, int height) {
		mCamera.setViewport(width, height);
		mViewBoundaries.mWidth = width;
		mViewBoundaries.mHeight = height;
	}

	@Override
	public void onDrawFrame(GL10 arg0) {
		gameLoopStep();
	}



	private void gameLoopStep() {
		currentFrame++;
		if(currentFrame <0){currentFrame = 0;}
		clearScreen();
		mCurrentEngineState.onWorldUpdate();
		mCurrentEngineState.onDrawFrame();
		countFPS();
	}


	public void initFpsCount() {
		prevTime = SystemClock.uptimeMillis();
	}


	public void initGameObjects() {
		CommonGameObject.facesCount = 0;
		
		scene = new GLScene(mCamera);
//		scene.setPosition(centerX, centerY, centerZ)
//		bmw1 = new BMW(scene);
//		bmw1.setPosition(-8, -7);

//		bmw2 = new BMW(scene);
//		cube1 = new Cube(scene);
//		cube1.setPosition(0, -6);
//		cube1.moveTo(new Point3D(-5, 0, 0));

		
//		cube2 = new Cube(scene);
//		cube2.setPosition(4, 4);
//
//		earth = new Earth(scene);
//		earth.setPosition(-6, 3);
		MainBase mainBase = new MainBase(scene, Clan.BLUE);
		mainBase.setPosition(-40, 40);

		MainBase enemyMainBase = new MainBase(scene, Clan.RED);
		enemyMainBase.setPosition(40, -40);
		
//		for(int i = 0; i < 4; ++i){
//			gameObjectList.add(new BMW(scene));
//		}
	}




	private void clearScreen() {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
	}




	private void countFPS() {
		fps++;
		if(SystemClock.uptimeMillis() - prevTime>= 1000){
//			Log.i("tag", "fps = "+fps);
			worldView.updateFPS(fps, CommonGameObject.facesCount);
			fps = 0;
			prevTime = SystemClock.uptimeMillis();			
		}
	}

	


	public GLScene getScene() {
		return scene;
	}


	public void onSingleTap(float x, float y) {
			
		int w = mCamera.getViewportWidth();
		int h = mCamera.getViewportHeight();
		y = h - y;

		if(scene == null){
			return;
		}
		GLU.gluUnProject(x, y, 0, scene.getMVMatrix(), 0, scene.getProjectionMatrix(), 0, new int[]{0, 0, w, h}, 0, resXYZ0, 0);
		Point3D srcPoint = new Point3D(resXYZ0);
		GLU.gluUnProject(x, y, 1, scene.getMVMatrix(), 0, scene.getProjectionMatrix(), 0, new int[]{0, 0, w, h}, 0, resXYZ1, 0);
		Point3D destPoint = new Point3D(resXYZ1);

		Vector3D vector = new Vector3D(srcPoint, destPoint);
		scene.checkObjectRayIntersection(vector);
		Log.i("tag", "ray = "+vector);
		Log.i("tag", "-------------------------------");
		}


		public void release() {
			if(mCamera != null){
				mCamera.release();				
			} 
		}

		public void rotateScene(float angleX, float angleY, float angleZ) {
			if(scene == null){
				Log.w(TAG, "rotateScene(): scene is null");
				return;
			}
			scene.rotate(angleX, angleY, angleZ);
		}
		

		public void scaleScene(float scaleFactor) {
			if(scene == null){
				Log.w(TAG, "scaleScene: scene is null");
				return;
			}
			scene.scale(scaleFactor);
			
		}


		public void translateScene(float dx, float dz) {
			if(scene == null){
				Log.w(TAG, "translateScene: scene is null");
				return;
			}
			
			scene.translate(dx, dz);
		}


		public Context getContext() {
			return worldView.getContext();
		}


		@Override
		public boolean onTouchEvent(MotionEvent event) {
			return mCurrentEngineState.onTouchEvent(event);
		}
		


		public EngineState getEngineState() {
			return mCurrentEngineState;
		}


		public void setEngineState(EngineState engineState) {
			this.mCurrentEngineState = engineState;
		}


		public LoadingLevelState getLoadingLevelState() {
			return mLoadingLevelState;
		}


		public void setLoadingLevelState(LoadingLevelState loadingLevelState) {
			this.mLoadingLevelState = loadingLevelState;
		}


		public GameInProgressState getGameInProgressState() {
			return mGameInProgressState;
		}


		public void setGameInProgressState(GameInProgressState gameInProgressState) {
			this.mGameInProgressState = gameInProgressState;
		}


		public Handler getCallbackHandler() {
			return callbackHandler;
		}


		public void setCallbackHandler(Handler callbackHandler) {
			this.callbackHandler = callbackHandler;
		}


		@Override
		public Rect2D getBoundariesRectInPixel() {
			return mViewBoundaries;
		}


		public Camera getCamera() {
			return mCamera;
		}


	
}
