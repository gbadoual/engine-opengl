package com.android.opengl.gameobject;

import android.opengl.GLES20;

import com.android.opengl.Camera;
import com.android.opengl.R;
import com.android.opengl.gameobject.CommonGameObject.VboDataHandler;
import com.android.opengl.shader.Shader;
import com.android.opengl.util.GLUtil;
import com.android.opengl.util.LoaderManager;
import com.android.opengl.util.LoaderManager.MeshData;
import com.android.opengl.util.ShaderManager;

public class GLSkyDome {
	
	private SkyDomeShader mSkyBoxShader = ShaderManager.getInstance().getShader(SkyDomeShader.class);
	private Camera camera;

	int textureResId = R.raw.skydome_texture_2048;//R.raw.skydome_texture_small;
	private VboDataHandler mVboDataHandler = new VboDataHandler();
	
	
	public GLSkyDome(Camera camera, int textureResId) {
		this(camera);
		this.textureResId = textureResId;
		init();
	}
	public GLSkyDome(Camera camera) {
		this.camera = camera;
		init();
	}
	
	private void init() {
		
		MeshData meshData = LoaderManager.getInstance(camera.getContext().getResources()).loadMeshData(R.raw.skydome_data);
		mVboDataHandler.textureDataHandler = LoaderManager.getInstance(camera.getContext().getResources()).loadTexture(textureResId);
		mVboDataHandler.indexDataLength = meshData.indexData.length;

		int[] vboBufs = new int[4];
		GLES20.glGenBuffers(vboBufs.length, vboBufs, 0);

		mVboDataHandler.vboVertexHandle = vboBufs[0];
		mVboDataHandler.vboColorHandle = vboBufs[1];
		mVboDataHandler.vboIndexHandle = vboBufs[2];
		mVboDataHandler.vboTextureCoordHandle = vboBufs[3];

		GLUtil.attachArrayToHandler(meshData.vertexData, mVboDataHandler.vboVertexHandle);
		GLUtil.attachArrayToHandler(meshData.textureData, mVboDataHandler.vboTextureCoordHandle);
		GLUtil.attachIndexesToHandler(meshData.indexData, mVboDataHandler.vboIndexHandle);
	}
	
	public void onDrawFrame(){

		boolean isCulingTestEnabled = GLES20.glIsEnabled(GLES20.GL_CULL_FACE);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glUseProgram(mSkyBoxShader.programHandle);
		GLES20.glUniformMatrix4fv(mSkyBoxShader.mvpMatrixHandle, 1, false, camera.getVpMatrix(), 0);

	    GLUtil.passTextureToShader(mVboDataHandler.textureDataHandler, mSkyBoxShader.textureHandle);
	    GLUtil.passBufferToShader(mVboDataHandler.vboTextureCoordHandle, mSkyBoxShader.textureCoordHandle, GLUtil.TEXTURE_SIZE);
		
		GLUtil.passBufferToShader(mVboDataHandler.vboVertexHandle, mSkyBoxShader.positionHandle, GLUtil.VERTEX_SIZE_3D);

		GLUtil.drawElements(mVboDataHandler.vboIndexHandle, mVboDataHandler.indexDataLength);
		
        GLES20.glUseProgram(0);		
		if(!isCulingTestEnabled){
			GLES20.glDisable(GLES20.GL_CULL_FACE);
		}
	}
	
	
	
	
	public static class SkyDomeShader extends Shader{

		public static final String UNIFORM_TEXTURE = "u_Texture";
		public static final String ATTRIBUTE_POSITION = "aPosition";
		public static final String ATTRIBUTE_TEXTURE_COORD = "aTexCoord";

		public static final String UNIFORM_MVP_MATRIX = "u_MVPMatrix";
		

		
		public final int mvpMatrixHandle;
		
		public final int positionHandle;
		public final int textureCoordHandle;
		public final int textureHandle;

		public SkyDomeShader() {
			mvpMatrixHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_MVP_MATRIX);
			
			positionHandle = GLES20.glGetAttribLocation(programHandle, ATTRIBUTE_POSITION);
			textureHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_TEXTURE);
			textureCoordHandle = GLES20.glGetAttribLocation(programHandle, ATTRIBUTE_TEXTURE_COORD);
			
		}
		
		@Override
		public String getVertexShaderSrc() {
			return 
			"uniform 	mat4 "+UNIFORM_MVP_MATRIX + "; 													" +
			"attribute 	vec3 "+ATTRIBUTE_POSITION+";													" +
			"attribute 	vec2 "+ATTRIBUTE_TEXTURE_COORD+";												" +
			
			"																							" +
			"varying 	vec2 v_TexCoord; 																" +
			"																							" +
			"void main(){																				" +
			"	v_TexCoord = "+ATTRIBUTE_TEXTURE_COORD+";												" +
			"	vec3 pos = mat3(" +UNIFORM_MVP_MATRIX+ ") * ("+ATTRIBUTE_POSITION+" * 2.4);" +
			"	gl_Position =  vec4(pos.xy, 0.99997, 1.0);										" +
			"}																							";
	}


		@Override
		public String getFragmentShaderSrc() {
			return 			
				"precision mediump float;																" +
				"uniform	sampler2D "+UNIFORM_TEXTURE+ ";												" +

				"varying 	vec2 v_TexCoord; 															" +
				"void main(){																			" +
				"		gl_FragColor = texture2D("+UNIFORM_TEXTURE+", v_TexCoord) * " + INITIAL_DIFFUSE + ";						" +
				"}																						";	
			}
		
	}

}
