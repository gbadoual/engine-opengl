package com.android.opengl.gameobject.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import android.util.Log;

import com.android.opengl.gameobject.util.geometry.Plane;
import com.android.opengl.gameobject.util.geometry.Point3D;
import com.android.opengl.gameobject.util.geometry.Triangle3D;
import com.android.opengl.gameobject.util.geometry.Vector3D;

public class MeshQuadNode2D {
	
	private static final String TAG = MeshQuadNode2D.class.getSimpleName();
	private static final int TREE_SONS_COUNT = 4;

	private MeshQuadNode2D parent;
	
	private MeshQuadNode2D[] treeSons;
	
	
	private static final int X_OFFSET = 0;
	private static final int Y_OFFSET = 1;
	private static final int Z_OFFSET = 2;
	private static final int VERTEX_ELEMENTS_COUNT = 3;
	private static final int VERTEX_PER_FACE = 3;
	private static final int FACE_ELEMENTS_COUNT = VERTEX_PER_FACE * VERTEX_ELEMENTS_COUNT;

	private static final int MAX_LEVEL = 10;

	private static final float EPSILON = 0.000001f;
	private static final int K_OFFSET = 0;
	private static final int B_OFFSET = 1;


	
//	private static final int FACE_ELEMENT_COUNT = 3; 
	private Integer[] indexData;
	
	private float left;
	private float far;
	private float right;
	private float near;	
	private float top = Float.MIN_VALUE;	
	private float bottom = Float.MAX_VALUE;	
	
	
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
	
	private MeshQuadNode2D(MeshQuadNode2D parent,  int level, Integer[] indexData, float left, float near, float right, float far) {
		this.indexData = indexData;
		this.left = left;
		this.near = near;
		this.right = right;
		this.far = far;
		this.parent = parent;
		init(level);		
	}	
	
	private MeshQuadNode2D(){
		this.indexData = new Integer[0];
		treeSons = new MeshQuadNode2D[TREE_SONS_COUNT];
	}
	
