package com.android.opengl;


import android.content.Context;

import com.android.opengl.util.geometry.Matrix;
import com.android.opengl.util.geometry.Point3D;

public class Camera {
	
	private static final int MATRIX_X_OFFSET = 12;
	private static final int MATRIX_Y_OFFSET = 13;
	private static final int MATRIX_Z_OFFSET = 14;
	
	private static final float CAMERA_MIN_DISTANCE = -80;
	private static final float CAMERA_MAX_DISTANCE = -5;
	
	
	private float[] viewMatrix = new float[16];
	private float[] projectionMatrix = new float[16];
	
	private int viewportWidth;
	private int viewportHeight;
	
	private float angleX;
	private float angleY;
	private float angleZ;
	

	
	public Camera(int width, int height) {
		initViewMatrix(viewMatrix);
		setProjectionMatrix(calculateProjectionMatrix(width, height));
	}
	
	private float[] calculateProjectionMatrix(int width, int height) {
		float ratio = (float) width / height;
		float left = -1;
		float right = 1;
		float bottom = -1;
		float top = 1;
		float near = 1;
		float far = 105;
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
	}
	
	public void translate(float dx, float dz){
		float[] position = getPosition().asFloatArray();
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
	}
	
	public float[] getViewMatrix() {
		return viewMatrix;
	}

	public void setViewMatrix(float[] viewMatrix) {
		this.viewMatrix = viewMatrix;
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
	}

	public void setViewport(int width, int height) {
		viewportWidth = width;
		viewportHeight = height;
		projectionMatrix = calculateProjectionMatrix(viewportWidth, viewportHeight);
	}

	public int getViewportHeight() {
		return viewportHeight;
	}

	public int getViewportWidth() {
		return viewportWidth;
	}



}
