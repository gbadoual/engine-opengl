package com.android.opengl.view;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;

import com.android.opengl.Shader;
import com.android.opengl.gameobject.Cube;
import com.android.opengl.gameobject.Earth;
import com.android.opengl.gameobject.base.CommonGameObject;
import com.android.opengl.gameobject.base.GameObject;
import com.android.opengl.gameobject.base.Scene;
import com.android.opengl.gameobject.util.geometry.Point3D;
import com.android.opengl.gameobject.util.geometry.Vector3D;
import com.android.opengl.gameobject.vehicle.BMW;

public class WorldRenderer implements Renderer {


	private static final String TAG = WorldRenderer.class.getSimpleName();
	private long currentFrame = 0;
	private Handler callbackHandler;
//	private List<GameObject> gameObjectList = new ArrayList<GameObject>();
	private Cube cube1;
	private Cube cube2;
//	private BMW bmw1;
//	private GameObject bmw2;
	private Earth earth;
	private Scene scene;

	private Shader shader;
	private int programHandle;


	private int fps;
	private long prevTime;
	private WorldView worldView;
	private float x;
	private float y;
	private float[] resXYZ0 = new float[4];
	private float[] resXYZ1 = new float[4];
	private float[] screenXYZ = new float[16];
	

	public WorldRenderer(WorldView worldView, Handler handler) {
		this.worldView = worldView;
		this.callbackHandler = handler;
	}


	@Override
	public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
		long time = System.currentTimeMillis();
		callbackHandler.sendEmptyMessage(WorldView.DIALOG_LOADING_SHOW);
		// Use culling to remove back faces.
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		// Enable depth testing
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		shader = new Shader();
		programHandle = shader.createAndLinkProgram();
		
		initGameObjects();
		initFpsCount();
		callbackHandler.sendEmptyMessage(WorldView.DIALOG_LOADING_DISMISS);
		time = System.currentTimeMillis() - time;
		Log.i(TAG, "world loaded for " + time / 1000.0f + " sec.");
	}


	private float[] calculateProjectionMatrix(int width, int height) {
		float ratio = (float) width / height;
		float left = -1;
		float right = 1;
		float bottom = -1;
		float top = 1;
		float near = 1;
		float far = 55;
		if(ratio > 1){
			top = 1/ratio;
			bottom = -1/ratio;
		} else{
			left = -ratio;
			right = ratio;
		}
		float [] projectionMatrix = new float[16];
		Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near,
				far);
		return projectionMatrix;
	}


	private void initFpsCount() {
		prevTime = SystemClock.uptimeMillis();
	}


	private void initGameObjects() {
		CommonGameObject.facesCount = 0;
		scene = new Scene(worldView.getContext(), programHandle, calculateProjectionMatrix(0, 0));
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
		
//		for(int i = 0; i < 4; ++i){
//			gameObjectList.add(new BMW(scene));
//		}
	}


	@Override
	public void onSurfaceChanged(GL10 arg0, int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		scene.setProjectionMatrix(calculateProjectionMatrix(width, height));
//		notifyVPMatrixChanged();
//		Matrix.rotateM(vpMatrix, 0, 35, 1, 0, 0);
	}

	@Override
	public void onDrawFrame(GL10 arg0) {
		currentFrame++;
		if(currentFrame <0){currentFrame = 0;}
		clearScreen();
		
//		drawRayWithScreen();
		drawWorld();

		countFPS();
	}



