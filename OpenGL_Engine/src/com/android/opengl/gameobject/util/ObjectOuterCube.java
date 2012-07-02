package com.android.opengl.gameobject.util;

import android.opengl.Matrix;
import android.util.Log;

import com.android.opengl.gameobject.base.CommonGameObject;
import com.android.opengl.gameobject.base.GameObject;
import com.android.opengl.gameobject.util.geometry.Plane;
import com.android.opengl.gameobject.util.geometry.Point3D;
import com.android.opengl.gameobject.util.geometry.Vector3D;

public class ObjectOuterCube {

	private static final String TAG = ObjectOuterCube.class.getSimpleName();
	
	
	private static final int PLANES_COUNT = 6;

	public enum PLANE{
		PLANE_LEFT(0, "Left"),
		PLANE_RIGHT(1, "Right"),
		PLANE_TOP(2, "Top"),
		PLANE_BOTTOM(3, "Bottom"),
		PLANE_NEAR(4, "Near"),
		PLANE_FAR(5, "Far");
		
		private final int index;
		private final String name;
		
		private PLANE(int index, String name){
			this.index = index;
			this.name = name;
		}
		public int getIndex() {
			return index;
		}
		public String getName() {
			return name;
		}
		
		
	}
	public static final int PLANE_LEFT = PLANE.PLANE_LEFT.index;
	public static final int PLANE_RIGHT = PLANE.PLANE_RIGHT.index;
	public static final int PLANE_TOP = PLANE.PLANE_TOP.index;
	public static final int PLANE_BOTTOM = PLANE.PLANE_BOTTOM.index;
	public static final int PLANE_NEAR = PLANE.PLANE_NEAR.index;
	public static final int PLANE_FAR = PLANE.PLANE_FAR.index;

	float coordLeft, coordRight, coordTop, coordBottom, coordNear, coordFar;

	
	
	private Plane[] planesData = new Plane[PLANES_COUNT];

	private GameObject innerGameObjectName;
	
	private ObjectOuterCube(float[] vertexData) {
		if(vertexData == null || vertexData.length == 0){
			throw new IllegalArgumentException(TAG +": Vertex Data length is 0. Something wrong...");
		}
		initPlanesData(vertexData);
	}

	public ObjectOuterCube(GameObject innerGameObject) {
		this(innerGameObject.getVertexData());
		this.innerGameObjectName = innerGameObject;
	}

	private void initPlanesData(float[] vertexData) {
		coordLeft = vertexData[Plane.X_OFFSET];
		coordRight = vertexData[Plane.X_OFFSET];
		coordTop = vertexData[Plane.Y_OFFSET];
		coordBottom = vertexData[Plane.Y_OFFSET];
		coordNear = vertexData[Plane.Z_OFFSET];
		coordFar = vertexData[Plane.Z_OFFSET];
		int curVertex = 0;
		while (curVertex < vertexData.length){
			if(coordLeft > vertexData[curVertex + Plane.X_OFFSET]){coordLeft = vertexData[curVertex + Plane.X_OFFSET];}
			if(coordRight < vertexData[curVertex + Plane.X_OFFSET]){coordRight = vertexData[curVertex + Plane.X_OFFSET];}
			if(coordTop < vertexData[curVertex + Plane.Y_OFFSET]){coordTop = vertexData[curVertex + Plane.Y_OFFSET];}
			if(coordBottom > vertexData[curVertex + Plane.Y_OFFSET]){coordBottom = vertexData[curVertex + Plane.Y_OFFSET];}
			if(coordNear < vertexData[curVertex + Plane.Z_OFFSET]){coordNear = vertexData[curVertex + Plane.Z_OFFSET];}
			if(coordFar > vertexData[curVertex + Plane.Z_OFFSET]){coordFar = vertexData[curVertex + Plane.Z_OFFSET];}
			curVertex += CommonGameObject.VERTEX_ELEMENT_SIZE;
		}
		
//		generatePlanes(null);		
	}
	
	
	public boolean isIntersected(Vector3D ray){
//		float[] mvMatrix = new float[16];
//		float[] viewMatrix = new float[16];
//		viewMatrix = innerGameObjectName.getParentScene().getModelMatrix();
//		Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, innerGameObjectName.getModelMatrix(), 0);
		generatePlanes(innerGameObjectName.getModelMatrix());
		for(int i = 0; i < PLANES_COUNT; i++){
			if (planeIntersectionTest(i, ray)){
				Log.i("tag" , "left = " + planesData[PLANE_LEFT] + ", right = " + planesData[PLANE_RIGHT]);
				Log.i("tag" , "top = " + planesData[PLANE_TOP] + ", bottom = " + planesData[PLANE_BOTTOM]);
				Log.i("tag" , "near = " + planesData[PLANE_NEAR] + ", far = " + planesData[PLANE_FAR]);
				return true;
			};
		}
		return false;
	}
	

