package com.android.opengl;

import com.android.opengl.gameobject.util.geometry.Point3D;

import android.opengl.Matrix;

public class Camera {
	
	private static final int MATRIX_X_OFFSET = 12;
	private static final int MATRIX_Y_OFFSET = 13;
	private static final int MATRIX_Z_OFFSET = 14;
	
	public static final double DEGREE_TO_RADIAN_RATIO = Math.PI / 180; 
	
	private float[] viewMatrix = new float[16];
	
	private float angleX;
	private float angleY;
	private float angleZ;
	

	
	public Camera() {
		initViewMatrix(viewMatrix);
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
		Matrix.rotateM(locaViewMatrix, 0, angleX, 1, 0, 0);
		Matrix.rotateM(locaViewMatrix, 0, angleY, 0, 1, 0);
		Matrix.rotateM(locaViewMatrix, 0, angleZ, 0, 0, 1);
		viewMatrix = locaViewMatrix;
//		float[] pos = position.asFloatArray();
//		float[] matr = new float[16];
//		Matrix.setRotateEulerM(matr, 0, dx, dy, -dz);
//		Matrix.multiplyMV(pos, 0, matr, 0, pos, 0);
//		setPosition(new Point3D(pos));		
		setPosition(position);
	}
	
	public void moveForward(float distance){
		viewMatrix[MATRIX_Z_OFFSET] += distance;
	}
	
	public void translate(float dx, float dz){
		float[] position = getPosition().asFloatArray();
		float sinX = (float)Math.sin(angleX * DEGREE_TO_RADIAN_RATIO);
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

}
