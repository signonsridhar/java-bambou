package bambou;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpMethod;

public abstract class RestFetcher<T extends RestObject> extends ArrayList<T> {
	private static final long serialVersionUID = 1L;

	private RestObject parentObject;

	protected RestFetcher(RestObject parentObject) {
		this.parentObject = parentObject;

		String restName = getManagedObjectRestName();
		parentObject.registerFetcher(this, restName);
	}

	protected abstract Class<T> getManagedClass();

	public List<T> get() throws RestException {
		return get(null, null, null, null, null, null, true);
	}

	public List<T> fetch() throws RestException {
		return fetch(null, null, null, null, null, null, true);
	}

	public T getFirst() throws RestException {
		return getFirst(null, null, null, null, null, null, true);
	}

	public int count() throws RestException {
		return count(null, null, null, null, null, null, true);
	}

	public List<T> get(String filter, String orderBy, String[] groupBy, Integer page, Integer pageSize, String queryParameters, boolean commit)
	        throws RestException {
		return fetch(filter, orderBy, groupBy, page, pageSize, queryParameters, commit);
	}

	public T getFirst(String filter, String orderBy, String[] groupBy, Integer page, Integer pageSize, String queryParameters, boolean commit)
	        throws RestException {
		List<T> restObjects = get(filter, orderBy, groupBy, page, pageSize, queryParameters, commit);
		return (restObjects != null && !restObjects.isEmpty()) ? restObjects.get(0) : null;
	}

	public List<T> fetch(String filter, String orderBy, String[] groupBy, Integer page, Integer pageSize, String queryParameters, boolean commit)
	        throws RestException {
		RestRequest request = new RestRequest(HttpMethod.GET, prepareUrl(), queryParameters, getManagedClass(), true);
		prepareHeaders(request, filter, orderBy, groupBy, page, pageSize);
		Map<String, Object> userInfo = new HashMap<String, Object>();
		userInfo.put("commit", commit);
		RestConnection connection = parentObject.sendRequest(request, userInfo);
		return didFetch(connection);
	}

	public int count(String filter, String orderBy, String[] groupBy, Integer page, Integer pageSize, String queryParameters, boolean commit)
	        throws RestException {
		RestRequest request = new RestRequest(HttpMethod.HEAD, prepareUrl(), queryParameters, getManagedClass(), true);
		prepareHeaders(request, filter, orderBy, groupBy, page, pageSize);
		RestConnection connection = parentObject.sendRequest(request, null);
		return didCount(connection);
	}

	@SuppressWarnings("unchecked")
	private List<T> didFetch(RestConnection connection) throws RestException {
		RestResponse response = connection.getResponse();
		boolean shouldCommit = connection.getUserInfo().get("commit") == null || connection.getUserInfo().get("commit").equals(true);

		List<T> fetchedObjects = new ArrayList<T>();
		List<String> currentIds = new ArrayList<String>();

		for (T restObject : (T[]) response.getRestObjects()) {
			fetchedObjects.add(restObject);

			if (!shouldCommit) {
				continue;
			}

			currentIds.add(restObject.getId());

			if (contains(restObject)) {
				int idx = indexOf(restObject);
				RestObject currentObject = get(idx);
				currentObject.fromRestObject(restObject);
			} else {
				add(restObject);
			}
		}

		if (shouldCommit) {
			for (Iterator<T> iter = iterator(); iter.hasNext();) {
				T obj = iter.next();
				if (!currentIds.contains(obj.id)) {
					iter.remove();
				}
			}
		}

		return fetchedObjects;
	}

	private int didCount(RestConnection connection) {

		RestResponse response = connection.getResponse();

		int count = 0;
		if (response.getHeaders().containsKey("X-Nuage-Count")) {
			count = Integer.valueOf(response.getHeaders().getFirst("X-Nuage-Count"));
		}

		return count;
	}

	private String prepareUrl() {
		return parentObject.getResourceUrlForChildType(getManagedClass());
	}

	private void prepareHeaders(RestRequest request, String filter, String orderBy, String[] groupBy, Integer page, Integer pageSize) {
		if (filter != null) {
			request.setHeader("X-Nuage-Filter", filter);
		}

		if (orderBy != null) {
			request.setHeader("X-Nuage-OrderBy", orderBy);
		}

		if (page != null) {
			request.setHeader("X-Nuage-Page", String.valueOf(page));
		}

		if (pageSize != null) {
			request.setHeader("X-Nuage-PageSize", String.valueOf(pageSize));
		}

		if (groupBy != null && groupBy.length > 0) {
			String header = "";
			for (Iterator<String> iter = Arrays.asList(groupBy).iterator(); iter.hasNext();) {
				header += iter.next();
				if (iter.hasNext()) {
					header += ", ";
				}

			}
			request.setHeader("X-Nuage-GroupBy", "true");
			request.setHeader("X-Nuage-Attributes", header);
		}
	}

	private String getManagedObjectRestName() {
		return RestObject.getRestName(getManagedClass());
	}
}
