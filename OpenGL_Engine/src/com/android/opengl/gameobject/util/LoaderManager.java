package com.android.opengl.gameobject.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.android.opengl.gameobject.base.CommonGameObject;

public class LoaderManager {
	
	private static final String TAG = LoaderManager.class.getSimpleName();
	private static final float EPS = 0.00001f;
	private long startTime;
	
	private CommonGameObject commonGameObject;
	private int facesCount = 0;
	char[] lineSeparator = System.getProperty("line.separator").toCharArray();
	char[] potentialLineSeparator = new char[lineSeparator.length];

	private int vCapacity = 100, vtCapacity = 100, vnCapacity = 100, fCapacity = 100;
	private int capacityStep = 100;

	private ArrayList<Float> v; // vertices
	private ArrayList<Float> vt; // texture coords
	private ArrayList<Float> vn; // normals
	private ArrayList<Integer> vf; // vertex indices
	private ArrayList<Integer> vtf; // texture indices
	private ArrayList<Integer> vnf; // normals indices

	//for normalizing texture coordinates
	float maxTextCoordX = Float.MIN_VALUE;
	float maxTextCoordY = Float.MIN_VALUE;

//	private ProgressDialog progressDialog;
//	private Handler handler = new Handler(){
//		@Override
//		public void dispatchMessage(Message msg) {
//			switch (msg.what) {
//			case 0:
//				progressDialog.show();
//				break;
//			case 1:
//				progressDialog.dismiss();
//
//			default:
//				break;
//			}
//			
//			super.dispatchMessage(msg);
//		}
//		
//	};
	
	public LoaderManager(CommonGameObject commonGameObject) {
		this.commonGameObject = commonGameObject;
//		progressDialog = new ProgressDialog(context);
//		progressDialog.setTitle("Loading objects...");
	}

	public MeshData loadFromRes(int objectRawId) {
		startTime = System.currentTimeMillis();
		MeshData objData = null;
		try {
			
			InputStream inputStream = commonGameObject.getContext().getResources().openRawResource(objectRawId);
			v = new ArrayList<Float>(vCapacity); // vertices
			vt = new ArrayList<Float>(vtCapacity); // texture coords
			vn = new ArrayList<Float>(vnCapacity); // normals
			vf = new ArrayList<Integer>(fCapacity); // vertex indices
			vtf = new ArrayList<Integer>(fCapacity); // texture indices
			vnf = new ArrayList<Integer>(fCapacity); // normals indices

			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			objData = loadOBJ(in);

//			in.close();
		}catch (NotFoundException e) {
			Log.e("tag", TAG + ":loadFromRes() " + e);
		} catch (IOException e) {
			Log.e("tag", TAG + ":loadFromRes() " + e);
		}
		Log.d("tag", "loadRes time = " + (System.currentTimeMillis() - startTime)/1000.0 +" sec. (" + commonGameObject +")");
		return objData;
	}
	
	char[] charBuf;
	
	BufferedReader in;
	int curIndex = 0;

