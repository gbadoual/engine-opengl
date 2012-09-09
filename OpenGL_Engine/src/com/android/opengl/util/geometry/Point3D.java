package com.android.opengl.util.geometry;

public class Point3D extends Point2D{
	public float z, w = 1;
	private float[] floatPos = new float[4];

	public Point3D() {
	}

	public Point3D(float[] fPoint, int offset) {
		this.x = fPoint[0 + offset];
		this.y = fPoint[1 + offset];
		this.z = fPoint[2 + offset];
		this.w = fPoint[3 + offset];
		normalize();
		notifyPosChanged();
	}
	

	public Point3D(float[] fPoint) {
		this.x = fPoint[0];
		this.y = fPoint[1];
		this.z = fPoint[2];
		if(fPoint.length == 4){
			this.w = fPoint[3];
		}
		normalize();
		notifyPosChanged();
	}

	private void normalize() {
		if (this.w != 1 && this.w != 0){
			this.x/=this.w;
			this.y/=this.w;
			this.z/=this.w;
			this.w= 1;
			notifyPosChanged();
		}
	}

	public Point3D(float x, float y, float z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = 1;
		notifyPosChanged();
	}

	public Point3D(float x, float y, float z, float w) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		notifyPosChanged();
	}
	
	public void incXYZ(float dx, float dy, float dz){
		x += dx;
		y += dy;
		z += dz;
		notifyPosChanged();
	}
	
	private void notifyPosChanged() {
		floatPos[0] = x;
		floatPos[1] = y;
		floatPos[2] = z;
		floatPos[3] = w;
	}


	public static float getmaxNorma(Point3D one, Point3D two){
		return Math.max(Math.abs(one.x - two.x), Math.max(Math.abs(one.y - two.y), Math.abs(one.z - two.z)));
	}
	public static float getSquaredDistance(Point3D one, Point3D two){
		float x = one.x - two.x;
		float y = one.y - two.y;
		float z = one.z - two.z;
		return x * x + y * y + z * z;
	}
	
	public float[] asFloatArray(){
		return new float[]{x, y, z, w};//floatPos;
	}
	@Override
	public String toString() {
		return new StringBuilder().append("{").append(x).append(", ").append(y)
				.append(", ").append(z).append(", ").append(w).append("}").toString();
	}
	
	public Point3D clone() {
		return new Point3D(x, y, z, w);
	}
	
	

}
