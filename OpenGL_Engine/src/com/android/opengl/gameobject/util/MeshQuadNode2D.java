package com.android.opengl.gameobject.util;

import java.util.ArrayList;
import java.util.Arrays;

public class MeshQuadNode2D {
	
	private MeshQuadNode2D parent;
	
	private MeshQuadNode2D leftNearSun;
	private MeshQuadNode2D leftFarSun;
	private MeshQuadNode2D rightNearSun;
	private MeshQuadNode2D rightFarSun;
	
	private MeshData meshData;
	
	private int level;

	private float[] vertexData;
	/** 
	 * vertexData - i%3     - x
	 *  			i%3 + 1 - y
	 *  			i%3 + 2 - z
	 *  should be sorted by x-z - plane coordinates
	 *  
	 *  indexData - every 3 elements forms triangle with vertexData's coordinates
	 */
	
	public MeshQuadNode2D(float[] vertexData, long[] indexData) {
		this.vertexData = vertexData;
		init(new MeshData(indexData), 0);
	}
	
	
	
	private MeshQuadNode2D(MeshData meshDataQuad, int level) {
		init(meshDataQuad, level);		
	}
	
	private void init(MeshData meshDataQuad, int level){
		this.level = level;
		if(meshDataQuad != null){
			this.meshData = meshDataQuad;
			leftNearSun = new MeshQuadNode2D(meshDataQuad.getLeftNearQuadData(), level+1);
			leftFarSun = new MeshQuadNode2D(meshDataQuad.getLeftFarQuadData(), level+1);
			rightNearSun = new MeshQuadNode2D(meshDataQuad.getRightNearQuadData(), level+1);
			rightFarSun = new MeshQuadNode2D(meshDataQuad.getRightFarQuadData(), level+1);
		}
	}

	private float[] getVerdexData(){
		if(parent == null){
			return vertexData;
		} else {
			return parent.getVerdexData();
		}
	}



	private class MeshData{

		private static final int X_OFFSET = 0;
		private static final int Z_OFFSET = 2;
		private long[] indexData;
		
		private float minX;
		private float minZ;
		private float maxX;
		private float maxZ;

		public MeshData(long[] indexData) {
			this.indexData = indexData;
			float[] vertexData = getVerdexData();
			minX = vertexData[0 + X_OFFSET]; // the most left-near edge;
			maxZ = vertexData[0 + Z_OFFSET]; // the most left-near edge;
			
		}
		
		public MeshData getLeftNearQuadData(){
			return new MeshData(indexData);
		}
		public MeshData getRightNearQuadData(){
			return new MeshData(indexData);
		}
		public MeshData getLeftFarQuadData(){
			return new MeshData(indexData);
		}
		public MeshData getRightFarQuadData(){
			return new MeshData(indexData);
		}
		
		private long[] getTriagleArrayWithinRect(){
			ArrayList<Long> arrayList = new ArrayList<Long>();
			long [] array = new long[arrayList.size()];
			int i = 0;
			for(Long l:arrayList){
				array[i++] = l;
			}
			
			return array;
		}
	}
	

}