//	private void drawRayWithScreen() {
//		float lineVertices[] = { resXYZ0[0], resXYZ0[1], resXYZ0[2], resXYZ1[0], resXYZ1[1], resXYZ1[2]};
//		ByteBuffer bb = ByteBuffer.allocateDirect(lineVertices.length * 4);
//		bb.order(ByteOrder.nativeOrder());
//		FloatBuffer line = bb.asFloatBuffer();
//		line.put(lineVertices);
//		line.position(0);
//		GLES20.glVertexAttribPointer(scene.positionHandle, 3 , GLES20.GL_FLOAT, false, 0, line);
//		GLES20.glEnableVertexAttribArray(scene.positionHandle);
//
//		float lineColor[] = { 0, 0, 1, 1, 1f, 0f, 0f, 1};
//		bb = ByteBuffer.allocateDirect(lineColor.length * 4);
//		bb.order(ByteOrder.nativeOrder());
//		FloatBuffer color = bb.asFloatBuffer();
//		color.put(lineColor);
//		color.position(0);
////		GLES20.glVertexAttribPointer(scene.colorHandle, 4 , GLES20.GL_FLOAT, false, 0, color);
////		GLES20.glEnableVertexAttribArray(scene.colorHandle);
//		
//		GLES20.glUniformMatrix4fv(scene.mvpMatrixHandle, 1, false, scene.getVpMatrix(), 0);		
//		GLES20.glDrawArrays(GLES20.GL_LINES, 0, lineVertices.length/3);
//
//		
//		
//		
//		
//		float screenVertices[] = screenXYZ.clone();
//		bb = ByteBuffer.allocateDirect(screenVertices.length * 4);
//		bb.order(ByteOrder.nativeOrder());
//		FloatBuffer screen = bb.asFloatBuffer();
//		screen.put(screenVertices);
//		screen.position(0);
//		GLES20.glVertexAttribPointer(scene.positionHandle, 4 , GLES20.GL_FLOAT, false, 0, screen);
//		GLES20.glEnableVertexAttribArray(scene.positionHandle);
//
//		float screenColor[] = { 1, 1, 1, 1, 
//				1, 1, 1, 1, 
//				1, 1, 1, 1, 
//				1, 1, 1, 1};
//		bb = ByteBuffer.allocateDirect(screenColor.length * 4);
//		bb.order(ByteOrder.nativeOrder());
//		FloatBuffer screenColorBuffer = bb.asFloatBuffer();
//		screenColorBuffer.put(screenColor);
//		screenColorBuffer.position(0);
////		GLES20.glVertexAttribPointer(scene.colorHandle, 4 , GLES20.GL_FLOAT, false, 0, screenColorBuffer);
////		GLES20.glEnableVertexAttribArray(scene.colorHandle);
////		GLES20.g
//		GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, screenVertices.length/4);
//		
//	}


	private void clearScreen() {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
	}


	private void drawWorld() {
		scene.drawFrame();
		cube1.drawFrame();
		
		cube2.drawFrame();

//		bmw1.drawFrame();

//		float step = 7;
//		int size = gameObjectList.size();
//		int iSize = (int)Math.sqrt(size);
//		int x = 0, z = 0;
//		for(int i = 0; i < size ; ++i){
//		
////			i = i*2;
//			gameObjectList.get(i).translate(-20 +step * x , 4, -20 + step * z);
//			gameObjectList.get(i).drawFrame();
//			x++;
//			if(x > iSize){
//				x = 0;
//				z++;
//			}
////			bmw1.translate(-10 +step  * i, 4, -9);
////			bmw1.drawFrame();
//	
////			bmw1.translate(0 +step  * i, 8, 0);
////			bmw1.drawFrame();
//	
////			bmw1.translate(10 +step  * i, 8, -9);
////			bmw1.drawFrame();
//	
////			bmw1.translate(0 +step  * i, 12, 0);
////			bmw1.drawFrame();
//	
////			bmw1.translate(-10 +step  * i, 12, -9);
////			bmw1.drawFrame();
////			i = i/2;
//		}

		earth.drawFrame();

		scene.setRendingFinished(true);
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

	

	public void rotateScene(float angleX, float angleY, float angleZ) {
		angleX = angleX%360;
		angleY = angleY%360;
		angleZ = angleZ%360;
		if(scene != null){
			scene.rotate(angleX, angleY, angleZ);
		}
	}

	public Scene getScene() {
		return scene;
	}


	public void onSingleTap(MotionEvent event, int w, int h) {
		onSingleTap(event.getX(), event.getY(), w, h);
	}
		private void onSingleTap(float x, float y, int w, int h) {
			
		this.x = x;
		this.y = h-y;
//		this.w = w;
//		this.h = h;		
//		float [] resXYZ0 = new float[]{0, 0, 0, 0};
//		float [] resXYZ1 = new float[]{0, 0, 0, 0};

		if(scene == null){
			return;
		}
		GLU.gluUnProject(this.x, this.y, 0.01f, scene.getModelMatrix(), 0, scene.getProjectionMatrix(), 0, new int[]{0, 0, w, h}, 0, resXYZ0, 0);
		resXYZ0[0] = resXYZ0[0]/resXYZ0[3];
		resXYZ0[1] = resXYZ0[1]/resXYZ0[3];
		resXYZ0[2] = resXYZ0[2]/resXYZ0[3];
		resXYZ0[3] = 1;
		GLU.gluUnProject(this.x, this.y, 1, scene.getModelMatrix(), 0, scene.getProjectionMatrix(), 0, new int[]{0, 0, w, h}, 0, resXYZ1, 0);
		resXYZ1[0] = resXYZ1[0]/resXYZ1[3];
		resXYZ1[1] = resXYZ1[1]/resXYZ1[3];
		resXYZ1[2] = resXYZ1[2]/resXYZ1[3];
		resXYZ1[3] = 1;

		Vector3D vector = new Vector3D(resXYZ0, resXYZ1);
//		vector.transform(scene.getModelMatrix());
		scene.setIsSelected(vector);
//		for(GameObject gameObject: gameObjectList){
//			gameObject.setIsSelected(vector);
//		}
		
		screenXYZ = new float[16];
		int offset = 0;
		GLU.gluUnProject(0, 0, -0.01f, scene.getModelMatrix().clone(), 0, scene.getProjectionMatrix(), 0, new int[]{0, 0, w, h}, 0, screenXYZ, offset);
		screenXYZ[0+offset] = screenXYZ[0+offset]/screenXYZ[3+offset];
		screenXYZ[1+offset] = screenXYZ[1+offset]/screenXYZ[3+offset];
		screenXYZ[2+offset] = screenXYZ[2+offset]/screenXYZ[3+offset];
		offset+=4;
		GLU.gluUnProject(0, h, -0.01f, scene.getModelMatrix().clone(), 0, scene.getProjectionMatrix(), 0, new int[]{0, 0, w, h}, 0, screenXYZ, offset);
		screenXYZ[0+offset] = screenXYZ[0+offset]/screenXYZ[3+offset];
		screenXYZ[1+offset] = screenXYZ[1+offset]/screenXYZ[3+offset];
		screenXYZ[2+offset] = screenXYZ[2+offset]/screenXYZ[3+offset];
		offset+=4;
		GLU.gluUnProject(w, h, -0.01f, scene.getModelMatrix().clone(), 0, scene.getProjectionMatrix(), 0, new int[]{0, 0, w, h}, 0, screenXYZ, offset);
		screenXYZ[0+offset] = screenXYZ[0+offset]/screenXYZ[3+offset];
		screenXYZ[1+offset] = screenXYZ[1+offset]/screenXYZ[3+offset];
		screenXYZ[2+offset] = screenXYZ[2+offset]/screenXYZ[3+offset];
		offset+=4;
		GLU.gluUnProject(w, 0, -0.01f, scene.getModelMatrix().clone(), 0, scene.getProjectionMatrix(), 0, new int[]{0, 0, w, h}, 0, screenXYZ, offset);
		screenXYZ[0+offset] = screenXYZ[0+offset]/screenXYZ[3+offset];
		screenXYZ[1+offset] = screenXYZ[1+offset]/screenXYZ[3+offset];
		screenXYZ[2+offset] = screenXYZ[2+offset]/screenXYZ[3+offset];
		Log.i("tag", "ray = "+vector);
		Log.i("tag", "-------------------------------");
		}


		public void deinit() {
			if(scene != null){
				scene.deinit();				
			} 
			
			
		}


		public void scaleScene(float scaleFactor) {
			
			getScene().scale(scaleFactor);
			
		}


		public void translateScene(float dx, float dy, float dz) {
			float[] position = getScene().getPosition().asFloatArray();
			float[] rotation = getScene().getAngleXYZ();
			position[0] += -dx / 2;
			position[1] +=  dz * Math.sin(rotation[0] * Math.PI / 180);
			position[2] += -dz * Math.cos(rotation[0] * Math.PI / 180);
			getScene().setPosition(position);
		}
		



	
}
