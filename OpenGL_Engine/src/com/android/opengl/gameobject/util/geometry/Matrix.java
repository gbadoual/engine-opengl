package com.android.opengl.gameobject.util.geometry;


public class Matrix extends android.opengl.Matrix{
	
	/*
	 * matrix structure
	 * 
	 * UpX, UpY, UpZ, 0 
	 *  Rx,  Ry,  Rz, 0 
	 *  Vx,  Vy,  Vz, 0 
	 *  dX,  dY,  dZ, 1 
	 * 
	 * 
	 */

	public static final int UP_X_OFFSET = 4;
	public static final int UP_Y_OFFSET = 5;
	public static final int UP_Z_OFFSET = 6;

	public static final int VIEX_X_OFFSET = 8;
	public static final int VIEX_Y_OFFSET = 9;
	public static final int VIEX_Z_OFFSET = 10;
	
	
	public static void rotate(float[] m, float angleX, float angleY, float angleZ){
		rotateRad(m, (float) Math.toRadians(angleX), 
					 (float) Math.toRadians(angleY), 
					 (float) Math.toRadians(angleZ));
	};
	
	public static void rotateRad(float[] m, float angleRadX, float angleRadY, float angleRadZ){
		rotateRadX(m, angleRadX);
		rotateRadY(m, angleRadY);
		rotateRadZ(m, angleRadZ);
	};

	public static void rotateRadX(float[] m, float angleRad) {
		if(angleRad == 0){
			return;
		}
		float cos = (float) Math.cos(angleRad);
		float sin = (float) Math.sin(angleRad);
		rotateRadCosSinX(m, cos, sin);
	
	}
	
	public static void rotateRadY(float[] m, float angleRad) {
		if(angleRad == 0){
			return;
		}
		float cos = (float) Math.cos(angleRad);
		float sin = (float) Math.sin(angleRad);
		rotateRadCosSinY(m, cos, sin);
	};

	public static void rotateRadZ(float[] m, float angleRad) {
		if(angleRad == 0){
			return;
		}
		float cos = (float) Math.cos(angleRad);
		float sin = (float) Math.sin(angleRad);
		rotateRadCosSinZ(m, cos, sin);
	}
	
	public static void rotateRadCosSinX(float[] m ,float cos, float sin){
		float tmp1;
		float tmp2;
		for(int i = 0; i< 3; ++i){
			tmp1 = m[i + 4];
			tmp2 = m[i + 8];
			m[i + 4] =  tmp1 *   cos  + tmp2 * sin;
			m[i + 8] =  tmp1 * (-sin) + tmp2 * cos;
		}			
	}
	public static void rotateRadCosSinY(float[] m ,float cos, float sin){
		float tmp1;
		float tmp2;
		for(int i = 0; i< 3; ++i){
			tmp1 = m[i];
			tmp2 = m[i + 8];
			m[i]	 =  tmp1 * cos - tmp2 * sin;
			m[i + 8] =  tmp1 * sin + tmp2 * cos;
		}
	}
	public static void rotateRadCosSinZ(float[] m ,float cos, float sin){
		float tmp1;
		float tmp2;
		for(int i = 0; i< 3; ++i){
			tmp1 = m[i];
			tmp2 = m[i + 4];
			m[i]	 =  tmp1 *   cos  + tmp2 * sin;
			m[i + 4] =  tmp1 * (-sin) + tmp2 * cos;
		}				
	}

}
