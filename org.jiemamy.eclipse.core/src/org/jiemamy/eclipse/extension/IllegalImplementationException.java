package org.jiemamy.eclipse.extension;

@SuppressWarnings("serial")
public class IllegalImplementationException extends RuntimeException {

	public Object getObj() {
		return obj;
	}

	private final Object obj;

	public IllegalImplementationException(Object obj) {
		this.obj = obj;
	}

}
