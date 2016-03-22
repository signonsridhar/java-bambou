package bambou.testobj;

import bambou.RestRootObject;
import bambou.RestSession;

public class TestRestSession extends RestSession {
	private RestRootObject rootObject;

	public TestRestSession() {
		this("", "", "");
	}

	public TestRestSession(String username, String password, String enterprise) {
		super(username, password, enterprise, "", "", 1.0, null);
	}

	public TestRestSession(RestRootObject rootObject, String username, String password, String enterprise, String apiUrl, String apiPrefix, double version) {
		super(username, password, enterprise, apiUrl, apiPrefix, version, null);

		this.rootObject = rootObject;
	}

	@Override
	protected RestRootObject createRootObject() {
		return rootObject;
	}
}