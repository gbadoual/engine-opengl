package com.android.opengl.gameobject.util;

import java.util.ArrayList;

import android.util.Log;

import com.android.opengl.gameobject.util.geometry.Vector3D;

public class MeshQuadNode2D {
	
	private static final String TAG = MeshQuadNode2D.class.getSimpleName();
	private static final int TREE_SONS_COUNT = 4;
	private static final int NEIGHBOURS_COUNT = 3;
	
	private static final short LEFT_NEAR_SON_INDEX = 0;
	private static final short RIGHT_NEAR_SON_INDEX = 1;
	private static final short RIGHT_FAR_SON_INDEX = 2;
	private static final short LEFT_FAR_SON_INDEX = 3;

	private static final short LEFT_NEIGHBOUR = 0;
	private static final short LEFT_NEAR_NEIGHBOUR = 1;
	private static final short NEAR_NEIGHBOUR = 2;

	private MeshQuadNode2D parent;
	
	private MeshQuadNode2D[] treeSons;
	private MeshQuadNode2D[] neighboringQuads;
	
	
	private static final int X_OFFSET = 0;
	private static final int Y_OFFSET = 1;
	private static final int Z_OFFSET = 2;
	private static final int VERTEX_ELEMENTS_COUNT = 3;
	private static final int VERTEX_PER_FACE = 3;

	private static final int MAX_LEVEL = 10;

	private static final float EPSILON = 0.000001f;
	private static final int K_OFFSET = 0;
	private static final int B_OFFSET = 1;

	private ArrayList<Integer> indexToDrawList;

	
	// index data contains unique faces + border faces which are actually listed in neighboring cells 
	// but need to cover all the surface of the quad to complete intersection test properly
	private Integer[] indexData;
	// count of unique elemnts in indexData array (i.e. length of indexData array without ending of tiled faces);
	private int indexDataUniqElementsCount;
	
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
		neighboringQuads = new MeshQuadNode2D[NEIGHBOURS_COUNT];
		indexToDrawList = new ArrayList<Integer>();
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
	
	private MeshQuadNode2D(MeshQuadNode2D parent,  int level, Integer[] indexData, MeshQuadNode2D[] neighbouringQuads, float left, float near, float right, float far) {
		this.indexData = indexData;
		this.left = left;
		this.near = near;
		this.right = right;
		this.far = far;
		this.parent = parent;
		this.neighboringQuads = neighbouringQuads;
		init(level);		
	}	
	
	private void init(int level) {
		this.level = level;
		treeSons = new MeshQuadNode2D[TREE_SONS_COUNT];
		treeSons[LEFT_NEAR_SON_INDEX] = getLeftNearQuadData(level+1);

		if(treeSons[LEFT_NEAR_SON_INDEX] != null){
			initLeftNearQuadNeighbours();	
			treeSons[RIGHT_NEAR_SON_INDEX] = getRightNearQuadData(level+1);
		}
		if(treeSons[RIGHT_NEAR_SON_INDEX] != null){
			initRightNearQuadNeighbours();
			treeSons[LEFT_FAR_SON_INDEX] = getLeftFarQuadData(level+1);
		}
		if(treeSons[LEFT_FAR_SON_INDEX] != null){
			initLeftFarQuadNeighbours();
			treeSons[RIGHT_FAR_SON_INDEX] = getRightFarQuadData(level+1);
		}
		
		if(treeSons[RIGHT_FAR_SON_INDEX] != null){
			initRightFarQuadNeighbours();	
		}else{
			for(int i = 0; i < TREE_SONS_COUNT; ++i){
				treeSons[i] = null;
			}
		}
	}


	private void initLeftNearQuadNeighbours() {
		if(parent == null){
			return;
		}
		MeshQuadNode2D tmpNeibourQuad;
		tmpNeibourQuad = parent.neighboringQuads[LEFT_NEIGHBOUR];
		if(tmpNeibourQuad != null){
			neighboringQuads[LEFT_NEIGHBOUR] = tmpNeibourQuad.treeSons[RIGHT_NEAR_SON_INDEX];
		}
		tmpNeibourQuad = parent.neighboringQuads[LEFT_NEAR_NEIGHBOUR];
		if(tmpNeibourQuad != null){
			neighboringQuads[LEFT_NEAR_NEIGHBOUR] = tmpNeibourQuad.treeSons[RIGHT_FAR_SON_INDEX];
		}
		tmpNeibourQuad = parent.neighboringQuads[NEAR_NEIGHBOUR];
		if(tmpNeibourQuad != null){
			neighboringQuads[NEAR_NEIGHBOUR] = tmpNeibourQuad.treeSons[LEFT_FAR_SON_INDEX];
		}
	}

