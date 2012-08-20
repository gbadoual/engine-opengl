package com.android.opengl.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

public class LoaderManager {
	
	private static final String TAG = LoaderManager.class.getSimpleName();


	private static final char[] typeV = new char[]{'v', ' '};
	private static final char[] typeVn = new char[]{'v', 'n'};
	private static final char[] typeVt = new char[]{'v', 't'};
	private static final char[] typeF = new char[]{'f', ' '};
	
//	private Map<Integer, MeshData> meashDataCache = new HashMap<Integer, MeshData>();
	private Map<Integer, Integer> textureHandlerCache = new HashMap<Integer, Integer>();
	
//	private CommonGameObject commonGameObject;
	private int facesCount = 0;
	char[] lineSeparator = System.getProperty("line.separator").toCharArray();
	char[] potentialLineSeparator = new char[lineSeparator.length];
	char[] charBuf;
	
	int curIndex = 0;

	private int vCapacity = 100, vtCapacity = 100, vnCapacity = 100, fCapacity = 100;
	private int capacityStep = 100;

	private ArrayList<Float> v; // vertices
	private ArrayList<Float> vt; // texture coords
	private ArrayList<Float> vn; // normals
	private ArrayList<Integer> vf; // vertex indices
	private ArrayList<Integer> vtf; // texture indices
	private ArrayList<Integer> vnf; // normals indices

	//for normalizing texture coordinates
	float maxTextureCoordX = Float.MIN_VALUE;
	float maxTextureCoordY = Float.MIN_VALUE;


	private Resources resources;

	private static LoaderManager mInstanse;

	
	public static LoaderManager getInstance(Context context){
		if(mInstanse == null){
			mInstanse = new LoaderManager(context);
		}
		return mInstanse;
	}	

	public static LoaderManager getInstance(Resources resources){
		if(mInstanse == null){
			mInstanse = new LoaderManager(resources);
		}
		return mInstanse;
	}
	
	public void release(){
		Set<Entry<Integer, Integer>> entries = textureHandlerCache.entrySet();
		int[] textureIDs = new int[entries.size()];
		int i = 0;
		for(Entry<Integer,  Integer> entry: entries){
			textureIDs[i] = entry.getKey();
			i++;
		}
		GLES20.glDeleteTextures(textureIDs.length, textureIDs, 0);
		textureHandlerCache.clear();
		mInstanse = null;
	};
	
	private LoaderManager(Context context) {
		this.resources = context.getResources();
	}
	private LoaderManager(Resources resources) {
		this.resources = resources;
	}

	public MeshData loadMeshData(int objectRawId) {
		long time = System.currentTimeMillis();
		MeshData objData = null;
		try {
			
			v = new ArrayList<Float>(vCapacity); // vertices
			vt = new ArrayList<Float>(vtCapacity); // texture coords
			vn = new ArrayList<Float>(vnCapacity); // normals
			vf = new ArrayList<Integer>(fCapacity); // vertex indices
			vtf = new ArrayList<Integer>(fCapacity); // texture indices
			vnf = new ArrayList<Integer>(fCapacity); // normals indices
			curIndex = 0;
			charBuf = null;
			maxTextureCoordX = Float.MIN_VALUE;
			maxTextureCoordY = Float.MIN_VALUE;
			facesCount = 0;
			vCapacity = 100;
			vtCapacity = 100;
			vnCapacity = 100;
			fCapacity = 100;
			capacityStep = 100;

			objData = loadOBJ(objectRawId);

		}catch (NotFoundException e) {
			Log.e(TAG, TAG + ":loadFromRes() " + e);
		} catch (IOException e) {
			Log.e(TAG, TAG + ":loadFromRes() " + e);
		}
		time = System.currentTimeMillis() - time;
		Log.d(TAG, "loadRes time = " + time/1000.0d +" sec.");
		return objData;
	}
	
