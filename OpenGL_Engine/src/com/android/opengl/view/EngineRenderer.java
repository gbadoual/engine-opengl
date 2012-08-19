package com.android.opengl.view;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;

import com.android.opengl.Camera;
import com.android.opengl.gameobject.CommonGameObject;
import com.android.opengl.gameobject.Scene;
import com.android.opengl.gameobject.building.MainBase;
import com.android.opengl.gameobject.unit.Cube;
import com.android.opengl.gameobject.unit.Earth;
import com.android.opengl.gameobject.unit.vehicle.BMW;
import com.android.opengl.shader.CommonShader;
import com.android.opengl.shader.Shader;
import com.android.opengl.util.geometry.Point3D;
import com.android.opengl.util.geometry.Vector3D;
import com.android.opengl.view.control.GLButton;
import com.android.opengl.view.control.GLView;
import com.android.opengl.view.state.EngineState;
import com.android.opengl.view.state.GameInProgressState;
import com.android.opengl.view.state.LoadingLevelState;

public class EngineRenderer implements Renderer {

	
	private EngineState currentEngineState;
	
	private LoadingLevelState loadingLevelState;
	private GameInProgressState gameInProgressState;
	
	private Camera camera;
	



	private static final String TAG = EngineRenderer.class.getSimpleName();
	private long currentFrame = 0;
	private Handler callbackHandler;
//	private List<GameObject> gameObjectList = new ArrayList<GameObject>();
	private Cube cube1;
	private Cube cube2;
//	private BMW bmw1;
	private MainBase mainBase;
//	private GameObject bmw2;
	private Earth earth;
	private Scene scene;

//	private CommonShader shader;


	private int fps;
	private long prevTime;
	private WorldView worldView;
	private float[] resXYZ0 = new float[4];
	private float[] resXYZ1 = new float[4];
	

	public EngineRenderer(WorldView worldView, Handler handler) {
		this.worldView = worldView;
		this.callbackHandler = handler;
		initStates();
	}


	private void initStates() {
		loadingLevelState = new LoadingLevelState(this);
		gameInProgressState = new GameInProgressState(this);
	}


	@Override
	public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
		currentEngineState = loadingLevelState;
		currentEngineState.loadLevel();		
	}


	@Override
	public void onSurfaceChanged(GL10 arg0, int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		camera.setViewport(width, height);
	}

	@Override
	public void onDrawFrame(GL10 arg0) {
		currentFrame++;
		if(currentFrame <0){currentFrame = 0;}
		clearScreen();
		currentEngineState.onDrawFrame();
		countFPS();
	}



	public void initFpsCount() {
		prevTime = SystemClock.uptimeMillis();
	}


	public void initGameObjects() {
		CommonGameObject.facesCount = 0;
		this.camera = new Camera(worldView.getContext(), 100, 100);
		scene = new Scene(camera);
//		bmw1 = new BMW(scene);
//		bmw1.setPosition(-8, -7);

//		bmw2 = new BMW(scene);
		cube1 = new Cube(scene);
		cube1.setPosition(0, -6);
//		cube1.moveTo(new Point3D(-5, 0, 0));

		
		cube2 = new Cube(scene);
		cube2.setPosition(4, 4);

		earth = new Earth(scene);
		earth.setPosition(-6, 3);
//		mainBase = new MainBase(scene);
		
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

	


	public Scene getScene() {
		return scene;
	}


	public void onSingleTap(float x, float y) {
			
		int w = worldView.getWidth();
		int h = worldView.getHeight();
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
			if(scene != null){
				scene.release();				
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


		public boolean onTouchEvent(MotionEvent event) {
			return currentEngineState.onTouchEvent(event);
		}
		


		public EngineState getEngineState() {
			return currentEngineState;
		}


		public void setEngineState(EngineState engineState) {
			this.currentEngineState = engineState;
		}


		public LoadingLevelState getLoadingLevelState() {
			return loadingLevelState;
		}


		public void setLoadingLevelState(LoadingLevelState loadingLevelState) {
			this.loadingLevelState = loadingLevelState;
		}


		public GameInProgressState getGameInProgressState() {
			return gameInProgressState;
		}


		public void setGameInProgressState(GameInProgressState gameInProgressState) {
			this.gameInProgressState = gameInProgressState;
		}


		public Handler getCallbackHandler() {
			return callbackHandler;
		}


		public void setCallbackHandler(Handler callbackHandler) {
			this.callbackHandler = callbackHandler;
		}


//		public Shader getShader() {
//			return shader;
//		}
//
//
//		public void setShader(CommonShader shader) {
//			this.shader = shader;
//		}

	
}
