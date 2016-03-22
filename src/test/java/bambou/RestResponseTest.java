package bambou;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import bambou.RestObject;
import bambou.RestResponse;

public class RestResponseTest {

	@Test
	public void testNewResponse1() {
		HttpStatus status = HttpStatus.OK;
		HttpHeaders headers = new HttpHeaders();
		headers.add("header1", "value1");
		headers.add("header2", "value2");
		String body = "content";
		RestResponse response = new RestResponse(headers, status, body);
		Assert.assertEquals(headers, response.getHeaders());
		Assert.assertEquals(body, response.getBody());
		Assert.assertEquals(status, response.getStatusCode());
		Assert.assertNull(response.getRestObjects());
	}

	@Test
	public void testNewResponse2() {
		HttpStatus status = HttpStatus.NO_CONTENT;
		HttpHeaders headers = new HttpHeaders();
		headers.add("header1", "value1");
		headers.add("header2", "value2");
		RestObject object1 = new RestObject();
		RestObject object2 = new RestObject();
		RestObject[] body = new RestObject[] { object1, object2 };
		RestResponse response = new RestResponse(headers, status, body);
		Assert.assertEquals(headers, response.getHeaders());
		Assert.assertEquals(body, response.getBody());
		Assert.assertEquals(status, response.getStatusCode());
		Assert.assertArrayEquals(body, response.getRestObjects());
	}
}