	private MeshData loadOBJ(int objectRawId) throws IOException{

		InputStream meshStream = resources.openRawResource(objectRawId);
		charBuf = IOUtils.toCharArray(meshStream) ;
		meshStream.close();
		meshStream = null;
		
		char[] type = new char[2];
		while (curIndex < charBuf.length){
			type [0] = charBuf[curIndex];
			type [1] = charBuf[curIndex+1];
			
			if(Arrays.equals(type, typeV)) {
				addVertex();				
			} else	if(Arrays.equals(type, typeVn)) {
				addNormal();
			} else	if(Arrays.equals(type, typeVt)) {
				addTextureCoord();
			} else	if(Arrays.equals(type, typeF)) {
				addFace();
			}
			nextLine();
		}
		charBuf = null;
		v.trimToSize();
		vn.trimToSize();
		vt.trimToSize();
		vf.trimToSize();
		vnf.trimToSize();
		vtf.trimToSize();
		
//		Log.d(TAG, "parsing file time = " + (System.currentTimeMillis() - startTime)/1000.0 +" sec. (" + commonGameObject +")");
		maxTextureCoordX = Float.MIN_VALUE;
		maxTextureCoordY = Float.MIN_VALUE;
			
		MeshData meshData = new MeshData();
		fillMeshData(meshData);
		normalizeTextureCoords(meshData);
		deinitData();
		return meshData;
	}
	
	
	private void addFace() {
		curIndex += 2;
		facesCount++;

		for(int i = 0; i < 3; ++i){
			vf.add(nextIntToken('/') - 1);
			vtf.add(nextIntToken('/') - 1);
			vnf.add(nextIntToken(' ') - 1);
			if (v.size() == vCapacity){
				vCapacity += capacityStep ;
				v.ensureCapacity(vCapacity);
			}
		}
		curIndex--;
	}

	private void addTextureCoord() {
		curIndex += 3;
		vt.add(nextFloatToken(' ')); 	// x
		vt.add(nextFloatToken(' ')); 	// y
//		vt.add(nextFloatToken('\n')); 	// z
		nextFloatToken('\n');
		curIndex--;
	}

	private void addNormal() {
		curIndex += 3;
		vn.add(nextFloatToken(' ')); 	// x
		vn.add(nextFloatToken(' ')); 	// y
		vn.add(nextFloatToken('\n')); 	// z
		curIndex--;
		if (v.size() == vCapacity){
			vCapacity += capacityStep ;
			v.ensureCapacity(vCapacity);
		}
	}

	private void addVertex() {
		curIndex += 3;
		v.add(nextFloatToken(' ')); 	// x
		v.add(nextFloatToken(' ')); 	// y
		v.add(nextFloatToken('\n')); 	// z
		curIndex--;

		if (v.size() == vCapacity){
			vCapacity += capacityStep ;
			v.ensureCapacity(vCapacity);
		}
	}

	private void normalizeTextureCoords(MeshData meshData) {
		if(maxTextureCoordX > 1 && maxTextureCoordY > 1){
			int i = 0;
			while (i < meshData.textureData.length){
				meshData.textureData[i] /= maxTextureCoordX;
				meshData.textureData[i + 1] /= maxTextureCoordY;
				i+= GLUtil.TEXTURE_SIZE;
			}
		}
	}




