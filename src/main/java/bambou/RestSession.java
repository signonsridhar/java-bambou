package bambou;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RestSession {
	private static final Logger logger = LoggerFactory.getLogger(RestSession.class);
	
	private RestLoginController loginController;
	private RestPushCenter pushCenter;
	protected RestRootObject rootObject;

	protected RestSession(String username, String password, String enterprise, String apiUrl, String apiPrefix, double version, String certificate) {
		loginController = new RestLoginController();
		loginController.setUser(username);
		loginController.setPassword(username);
		loginController.setCertificate(certificate);
		loginController.setUsername(username);
		loginController.setEnterprise(enterprise);
		String url = String.format("%s/%s/v%s", apiUrl, apiPrefix, String.valueOf(version).replace('.', '_'));	
		loginController.setUrl(url);

		pushCenter = new RestPushCenter(this);
		pushCenter.setUrl(url);
	}

	public RestPushCenter getPushCenter() {
		return pushCenter;
	}

	public void start() throws RestException {
		RestSessionContext.session.set(this);
		authenticate();
	}

	public void reset() {
		rootObject = null;
		loginController.setApiKey(null);
	}

	protected abstract RestRootObject createRootObject();

	protected RestLoginController getLoginContoller() {
		return loginController;
	}

	protected RestRootObject getRootObject() {
		return rootObject;
	}

	protected static RestSession getCurrentSession() {
		return RestSessionContext.session.get();
	}

	protected void authenticate() throws RestException {
		if (rootObject == null) {
			rootObject = createRootObject();
			rootObject.fetch();
		}

		loginController.setApiKey(rootObject.getApiKey());
		logger.debug("[NURESTSession] Started session with username " + loginController.getUser() + " in enterprise " + loginController.getEnterprise());
	}
}
