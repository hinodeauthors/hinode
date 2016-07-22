package hwanglab.net.message;

import java.lang.reflect.Method;

/**
 * A MethodInvocationRequest represents a method invocation.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class MethodInvocationRequest implements java.io.Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -6388547314732349612L;

	/**
	 * The identifier of the object.
	 */
	protected String id;

	/**
	 * The name of the method.
	 */
	protected String methodName;

	/**
	 * The parameter types.
	 */
	protected Class<?>[] parameterTypes;

	/**
	 * The arguments.
	 */
	protected Object[] args;

	/**
	 * Constructs a MethodInvocationRequest.
	 * 
	 * @param id
	 *            the identifier of the object.
	 * @param method
	 *            the method to invoke.
	 * @param args
	 *            the arguments to the method.
	 */
	public MethodInvocationRequest(String id, Method method, Object[] args) {
		this.id = id;
		this.methodName = method.getName();
		this.parameterTypes = method.getParameterTypes();
		this.args = args;
	}

	@Override
	public String toString() {
		return methodName + java.util.Arrays.toString(args);
	}

	/**
	 * Returns the identifier of the object.
	 * 
	 * @return the identifier of the object.
	 */
	public String objectID() {
		return id;
	}

	/**
	 * Returns the name of the method to invoke.
	 * 
	 * @return the name of the method to invoke.
	 */
	public String methodName() {
		return methodName;
	}

	/**
	 * Returns the arguments for method invocation.
	 * 
	 * @return the arguments for method invocation.
	 */
	public Object[] args() {
		return args;
	}

	/**
	 * Returns the parameter types of the method to invoke.
	 * 
	 * @return the parameter types of the method to invoke.
	 */
	public Class<?>[] parameterTypes() {
		return parameterTypes;
	}

}