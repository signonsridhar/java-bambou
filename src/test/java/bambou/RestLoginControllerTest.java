package bambou;
import org.junit.Assert;
import org.junit.Test;

import bambou.RestLoginController;

public class RestLoginControllerTest {

	@Test
	public void testNewLoginController() {
		String url = "http://vsd";
		String user = "martin";
		String username = "mleclerc";
		String enterprise = "martin";
		String password = "martin";
		String apiKey = "12345";
		String certificate = "67890";
		
		RestLoginController controller = new RestLoginController();
		controller.setUrl(url);
		controller.setUser(user);
		controller.setUsername(username);
		controller.setEnterprise(enterprise);
		controller.setPassword(password);
		controller.setApiKey(apiKey);
		controller.setCertificate(certificate);
		
		Assert.assertEquals(url, controller.getUrl());
		Assert.assertEquals(user, controller.getUser());
		Assert.assertEquals(username, controller.getUsername());
		Assert.assertEquals(enterprise, controller.getEnterprise());
		Assert.assertEquals(password, controller.getPassword());
		Assert.assertEquals(apiKey, controller.getApiKey());
		Assert.assertEquals(certificate, controller.getCertificate());
	}
	
	@Test
	public void testGetAuthenticationHeader1() {
		String user = "martin";
		String password = "martin";
		
		RestLoginController controller = new RestLoginController();
		controller.setUser(user);
		controller.setPassword(password);
		
		String authenticationHeaderValue = controller.getAuthenticationHeader(null, null, null, null);
		
		Assert.assertEquals("XREST bWFydGluOm1hcnRpbg==", authenticationHeaderValue);
	}
	
	@Test
	public void testGetAuthenticationHeader2() {
		String user = "martin";
		String password = "martin";
		
		RestLoginController controller = new RestLoginController();
		
		String authenticationHeaderValue = controller.getAuthenticationHeader(user, null, password, null);
		
		Assert.assertEquals("XREST bWFydGluOm1hcnRpbg==", authenticationHeaderValue);
	}
	
	@Test
	public void testGetAuthenticationHeader3() {
		String user = "martin";
		String apiKey = "0123456789";
		
		RestLoginController controller = new RestLoginController();
		
		String authenticationHeaderValue = controller.getAuthenticationHeader(user, apiKey, null, null);
		
		Assert.assertEquals("XREST bWFydGluOjAxMjM0NTY3ODk=", authenticationHeaderValue);
	}
	
	@Test
	public void testGetAuthenticationHeader4() {
		String user = "martin";
		String certificate = "0123456789";
		
		RestLoginController controller = new RestLoginController();
		
		String authenticationHeaderValue = controller.getAuthenticationHeader(user, null, null, certificate);
		
		Assert.assertEquals("XREST bWFydGluOg==", authenticationHeaderValue);
	}
}
