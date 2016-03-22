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

public class RestPushCenterTest {

	@Test
	public void testNewPushCenter() {
		String url = "http://vsd";

		RestSession session = new TestRestSession();
		RestPushCenter pushCenter = new RestPushCenter(session);
		pushCenter.setUrl(url);
		Assert.assertEquals(url, pushCenter.getUrl());
		Assert.assertFalse(pushCenter.isRunning());
	}

	@Test
	public void testStartPushCenter() throws InterruptedException {
		String url = "http://vsd";

		RestOperations restOperations = EasyMock.createStrictMock(RestOperations.class);
		RestConnection.setRestOperations(restOperations);

		EasyMock.expect(restOperations.exchange(EasyMock.eq(url + "/events"), EasyMock.eq(HttpMethod.GET), EasyMock.anyObject(HttpEntity.class),
		        EasyMock.eq(String.class))).andReturn(new ResponseEntity<String>("{}", HttpStatus.OK)).times(1, 200);
		EasyMock.replay(restOperations);

		RestSession session = new TestRestSession();
		RestPushCenter pushCenter = new RestPushCenter(session);
		pushCenter.setUrl(url);
		pushCenter.start();

		Thread.sleep(500);
		Assert.assertTrue(pushCenter.isRunning());
		pushCenter.stop();
		Assert.assertFalse(pushCenter.isRunning());

		EasyMock.verify(restOperations);
	}
}