	private void initRightNearQuadNeighbours() {
		neighboringQuads[LEFT_NEIGHBOUR] = treeSons[LEFT_NEAR_SON_INDEX];
		if(parent == null){
			return;
		}
		MeshQuadNode2D tmpNeibourQuad;
		tmpNeibourQuad = parent.neighboringQuads[NEAR_NEIGHBOUR];
		if(tmpNeibourQuad != null){
			neighboringQuads[LEFT_NEAR_NEIGHBOUR] = tmpNeibourQuad.treeSons[LEFT_FAR_SON_INDEX];
			neighboringQuads[NEAR_NEIGHBOUR] = tmpNeibourQuad.treeSons[RIGHT_FAR_SON_INDEX];
		}
	}

	private void initLeftFarQuadNeighbours() {
		neighboringQuads[NEAR_NEIGHBOUR] = treeSons[LEFT_NEAR_SON_INDEX];
		if(parent == null){
			return;
		}
		MeshQuadNode2D tmpNeibourQuad;
		tmpNeibourQuad = parent.neighboringQuads[LEFT_NEIGHBOUR];
		if(tmpNeibourQuad != null){
			neighboringQuads[LEFT_NEIGHBOUR] = tmpNeibourQuad.treeSons[RIGHT_FAR_SON_INDEX];
			neighboringQuads[LEFT_NEAR_NEIGHBOUR] = tmpNeibourQuad.treeSons[RIGHT_NEAR_SON_INDEX];
		}
		
	}

	private void initRightFarQuadNeighbours() {
		neighboringQuads[LEFT_NEIGHBOUR] = treeSons[LEFT_FAR_SON_INDEX];
		neighboringQuads[LEFT_NEAR_NEIGHBOUR] = treeSons[LEFT_NEAR_NEIGHBOUR];
		neighboringQuads[NEAR_NEIGHBOUR] = treeSons[RIGHT_NEAR_SON_INDEX];
	}
	
	private float[] getVerdexData(){
		if(parent == null){
			return vertexData;
		} else {
			return parent.getVerdexData();
		}
	}
	public ArrayList<Integer> getIndexToDrawList(){
		if(parent == null){
			return indexToDrawList;
		} else {
			return parent.getIndexToDrawList();
		}
	}

	private MeshQuadNode2D getQuad(float left, float near, float right, float far, MeshQuadNode2D[] neighbours, int level){
		Integer[] indexData;
		indexData = getTriangleArrayWithinRect(left, near, right, far);
		if(indexData.length == 0 || indexData.length >= this.indexData.length || level >= MAX_LEVEL){
			return null;
		}
		return new MeshQuadNode2D(this, level, indexData, neighbours, left, near, right, far);
		
	}
		
	public MeshQuadNode2D getLeftNearQuadData(int level){
		MeshQuadNode2D[] neighbours = new MeshQuadNode2D[NEIGHBOURS_COUNT];
		return getQuad(left, near, (left + right)/2,(near+far)/2, neighbours, level);
	}
	public MeshQuadNode2D getRightNearQuadData(int level){
		MeshQuadNode2D[] neighbours = new MeshQuadNode2D[NEIGHBOURS_COUNT];
		neighbours[LEFT_NEIGHBOUR] = treeSons[LEFT_NEAR_SON_INDEX];
		return getQuad((left + right)/2, near, right,(near+far)/2, neighbours, level);
	}
	public MeshQuadNode2D getLeftFarQuadData(int level){
		MeshQuadNode2D[] neighbours = new MeshQuadNode2D[NEIGHBOURS_COUNT];
		neighbours[NEAR_NEIGHBOUR] = treeSons[LEFT_NEAR_SON_INDEX];
		return getQuad(left, (near + far)/2, (left+right)/2, far, neighbours, level);
	}
	public MeshQuadNode2D getRightFarQuadData(int level){
		MeshQuadNode2D[] neighbours = new MeshQuadNode2D[NEIGHBOURS_COUNT];
		neighbours[LEFT_NEIGHBOUR] = treeSons[LEFT_FAR_SON_INDEX];
		neighbours[LEFT_NEAR_NEIGHBOUR] = treeSons[LEFT_NEAR_SON_INDEX];
		neighbours[NEAR_NEIGHBOUR] = treeSons[RIGHT_NEAR_SON_INDEX];
		return getQuad((left + right)/2, (near+far)/2, right, far, neighbours, level);
	}
	
	
	private Integer[] getTriangleArrayWithinRect(float left, float near, float right, float far){
		ArrayList<Integer> uniqueTriangleList = new ArrayList<Integer>();
		ArrayList<Integer> tiledTriangleList = new ArrayList<Integer>();
		int curFace = 0;
		float[] vertexData = getVerdexData();
		while(curFace < indexData.length){
			for(int i = 0; i < VERTEX_PER_FACE; ++ i){
				int faceIndex = indexData[curFace] * VERTEX_ELEMENTS_COUNT;
				if(top < vertexData[faceIndex + Y_OFFSET]){top = vertexData[faceIndex + Y_OFFSET];}
				if(bottom > vertexData[faceIndex + Y_OFFSET]){bottom = vertexData[faceIndex + Y_OFFSET];}
			}

			if(faceAnyWithinBounds(curFace, left, near, right, far)){
				if(faceAllFartherNearLeftBorder(curFace, left, near, right, far)){
					for(int j = 0; j < VERTEX_PER_FACE; ++ j)
					uniqueTriangleList.add(indexData[curFace + j]);
				}else{
					for(int j = 0; j < VERTEX_PER_FACE; ++ j)
					tiledTriangleList.add(indexData[curFace + j]);
				}
			}
			curFace += VERTEX_PER_FACE;
		}

		
		indexDataUniqElementsCount = uniqueTriangleList.size(); 
//		uniqueTriangleList.addAll(tiledTriangleList);
		
		return uniqueTriangleList.toArray(new Integer[]{});
	}
	

