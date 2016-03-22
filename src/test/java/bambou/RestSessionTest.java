package bambou;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import bambou.testobj.TestRestSession;
import bambou.testobj.TestRootObject;

public class RestSessionTest {

	@Test
	public void testStartSession() throws RestException {
		RestSession session = startSession();
		Assert.assertEquals(session, RestSessionContext.session.get());
		RestSessionContext.session.set(null);
	}

	@Test
	public void testResetSession() throws RestException {
		RestSession session = startSession();
		Assert.assertEquals(session, RestSessionContext.session.get());
		
		session.reset();
		Assert.assertNull(RestSessionContext.session.get());
	}

	private RestSession startSession() throws RestException {
		String username = "martin";
		String password = "martin";
		String enterprise = "martin";
		String url = "http://vsd";
		String apiPrefix = "api";
		double version = 2.1;

		RestOperations restOperations = EasyMock.createStrictMock(RestOperations.class);
		RestConnection.setRestOperations(restOperations);

		EasyMock.expect(restOperations.exchange(EasyMock.eq(url + '/' + apiPrefix + "/v2_1/" + TestRootObject.REST_NAME), EasyMock.eq(HttpMethod.GET),
		        EasyMock.anyObject(HttpEntity.class), EasyMock.eq(String.class))).andReturn(new ResponseEntity<String>("[{}]", HttpStatus.OK));
		EasyMock.replay(restOperations);

		RestRootObject rootObject = new TestRootObject();
		RestSession session = new TestRestSession(rootObject, username, password, enterprise, url, apiPrefix, version);
		session.start();

		EasyMock.verify(restOperations);

		return session;
	}
}
