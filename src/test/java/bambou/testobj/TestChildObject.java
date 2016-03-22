package bambou.testobj;

import com.fasterxml.jackson.annotation.JsonProperty;

import bambou.RestObject;

public class TestChildObject extends RestObject {
	public final static String REST_NAME = "childobject";
	public final static String RESOURCE_NAME = "childobject";

	@JsonProperty(value = "templateId")
	protected String templateId;

	@JsonProperty("myOtherProperty")
	protected String myOtherProperty;

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getMyOtherProperty() {
		return myOtherProperty;
	}

	public void setMyOtherProperty(String myOtherProperty) {
		this.myOtherProperty = myOtherProperty;
	}
}