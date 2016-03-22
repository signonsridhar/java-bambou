package bambou.testobj;

import bambou.RestFetcher;
import bambou.RestObject;

public class TestChildObjectFetcher extends RestFetcher<TestChildObject> {
	private static final long serialVersionUID = 1L;

	public TestChildObjectFetcher(RestObject parentObject) {
		super(parentObject);
	}

	@Override
	protected Class<TestChildObject> getManagedClass() {
		return TestChildObject.class;
	}
}
