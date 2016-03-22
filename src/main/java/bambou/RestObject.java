package bambou;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RestObject {
	@JsonProperty(value = "ID")
	protected String id;

	@JsonProperty(value = "parentID")
	protected String parentId;

	@JsonProperty(value = "parentType")
	protected String parentType;

	@JsonProperty(value = "creationDate")
	protected String creationDate;

	@JsonProperty(value = "lastUpdatedDate")
	protected String lastUpdatedDate;

	@JsonProperty(value = "owner")
	protected String owner;

	private Map<String, RestFetcher<?>> fetcherRegistry = new HashMap<String, RestFetcher<?>>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getParentType() {
		return parentType;
	}

	public void setParentType(String parentType) {
		this.parentType = parentType;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	public String getLastUpdatedDate() {
		return lastUpdatedDate;
	}

	public void setLastUpdatedDate(String lastUpdatedDate) {
		this.lastUpdatedDate = lastUpdatedDate;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public void fetch() throws RestException {
		if (id == null) {
			throw new RestException("Cannot fetch an object that does not have an ID");
		}

		RestRequest request = new RestRequest(HttpMethod.GET, getResourceUrl(), null, this.getClass(), true);
		RestConnection connection = sendRequest(request, null);
		didRetrieve(connection);
	}

	public void save() throws RestException {
		RestConnection connection = manageChildObject(this, HttpMethod.PUT, null, false);
		didPerformStandardOperation(connection);
	}

	public void delete() throws RestException {
		RestConnection connection = manageChildObject(this, HttpMethod.DELETE, 1, false);
		didPerformStandardOperation(connection);		
	}

	public void createChild(RestObject restObject) throws RestException {
		RestConnection connection = manageChildObject(restObject, HttpMethod.POST, null, true);
		didCreateChild(connection);
	}

	public void instantiateChild(RestObject restObject, RestObject fromTemplate) throws RestException {
		if (fromTemplate.getId() == null) {
			throw new RestException(String.format("Cannot instantiate a child from a template with no ID: %s", fromTemplate));
		}

		PropertyDescriptor pd = getPropertyDescriptor(restObject, "templateId");
		if (pd != null) {
			setRestObjectProperty(pd, restObject, fromTemplate.getId());
		} else {
			throw new RestException(String.format("Cannot instantiate a child that does not have a templateId property: %s", fromTemplate));
		}

		RestConnection connection = manageChildObject(restObject, HttpMethod.POST, null, true);
		didCreateChild(connection);
	}

	public void assign(List<? extends RestObject> restObjects, Class<?> restObjectClass) throws RestException {
		List<String> ids = new ArrayList<String>();

		for (RestObject restObject : restObjects) {
			ids.add(restObject.getId());
		}

		String url = getResourceUrlForChildType(restObjectClass);
		
		RestRequest request = new RestRequest(HttpMethod.PUT, url, ids, getClass());
		
		Map<String, Object> userInfo = new HashMap<String, Object>();
		userInfo.put("nurest_objects", restObjects);
		userInfo.put("commit", true);
		
		RestConnection connection = sendRequest(request, userInfo);
		didPerformStandardOperation(connection);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (!(obj instanceof RestObject)) {
			throw new IllegalStateException();
		}

		RestObject restObject = (RestObject) obj;

		if (!getRestName(getClass()).equals(getRestName(restObject.getClass()))) {
			return false;
		}

		if (id != null && restObject.id != null) {
			return id.equals(restObject.id);
		}

		return false;
	}

	public String toString() {
		return String.format("%s (ID=%s)", getClass().getName(), id);
	}

	protected static String getRestBaseUrl() {
		RestLoginController controller = RestSession.getCurrentSession().getLoginContoller();
		return controller.getUrl();
	}

	protected RestConnection sendRequest(RestRequest request, Map<String, Object> userInfo) throws RestException {
		RestConnection connection = new RestConnection(request);
		connection.setUserInfo(userInfo);
		RestSession session = RestSession.getCurrentSession();
		connection.start(session);
		return connection;
	}

	private RestConnection manageChildObject(RestObject restObject, HttpMethod method, Integer responseChoice, boolean commit) throws RestException {
		String url;
		if (method == HttpMethod.POST) {
			url = getResourceUrlForChildType(restObject.getClass());
		} else {
			url = getResourceUrl();
		}

		if (responseChoice != null) {
			url += String.format("?responseChoice=%s", responseChoice);
		}

		RestRequest request;
		if (method == HttpMethod.DELETE) {
			request = new RestRequest(method, url, null, restObject.getClass(), true);
		} else {
			request = new RestRequest(method, url, restObject);
		}
		
		Map<String, Object> userInfo = new HashMap<String, Object>();
		userInfo.put("nurest_object", restObject);
		userInfo.put("commit", commit);
		
		RestConnection connection = sendRequest(request, userInfo);
		return connection;
	}

	protected void fromRestObject(RestObject restObject) throws RestException {
		if (restObject.getClass() != this.getClass()) {
			return;
		}

		List<Field> fields = getAllFields(restObject.getClass());
		for (Field field : fields) {
			if (field.getAnnotation(JsonProperty.class) != null) {
				PropertyDescriptor pd = getPropertyDescriptor(this, field.getName());
				if (pd != null) {
					Object value = getRestObjectProperty(pd, restObject);
					setRestObjectProperty(pd, this, value);
				}
			}
		}
	}

	private PropertyDescriptor getPropertyDescriptor(RestObject restObject, String propertyName) throws RestException {
		try {
			for (PropertyDescriptor pd : Introspector.getBeanInfo(restObject.getClass()).getPropertyDescriptors()) {
				if (pd.getName().equals(propertyName)) {
					return pd;
				}
			}
		} catch (IntrospectionException ex) {
			throw new RestException(ex);
		}

		return null;
	}

	private void didCreateChild(RestConnection connection) throws RestException {
		RestObject restObject = (RestObject) connection.getUserInfo().get("nurest_object");
		if (restObject != null) {
			RestResponse response = connection.getResponse();
			restObject.fromRestObject(response.getRestObjects()[0]);
		}

		didPerformStandardOperation(connection);
	}

	protected void didRetrieve(RestConnection connection) throws RestException {
		RestResponse response = connection.getResponse();
		fromRestObject(response.getRestObjects()[0]);
		didPerformStandardOperation(connection);
	}

	private void didPerformStandardOperation(RestConnection connection) throws RestException {
		Map<String, Object> userInfo = connection.getUserInfo();
		if (userInfo != null && userInfo.containsKey("nurest_objects")) {
			if ((Boolean) userInfo.get("commit")) {
				@SuppressWarnings("unchecked")
				List<RestObject> restObjects = (List<RestObject>) userInfo.get("nurest_objects");
				for (RestObject restObject : restObjects) {
					addChild(restObject);
				}
			}
		}

		if (userInfo != null && userInfo.containsKey("nurest_object")) {
			if ((Boolean) userInfo.get("commit")) {
				RestObject userObject = (RestObject) userInfo.get("nurest_object");
				addChild(userObject);
			}
		}
	}

	private void addChild(RestObject child) throws RestException {
		String restName = getRestName(child.getClass());
		@SuppressWarnings("unchecked")
		RestFetcher<RestObject> children = (RestFetcher<RestObject>) fetcherRegistry.get(restName);
		if (children == null) {
			throw new RestException(String.format("Could not find fetcher with name %s while adding %s in parent %s", restName, child, this));
		}

		if (!children.contains(child)) {
			children.add(child);
		}
	}

	@JsonIgnore
	protected String getResourceUrl() {
		String name = RestObject.getResourceName(getClass());
		String url = RestObject.getRestBaseUrl();

		if (id != null) {
			return String.format("%s/%s/%s", url, name, id);
		}

		return String.format("%s/%s", url, name);
	}

	protected String getResourceUrlForChildType(Class<?> restObjectClass) {
		return String.format("%s/%s", getResourceUrl(), RestObject.getResourceName(restObjectClass));
	}

	protected void registerFetcher(RestFetcher<?> fetcher, String restName) {
		fetcherRegistry.put(restName, fetcher);
	}

	private List<Field> getAllFields(Class<?> type) {
		return getAllFields(new LinkedList<Field>(), type);
	}

	private List<Field> getAllFields(List<Field> fields, Class<?> type) {
		fields.addAll(Arrays.asList(type.getDeclaredFields()));

		if (type.getSuperclass() != null) {
			fields = getAllFields(fields, type.getSuperclass());
		}

		return fields;
	}

	private void setRestObjectProperty(PropertyDescriptor pd, RestObject restObject, Object value) throws RestException {
		try {
			pd.getWriteMethod().invoke(restObject, value);
		} catch (IllegalAccessException | InvocationTargetException ex) {
			throw new RestException(ex);
		}
	}

	private Object getRestObjectProperty(PropertyDescriptor pd, RestObject restObject) throws RestException {
		try {
			return pd.getReadMethod().invoke(restObject);
		} catch (IllegalAccessException | InvocationTargetException ex) {
			throw new RestException(ex);
		}
	}

	protected static String getRestName(Class<?> restObjectClass) {
		try {
			Field field = restObjectClass.getField("REST_NAME");
			return (String) field.get(null);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	protected static String getResourceName(Class<?> restObjectClass) {
		try {
			Field field = restObjectClass.getField("RESOURCE_NAME");
			return (String) field.get(null);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
