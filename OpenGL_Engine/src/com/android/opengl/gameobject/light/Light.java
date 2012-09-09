package com.android.opengl.gameobject.light;

import com.android.opengl.util.geometry.Point3D;

public abstract class Light {
	private Point3D position;

	public Light() {
		this.position = new Point3D();
	}

	public Light(Point3D position) {
		this.position = position;
	}

	public Point3D getPosition() {
		return position;
	}

	public void setPosition(Point3D position) {
		this.position = position;
	}
	

}
