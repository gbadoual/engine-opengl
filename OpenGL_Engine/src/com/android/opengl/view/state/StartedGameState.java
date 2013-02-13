package com.android.opengl.view.state;

import android.opengl.GLU;
import android.util.Log;
import android.view.MotionEvent;

import com.android.opengl.Camera;
import com.android.opengl.gameobject.GLScene;
import com.android.opengl.gameobject.unit.vehicle.BMW;
import com.android.opengl.util.geometry.Point3D;
import com.android.opengl.util.geometry.Vector3D;
import com.android.opengl.view.EngineRenderer;
import com.android.opengl.view.GestureDetector;
import com.android.opengl.view.state.startedgamestate.BaseStartedGameSubstate;
import com.android.opengl.view.state.startedgamestate.GameInProgressState;
import com.android.opengl.view.state.startedgamestate.LoadingLevelState;

public class StartedGameState extends GameState{
	
	private static final String TAG = StartedGameState.class.getSimpleName();
	
	private BaseStartedGameSubstate mCurrentState;
	private GameInProgressState mGameInProgressState;
	private LoadingLevelState mLoadingLevelState;
	
	private GestureDetector gestureDetector;
	private Camera mCamera;
	
//	private Cube cube1;
//	private Cube cube2;
	private BMW bmw1;
//	private MainBase mainBase;\
////	private GameObject bmw2;
//	private Earth earth;
	private GLScene mScene;
	private float[] resXYZ0 = new float[4];
	private float[] resXYZ1 = new float[4];
	
	
	public StartedGameState(EngineRenderer engineRenderer) {
		super(engineRenderer);
		gestureDetector = new GestureDetector(engineRenderer.getContext(), gestureListener);
		initStates();
	}
	
	private void initStates() {
		setLoadingLevelState(new LoadingLevelState(this));
		mGameInProgressState = new GameInProgressState(this);
	}

	@Override
	public void startGame() {
		super.startGame();
		mCamera = mEngineRenderer.getCamera();
		mCurrentState = mLoadingLevelState;
		mCurrentState.loadLevel();
	}

	@Override
	public void onWorldUpdate() {
		mCurrentState.onWorldUpdate();
	}		

	@Override
	public void onDrawFrame() {
		mCurrentState.onDrawFrame();
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		gestureDetector.onTouchEvent(event);
		return true;
	}
	
	
	
	public void onSingleTap(float x, float y) {
		
		int w = mCamera.getViewportWidth();
		int h = mCamera.getViewportHeight();
		y = h - y;

		if(mScene == null){
			return;
		}
		GLU.gluUnProject(x, y, 0, mScene.getMVMatrix(), 0, mScene.getProjectionMatrix(), 0, new int[]{0, 0, w, h}, 0, resXYZ0, 0);
		Point3D srcPoint = new Point3D(resXYZ0);
		GLU.gluUnProject(x, y, 1, mScene.getMVMatrix(), 0, mScene.getProjectionMatrix(), 0, new int[]{0, 0, w, h}, 0, resXYZ1, 0);
		Point3D destPoint = new Point3D(resXYZ1);

		Vector3D vector = new Vector3D(srcPoint, destPoint);
		mScene.checkObjectRayIntersection(vector);
		Log.i("tag", "ray = "+vector);
		Log.i("tag", "-------------------------------");
		}

		public void rotateScene(float angleX, float angleY, float angleZ) {
			if(mScene == null){
				Log.w(TAG, "rotateScene(): scene is null");
				return;
			}
			mScene.rotate(angleX, angleY, angleZ);
		}
		

		public void scaleScene(float scaleFactor) {
			if(mScene == null){
				Log.w(TAG, "scaleScene: scene is null");
				return;
			}
			mScene.scale(scaleFactor);
			
		}


		public void translateScene(float dx, float dz) {
			if(mScene == null){
				Log.w(TAG, "translateScene: scene is null");
				return;
			}
			
			mScene.translate(dx, dz);
		}

	
	
	private GestureDetector.OnGestureListener gestureListener = new GestureDetector.OnGestureListener() {

		private static final float SLIDE_FACTOR = (float) (Math.PI / 180);
		private static final float TRANSLATE_FACTOR = 10;

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			translateScene(distanceX / TRANSLATE_FACTOR, distanceY / TRANSLATE_FACTOR);
			return true;
		}
	
		@Override
		public boolean onRotate(float centerX, float centerY, float angle) {
			rotateScene(0, angle ,0);
//			Log.i("tag", "onRotate: angle = " + angle);
			return true;
		}
		@Override
		public boolean onDoubleSlide(float distanceX, float distanceY) {
			rotateScene(distanceY * SLIDE_FACTOR, 0 ,0);
//			Log.i("tag", "onDoubleSlide: distanceY = " + distanceY);
			return true;
		}	
		@Override
		public boolean onPinch(float centerX, float centerY, float scaleFactor) {
			scaleScene(scaleFactor);
//			Log.i("tag", "onPinch: scaleFactor = " + scaleFactor);
			return true;
		}
		
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			onSingleTap(e.getX(), e.getY());
			return true;
		}
	
		@Override
		public boolean onDown(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			// TODO Auto-generated method stub
			return false;
		}
		@Override
		public void onLongPress(MotionEvent e) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void onShowPress(MotionEvent e) {
			// TODO Auto-generated method stub
			
		}
	
	};


	public Camera getCamera() {
		return mCamera;
	}

	public BaseStartedGameSubstate getCurrentState() {
		return mCurrentState;
	}

	public void setCurrentState(BaseStartedGameSubstate mCurrentState) {
		this.mCurrentState = mCurrentState;
	}

	public GameInProgressState getGameInProgressState() {
		return mGameInProgressState;
	}

	public void setGameInProgressState(GameInProgressState gameInProgressState) {
		mGameInProgressState = gameInProgressState;
	}

	public LoadingLevelState getLoadingLevelState() {
		return mLoadingLevelState;
	}

	public void setLoadingLevelState(LoadingLevelState loadingLevelState) {
		mLoadingLevelState = loadingLevelState;
	}

	public void setScene(GLScene scene) {
		mScene = scene;
	}

	public GLScene getScene() {
		return mScene;
	}

	@Override
	public boolean onBackPressed() {
		return false;
	}



}
