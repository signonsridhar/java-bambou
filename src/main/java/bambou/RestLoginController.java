package bambou;

import org.apache.commons.codec.binary.Base64;

public class RestLoginController {
	private String user;
	private String username;
	private String password;
	private String enterprise;
	private String url;
	private String apiKey;
	private String certificate;

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEnterprise() {
		return enterprise;
	}

	public void setEnterprise(String enterprise) {
		this.enterprise = enterprise;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getAuthenticationHeader(String user, String apiKey, String password, String certificate) {

		if (user == null) {
			user = this.user;
		}

		if (apiKey == null) {
			apiKey = this.apiKey;
		}

		if (password == null) {
			password = this.password;
		}

		if (certificate == null) {
			certificate = this.certificate;
		}

		if (certificate != null) {
			return String.format("XREST %s", Base64.encodeBase64String(String.format("%s:%s", user, "").getBytes()));
		}

		if (apiKey != null) {
			return String.format("XREST %s", Base64.encodeBase64String(String.format("%s:%s", user, apiKey).getBytes()));
		}

		return String.format("XREST %s", Base64.encodeBase64String(String.format("%s:%s", user, password).getBytes()));
	}

	public String getCertificate() {
		return certificate;
	}

	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}
}
