package com.shoky.myapp.opengl;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import android.opengl.Matrix;

public class Mx {
	public float[] mMatrix;
	private LinkedList<float[]> mStack;
	
	public Mx() {
		mMatrix = new float[16];
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
	
	public Mx transpose() {
		Matrix.transposeM(mMatrix, 0, mMatrix, 0);
		return this;
		/*
		return new Mx(new float[] {
			mMatrix[0], mMatrix[4], mMatrix[8], mMatrix[12],
			mMatrix[1], mMatrix[5], mMatrix[9], mMatrix[13], 
			mMatrix[2], mMatrix[6], mMatrix[10], mMatrix[14],
			mMatrix[3], mMatrix[7], mMatrix[11], mMatrix[15]
		});*/
	}
	
	public Mx invert() {
		Matrix.invertM(mMatrix, 0, mMatrix, 0);
		return this;
		/*
	    float s0 = mMatrix[0] * mMatrix[5] - mMatrix[4] * mMatrix[1];
	    float s1 = mMatrix[0] * mMatrix[6] - mMatrix[4] * mMatrix[2];
	    float s2 = mMatrix[0] * mMatrix[7] - mMatrix[4] * mMatrix[3];
	    float s3 = mMatrix[1] * mMatrix[6] - mMatrix[5] * mMatrix[2];
	    float s4 = mMatrix[1] * mMatrix[7] - mMatrix[5] * mMatrix[3];
	    float s5 = mMatrix[2] * mMatrix[7] - mMatrix[6] * mMatrix[3];

	    float c5 = mMatrix[10] * mMatrix[15] - mMatrix[14] * mMatrix[11];
	    float c4 = mMatrix[9] * mMatrix[15] - mMatrix[13] * mMatrix[11];
	    float c3 = mMatrix[9] * mMatrix[14] - mMatrix[13] * mMatrix[10];
	    float c2 = mMatrix[8] * mMatrix[15] - mMatrix[12] * mMatrix[11];
	    float c1 = mMatrix[8] * mMatrix[14] - mMatrix[12] * mMatrix[10];
	    float c0 = mMatrix[8] * mMatrix[13] - mMatrix[12] * mMatrix[9];

	    // Should check for 0 determinant
	    float invdet = 1.0f / (s0 * c5 - s1 * c4 + s2 * c3 + s3 * c2 - s4 * c1 + s5 * c0);

	    float b[] = new float[16];

	    b[0] = ( mMatrix[5] * c5 - mMatrix[6] * c4 + mMatrix[7] * c3) * invdet;
	    b[1] = (-mMatrix[1] * c5 + mMatrix[2] * c4 - mMatrix[3] * c3) * invdet;
	    b[2] = ( mMatrix[13] * s5 - mMatrix[14] * s4 + mMatrix[15] * s3) * invdet;
	    b[3] = (-mMatrix[9] * s5 + mMatrix[10] * s4 - mMatrix[11] * s3) * invdet;

	    b[4] = (-mMatrix[4] * c5 + mMatrix[6] * c2 - mMatrix[7] * c1) * invdet;
	    b[5] = ( mMatrix[0] * c5 - mMatrix[2] * c2 + mMatrix[3] * c1) * invdet;
	    b[6] = (-mMatrix[12] * s5 + mMatrix[14] * s2 - mMatrix[15] * s1) * invdet;
	    b[7] = ( mMatrix[8] * s5 - mMatrix[10] * s2 + mMatrix[11] * s1) * invdet;

	    b[8] = ( mMatrix[4] * c4 - mMatrix[5] * c2 + mMatrix[7] * c0) * invdet;
	    b[9] = (-mMatrix[0] * c4 + mMatrix[1] * c2 - mMatrix[3] * c0) * invdet;
	    b[10] = ( mMatrix[12] * s4 - mMatrix[13] * s2 + mMatrix[15] * s0) * invdet;
	    b[11] = (-mMatrix[8] * s4 + mMatrix[9] * s2 - mMatrix[11] * s0) * invdet;

	    b[12] = (-mMatrix[4] * c3 + mMatrix[5] * c1 - mMatrix[6] * c0) * invdet;
	    b[13] = ( mMatrix[0] * c3 - mMatrix[1] * c1 + mMatrix[2] * c0) * invdet;
	    b[14] = (-mMatrix[12] * s3 + mMatrix[13] * s1 - mMatrix[14] * s0) * invdet;
	    b[15] = ( mMatrix[8] * s3 - mMatrix[9] * s1 + mMatrix[10] * s0) * invdet;

	    return new Mx(b);*/
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