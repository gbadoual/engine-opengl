package com.android.opengl.gameobject.util.geometry;

import android.opengl.Matrix;

public class Vector3D {
	public static final int P0_OFFSET = 0;
	public static final int P1_OFFSET = 3;

	public Point3D position;
	public Point3D direction;
	public float length;

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

	public Vector3D normalize() {
		float x = direction.x;
		float y = direction.y;
		float z = direction.z;

		if (length != 1.0f) {
			direction.x = x / length;
			direction.y = y / length;
			direction.z = z / length;
		}
		return this;
	}

	public Vector3D transform(float[] tranformMatrix) {
		float[] res = new float[4];
		float[] tmp = new float[4];
		checkLength();
		tmp[0] = position.x + direction.x * length;
		tmp[1] = position.y + direction.y * length;
		tmp[2] = position.z + direction.z * length;
		tmp[3] = 1;
		Matrix.multiplyMV(res, 0, tranformMatrix, 0, position.asFloatArray(), 0);
		position = new Point3D(res);
		Matrix.multiplyMV(res, 0, tranformMatrix, 0, tmp, 0);
		direction = new Point3D(res[0] - position.x, res[1] - position.y,
				res[2] - position.z);
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
				.append("}").toString();
	}

}
