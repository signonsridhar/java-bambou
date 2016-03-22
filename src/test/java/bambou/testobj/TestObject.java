package bambou.testobj;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import bambou.RestObject;

public class TestObject extends RestObject {
	public final static String REST_NAME = "object";
	public final static String RESOURCE_NAME = "object";

	@JsonProperty("myProperty")
	protected String myProperty;

	@JsonIgnore
	private TestChildObjectFetcher childObjectFetcher;

	public TestObject() {
		childObjectFetcher = new TestChildObjectFetcher(this);
	}

	public String getMyProperty() {
		return myProperty;
	}

	public void setMyProperty(String myProperty) {
		this.myProperty = myProperty;
	}
}