	private boolean planeIntersectionTest(int planeIndex, Vector3D ray){

		Plane trPlane = planesData[planeIndex];

		Point3D pointOnPlane = trPlane.getRayIntersectionPoint(ray);
		if (pointOnPlane == null){
			// the ray has no intersection with the plane or the point is behind the ray
			return false;
		}
		
		float res1 = new Vector3D(trPlane.getP1(), trPlane.getP2()).dotProduct(new Vector3D(trPlane.getP1(), pointOnPlane));
		if (res1 < 0) return false;
		float res2 = new Vector3D(trPlane.getP2(), trPlane.getP3()).dotProduct(new Vector3D(trPlane.getP2(), pointOnPlane));
		if (res2 < 0) return false;
		float res3 = new Vector3D(trPlane.getP3(), trPlane.getP4()).dotProduct(new Vector3D(trPlane.getP3(), pointOnPlane));
		if (res3 < 0) return false;
		float res4 = new Vector3D(trPlane.getP4(), trPlane.getP1()).dotProduct(new Vector3D(trPlane.getP4(), pointOnPlane));
		if (res4 < 0) return false;

		// if we are here then point is in plane's rect - user hit it
		Log.d("tag", "Intersection found on " +innerGameObjectName+" on plane " + PLANE.values()[planeIndex]);
		
		return true;
	}
	


	private void generatePlanes(float[] mvMatrix) {
		float coordLeft 	= 	this.coordLeft;
		float coordRight 	= 	this.coordRight;
		float coordTop 		= 	this.coordTop;
		float coordBottom 	= 	this.coordBottom;
		float coordNear 	= 	this.coordNear;
		float coordFar 		= 	this.coordFar;
		Point3D p0 = new Point3D(coordLeft, coordTop, coordNear); 
		Point3D p1 = new Point3D(coordLeft, coordTop, coordFar); 
		Point3D p2 = new Point3D(coordLeft, coordBottom, coordFar); 
		Point3D p3 = new Point3D(coordLeft, coordBottom, coordNear); 
		Point3D p4 = new Point3D(coordRight, coordTop, coordNear); 
		Point3D p5 = new Point3D(coordRight, coordBottom, coordNear); 
		Point3D p6 = new Point3D(coordRight, coordBottom, coordFar); 
		Point3D p7 = new Point3D(coordRight, coordTop, coordFar); 
		
 		if(mvMatrix != null){
 			int P0_OFFSET = 0;
 			int P1_OFFSET = 4;
 			int P2_OFFSET = 8;
 			int P3_OFFSET = 12;
 			float[] leftPlane = new float[]{
 					coordLeft, coordTop, 	coordNear, 	1,
 					coordLeft, coordTop, 	coordFar, 	1,
 					coordLeft, coordBottom, coordFar, 	1,
 					coordLeft, coordBottom, coordNear, 	1}; 
 			float[] rightPlane = new float[]{
 					coordRight, coordTop, 		coordNear, 	1,
 					coordRight, coordBottom, 	coordNear, 	1,
 					coordRight, coordBottom, 	coordFar, 	1,
 					coordRight, coordTop, 		coordFar, 	1};

 			float[] trLeftPlane = new float[16];
 			float[] trRightPlane = new float[16];
 			Matrix.multiplyMM(trLeftPlane, 0, mvMatrix, 0, leftPlane, 0);
 			Matrix.multiplyMM(trRightPlane, 0, mvMatrix, 0, rightPlane, 0);
 			p0 = new Point3D(trLeftPlane,  P0_OFFSET); 
 			p1 = new Point3D(trLeftPlane,  P1_OFFSET); 
 			p2 = new Point3D(trLeftPlane,  P2_OFFSET); 
 			p3 = new Point3D(trLeftPlane,  P3_OFFSET); 
 			p4 = new Point3D(trRightPlane, P0_OFFSET); 
 			p5 = new Point3D(trRightPlane, P1_OFFSET); 
 			p6 = new Point3D(trRightPlane, P2_OFFSET); 
 			p7 = new Point3D(trRightPlane, P3_OFFSET); 
		}
		planesData[PLANE_LEFT] 		= 	new Plane(p0, p1, p2, p3);
		planesData[PLANE_RIGHT] 	= 	new Plane(p4, p5, p6, p7);
		planesData[PLANE_TOP] 		= 	new Plane(p0, p4, p7, p1);
		planesData[PLANE_BOTTOM] 	= 	new Plane(p3, p2, p6, p5);
		planesData[PLANE_NEAR] 		= 	new Plane(p0, p3, p5, p4);
		planesData[PLANE_FAR] 		= 	new Plane(p1, p7, p6, p2);
	}
}
