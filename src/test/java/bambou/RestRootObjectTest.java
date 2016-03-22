package bambou;

import java.util.Arrays;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import bambou.testobj.TestRestSession;
import bambou.testobj.TestRootObject;

public class RestRootObjectTest {

	private ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testNewRootObject() throws RestException {
		String username = "martin";
		String password = "martin";
		String apiKey = "12345";

		RestRootObject rootObject = new TestRootObject();
		rootObject.setUserName(username);
		rootObject.setPassword(password);
		rootObject.setApiKey(apiKey);

		Assert.assertEquals(apiKey, rootObject.getApiKey());
		Assert.assertEquals(username, rootObject.getUserName());
		Assert.assertEquals(password, rootObject.getPassword());
	}

	@Test
	public void testFetchObject() throws RestException, RestClientException, JsonProcessingException {
		String id = "12345";

		// Create response object to fetch REST call
		TestRootObject refRootObject = new TestRootObject();
		refRootObject.setId(id);
		refRootObject.setApiKey("12345");

		// Start session
		RestOperations restOperations = EasyMock.createStrictMock(RestOperations.class);
		RestConnection.setRestOperations(restOperations);
		startSession(restOperations, TestRootObject.REST_NAME, HttpMethod.GET, HttpStatus.OK, mapper.writeValueAsString(Arrays.asList(refRootObject)));

		// Fetch root object
		RestRootObject rootObject = new TestRootObject();
		rootObject.fetch();

		// Expect some object properties to be set
		Assert.assertEquals(id, rootObject.getId());
		Assert.assertEquals(refRootObject.getApiKey(), rootObject.getApiKey());

		// Verify mock calls
		EasyMock.verify(restOperations);
	}

	@Test
	public void testSaveObject() throws RestException, RestClientException, JsonProcessingException {
		String id = "12345";

		// Start session
		RestOperations restOperations = EasyMock.createStrictMock(RestOperations.class);
		RestConnection.setRestOperations(restOperations);
		startSession(restOperations, TestRootObject.REST_NAME, HttpMethod.PUT, HttpStatus.NO_CONTENT, "[]");

		// Save root object
		RestRootObject rootObject = new TestRootObject();
		rootObject.setId(id);
		rootObject.prepareChangePassword("password");
		rootObject.save();

		// Verify mock calls
		EasyMock.verify(restOperations);
	}

	private RestSession startSession(RestOperations restOperations, String urlSuffix, HttpMethod method, HttpStatus responseStatus, String responseString)
	        throws RestException {
		String username = "martin";
		String password = "martin";
		String enterprise = "martin";
		String url = "http://vsd";
		String apiPrefix = "api";
		double version = 2.1;

		Capture<HttpEntity<?>> capturedHttpEntity = EasyMock.newCapture();

		// Expected REST calls
		EasyMock.expect(restOperations.exchange(EasyMock.eq(url + '/' + apiPrefix + "/v2_1/" + TestRootObject.REST_NAME), EasyMock.eq(HttpMethod.GET),
		        EasyMock.anyObject(HttpEntity.class), EasyMock.eq(String.class))).andReturn(new ResponseEntity<String>("[{}]", HttpStatus.OK));
		EasyMock.expect(restOperations.exchange(EasyMock.eq(url + '/' + apiPrefix + "/v2_1/" + urlSuffix), EasyMock.eq(method),
		        EasyMock.capture(capturedHttpEntity), EasyMock.eq(String.class))).andReturn(new ResponseEntity<String>(responseString, responseStatus));
		EasyMock.replay(restOperations);

		// Start REST session
		RestRootObject rootObject = new TestRootObject();
		RestSession session = new TestRestSession(rootObject, username, password, enterprise, url, apiPrefix, version);
		session.start();

		return session;
	}
}
