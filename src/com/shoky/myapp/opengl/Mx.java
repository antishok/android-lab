package com.shoky.myapp.opengl;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import android.opengl.Matrix;

public class Mx {
	public float[] mMatrix;
	private LinkedList<float[]> mStack;
	
	public static Mx scratch = new Mx();
	
	public Mx() {
		mMatrix = new float[16];
		Matrix.setIdentityM(mMatrix, 0);
	}
	public Mx(float[] matrix) {
		mMatrix = Arrays.copyOf(matrix, 16);
	}
	public Mx(Mx mx) {
		this(mx.mMatrix);
	}
	
	public void push() {
		if (mStack == null)
			mStack = new LinkedList<float[]>();
		
		mStack.addFirst( Arrays.copyOf(mMatrix, 16) ); // TODO: pre-allocate stack space
	}
	
	public void pop() {
		if (mStack == null)
			return;
		try {
			mMatrix = mStack.removeFirst();
		} catch (NoSuchElementException e) {
			e.printStackTrace();
		}
	}
	
	public float[] transformVector(float[] vector) {
		float[] t = new float[4];
		Matrix.multiplyMV(t, 0, mMatrix, 0, vector, 0);
		return t;
	}
	
	public Mx transpose() {
		Matrix.transposeM(mMatrix, 0, mMatrix, 0);
		return this;
	}
	
	public Mx invert() {
		Matrix.invertM(mMatrix, 0, mMatrix, 0);
		return this;
	}
	
	
	public Mx setLookAt(float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
		Matrix.setLookAtM(mMatrix, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
		return this;
	}	
	public Mx setMultiply(Mx lhs, Mx rhs) {
		Matrix.multiplyMM(mMatrix, 0, lhs.mMatrix, 0, rhs.mMatrix, 0);
		return this;
	}
	public Mx setRotate(float angle, float x, float y, float z) {
		Matrix.setRotateM(mMatrix, 0, angle, x, y, z);
		return this;
	}
	public Mx setFrustum(float left, float right, float bottom, float top, float near, float far) {
		Matrix.frustumM(mMatrix, 0, left, right, bottom, top, near, far);	
		return this;
	}
	public Mx rotate(float angle, float x, float y, float z) {
		Matrix.rotateM(mMatrix, 0, angle, x, y, z);
		return this;
	}
	public Mx translate(float x, float y, float z) {
		Matrix.translateM(mMatrix, 0, x, y, z);
		return this;
	}
	
	public Mx scale(float x, float y, float z) {
		Matrix.scaleM(mMatrix, 0, x, y, z);
		return this;
	}
	
	public Mx scaleUniform(float amount) {
		Matrix.scaleM(mMatrix, 0, amount, amount, amount);
		return this;
	}
	
	public Mx setIdentity() {
		Matrix.setIdentityM(mMatrix, 0);		
		return this;
	}
	public Mx setTranslate(float x, float y, float z) {
		return setIdentity().translate(x, y, z);
	}
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < 16; i++) {
			if (i > 0 && i % 4 == 0)
				s.append("\n");
			s.append(mMatrix[i]).append(" ");
		}
		
		return s.append("\n---").toString();
	}
	
	
}