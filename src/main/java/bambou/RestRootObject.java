package bambou;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RestRootObject extends RestObject {
	private String newPassword;

	@JsonProperty(value = "userName")
	protected String userName;

	@JsonProperty(value = "password")
	protected String password;

	@JsonProperty(value = "APIKey")
	protected String apiKey;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String username) {
		this.userName = username;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void prepareChangePassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public void save() throws RestException {
		if (newPassword != null) {
			password = DigestUtils.sha1Hex(newPassword);
		}

		RestLoginController controller = RestSession.getCurrentSession().getLoginContoller();
		controller.setPassword(newPassword);
		controller.setApiKey(null);

		RestRequest request = new RestRequest(HttpMethod.PUT, getResourceUrl(), this);
		RestConnection connection = sendRequest(request, null);
		didSave(connection);
	}

	private void didSave(RestConnection connection) {
		newPassword = null;

		RestLoginController controller = RestSession.getCurrentSession().getLoginContoller();
		controller.setPassword(null);
		controller.setApiKey(apiKey);
	}

	public void fetch() throws RestException {
		RestRequest request = new RestRequest(HttpMethod.GET, getResourceUrl(), null, this.getClass(), true);
		RestConnection connection = sendRequest(request, null);
		didRetrieve(connection);
	}

	@JsonIgnore
	protected String getResourceUrl() {
		String name = getResourceName(getClass());
		String url = getRestBaseUrl();
		return String.format("%s/%s", url, name);
	}

	protected String getResourceUrlForChildType(Class<?> restObjectClass) {
		return String.format("%s/%s", getRestBaseUrl(), getResourceName(restObjectClass));
	}
}
