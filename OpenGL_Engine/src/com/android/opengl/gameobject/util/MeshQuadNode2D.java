package com.android.opengl.gameobject.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import android.util.Log;

import com.android.opengl.gameobject.util.geometry.Point3D;
import com.android.opengl.gameobject.util.geometry.Triangle3D;
import com.android.opengl.gameobject.util.geometry.Vector3D;

public class MeshQuadNode2D {
	
	private static final String TAG = MeshQuadNode2D.class.getSimpleName();

	private MeshQuadNode2D parent;
	
	private MeshQuadNode2D leftNearSon;
	private MeshQuadNode2D leftFarSon;
	private MeshQuadNode2D rightNearSon;
	private MeshQuadNode2D rightFarSon;
	
	
	private static final int X_OFFSET = 0;
	private static final int Z_OFFSET = 2;
	private static final int VERTEX_ELEMENTS_COUNT = 3;
	private static final int VERTEX_PER_FACE = 3;
	private static final int FACE_ELEMENTS_COUNT = VERTEX_PER_FACE * VERTEX_ELEMENTS_COUNT;

	private static final int MAX_LEVEL = 10;

	private static final float EPSILON = 0.000001f;

	
//	private static final int FACE_ELEMENT_COUNT = 3; 
	private Integer[] indexData;
	
	private float left;
	private float far;
	private float right;
	private float near;	
	
//	private MeshData meshData;
	
	private int level;

	private float[] vertexData;

	public MeshQuadNode2D(float[] vertexData, int[] indexData) {
		long begTime = System.currentTimeMillis();
		this.indexData = new Integer[indexData.length];
		for(int i = 0; i < indexData.length; ++i){
			this.indexData[i] = indexData[i];			
		}
		
		this.vertexData = vertexData;
		int i = 0;
		while(i < vertexData.length){
			if(vertexData[i + X_OFFSET] < left) {left = vertexData[i + X_OFFSET];}
			if(vertexData[i + Z_OFFSET] > near) {near = vertexData[i + Z_OFFSET];}
			if(vertexData[i + X_OFFSET] > right) {right = vertexData[i + X_OFFSET];}
			if(vertexData[i + Z_OFFSET] < far) {far = vertexData[i + Z_OFFSET];}
			i += VERTEX_PER_FACE;
		}
		init(0);		
		float creatingTime = (System.currentTimeMillis() - begTime)/ 1000f;
		Log.i("tag", "MeshQuadNode2D() created for "+ creatingTime+" sec");
	}
	
	private void init(int level) {
		this.level = level;
		leftNearSon = getLeftNearQuadData(level+1);
		leftFarSon = getLeftFarQuadData(level+1);
		rightNearSon = getRightNearQuadData(level+1);
		rightFarSon = getRightFarQuadData(level+1);
	}

	public MeshQuadNode2D(MeshQuadNode2D parent,  int level, Integer[] indexData, float left, float near, float right, float far) {
		this.indexData = indexData;
		this.left = left;
		this.near = near;
		this.right = right;
		this.far = far;
		this.parent = parent;
		init(level);		
	}	
	

	private float[] getVerdexData(){
		if(parent == null){
			return vertexData;
		} else {
			return parent.getVerdexData();
		}
	}

	private MeshQuadNode2D getQuad(float left, float near, float right, float far, int level){
		Integer[] indexData;
		indexData = getTriagleArrayWithinRect(left, near, right, far);
		if(indexData.length == 0 || indexData.length >= this.indexData.length || level >= MAX_LEVEL){
//			Log.i("tag", "+leaf: indexData size = " + this.indexData.length);
			return null;
		}
		return new MeshQuadNode2D(this, level, indexData, left, near, right, far);
		
	}
		
	public MeshQuadNode2D getLeftNearQuadData(int level){

		return getQuad(left, near, (left + right)/2,(near+far)/2, level);
	}
	public MeshQuadNode2D getRightNearQuadData(int level){

		return getQuad(left, near, (left + right)/2,(near+far)/2, level);
	}
	public MeshQuadNode2D getLeftFarQuadData(int level){

		return getQuad(left, (near + far)/2, (left+right)/2, far, level);
	}
	public MeshQuadNode2D getRightFarQuadData(int level){

		return getQuad((left + right)/2, (near+far)/2, right, far, level);
	}
	
	
	private Integer[] getTriagleArrayWithinRect(float left, float near, float right, float far){
		ArrayList<Integer> arrayList = new ArrayList<Integer>();
		int curFace = 0;
		while(curFace < indexData.length){
			if(faceAllFartherNearLeftBorder(curFace, left, near, right, far) && faceAnyNearerFarRightBorder(curFace, left, near, right, far)){
				for(int j = 0; j < VERTEX_PER_FACE; ++ j)
				arrayList.add(indexData[curFace + j]);
			}
			curFace += VERTEX_PER_FACE;
		}
		
		Integer [] array = arrayList.toArray(new Integer[]{});//new int[arrayList.size()];
//		int i = 0;
//		for(Integer l:arrayList){
//			array[i++] = l;
//		}
		
		return array;
	}
	
