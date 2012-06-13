package com.android.opengl.gameobject;

import android.opengl.Matrix;

import com.android.opengl.R;
import com.android.opengl.gameobject.base.GameObject;
import com.android.opengl.gameobject.base.Scene;
import com.android.opengl.gameobject.util.geometry.Vector3D;

public class Cube extends GameObject{
	
//	// X, Y, Z
//	private float[] vertexData; 
//	// R, G, B, A
//	private float[] colorData;
//
//	// X, Y, Z
//	// The normal is used in light calculations and is a vector which points
//	// orthogonal to the plane of the surface. For a cube model, the normals
//	// should be orthogonal to the points of each face.
//	private float[] normalData;


	public Cube(Scene parentScene) {
		super(parentScene);
	}



	@Override
	public void drawFrame() {
		Matrix.rotateM(modelMatrix, 0, 0.5f, 0.5f, 1f, 1f);
		super.drawFrame();

	}



	@Override
	public int getMeshResource() {
		// TODO Auto-generated method stub
		return R.raw.twisted_cube;
	}




}
