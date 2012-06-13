package com.android.opengl.gameobject.util.geometry;

public class Plane {

	public static final int X_OFFSET = 0;
	public static final int Y_OFFSET = 1;
	public static final int Z_OFFSET = 2;
	public static final int W_OFFSET = 3;

	public static final int P1_OFFSET = 0;
	public static final int P2_OFFSET = 4;
	public static final int P3_OFFSET = 8;
	public static final int P4_OFFSET = 12;

	private Point3D p1 = new Point3D();
	private Point3D p2 = new Point3D();
	private Point3D p3 = new Point3D();
	private Point3D p4 = new Point3D();
	private Vector3D normal = new Vector3D();

	public Plane() {
	}

	public Plane(Point3D p1, Point3D p2, Point3D p3, Point3D p4) {
		super();
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
		this.p4 = p4;
		calculateNormal();
	}

	public Plane(float[] fPlane) {
		if (fPlane.length != 16) {
			throw new IllegalArgumentException(
					"fPlane source need to be formed as 4x4 matrix with point coordinates as columns");
		}
		p1.x = fPlane[X_OFFSET + P1_OFFSET];
		p1.y = fPlane[Y_OFFSET + P1_OFFSET];
		p1.z = fPlane[Z_OFFSET + P1_OFFSET];

		p2.x = fPlane[X_OFFSET + P2_OFFSET];
		p2.y = fPlane[Y_OFFSET + P2_OFFSET];
		p2.z = fPlane[Z_OFFSET + P2_OFFSET];

		p3.x = fPlane[X_OFFSET + P3_OFFSET];
		p3.y = fPlane[Y_OFFSET + P3_OFFSET];
		p3.z = fPlane[Z_OFFSET + P3_OFFSET];

		p4.x = fPlane[X_OFFSET + P4_OFFSET];
		p4.y = fPlane[Y_OFFSET + P4_OFFSET];
		p4.z = fPlane[Z_OFFSET + P4_OFFSET];
		calculateNormal();
	}

	private void calculateNormal() {
		Point3D p22 = new Point3D(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z);
		Point3D p32 = new Point3D(p3.x - p1.x, p3.y - p1.y, p3.z - p1.z);
		float x = p32.z * p22.y - p32.y * p22.z;
		float y = p32.x * p22.z - p32.z * p22.x;
		float z = p32.y * p22.x - p32.x * p22.y;
		normal = new Vector3D(new Point3D(p1.x, p1.y, p1.z), new Point3D(x + p1.x, y + p1.y, z + p1.z));
	}

	public float[] asFloatArray() {
		float[] fPlane = new float[16];
		fPlane[X_OFFSET + P1_OFFSET] = (float)p1.x;
		fPlane[Y_OFFSET + P1_OFFSET] = (float)p1.y;
		fPlane[Z_OFFSET + P1_OFFSET] = (float)p1.z;
		fPlane[W_OFFSET + P1_OFFSET] = 1;

		fPlane[X_OFFSET + P2_OFFSET] = (float)p2.x;
		fPlane[Y_OFFSET + P2_OFFSET] = (float)p2.y;
		fPlane[Z_OFFSET + P2_OFFSET] = (float)p2.z;
		fPlane[W_OFFSET + P2_OFFSET] = 1;

		fPlane[X_OFFSET + P3_OFFSET] = (float)p3.x;
		fPlane[Y_OFFSET + P3_OFFSET] = (float)p3.y;
		fPlane[Z_OFFSET + P3_OFFSET] = (float)p3.z;
		fPlane[W_OFFSET + P3_OFFSET] = 1;

		fPlane[X_OFFSET + P4_OFFSET] = (float)p4.x;
		fPlane[Y_OFFSET + P4_OFFSET] = (float)p4.y;
		fPlane[Z_OFFSET + P4_OFFSET] = (float)p4.z;
		fPlane[W_OFFSET + P4_OFFSET] = 1;
		return fPlane;
	}

	@Override
	public String toString() {
		return new StringBuilder("{ normal = ").append(normal).append(", {p1 = ").append(p1)
				.append(", p2 = ").append(p2).append(", p3 = ").append(p3)
				.append(", p4 = ").append(p4).append("}}").toString();
	}

	public Point3D getP1() {
		return p1;
	}

	public Point3D getP2() {
		return p2;
	}

	public Point3D getP3() {
		return p3;
	}

	public Point3D getP4() {
		return p4;
	}

	public Vector3D getNormal() {
		return normal;
	}
	
	

}
