package com.android.opengl.gameobject.util.geometry;

import android.opengl.Matrix;

public class Vector3D {
	public static final int P0_OFFSET = 0;
	public static final int P1_OFFSET = 3;

	public Point3D position;
	public Point3D direction;
	private float length;
	
	private boolean isNormalized;

	public Vector3D() {

	}

	public Vector3D(Point3D direction) {
		this.direction = direction;
		this.position = new Point3D();
//		normalize();
	}

	public Vector3D(Point3D startPoint, Point3D endPoint) {
		super();
		this.position = startPoint;
		this.direction = new Point3D(
				endPoint.x - startPoint.x, 
				endPoint.y - startPoint.y,
				endPoint.z - startPoint.z);
//		normalize();
	}

	public Vector3D(float[] fStartPoint, float[] fEndPoint) {
		this.position = new Point3D(fStartPoint);
		this.direction = new Point3D(
				fEndPoint[0] - fStartPoint[0],
				fEndPoint[1] - fStartPoint[1], 
				fEndPoint[2] - fStartPoint[2]);
//		normalize();
	}

	public float dotProduct(Vector3D other) {
		return direction.x * other.direction.x + direction.y
				* other.direction.y + direction.z * other.direction.z;
	}

	public Vector3D vectorProduct(Vector3D other) {
		throw new UnsupportedOperationException("not supported yet");
	}
	
	public static float dotProduct(float[] vector1, float[] vector2) {
		return vector2[0] * vector1[0] +   vector2[1] * vector1[1] +  vector2[2] * vector1[2];
	}

	public static float[] vectorProduct(float[] vector1, float[] vector2){
		return new float[]{vector2[1] * vector1[2] - vector2[2] * vector1[1], 
						   vector2[2] * vector1[0] - vector2[0] * vector1[2], 
						   vector2[0] * vector1[1] - vector2[1] * vector1[0]};
	}
	
	public static float[] sub(float[] vector1, float[] vector2){
		return new float[]{vector2[0] - vector1[0], 
						   vector2[1] - vector1[1], 
						   vector2[2] - vector1[2]};
	}

	public Vector3D normalize() {
		if(isNormalized){
			return this;
		}
		float x = direction.x;
		float y = direction.y;
		float z = direction.z;
		
		length = (float) Math.sqrt(x * x + y * y + z * z);
		
		if (length != 1.0f && length != 0.0f) {
			direction.x = x / length;
			direction.y = y / length;
			direction.z = z / length;
		}
		isNormalized = true;
		return this;
	}

	public Vector3D transform(float[] tranformMatrix) {
		float[] res = new float[4];
		float[] tmp = new float[4];
//		checkLength();
		normalize();
		tmp[0] = position.x + direction.x * length;
		tmp[1] = position.y + direction.y * length;
		tmp[2] = position.z + direction.z * length;
		tmp[3] = 1;
		Matrix.multiplyMV(res, 0, tranformMatrix, 0, position.asFloatArray(), 0);
		position = new Point3D(res);
		Matrix.multiplyMV(res, 0, tranformMatrix, 0, tmp, 0);
		direction = new Point3D(res[0] - position.x, res[1] - position.y,
				res[2] - position.z);
		isNormalized = false;
		normalize();

		return this;
	}

	private void checkLength() {
		float x = direction.x;
		float y = direction.y;
		float z = direction.z;
		if(length == 0){
			length = (float) Math.sqrt(x * x + y * y + z * z);
		}
		if(length == 0){
			throw new IllegalArgumentException("length of vector is 0. Something wrong");
		}
	}

	public float[] asFloatArray() {
		return new float[] { direction.x, direction.y,
				direction.z, position.x, position.y,
				position.z };
	}

	@Override
	public String toString() {
		return new StringBuilder().append("Vector {direction = ")
				.append(direction).append(", position = ").append(position)
				.append(", length = ").append(length).append("}").toString();
	}

	public Point3D getPosition() {
		return position;
	}

//	public void setPosition(Point3D position) {
//		this.position = position;
//	}

	public Point3D getDirection() {
		return direction;
	}

//	public void setDirection(Point3D direction) {
//		this.direction = direction;
//	}

	public float getLength() {
		return length;
	}

	public void setLength(float length) {
		this.length = length;
	}
	
	

}
