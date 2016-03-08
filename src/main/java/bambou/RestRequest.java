package bambou;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

public class RestRequest {
	private HttpMethod method;
	private String url;
	private Class<?> requestRestObjectClass;
	private Class<?> responseRestObjectClass;
	private Object requestRestObject;
	private HttpHeaders headers = new HttpHeaders();
	private String params;
	
	private RestRequest() {
		setHeader("Content-Type", "application/json");
	}
	
	public RestRequest(HttpMethod method, String url, String params, Class<?> requestRestObjectClass, boolean isResponseArray) {
		this();
		
		this.method = method;
		this.url = url;
		this.params = params;
		this.requestRestObjectClass = requestRestObjectClass;
		this.responseRestObjectClass = isResponseArray ? getArrayClass(requestRestObjectClass) : requestRestObjectClass;
		this.requestRestObject = null;
	}

	public RestRequest(HttpMethod method, String url, Object requestRestObject, Class<?> responseRestObjectClass) {
		this();
		
		this.method = method;
		this.url = url;
		this.requestRestObjectClass = requestRestObject.getClass();		
		this.requestRestObject = requestRestObject;
		this.responseRestObjectClass = getArrayClass(responseRestObjectClass);
	}

	public RestRequest(HttpMethod method, String url, Object requestRestObject) {
		this();
		
		this.method = method;
		this.url = url;
		this.requestRestObjectClass = requestRestObject.getClass();
		this.requestRestObject = requestRestObject;
		this.responseRestObjectClass = getArrayClass(requestRestObject.getClass());
	}

	public HttpMethod getMethod() {
		return method;
	}

	public String getUrl() {
		return url;
	}

	public String getParams() {
		return params;
	}
	
	public Class<?> getRequestRestObjectClass() {
		return requestRestObjectClass;
	}

	public Class<?> getResponseRestObjectClass() {
		return responseRestObjectClass;
	}

	public Object getRequestRestObject() {
		return requestRestObject;
	}

	public void setHeader(String headerName, String headerValue) {
		headers.set(headerName, headerValue);
	}
	
	public HttpHeaders getHeaders() {
		return headers;
	}
	
	private Class<?> getArrayClass(Class<?> responseRestObjectClass) {
		try {
			return Class.forName("[L" + responseRestObjectClass.getName() + ";");
		} catch(ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
}
