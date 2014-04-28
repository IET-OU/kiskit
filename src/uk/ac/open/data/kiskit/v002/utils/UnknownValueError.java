package uk.ac.open.data.kiskit.v002.utils;

/**
 * A value is not valid/supported or it is not mapped to an existing
 * item in some enumeration/index.
 * 
 * @author enridaga
 *
 */
public class UnknownValueError extends Exception {

	private Object unknown;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnknownValueError(Object value) {
		super("Unknown value");
		this.unknown = value;
	}
	
	public UnknownValueError(String field, Object value){
		super("Unknown value for field " + field);
		this.unknown = value;
	}
	
	public UnknownValueError(String field, Object value, Throwable cause){
		super("Unknown value for field " + field, cause);
		this.unknown = value;
	}
	
	public Object getUnknownValue(){
		return this.unknown;
	}
}
