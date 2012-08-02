package com.android.opengl.gameobject.util.geometry;

public class Point3D extends Point2D{
	public float z, w = 1;

	public Point3D() {
	}

	public Point3D(float[] fPoint, int offset) {
		this.x = fPoint[0 + offset];
		this.y = fPoint[1 + offset];
		this.z = fPoint[2 + offset];
		this.w = fPoint[3 + offset];
		normalize();
	}
	
	public Point3D(float[] fPoint) {
		this.x = fPoint[0];
		this.y = fPoint[1];
		this.z = fPoint[2];
		if(fPoint.length == 4){
			this.w = fPoint[3];
		}
		normalize();
	}

	private void normalize() {
		if (this.w != 1 && this.w != 0){
			this.x/=this.w;
			this.y/=this.w;
			this.z/=this.w;
			this.w= 1;
		}
	}

	public Point3D(float x, float y, float z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = 1;
	}

	public Point3D(float x, float y, float z, float w) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public static float getmaxNorma(Point3D one, Point3D two){
		return Math.max(Math.abs(one.x - two.x), Math.max(Math.abs(one.y - two.y), Math.abs(one.z - two.z)));
	}
	public float[] asFloatArray(){
		return new float[]{x, y, z, w};
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