	private void fillMeshData(MeshData objData) {
		int beforeCopying = v.size() / 3;
		cloneVertexWithDifferentTextureOrNormalCoords();
		int afterCopying = v.size() / 3;
		Log.i(TAG, "vertex count after copying = " + afterCopying + " (copied " + (afterCopying - beforeCopying)+" vertecies)");
		Log.i(TAG, "faces count = " + vf.size() / 3);
		objData.facesCount = facesCount;
		int vertCount = v.size();
		objData.vertexData = new float[vertCount];
		objData.textureData = new float[vertCount / GLUtil.VERTEX_SIZE * GLUtil.TEXTURE_SIZE];
		objData.normalData = new float[vertCount];
		objData.indexData = new int[vf.size()];
		
		
		int indexCount = vf.size();
		for(int i = 0; i < indexCount; ++i){

			int curDestIndexUnpacked = vf.get(i) * GLUtil.VERTEX_SIZE;
			int curSrcIndexUnpacked = vf.get(i) * GLUtil.VERTEX_SIZE;
			for(int j = 0; j < GLUtil.VERTEX_SIZE; ++j){
				objData.vertexData[curDestIndexUnpacked + j] = v.get(curSrcIndexUnpacked + j);
				
			}

			curDestIndexUnpacked = vf.get(i) * GLUtil.NORMAL_SIZE;
			curSrcIndexUnpacked = vnf.get(i) * GLUtil.NORMAL_SIZE;
			for(int j = 0; j < GLUtil.NORMAL_SIZE; ++j){
				objData.normalData[curDestIndexUnpacked + j] = vn.get(curSrcIndexUnpacked + j);
			}

			curDestIndexUnpacked = vf.get(i) * GLUtil.TEXTURE_SIZE;
			curSrcIndexUnpacked = vtf.get(i) * GLUtil.TEXTURE_SIZE;
			for(int j = 0; j < GLUtil.TEXTURE_SIZE; ++j){
				objData.textureData[curDestIndexUnpacked + j] = vt.get(curSrcIndexUnpacked + j);
			}

			if (maxTextureCoordX < objData.textureData[curDestIndexUnpacked]){maxTextureCoordX = objData.textureData[curDestIndexUnpacked];}
			if (maxTextureCoordY < objData.textureData[curDestIndexUnpacked + 1]){maxTextureCoordY = objData.textureData[curDestIndexUnpacked + 1];}
			
			objData.indexData[i] = vf.get(i);
		}
	}
	private void cloneVertexWithDifferentTextureOrNormalCoords() {
		int facesCount = vf.size();
		int[][] vertexStat = new int[2][v.size() / 3];
		Arrays.fill(vertexStat[0], -1);
		for(int i = 0; i < facesCount; ++ i){
			int curIndex = vf.get(i);
			if(vertexStat[0][curIndex] == -1){
				vertexStat[0][curIndex] = vtf.get(i);
				vertexStat[1][curIndex] = vnf.get(i);
			} else {
				if(vertexStat[0][curIndex] != vtf.get(i)
					|| vertexStat[1][curIndex] != vnf.get(i)){
					// copying current vertex as it referenced with another texture coordinate or normal index
					int unpackedCurIndex = curIndex * GLUtil.VERTEX_SIZE; 
					v.add(v.get(unpackedCurIndex + 0));
					v.add(v.get(unpackedCurIndex + 1));
					v.add(v.get(unpackedCurIndex + 2));

					unpackedCurIndex = vnf.get(i) * GLUtil.NORMAL_SIZE;
					vn.add(vn.get(unpackedCurIndex + 0));
					vn.add(vn.get(unpackedCurIndex + 1));
					vn.add(vn.get(unpackedCurIndex + 2));
					
					unpackedCurIndex = vtf.get(i) * GLUtil.TEXTURE_SIZE;
					vt.add(vt.get(unpackedCurIndex + 0));
					vt.add(vt.get(unpackedCurIndex + 1));
					
					//update references
					vf.set(i, v.size() / GLUtil.VERTEX_SIZE - 1);
					vnf.set(i, vn.size() / GLUtil.NORMAL_SIZE - 1);
					vtf.set(i, vt.size() / GLUtil.TEXTURE_SIZE - 1);
				}				
			} 
		}
		
	}
	
	
	
	

	private void nextLine() {
		while(curIndex < charBuf.length){
			for(int i = 0 ; i < potentialLineSeparator.length; ++i){
				potentialLineSeparator[i] = charBuf[curIndex + i];
			}
			if(Arrays.equals(potentialLineSeparator, lineSeparator)){
				curIndex += potentialLineSeparator.length;
				break;
			}
			curIndex++;
		}
	}

	private float nextFloatToken(char delim) {
		int tokenLength = 0;
		while(charBuf[curIndex + tokenLength] != delim && 
				curIndex + tokenLength < charBuf.length - 1 && 
				charBuf[curIndex + tokenLength] != '\n'){
			tokenLength++;
		}
		int prevIndex = curIndex;
		curIndex += tokenLength+1;
		return ParseNumberUtil.parseFloat(charBuf, prevIndex, tokenLength);
	}

	private int nextIntToken(char delim) {
		int tokenLength = 0;
		while(charBuf[curIndex + tokenLength] != delim && 
				curIndex + tokenLength < charBuf.length - 1 && 
				charBuf[curIndex + tokenLength] != '\n'){
			tokenLength++;
		}
		int prevIndex = curIndex;
		curIndex += tokenLength + 1;
		return ParseNumberUtil.parseInt(charBuf, prevIndex, tokenLength);
	}

