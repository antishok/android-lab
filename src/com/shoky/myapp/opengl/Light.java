package com.shoky.myapp.opengl;

public class Light {
	public enum Type { DIRECTIONAL, POSITIONAL, SPOTLIGHT };
	
	public Type mType;
	public float[] coords; // world-space coordinates
	
	public Light(Type type, float[] coords) {
		// coords is direction if directional, position if positional
		this.coords = new float[4];
		System.arraycopy(coords, 0, this.coords, 0, 3);
		mType = type;
		this.coords[3] = (mType == Type.DIRECTIONAL) ? 0.0f : 1.0f;		
	}
}