	private boolean faceAllFartherNearLeftBorder(int curFaceIndex, float left, float near, float right, float far) {
		float[] vertexData = getVerdexData();
		for(int i = 0; i < VERTEX_PER_FACE; ++i){
			int vertexDataIndex = indexData[curFaceIndex + i] * VERTEX_ELEMENTS_COUNT;
			float x = vertexData[vertexDataIndex + X_OFFSET];
			float z = vertexData[vertexDataIndex + Z_OFFSET];
			if(x < left || z > near){
				return false;
			}
		}
		return true;
	}
	
	private boolean faceAnyWithinBounds(int curFaceIndex, float left, float near, float right, float far) {
		float[] vertexData = getVerdexData();
		for(int i = 0; i < VERTEX_PER_FACE; ++i){
			int vertexDataIndex = indexData[curFaceIndex + i] * VERTEX_ELEMENTS_COUNT;
			float x = vertexData[vertexDataIndex + X_OFFSET];
			float z = vertexData[vertexDataIndex + Z_OFFSET];
			if(x <= right && z >= far && x >= left && z <= near){
				return true;
			}
		}
		return false;
	}
	
	
	// ******************************************************************************************************************
	// intersection founding implementation
	
	// returns indexies of intersected triangle 
	// or null if there is no any intersection detected
	
	public boolean intersectionTest(Vector3D ray){
		if (ray == null){
			Log.w(TAG, "intersectionTest() - incoming ray is null. Aborting");
			return false;
		}
		ray.normalize();
		getIndexToDrawList().clear();
		float[] point = ray.getTargetPoint().asFloatArray();
		float[] ray2DProjection = new float[]{ray.position.x, ray.position.z, point[X_OFFSET], point[Z_OFFSET]};
		double[] lineEquation = new double[]{Double.POSITIVE_INFINITY, 0};
		if(ray.position.x != point[X_OFFSET]){
			lineEquation[K_OFFSET] = (point[Z_OFFSET] - ray.position.z)/(point[X_OFFSET] - ray.position.x);
			lineEquation[B_OFFSET] = point[Z_OFFSET] - lineEquation[K_OFFSET] * point[X_OFFSET];
		}else{
			Log.w("tag", "Alarm! k = inifinity");
		}
		if(Math.abs(lineEquation[K_OFFSET]) < EPSILON){
			Log.w("tag", "Alarm! k < epsilon (" + lineEquation[K_OFFSET]+")");

		}
		testedFacesCount = 0;
		boolean res = intersectionTest(ray2DProjection, lineEquation, ray);
		Log.i("tag", "testedFacesCount = " + testedFacesCount);
		return res; 
		
	}	
	private static long testedFacesCount;
	private  boolean intersectionTest(float[] x1z1x2z2, double[] lineEquation, Vector3D ray){

		float[] x1z1x2z2Clipped = x1z1x2z2.clone();
		// it is important that clipping performed first 
		//	as ray intersection uses clipped data to determine ray's y-coordinate above the quad under test
		if(!clipProjection(x1z1x2z2Clipped, lineEquation)
			|| !isRayIntersectsQuad(x1z1x2z2Clipped, ray)){
			return false;
		}
		int sonCount = 0;

		// TODO
		// treeSons array should be sorted according to ray's direction to performe search from the user to the deep
		for(int i = 0; i < TREE_SONS_COUNT; ++i){
			if(treeSons[i] != null){
				sonCount++;
				if(treeSons[i].intersectionTest(x1z1x2z2Clipped, lineEquation, ray)){ return true; };
			}
		}

		if(sonCount == 0){
			if (checkIntersection(indexData, ray)){
				return true;
			};
			
			for(int i = 0; i < NEIGHBOURS_COUNT; ++i){
				MeshQuadNode2D neighbourQuad = neighboringQuads[i];
				if(neighbourQuad != null && checkIntersection(neighbourQuad.indexData, ray)){
					return true;
				}
			}
		}
		
		return false;
	}
	