	private void deinitData() {
		v = null;
		vn = null;
		vt = null;
		vf = null;
		vnf = null;
		vtf = null;
		charBuf = null;
		System.gc();
	}



	public static class MeshData{
		public float[] 	vertexData;
		public float[] 	textureData;
		public float[] 	normalData;
		public int[]   	indexData;
		public long		facesCount;
		
	}

	
//=======================================================================================================================
// texture loading
	
	public int loadTexture(final int resourceId)
	{
		Integer textureHandleInt = textureHandlerCache.get(resourceId);
		if(textureHandleInt != null){
			Log.i(TAG, "reusing cached texture");
			return textureHandleInt;
		}
		long time = System.currentTimeMillis();
	    final int[] textureHandle = new int[1];
	 
	    GLES20.glGenTextures(1, textureHandle, 0);
	 
	    if (textureHandle[0] != 0)
	    {
	    	Matrix flip = new Matrix();
	        flip.postScale(1f, -1f);
	        final BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inScaled = false;   // No pre-scaling
	        // Read in the resource
	        Bitmap temp = decodeAndFeetDegree2Dimens(resources, resourceId, options);
	        
	        
	        Bitmap bmp = Bitmap.createBitmap(temp, 0, 0, temp.getWidth(), temp.getHeight(), flip, true);
	        temp.recycle();
	        // Bind to the texture in OpenGL
	        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
	 
//	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
//	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
//	 	 
	        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
	        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR_MIPMAP_NEAREST);
	        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
	        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
//	        GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
//	        GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
//
//	        GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
//	        GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

	        
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
            
	        
	        // Generate, and load up all of the mipmaps:
//	        for(int level=0, height = bmp.getHeight(), width = bmp.getWidth(); true; level++) {
////	            if(true)break;
//	            // Push the bitmap onto the GPU:
//	            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, level, bmp, 0);
//	            // We need to stop when the texture is 1x1:
//	            if(height==1 && width==1) break;
//	            
//	            // Resize, and let's go again:
//	            float oldW = width;
//	            float oldH = height;
//	            width >>= 1; height >>= 1;
//	            if(width<1)  width = 1;
//	            if(height<1) height = 1;
//	            Bitmap bmp2 = Bitmap.createScaledBitmap(bmp, width, height, true);
//	            bmp.recycle();
//	            bmp = bmp2;
////	            height = bmp.getHeight();
////	            width = bmp.getWidth();
////	            if(width<1)  width = 1;
////	            if(height<1) height = 1;
////	            if(height==1 && width==1) break;
//	        }
	        
	        bmp.recycle();	
	    }
	 
	    time = System.currentTimeMillis() - time;
	    Log.i(TAG, "texture loaded for " + time/1000.0d + " sec.");
	    if (textureHandle[0] == 0)
	    {
	        throw new RuntimeException("loadTexture() - Error loading texture.");
	    }
	    textureHandlerCache.put(resourceId, textureHandle[0]);	 
	    return textureHandle[0];
	}

	private Bitmap decodeAndFeetDegree2Dimens(Resources resources2,
			int resourceId, Options options) {
		Bitmap bitmap = BitmapFactory.decodeResource(resources, resourceId, options);
	    int degree2Width = 1;	 
	    int degree2Height = 1;
	    int width = bitmap.getWidth();
	    int height = bitmap.getHeight();
	    
	    while(degree2Width < width){
	    	degree2Width<<=1;
	    }
	    while(degree2Height < height){
	    	degree2Height<<=1;
	    }
	    if(width != degree2Width || height != degree2Height){
	    	Log.w(TAG, "Dimensions of texture is not a degree of 2. Forcing it to feet the closest ^2 number");
	    	// find closest ^2 value
	    	if(width != degree2Width && (width - (degree2Width>>1) < degree2Width - width)){degree2Width>>=1;}
	    	if(height != degree2Height && (height - (degree2Height>>1) < degree2Height - height)){degree2Height>>=1;}
	    	Log.w(TAG, "old w/h = " + width +"/"+ height);
	    	Log.w(TAG, "target w/h = " + degree2Width +"/"+ degree2Height);
	    	return Bitmap.createScaledBitmap(bitmap, degree2Width, degree2Height, false);
	    }		
		return bitmap;
	}


	
}
