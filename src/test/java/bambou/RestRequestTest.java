package bambou;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpMethod;

import bambou.RestRequest;

public class RestRequestTest {

	@Test
	public void testNewRequest1() {
		HttpMethod method = HttpMethod.DELETE;
		String url = "http://test.com";
		String requestRestObject = "test";
		RestRequest request = new RestRequest(method, url, requestRestObject);
		Assert.assertEquals(method, request.getMethod());
		Assert.assertEquals(url, request.getUrl());
		Assert.assertEquals(requestRestObject, request.getRequestRestObject());
		Assert.assertEquals(null, request.getParams());
		Assert.assertEquals(String.class, request.getRequestRestObjectClass());
		Assert.assertEquals(String[].class, request.getResponseRestObjectClass());
		Assert.assertEquals(1, request.getHeaders().size());
		Assert.assertArrayEquals(request.getHeaders().get("Content-Type").toArray(), new String[] { "application/json" });
	}

	@Test
	public void testNewRequest2() {
		HttpMethod method = HttpMethod.POST;
		String url = "http://test.com";
		Object requestRestObject = new Object();
		Class<?> responseRestObjectClass = String.class;
		RestRequest request = new RestRequest(method, url, requestRestObject, responseRestObjectClass);
		Assert.assertEquals(method, request.getMethod());
		Assert.assertEquals(url, request.getUrl());
		Assert.assertEquals(requestRestObject, request.getRequestRestObject());
		Assert.assertEquals(null, request.getParams());
		Assert.assertEquals(requestRestObject.getClass(), request.getRequestRestObjectClass());
		Assert.assertEquals(String[].class, request.getResponseRestObjectClass());
		Assert.assertEquals(1, request.getHeaders().size());
		Assert.assertArrayEquals(request.getHeaders().get("Content-Type").toArray(), new String[] { "application/json" });
	}

	@Test
	public void testNewRequest3() {
		HttpMethod method = HttpMethod.GET;
		String url = "http://test.com";
		String params = "?test=true";
		Class<?> requestRestObjectClass = String.class;
		RestRequest request = new RestRequest(method, url, params, requestRestObjectClass, true);
		Assert.assertEquals(method, request.getMethod());
		Assert.assertEquals(url, request.getUrl());
		Assert.assertEquals(null, request.getRequestRestObject());
		Assert.assertEquals(params, request.getParams());
		Assert.assertEquals(requestRestObjectClass, request.getRequestRestObjectClass());
		Assert.assertEquals(String[].class, request.getResponseRestObjectClass());
	}

	@Test
	public void testSetHeader() {
		HttpMethod method = HttpMethod.DELETE;
		String url = "http://test.com";
		String requestRestObject = "test";
		RestRequest request = new RestRequest(method, url, requestRestObject);
		request.setHeader("header", "value");
		Assert.assertEquals(2, request.getHeaders().size());
		Assert.assertArrayEquals(request.getHeaders().get("Content-Type").toArray(), new String[] { "application/json" });
		Assert.assertArrayEquals(request.getHeaders().get("header").toArray(), new String[] { "value" });
	}
}
