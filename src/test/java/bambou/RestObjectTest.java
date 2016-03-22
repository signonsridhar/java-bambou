package bambou;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

import bambou.testobj.TestChildObject;
import bambou.testobj.TestChildTemplateObject;
import bambou.testobj.TestObject;
import bambou.testobj.TestRestSession;
import bambou.testobj.TestRootObject;

public class RestObjectTest {

	private ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testNewObject() {
		String id = "12345";
		String parentId = "67890";
		String parentType = "parentType";
		String creationDate = "2016-03-21";
		String lastUpdatedDate = "2016-03-22";
		String owner = "martin";

		RestObject object = new RestObject();
		object.setId(id);
		object.setParentId(parentId);
		object.setParentType(parentType);
		object.setCreationDate(creationDate);
		object.setLastUpdatedDate(lastUpdatedDate);
		object.setOwner(owner);

		Assert.assertEquals(id, object.getId());
		Assert.assertEquals(parentId, object.getParentId());
		Assert.assertEquals(parentType, object.getParentType());
		Assert.assertEquals(creationDate, object.getCreationDate());
		Assert.assertEquals(lastUpdatedDate, object.getLastUpdatedDate());
		Assert.assertEquals(owner, object.getOwner());
	}

	@Test
	public void testFetchObject() throws RestException, RestClientException, JsonProcessingException {
		String id = "12345";

		// Create response object to fetch REST call
		TestObject refObject = new TestObject();
		refObject.setId(id);
		refObject.setMyProperty("MyValue");

		// Start session
		RestOperations restOperations = EasyMock.createStrictMock(RestOperations.class);
		RestConnection.setRestOperations(restOperations);
		startSession(restOperations, TestObject.REST_NAME + '/' + id, HttpMethod.GET,  HttpStatus.OK, mapper.writeValueAsString(Arrays.asList(refObject)));

		// Fetch object
		TestObject object = new TestObject();
		object.setId(id);
		object.fetch();

		// Expect some object properties to be set
		Assert.assertEquals(id, object.getId());
		Assert.assertEquals(refObject.getMyProperty(), object.getMyProperty());
		
		// Verify mock calls
		EasyMock.verify(restOperations);
	}

	@Test
	public void testSaveObject() throws RestException, RestClientException, JsonProcessingException {
		String id = "12345";

		// Create object
		TestObject object = new TestObject();
		object.setId(id);
		object.setMyProperty("MyProperty");

		// Start session
		RestOperations restOperations = EasyMock.createStrictMock(RestOperations.class);
		RestConnection.setRestOperations(restOperations);		
		startSession(restOperations, TestObject.REST_NAME + '/' + id, HttpMethod.PUT, HttpStatus.NO_CONTENT, "[]");

		// Save object
		object.save();
		
		// Verify mock calls
		EasyMock.verify(restOperations);
	}

	@Test
	public void testDeleteObject() throws RestException, RestClientException, JsonProcessingException {
		String id = "12345";

		// Create object
		TestObject object = new TestObject();
		object.setId(id);

		// Start session
		RestOperations restOperations = EasyMock.createStrictMock(RestOperations.class);
		RestConnection.setRestOperations(restOperations);		
		startSession(restOperations, TestObject.REST_NAME + '/' + id + "?responseChoice=1", HttpMethod.DELETE, HttpStatus.NO_CONTENT, "[]");

		// Save object
		object.delete();
		
		// Verify mock calls
		EasyMock.verify(restOperations);
	}

	@Test
	public void testCreateChildObject() throws RestException, RestClientException, JsonProcessingException {
		String id = "12345";

		// Create object
		TestObject object = new TestObject();
		object.setId(id);

		// Create child object
		TestChildObject childObject = new TestChildObject();
		childObject.setMyOtherProperty("MyOtherValue");
		
		// Start session
		RestOperations restOperations = EasyMock.createStrictMock(RestOperations.class);
		RestConnection.setRestOperations(restOperations);		
		startSession(restOperations, TestObject.REST_NAME + '/' + id + '/' + TestChildObject.REST_NAME, HttpMethod.POST, HttpStatus.OK, mapper.writeValueAsString(Arrays.asList(childObject)));

		// Save object
		object.createChild(childObject);
		
		// Verify mock calls
		EasyMock.verify(restOperations);
	}

	@Test
	public void testInstantiateChildObject() throws RestException, RestClientException, JsonProcessingException {
		String id = "12345";

		// Create object
		TestObject object = new TestObject();
		object.setId(id);

		// Create child template object
		TestChildTemplateObject childTemplate = new TestChildTemplateObject();
		childTemplate.setId("67890");
		
		// Create child object
		TestChildObject childObject = new TestChildObject();

		// Start session
		RestOperations restOperations = EasyMock.createStrictMock(RestOperations.class);
		RestConnection.setRestOperations(restOperations);		
		startSession(restOperations, TestObject.REST_NAME + '/' + id + '/' + TestChildObject.REST_NAME, HttpMethod.POST, HttpStatus.OK, mapper.writeValueAsString(Arrays.asList(childObject)));

		// Save object
		object.instantiateChild(childObject, childTemplate);
		
		// Verify mock calls
		EasyMock.verify(restOperations);
	}

	@Test
	public void testAssignObjects() throws RestException, RestClientException, JsonProcessingException {
		String id = "12345";

		// Create object
		TestObject object = new TestObject();
		object.setId(id);

		// Create child objects
		List<RestObject> childObjects = new ArrayList<RestObject>();
		TestChildObject childObject1 = new TestChildObject();
		childObject1.setId("1");
		childObjects.add(childObject1);
		TestChildObject childObject2 = new TestChildObject();
		childObject2.setId("2");
		childObjects.add(childObject2);
		TestChildObject childObject3 = new TestChildObject();
		childObject3.setId("3");
		childObjects.add(childObject3);
		
		// Start session
		RestOperations restOperations = EasyMock.createStrictMock(RestOperations.class);
		RestConnection.setRestOperations(restOperations);		
		startSession(restOperations, TestObject.REST_NAME + '/' + id + '/' + TestChildObject.REST_NAME, HttpMethod.PUT, HttpStatus.OK, mapper.writeValueAsString(Arrays.asList(object)));		

		// Assign child objects
		object.assign(childObjects, TestChildObject.class);
		
		// Verify mock calls
		EasyMock.verify(restOperations);
	}

	private RestSession startSession(RestOperations restOperations, String urlSuffix, HttpMethod method, HttpStatus responseStatus, String responseString) throws RestException {
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