	private MeshData loadOBJ(BufferedReader in) throws IOException{
		this.in = in;

		char[] type = new char[2];
		char[] typeV = new char[]{'v', ' '};
		char[] typeVn = new char[]{'v', 'n'};
		char[] typeVt = new char[]{'v', 't'};
		char[] typeF = new char[]{'f', ' '};
		charBuf = IOUtils.toCharArray(in) ;//in.read(charBuf);
		in.close();
		while (curIndex < charBuf.length){
			type [0] = charBuf[curIndex];
			type [1] = charBuf[curIndex+1];
//			Log.i("tag", "type = [" + type[0] + ", " + type[1]+"]");
			
			if(Arrays.equals(type, typeV)) {
				curIndex += 3;
				v.add(nextFloatToken(' ')); 	// x
				v.add(nextFloatToken(' ')); 	// y
				v.add(nextFloatToken('\n')); 	// z
				curIndex--;

				if (v.size() == vCapacity){
					vCapacity += capacityStep ;
					v.ensureCapacity(vCapacity);
				}
				
			} else	if(Arrays.equals(type, typeVn)) {
					curIndex += 3;
					vn.add(nextFloatToken(' ')); 	// x
					vn.add(nextFloatToken(' ')); 	// y
					vn.add(nextFloatToken('\n')); 	// z
					curIndex--;
					if (v.size() == vCapacity){
						vCapacity += capacityStep ;
						v.ensureCapacity(vCapacity);
					}
					
			} else	if(Arrays.equals(type, typeVt)) {
				curIndex += 3;
				vt.add(nextFloatToken(' ')); 	// x
				vt.add(nextFloatToken(' ')); 	// y
				vt.add(nextFloatToken('\n')); 	// z
				curIndex--;

			} else	if(Arrays.equals(type, typeF)) {
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
			nextLine();
		}
		charBuf = null;
		v.trimToSize();
		vn.trimToSize();
		vt.trimToSize();
		vf.trimToSize();
		vnf.trimToSize();
		vtf.trimToSize();
		
		Log.d("tag", "parsing file time = " + (System.currentTimeMillis() - startTime)/1000.0 +" sec. (" + commonGameObject +")");
		maxTextCoordX = Float.MIN_VALUE;
		maxTextCoordY = Float.MIN_VALUE;
			
		MeshData meshData = new MeshData();
		fillMeshData_2(meshData);
		normalizeTextureCoords(meshData);

		deinitData();
		return meshData;
	}
	
	
	private void normalizeTextureCoords(MeshData meshData) {
		if(maxTextCoordX > 1 && maxTextCoordY > 1){
			int i = 0;
			while (i < meshData.textureData.length){
				meshData.textureData[i] /= maxTextCoordX;
				meshData.textureData[i + 1] /= maxTextCoordY;
				i+= 3;
			}
		}
	}

	private void fillMeshData_2(MeshData objData) {
		objData.facesCount = facesCount;
		int vertNum = v.size();//facesCount * 3;
		objData.vertexData = new float[vertNum];//vfs.size() * 3];
		byte[] vertexDegree = new byte[vertNum / 3];
		objData.textureData = new float[vertNum];//vfs.size() * 3];
		objData.normalData = new float[vertNum];//vfs.size() * 3];
		objData.indexData = new int[vf.size()];//vfs.size() * 3];
		
		
		int indexCount = vf.size();
		for(int i = 0; i < indexCount; ++i){

			int curVertexPacked = vf.get(i) * 3;
			int curVertex = vf.get(i);
			objData.vertexData[curVertexPacked] = v.get(curVertexPacked);
			objData.vertexData[curVertexPacked+1] = v.get(curVertexPacked+1);
			objData.vertexData[curVertexPacked+2] = v.get(curVertexPacked+2);
			
			vertexDegree[curVertex]++;

//			curVertexPacked = vf.get(i) * 3;
//			curVertex = vf.get(i);																
			if(vertexDegree[curVertex] == 1){
				objData.normalData[curVertexPacked] = vn.get(vnf.get(i) * 3);
				objData.normalData[curVertexPacked+1] = vn.get(vnf.get(i) * 3+1);
				objData.normalData[curVertexPacked+2] = vn.get(vnf.get(i) * 3+2);
			}
			else {
				objData.normalData[curVertexPacked] = (objData.normalData[curVertexPacked] * (vertexDegree[curVertex] - 1) + vn.get(vnf.get(i) * 3))/vertexDegree[curVertex];
//				curVertexPacked++;
				objData.normalData[curVertexPacked + 1] = (objData.normalData[curVertexPacked + 1] * (vertexDegree[curVertex] - 1) + vn.get(vnf.get(i) * 3 + 1))/vertexDegree[curVertex];
//				curVertexPacked++;
				objData.normalData[curVertexPacked + 2] = (objData.normalData[curVertexPacked + 2] * (vertexDegree[curVertex] - 1) + vn.get(vnf.get(i) * 3 + 2))/vertexDegree[curVertex];
				float x = objData.normalData[curVertexPacked];
				float y = objData.normalData[curVertexPacked + 1];
				float z = objData.normalData[curVertexPacked + 2];
				float len = (float)Math.sqrt(x*x + y*y + z*z);
				if( Math.abs(len) > EPS){
					objData.normalData[curVertexPacked] = x/len;
					objData.normalData[curVertexPacked + 1] = y/len;
					objData.normalData[curVertexPacked + 2] = z/len;
				}
				
			}
			curVertexPacked = vf.get(i) * 3;

			objData.textureData[curVertexPacked] = vt.get(vtf.get(i)*3);
			objData.textureData[curVertexPacked+1] = vt.get(vtf.get(i)*3+1);
			objData.textureData[curVertexPacked+2] = vt.get(vtf.get(i)*3+2);
			if (maxTextCoordX < objData.textureData[curVertexPacked]){maxTextCoordX = objData.textureData[curVertexPacked];}
			if (maxTextCoordY < objData.textureData[curVertexPacked + 1]){maxTextCoordY = objData.textureData[curVertexPacked + 1];}
			
			objData.indexData[i] = vf.get(i);

		}
	}

	private void fillMeshData_1(MeshData objData) {
		objData.facesCount = facesCount;
		int vertNum = facesCount * 3;
		objData.vertexData = new float[vertNum * 3];//vfs.size() * 3];
		objData.textureData = new float[vertNum * 3];//vfs.size() * 3];
		objData.normalData = new float[vertNum * 3];//vfs.size() * 3];
		objData.indexData = new int[vertNum * 3];//vfs.size() * 3];
		int cur = 0;
		for(int i = 0; i<vertNum;++i){
			int tmpInd = vf.get(i);
			objData.vertexData[cur] = v.get(tmpInd*3);
			objData.vertexData[cur+1] = v.get(tmpInd*3+1);
			objData.vertexData[cur+2] = v.get(tmpInd*3+2);

			tmpInd = vnf.get(i);
			objData.normalData[cur] = vn.get(tmpInd*3);
			objData.normalData[cur+1] = vn.get(tmpInd*3+1);
			objData.normalData[cur+2] = vn.get(tmpInd*3+2);
			
			tmpInd = vtf.get(i);
			objData.textureData[cur] = vt.get(tmpInd*3);
			objData.textureData[cur+1] = vt.get(tmpInd*3+1);
			objData.textureData[cur+2] = vt.get(tmpInd*3+2);
			if (maxTextCoordX < objData.textureData[cur]){maxTextCoordX = objData.textureData[cur];}
			if (maxTextCoordY < objData.textureData[cur + 1]){maxTextCoordY = objData.textureData[cur + 1];}

			cur +=3;
			objData.indexData[i] = i;

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
		System.gc();
	}



//	private void processLine(String type, StringTokenizer t) {
//	if(type.equals("v")) {
//		vs.add(Float.parseFloat(t.nextToken())); 	// x
//		vs.add(Float.parseFloat(t.nextToken()));	// y
//		vs.add(Float.parseFloat(t.nextToken()));	// z
//		if (vs.size() == vsCapacity){
//			vsCapacity += capacityStep ;
//			vs.ensureCapacity(vsCapacity);
//		}
//		
//	} else if (type.equals("vn")) {
//		ns.add(Float.parseFloat(t.nextToken())); 	// x
//		ns.add(Float.parseFloat(t.nextToken()));	// y
//		ns.add(Float.parseFloat(t.nextToken()));	// y
//		if (ns.size() == nsCapacity){
//			nsCapacity += capacityStep ;
//			ns.ensureCapacity(nsCapacity);
//		}
//		
//	} else if (type.equals("vt")) {
//		tc.add(Float.parseFloat(t.nextToken())); 	// u
//		tc.add(Float.parseFloat(t.nextToken()));	// v
//		if (tc.size() == tcCapacity){
//			tcCapacity += capacityStep ;
//			tc.ensureCapacity(tcCapacity);
//		}
//		
//	} else if (type.equals("f")) {
//		String fFace;
//		numFaces++;
//		for (int j = 0; j < 3; j++) {
//			fFace = t.nextToken();
//			// another tokenizer - based on /
//			ft = new StringTokenizer(fFace, "/");
//			vfs.add(Integer.parseInt(ft.nextToken()) - 1);
//			tfs.add(Integer.parseInt(ft.nextToken()) - 1);
//			nfs.add(Integer.parseInt(ft.nextToken()) - 1);
//		}
//		if (vfs.size() == fCapacity){
//			fCapacity += capacityStep ;
//			vfs.ensureCapacity(fCapacity);
//			tfs.ensureCapacity(fCapacity);
//			nfs.ensureCapacity(fCapacity);
//		}
//
//	}
//}
	public static class MeshData{
		public float[] 	vertexData;
		public float[] 	textureData;
		public float[] 	normalData;
		public int[]   	indexData;
		public long		facesCount;
		
	}
	
	
	public static int loadTexture(final Context context, final int resourceId)
	{
	    final int[] textureHandle = new int[1];
	 
	    GLES20.glGenTextures(1, textureHandle, 0);
	 
	    if (textureHandle[0] != 0)
	    {
	    	Matrix flip = new Matrix();
	        flip.postScale(1f, -1f);
	        final BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inScaled = false;   // No pre-scaling
	 
	        // Read in the resource
	        final Bitmap temp = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
	        
	        Bitmap bmp = Bitmap.createBitmap(temp, 0, 0, temp.getWidth(), temp.getHeight(), flip, true);
	        temp.recycle();
	        // Bind to the texture in OpenGL
	        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
	 
//	        // Set filtering
//	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
//	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
//	 
//	        // Load the bitmap into the bound texture.
//	        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
//	 
//	        // Recycle the bitmap, since its data has been loaded into OpenGL.
//	        bmp.recycle();
	        
	        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_NEAREST);
	        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR_MIPMAP_NEAREST);
	        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
	        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
	        
	        // Generate, and load up all of the mipmaps:
	        for(int level=0, height = bmp.getHeight(), width = bmp.getWidth(); true; level++) {
	            // Push the bitmap onto the GPU:
	            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, level, bmp, 0);
	            
	            // We need to stop when the texture is 1x1:
	            if(height==1 && width==1) break;
	            
	            // Resize, and let's go again:
	            width >>= 1; height >>= 1;
	            if(width<1)  width = 1;
	            if(height<1) height = 1;
	            
	            Bitmap bmp2 = Bitmap.createScaledBitmap(bmp, width, height, true);
	            bmp.recycle();
	            bmp = bmp2;
	        }
	        
	        bmp.recycle();	        
	    }
	 
	    if (textureHandle[0] == 0)
	    {
	        throw new RuntimeException("Error loading texture.");
	    }
	 
	    return textureHandle[0];
	}
	
}
