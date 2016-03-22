package bambou;

import java.util.ArrayList;
import java.util.List;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import bambou.testobj.TestChildObject;
import bambou.testobj.TestChildObjectFetcher;
import bambou.testobj.TestObject;
import bambou.testobj.TestRestSession;
import bambou.testobj.TestRootObject;

public class RestFetcherTest {

	private ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testGet() throws RestException, JsonProcessingException {
		// Create child objects
		List<RestObject> refChildObjects = new ArrayList<RestObject>();
		TestChildObject childObject1 = new TestChildObject();
		childObject1.setId("1");
		refChildObjects.add(childObject1);
		TestChildObject childObject2 = new TestChildObject();
		childObject2.setId("2");
		refChildObjects.add(childObject2);

		// Start session
		RestOperations restOperations = EasyMock.createStrictMock(RestOperations.class);
		RestConnection.setRestOperations(restOperations);
		startSession(restOperations, TestObject.REST_NAME + '/' + TestChildObject.REST_NAME, HttpMethod.GET, HttpStatus.OK,
		        mapper.writeValueAsString(refChildObjects), null);

		TestObject object = new TestObject();
		TestChildObjectFetcher fetcher = new TestChildObjectFetcher(object);
		List<TestChildObject> childObjects = fetcher.get();
		Assert.assertEquals(2, childObjects.size());
	}

	@Test
	public void testGetFirst() throws JsonProcessingException, RestException {
		// Create child objects
		List<RestObject> refChildObjects = new ArrayList<RestObject>();
		TestChildObject childObject1 = new TestChildObject();
		childObject1.setId("1");
		refChildObjects.add(childObject1);
		TestChildObject childObject2 = new TestChildObject();
		childObject2.setId("2");
		refChildObjects.add(childObject2);
		TestChildObject childObject3 = new TestChildObject();
		childObject3.setId("3");
		refChildObjects.add(childObject3);

		// Start session
		RestOperations restOperations = EasyMock.createStrictMock(RestOperations.class);
		RestConnection.setRestOperations(restOperations);
		startSession(restOperations, TestObject.REST_NAME + '/' + TestChildObject.REST_NAME, HttpMethod.GET, HttpStatus.OK,
		        mapper.writeValueAsString(refChildObjects), null);

		TestObject object = new TestObject();
		TestChildObjectFetcher fetcher = new TestChildObjectFetcher(object);
		TestChildObject childObject = fetcher.getFirst();
		Assert.assertEquals("1", childObject.getId());
	}

	@Test
	public void testFetch() throws JsonProcessingException, RestException {
		// Create child objects
		List<RestObject> refChildObjects = new ArrayList<RestObject>();
		TestChildObject childObject1 = new TestChildObject();
		childObject1.setId("1");
		refChildObjects.add(childObject1);
		TestChildObject childObject2 = new TestChildObject();
		childObject2.setId("2");
		refChildObjects.add(childObject2);
		TestChildObject childObject3 = new TestChildObject();
		childObject3.setId("3");
		refChildObjects.add(childObject3);

		// Start session
		RestOperations restOperations = EasyMock.createStrictMock(RestOperations.class);
		RestConnection.setRestOperations(restOperations);
		startSession(restOperations, TestObject.REST_NAME + '/' + TestChildObject.REST_NAME, HttpMethod.GET, HttpStatus.OK,
		        mapper.writeValueAsString(refChildObjects), null);

		TestObject object = new TestObject();
		TestChildObjectFetcher fetcher = new TestChildObjectFetcher(object);
		List<TestChildObject> childObjects = fetcher.fetch();
		Assert.assertEquals(3, childObjects.size());
	}

	@Test
	public void testCount() throws JsonProcessingException, RestException {
		// Create child objects
		List<RestObject> refChildObjects = new ArrayList<RestObject>();
		TestChildObject childObject1 = new TestChildObject();
		childObject1.setId("1");
		refChildObjects.add(childObject1);
		TestChildObject childObject2 = new TestChildObject();
		childObject2.setId("2");
		refChildObjects.add(childObject2);
		TestChildObject childObject3 = new TestChildObject();
		childObject3.setId("3");
		refChildObjects.add(childObject3);
		TestChildObject childObject4 = new TestChildObject();
		childObject3.setId("4");
		refChildObjects.add(childObject4);

		// Start session
		RestOperations restOperations = EasyMock.createStrictMock(RestOperations.class);
		RestConnection.setRestOperations(restOperations);
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("X-Nuage-Count", String.valueOf(refChildObjects.size()));
		startSession(restOperations, TestObject.REST_NAME + '/' + TestChildObject.REST_NAME, HttpMethod.HEAD, HttpStatus.OK,
		        mapper.writeValueAsString(refChildObjects), responseHeaders);

		TestObject object = new TestObject();
		TestChildObjectFetcher fetcher = new TestChildObjectFetcher(object);
		int count = fetcher.count();
		Assert.assertEquals(refChildObjects.size(), count);
	}

	private RestSession startSession(RestOperations restOperations, String urlSuffix, HttpMethod method, HttpStatus responseStatus, String responseString, HttpHeaders responseHeaders)
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
		        EasyMock.capture(capturedHttpEntity), EasyMock.eq(String.class))).andReturn(new ResponseEntity<String>(responseString, responseHeaders, responseStatus));
		EasyMock.replay(restOperations);

		// Start REST session
		RestRootObject rootObject = new TestRootObject();
		RestSession session = new TestRestSession(rootObject, username, password, enterprise, url, apiPrefix, version);
		session.start();

		return session;
	}
}