	private boolean faceAllFartherNearLeftBorder(int vertexIndex, float left, float near, float right, float far) {
		float[] vertexData = getVerdexData();
		for(int i = 0; i < VERTEX_PER_FACE; ++i){
			int vertexDataIndex = indexData[vertexIndex + i] * VERTEX_ELEMENTS_COUNT;
			float x = vertexData[vertexDataIndex + X_OFFSET];
			float z = vertexData[vertexDataIndex + Z_OFFSET];
			if(x < left || z > near){
				return false;
			}
		}
		return true;
	}
	
	private boolean faceAnyNearerFarRightBorder(int vertexIndex, float left, float near, float right, float far) {
		float[] vertexData = getVerdexData();
		for(int i = 0; i < VERTEX_PER_FACE; ++i){
			int vertexDataIndex = indexData[vertexIndex + i] * VERTEX_ELEMENTS_COUNT;
			float x = vertexData[vertexDataIndex + X_OFFSET];
			float z = vertexData[vertexDataIndex + Z_OFFSET];
			if(x <= right && z >= far){
				return true;
			}
		}
		return false;
	}
	
	
	// returns indexies of intersected triangle 
	// or null if there is no any intersection detected
	
	public boolean intersectionTest(Vector3D ray){
		if (ray == null){
			Log.w(TAG, "intersectionTest() - incoming ray is null");
			return false;
		}
		ray.normalize();
		int[] rawFaceData = new int[VERTEX_PER_FACE];

		int i = 0;
		while (i < indexData.length){
			for(int j = 0; j < VERTEX_PER_FACE; ++j){
				rawFaceData[j] = indexData[i + j];
			}
			if (rayTriangleIntersectionTest(ray, rawFaceData)){
				return true;//new Triangle3D(rawFaceData);
			};
			
			i += VERTEX_PER_FACE;
		}
		
		
				
		return false;
		
	}	
	
	private boolean rayTriangleIntersectionTest(Vector3D ray, int[] triangleIndices){
		float[] vertexData = getVerdexData();
		float[] edge1 = new float[VERTEX_ELEMENTS_COUNT];
		float[] edge2 = new float[VERTEX_ELEMENTS_COUNT];
		float[] tvec = new float[VERTEX_ELEMENTS_COUNT];
		float[] pvec;
		float[] qvec;
		float det, invDet;
		int vertexIndex0 = indexData[triangleIndices[0]] * VERTEX_ELEMENTS_COUNT;
		int vertexIndex1 = indexData[triangleIndices[1]] * VERTEX_ELEMENTS_COUNT;
		int vertexIndex2 = indexData[triangleIndices[2]] * VERTEX_ELEMENTS_COUNT;
		edge1[0] = vertexData[vertexIndex1 + 0] - vertexData[vertexIndex0 + 0];
		edge1[1] = vertexData[vertexIndex1 + 1] - vertexData[vertexIndex0 + 1];
		edge1[2] = vertexData[vertexIndex1 + 2] - vertexData[vertexIndex0 + 2];

		edge2[0] = vertexData[vertexIndex2 + 0] - vertexData[vertexIndex0 + 0];
		edge2[1] = vertexData[vertexIndex2 + 1] - vertexData[vertexIndex0 + 1];
		edge2[2] = vertexData[vertexIndex2 + 2] - vertexData[vertexIndex0 + 2];
		
		float[] direction = ray.getDirection().asFloatArray();
		pvec = Vector3D.vectorProduct(edge2, direction);
		det = Vector3D.dotProduct(pvec, edge1);
		
		if(det > -EPSILON && det < EPSILON){
			return false;
		}
		invDet = 1/det;
		float u, v;
		float[] position = ray.getPosition().asFloatArray();
		tvec[0] = position[0] - vertexData[vertexIndex0 + 0];
		tvec[1] = position[1] - vertexData[vertexIndex0 + 1];
		tvec[2] = position[2] - vertexData[vertexIndex0 + 2];
		u = Vector3D.dotProduct(tvec, pvec) * invDet;
		if(u < 0.0 || u > 1.0){
			return false;
		}
		qvec = Vector3D.vectorProduct(edge1, tvec);
		v = Vector3D.dotProduct(direction, qvec);
		if(v < 0.0 || u + v > 1.0 ){
			return false;
		}
		
		float len = Vector3D.dotProduct(edge2, qvec) * invDet;
		ray.setLength(len);
		
		return true;		
	}

	

}
