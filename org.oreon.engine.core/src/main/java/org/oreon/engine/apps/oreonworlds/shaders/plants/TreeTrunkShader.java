package org.oreon.engine.apps.oreonworlds.shaders.plants;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.GL_TEXTURE2;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import java.util.List;

import org.oreon.engine.engine.core.RenderingEngine;
import org.oreon.engine.engine.math.Matrix4f;
import org.oreon.engine.engine.scenegraph.GameObject;
import org.oreon.engine.engine.scenegraph.components.Material;
import org.oreon.engine.engine.shaders.Shader;
import org.oreon.engine.engine.utils.Constants;
import org.oreon.engine.engine.utils.ResourceLoader;
import org.oreon.engine.modules.instancing.InstancingCluster;
import org.oreon.engine.modules.terrain.Terrain;

public class TreeTrunkShader extends Shader{

	private static TreeTrunkShader instance = null;

	public static TreeTrunkShader getInstance() 
	{
	    if(instance == null) 
	    {
	    	instance = new TreeTrunkShader();
	    }
	      return instance;
	}
	
	protected TreeTrunkShader()
	{
		super();
		
		addVertexShader(ResourceLoader.loadShader("oreonworlds/shaders/Tree_Shader/TreeTrunk_VS.glsl"));
		addGeometryShader(ResourceLoader.loadShader("oreonworlds/shaders/Tree_Shader/TreeTrunk_GS.glsl"));
		addFragmentShader(ResourceLoader.loadShader("oreonworlds/shaders/Tree_Shader/TreeTrunk_FS.glsl"));
		compileShader();
		
		addUniform("sightRangeFactor");
		addUniform("material.diffusemap");
		addUniform("material.normalmap");
		addUniform("clipplane");
		addUniform("scalingMatrix");
		addUniform("isReflection");
		
		addUniformBlock("DirectionalLight");
		addUniformBlock("worldMatrices");
		addUniformBlock("modelMatrices");
		addUniformBlock("LightViewProjections");
		addUniformBlock("Camera");
		addUniform("shadowMaps");
		
		for (int i=0; i<100; i++)
		{
			addUniform("matrixIndices[" + i + "]");
		}
	}	
	
	public void updateUniforms(GameObject object)
	{
		bindUniformBlock("Camera", Constants.CameraUniformBlockBinding);
		bindUniformBlock("DirectionalLight", Constants.DirectionalLightUniformBlockBinding);
		bindUniformBlock("LightViewProjections",Constants.LightMatricesUniformBlockBinding);
		setUniformi("isReflection", RenderingEngine.isWaterReflection() ? 1 : 0);
		
		((InstancingCluster) object.getParent()).getWorldMatricesBuffer().bindBufferBase(0);
		bindUniformBlock("worldMatrices", 0);
		((InstancingCluster) object.getParent()).getModelMatricesBuffer().bindBufferBase(1);
		bindUniformBlock("modelMatrices", 1);
		
		setUniform("clipplane", RenderingEngine.getClipplane());
		setUniform("scalingMatrix", new Matrix4f().Scaling(object.getTransform().getScaling()));
		setUniformf("sightRangeFactor", Terrain.getInstance().getConfiguration().getSightRangeFactor());
		
		Material material = (Material) object.getComponent("Material");

		glActiveTexture(GL_TEXTURE0);
		material.getDiffusemap().bind();
		setUniformi("material.diffusemap", 0);
		
		glActiveTexture(GL_TEXTURE1);
		material.getNormalmap().bind();
		setUniformi("material.normalmap", 1);
		
		glActiveTexture(GL_TEXTURE2);
		RenderingEngine.getShadowMaps().getDepthMaps().bind();
		setUniformi("shadowMaps", 2);
		
		List<Integer> indices = ((InstancingCluster) object.getParent()).getHighPolyIndices();
		
		for (int i=0; i<indices.size(); i++)
		{
			setUniformi("matrixIndices[" + i +"]", indices.get(i));	
		}
	}
}