	private boolean checkIntersection(Integer[] indexData, Vector3D ray) {
		int[] rawFaceData = new int[VERTEX_PER_FACE];
		int i = 0;
		while (i < indexData.length){
			for(int j = 0; j < VERTEX_PER_FACE; ++j){
				rawFaceData[j] = indexData[i + j];
				getIndexToDrawList().add(indexData[i + j]);
			}
			testedFacesCount++;
			if (rayTriangleIntersectionTest(ray, rawFaceData)){
				Log.i("tag", "intersection found. level: " +level);
				return true;//new Triangle3D(rawFaceData);
			};
			
			i += VERTEX_PER_FACE;
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

		if(x1z1x2z2[0] < left){
			x1z1x2z2[0] = left;
			x1z1x2z2[1] = (float)(lineEquation[K_OFFSET] * left + lineEquation[B_OFFSET]);
		}
		if(x1z1x2z2[0] > right){
			x1z1x2z2[0] = right;
			x1z1x2z2[1] = (float)(lineEquation[K_OFFSET] * right + lineEquation[B_OFFSET]);
		}

		if(x1z1x2z2[2] < left){
			x1z1x2z2[2] = left;
			x1z1x2z2[3] = (float)(lineEquation[K_OFFSET] * left + lineEquation[B_OFFSET]);
		}
		if(x1z1x2z2[2] > right){
			x1z1x2z2[2] = right;
			x1z1x2z2[3] = (float)(lineEquation[K_OFFSET] * right + lineEquation[B_OFFSET]);
		}

		if(x1z1x2z2[1] > near){
			x1z1x2z2[1] = near;
			x1z1x2z2[0] = (float)((near - lineEquation[B_OFFSET]) / lineEquation[K_OFFSET]);
		}
		if(x1z1x2z2[1] < far){
			x1z1x2z2[1] = far;
			x1z1x2z2[0] = (float)((far - lineEquation[B_OFFSET]) / lineEquation[K_OFFSET]);
		}
		if(x1z1x2z2[3] > near){
			x1z1x2z2[3] = near;
			x1z1x2z2[2] = (float)((near - lineEquation[B_OFFSET]) / lineEquation[K_OFFSET]);
		}
		if(x1z1x2z2[3] < far){
			x1z1x2z2[3] = far;
			x1z1x2z2[2] = (float)((far - lineEquation[B_OFFSET]) / lineEquation[K_OFFSET]);
		}
		
		if(x1z1x2z2[0] < left || x1z1x2z2[0] > right
		|| x1z1x2z2[1] > near || x1z1x2z2[1] < far
		|| x1z1x2z2[2] < left || x1z1x2z2[2] > right
		|| x1z1x2z2[3] > near || x1z1x2z2[3] < far){
			return false;
		}
		return true;
	}

	private boolean isRayIntersectsQuad(float[] x1z1x2z2Clipped, Vector3D ray) {

		if((ray.position.y > top || ray.position.y < bottom) && (Math.abs(ray.direction.y)) > EPSILON){

			float cosValue = ray.getDirection().x;
			float sinValue = ray.getDirection().y;
			float y1 = sinValue *(x1z1x2z2Clipped[0] - ray.position.x)/cosValue + ray.position.y;
			float y2 = sinValue * (x1z1x2z2Clipped[2] - ray.position.x)/cosValue + ray.position.y;
			if(y1 > top && y2 > top 
				|| y1 < bottom && y2 < bottom){
				return false;
			}
		}

		return true;
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

	public int getIndexDataUniqElementsCount() {
		return indexDataUniqElementsCount;
	}


	

}