	private void init(int level) {
		this.level = level;
		treeSons = new MeshQuadNode2D[TREE_SONS_COUNT];
		treeSons[0] = getLeftNearQuadData(level+1);
		if(treeSons[0] != null)	treeSons[1] = getLeftFarQuadData(level+1);
		if(treeSons[1] != null)	treeSons[2] = getRightNearQuadData(level+1);
		if(treeSons[2] != null)	treeSons[3] = getRightFarQuadData(level+1);
		if(treeSons[3] == null){ 
			for(int i = 0; i < TREE_SONS_COUNT; ++i){
				treeSons[i] = null;
			}
		}
		
//		boolean isAnyQuadInited = false;
//		isAnyQuadInited |= treeSons[0]!=null;
//		treeSons[0] = getLeftNearQuadData(level+1);
//		treeSons[1] = getLeftFarQuadData(level+1);
//		treeSons[2] = getRightNearQuadData(level+1);
//		treeSons[3] = getRightFarQuadData(level+1);
//		for(int i = 0; i < TREE_SONS_COUNT; ++i){
//			isAnyQuadInited |= treeSons[i] != null;
//		}
//		if(isAnyQuadInited){
//			for(int i = 0; i < TREE_SONS_COUNT; ++i){
//				if (treeSons[i] == null){
//					treeSons[i] = new MeshQuadNode2D();
//				};
//			}
//		}

//		for(int i = 0; i < TREE_SONS_COUNT; ++i){
//			int len = treeSons[i] != null ? treeSons[i].indexData.length : 0;
//			Log.i("tag" ,"len = "+len+", treeSon["+i+"] = " + treeSons[i]+", level = " + level);
//		}
//		Log.i("tag", "*******************************************");
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

		return getQuad((left + right)/2, near, right,(near+far)/2, level);
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
		float[] vertexData = getVerdexData();
		while(curFace < indexData.length){
			for(int i = 0; i < VERTEX_PER_FACE; ++ i){
				int faceIndex = indexData[curFace] * VERTEX_ELEMENTS_COUNT;
				if(top < vertexData[faceIndex + Y_OFFSET]){top = vertexData[faceIndex + Y_OFFSET];}
				if(bottom > vertexData[faceIndex + Y_OFFSET]){bottom = vertexData[faceIndex + Y_OFFSET];}
			}

			if(faceAllFartherNearLeftBorder(curFace, left, near, right, far) 
				&& faceAnyNearerFarRightBorder(curFace, left, near, right, far)){
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
		float[] point = ray.getTargetPoint().asFloatArray();//getRayPlane2DIntersectionPoint(bottom, ray);
		float[] ray2DProjection = new float[]{ray.position.x, ray.position.z, point[X_OFFSET], point[Z_OFFSET]};
		double[] lineEquation = new double[]{Double.POSITIVE_INFINITY, 0};
		if(ray.position.x != point[X_OFFSET]){
			lineEquation[K_OFFSET] = (point[Z_OFFSET] - ray.position.z)/(point[X_OFFSET] - ray.position.x);
			lineEquation[B_OFFSET] = point[Z_OFFSET] - lineEquation[K_OFFSET] * point[X_OFFSET];
		}
		testedFacesCount = 0;
		boolean res = intersectionTest(ray2DProjection, lineEquation, ray);
		Log.i("tag", "testedFacesCount = " + testedFacesCount);
		return res; 
		
	}	
	private static long testedFacesCount;
	private  boolean intersectionTest(float[] x1z1x2z2, double[] lineEquation, Vector3D ray){

		if(
			//!isRayIntersectsQuad(ray) ||
			!clipProjection(x1z1x2z2.clone(), lineEquation)){
			return false;
		}
//		Log.i("tag", "quad intersected: level = " + level);
		int sonCount = 0;

		for(int i = 0; i < TREE_SONS_COUNT; ++i){
			if(treeSons[i] != null){
				sonCount++;
				if(treeSons[i].intersectionTest(x1z1x2z2, lineEquation, ray)){ return true; };
			}
		}

		if(sonCount == 0){
			int[] rawFaceData = new int[VERTEX_PER_FACE];

			int i = 0;
//			Log.i("tag", "len = "+ indexData.length + ", level = "+ level);
			while (i < indexData.length){
				for(int j = 0; j < VERTEX_PER_FACE; ++j){
					rawFaceData[j] = indexData[i + j];
				}
				testedFacesCount++;
				if (rayTriangleIntersectionTest(ray, rawFaceData)){
					Log.i("tag", "intersection found. level: " +level);
					return true;//new Triangle3D(rawFaceData);
				};
				
				i += VERTEX_PER_FACE;
			}
		}
		
		return false;
	}
	
	private boolean clipProjection(float[] x1z1x2z2, double[] lineEquation) {
		if(x1z1x2z2[0] > right && x1z1x2z2[2] > right
			|| x1z1x2z2[0] < left && x1z1x2z2[2] < left
			|| x1z1x2z2[1] < far && x1z1x2z2[3] < far
			|| x1z1x2z2[1] > near && x1z1x2z2[3] > near){
			return false;
		}
		int leftX = 0;
		int nearZ = 1;
		int rightX = 2; 
		int farZ = 3;
		if(x1z1x2z2[leftX] > x1z1x2z2[rightX]){
			int tmp = rightX; rightX = leftX; leftX = tmp;
		}
		if(x1z1x2z2[farZ] > x1z1x2z2[nearZ]){
			int tmp = farZ; farZ = nearZ; nearZ = tmp;
		}
		//clip left side
		if(x1z1x2z2[leftX] < left){
			x1z1x2z2[leftX] = left;
			x1z1x2z2[leftX + 1] = (float)(lineEquation[K_OFFSET] * left + lineEquation[B_OFFSET]);
		}
		//clip right side
		if(x1z1x2z2[rightX] > right){
			x1z1x2z2[rightX] = right;
			x1z1x2z2[rightX + 1] = (float)(lineEquation[K_OFFSET] * right + lineEquation[B_OFFSET]);
		}

		//clip near side
		if(x1z1x2z2[nearZ] > near){
			x1z1x2z2[nearZ] = near;
			x1z1x2z2[nearZ - 1] = (float)((near - lineEquation[B_OFFSET]) / lineEquation[K_OFFSET]);
		}
		//clip far side
		if(x1z1x2z2[farZ] < far){
			x1z1x2z2[farZ] = far;
			x1z1x2z2[farZ - 1] = (float)((far - lineEquation[B_OFFSET]) / lineEquation[K_OFFSET]);
		}
		
		if(x1z1x2z2[leftX] < left || x1z1x2z2[rightX] > right){
			return false;
		}
		
		return true;
				
	}

	private boolean isRayIntersectsQuad(Vector3D ray) {
		// checking bottom plane intersection
//		float[] point = getRayPlane2DLeftIntersectionPoint(bottom, ray);

		float leftClipping  =  - (ray.position.x - left) / ray.getDirection().x;
		float[] point = ray.getTargetPoint(leftClipping).asFloatArray();
		if(point[Y_OFFSET] > top && ray.position.y > top
		|| point[Y_OFFSET] < bottom && ray.position.y < bottom){
			return false;
		}
		float rightClipping =  - (ray.position.x - right) / ray.getDirection().x;
		point = ray.getTargetPoint(rightClipping).asFloatArray();
		if(point[Y_OFFSET] > top && ray.position.y > top
		|| point[Y_OFFSET] < bottom && ray.position.y < bottom){
			return false;
		}
		float nearClipping =  - (ray.position.z - near) / ray.getDirection().z;
		point = ray.getTargetPoint(nearClipping).asFloatArray();
		if(point[Y_OFFSET] > top && ray.position.y > top
		|| point[Y_OFFSET] < bottom && ray.position.y < bottom){
			return false;
		}
		float farClipping  =  - (ray.position.z - far ) / ray.getDirection().z;
		point = ray.getTargetPoint(farClipping).asFloatArray();
		if(point[Y_OFFSET] > top && ray.position.y > top
		|| point[Y_OFFSET] < bottom && ray.position.y < bottom){
			return false;
		}
				
//		if(point[X_OFFSET] < left || point[X_OFFSET] > right
//			|| point[Z_OFFSET] < far || point[Z_OFFSET] > near){
//			// if no intersection detected, check it with the bottom one
//			point = getRayPlane2DIntersectionPoint(top, ray);
//			if(point[X_OFFSET] < left || point[X_OFFSET] > right
//				|| point[Z_OFFSET] < far || point[Z_OFFSET] > near){
//				// ok, ray didn't hit current quad at all
//				return false;
//			}
//		}
//		ray.
		return true;
	}



	
	private float[] getRayPlane2DBottomIntersectionPoint(float yCoord, Vector3D ray){
		//2d plane normal is always {0, 1, 0}
		float y0 = ray.position.y - yCoord; 
		float s =  - y0 / ray.getDirection().y;
		return ray.getTargetPoint(s).asFloatArray();
	}
	private float[] getRayPlane2DLeftIntersectionPoint(float xCoord, Vector3D ray){
		//2d plane normal is always {1, 0, 0}
		float x0 = ray.position.x - xCoord; 
		float s =  - x0 / ray.getDirection().x;
		return ray.getTargetPoint(s).asFloatArray();
	}
	private float[] getRayPlane2DNearIntersectionPoint(float zCoord, Vector3D ray){
		//2d plane normal is always {0, 0, 1}
		float z0 = ray.position.z - zCoord; 
		float s =  - z0 / ray.getDirection().z;
		return ray.getTargetPoint(s).asFloatArray();
	}
	
	private boolean rayTriangleIntersectionTest(Vector3D ray, int[] triangleIndices){
		float[] vertexData = getVerdexData();
		float[] edge1 = new float[VERTEX_ELEMENTS_COUNT];
		float[] edge2 = new float[VERTEX_ELEMENTS_COUNT];
		float[] tvec = new float[VERTEX_ELEMENTS_COUNT];
		float[] pvec;
		float[] qvec;
		float det, invDet;
		int vertexIndex0 = triangleIndices[0] * VERTEX_ELEMENTS_COUNT;
		int vertexIndex1 = triangleIndices[1] * VERTEX_ELEMENTS_COUNT;
		int vertexIndex2 = triangleIndices[2] * VERTEX_ELEMENTS_COUNT;
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
		v = Vector3D.dotProduct(direction, qvec) * invDet;
		if(v < 0.0 || u + v > 1.0 ){
			return false;
		}
		
		float len = Vector3D.dotProduct(edge2, qvec) * invDet;
		ray.setLength(len);
		
		return true;		
	}

	

}
