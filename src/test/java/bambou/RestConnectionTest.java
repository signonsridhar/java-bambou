package bambou;

import java.util.HashMap;
import java.util.Map;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import bambou.testobj.TestRestSession;

public class RestConnectionTest {

	@Test
	public void testNewConnection() {
		RestRequest request = new RestRequest(HttpMethod.GET, "http://vsd", "test");
		RestConnection connection = new RestConnection(request);
		Map<String, Object> userInfo = new HashMap<String, Object>();
		userInfo.put("test", "value");
		connection.setUserInfo(userInfo);
		Assert.assertEquals(userInfo, connection.getUserInfo());
		Assert.assertNull(connection.getResponse());
	}

	@Test
	public void testStartRestConnection() throws RestException {
		String username = "martin";
		String password = "martin";
		String enterprise = "martin";
		HttpMethod method = HttpMethod.GET;
		String url = "http://vsd";
		String content = "test";

		RestSession session = new TestRestSession(username, password, enterprise);
		RestRequest request = new RestRequest(method, url, content);

		RestOperations restOperations = EasyMock.createStrictMock(RestOperations.class);
		Capture<HttpEntity<?>> capturedHttpEntity = EasyMock.newCapture();
		RestConnection connection = new RestConnection(request);
		RestConnection.setRestOperations(restOperations);

		EasyMock.expect(restOperations.exchange(EasyMock.eq(url), EasyMock.eq(method), EasyMock.capture(capturedHttpEntity), EasyMock.eq(String.class)))
		        .andReturn(new ResponseEntity<String>(HttpStatus.OK));
		EasyMock.replay(restOperations);

		connection.start(session);

		HttpHeaders requestHeaders = capturedHttpEntity.getValue().getHeaders();
		Assert.assertEquals(3, requestHeaders.size());
		Assert.assertEquals("application/json", requestHeaders.getFirst("Content-Type"));
		Assert.assertEquals(enterprise, requestHeaders.getFirst("X-Nuage-Organization"));
		Assert.assertEquals("XREST bWFydGluOm1hcnRpbg==", requestHeaders.getFirst("Authorization"));

		RestResponse response = connection.getResponse();
		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

		EasyMock.verify(restOperations);
	}
}
