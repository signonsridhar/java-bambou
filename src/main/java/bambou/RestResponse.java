package bambou;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public class RestResponse {
	private HttpHeaders headers;
	private HttpStatus statusCode;
	private Object body;
	
	public RestResponse(HttpHeaders headers, HttpStatus statusCode, Object body) {
		this.headers = headers;
		this.statusCode = statusCode;
		this.body = body;
	}

	public HttpHeaders getHeaders() {
		return headers;
	}
	
	public HttpStatus getStatusCode() {
		return statusCode;
	}

	public Object getBody() {
		return body;
	}
	
	public RestObject[] getRestObjects() {
		if (body.getClass().isArray()) {
			return (RestObject[]) body;
		} else {
			return null;
		}
	}
}
