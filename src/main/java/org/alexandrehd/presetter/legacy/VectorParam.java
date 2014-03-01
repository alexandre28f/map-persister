package org.alexandrehd.presetter.legacy;

import java.io.Serializable;

public class VectorParam implements Serializable {
	private static final long serialVersionUID = -8777365957301661733L;
	public float [] x;

	public VectorParam() {
		this.x = new float[0];
	}
	
	public VectorParam(float f[]) {
		this.x = f;
	}
	
	public VectorParam cloneVP() {
		return new VectorParam(resize(x.length));
	}
	
	/**
	 * Creates a new array containing this parameter's data either
	 * cropped or zerofilled to {@code newSize}.
	 *
	 * @param newSize size of the new array
	 *
	 * @return a new array cropped or zero padded to {@code newSize}
	 */
	public float[] resize(int newSize) {
		final float[] newVector = new float[newSize];
		final int copySize = (newSize > x.length) ? x.length : newSize;
		System.arraycopy(x, 0, newVector, 0, copySize);
		
		return newVector;
	}
}
