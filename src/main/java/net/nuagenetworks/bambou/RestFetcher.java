/*
  Copyright (c) 2015, Alcatel-Lucent Inc
  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:
      * Redistributions of source code must retain the above copyright
        notice, this list of conditions and the following disclaimer.
      * Redistributions in binary form must reproduce the above copyright
        notice, this list of conditions and the following disclaimer in the
        documentation and/or other materials provided with the distribution.
      * Neither the name of the copyright holder nor the names of its contributors
        may be used to endorse or promote products derived from this software without
        specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY
  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package net.nuagenetworks.bambou;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import net.nuagenetworks.bambou.annotation.RestEntity;
import net.nuagenetworks.bambou.operation.RestFetcherOperations;
import net.nuagenetworks.bambou.util.BambouUtils;

public class RestFetcher<T extends RestObject> extends ArrayList<T>implements RestFetcherOperations<T> {

	private static final String FILTER_HEADER = "X-Nuage-Filter";
	private static final String ORDER_BY_HEADER = "X-Nuage-OrderBy";
	private static final String PAGE_HEADER = "X-Nuage-Page";
	private static final String PAGE_SIZE_HEADER = "X-Nuage-PageSize";
	private static final String GROUP_BY_HEADER = "X-Nuage-GroupBy";
	private static final String ATTRIBUTES_HEADER = "X-Nuage-Attributes";
	private static final String COUNT_HEADER = "X-Nuage-Count";

	private static final long serialVersionUID = 1L;

	private RestObject parentRestObj;
	private Class<T> childRestObjClass;

	protected RestFetcher(RestObject parentRestObj, Class<T> childRestObjClass) {
		this.parentRestObj = parentRestObj;
		this.childRestObjClass = childRestObjClass;

		// Get the child object's REST name
		RestEntity annotation = childRestObjClass.getAnnotation(RestEntity.class);
		String childRestObjRestName = annotation.restName();

		// Register fetcher
		parentRestObj.registerFetcher(this, childRestObjRestName);
	}

	@Override
	public List<T> get() throws RestException {
		return RestSession.getCurrentSession().get(this);
	}

	@Override
	public List<T> fetch() throws RestException {
		return RestSession.getCurrentSession().fetch(this);
	}

	@Override
	public T getFirst() throws RestException {
		return RestSession.getCurrentSession().getFirst(this);
	}

	@Override
	public int count() throws RestException {
		return RestSession.getCurrentSession().count(this);
	}

	@Override
	public List<T> get(String filter, String orderBy, String[] groupBy, Integer page, Integer pageSize, String queryParameters, boolean commit)
	        throws RestException {
		return RestSession.getCurrentSession().get(this, filter, orderBy, groupBy, page, pageSize, queryParameters, commit);
	}

	@Override
	public List<T> fetch(String filter, String orderBy, String[] groupBy, Integer page, Integer pageSize, String queryParameters, boolean commit)
	        throws RestException {
		return RestSession.getCurrentSession().fetch(this, filter, orderBy, groupBy, page, pageSize, queryParameters, commit);
	}

	@Override
	public T getFirst(String filter, String orderBy, String[] groupBy, Integer page, Integer pageSize, String queryParameters, boolean commit)
	        throws RestException {
		return RestSession.getCurrentSession().getFirst(this, filter, orderBy, groupBy, page, pageSize, queryParameters, commit);
	}

	@Override
	public int count(String filter, String orderBy, String[] groupBy, Integer page, Integer pageSize, String queryParameters, boolean commit)
	        throws RestException {
		return RestSession.getCurrentSession().count(this, filter, orderBy, groupBy, page, pageSize, queryParameters, commit);
	}

	@Override
	public List<T> get(RestSession<?> session) throws RestException {
		return get(session, null, null, null, null, null, null, true);
	}

	@Override
	public List<T> fetch(RestSession<?> session) throws RestException {
		return fetch(session, null, null, null, null, null, null, true);
	}

	@Override
	public T getFirst(RestSession<?> session) throws RestException {
		return getFirst(session, null, null, null, null, null, null, true);
	}

	@Override
	public int count(RestSession<?> session) throws RestException {
		return count(session, null, null, null, null, null, null, true);
	}

	@Override
	public List<T> fetch(RestSession<?> session, String filter, String orderBy, String[] groupBy, Integer page, Integer pageSize, String queryParameters,
	        boolean commit) throws RestException {
		String resourceUrl = getResourceUrl(session);
		HttpHeaders headers = prepareHeaders(filter, orderBy, groupBy, page, pageSize);
		ResponseEntity<T[]> response = session.sendRequestWithRetry(HttpMethod.GET, resourceUrl, null, headers, null,
		        BambouUtils.getArrayClass(childRestObjClass));
		if (response.getStatusCode().series() == HttpStatus.Series.SUCCESSFUL) {
			// Success
			return merge(response.getBody(), commit);
		} else {
			// Error
			throw new RestException("Response received with status code: " + response.getStatusCode());
		}
	}

	@Override
	public List<T> get(RestSession<?> session, String filter, String orderBy, String[] groupBy, Integer page, Integer pageSize, String queryParameters,
	        boolean commit) throws RestException {
		return fetch(session, filter, orderBy, groupBy, page, pageSize, queryParameters, commit);
	}

	@Override
	public T getFirst(RestSession<?> session, String filter, String orderBy, String[] groupBy, Integer page, Integer pageSize, String queryParameters,
	        boolean commit) throws RestException {
		List<T> restObjs = get(session, filter, orderBy, groupBy, page, pageSize, queryParameters, commit);
		return (restObjs != null && !restObjs.isEmpty()) ? restObjs.get(0) : null;
	}

	@Override
	public int count(RestSession<?> session, String filter, String orderBy, String[] groupBy, Integer page, Integer pageSize, String queryParameters,
	        boolean commit) throws RestException {
		HttpHeaders headers = prepareHeaders(filter, orderBy, groupBy, page, pageSize);
		ResponseEntity<T[]> response = session.sendRequestWithRetry(HttpMethod.HEAD, getResourceUrl(session), queryParameters, headers, null,
		        BambouUtils.getArrayClass(childRestObjClass));
		if (response.getStatusCode().series() == HttpStatus.Series.SUCCESSFUL && response.getHeaders().containsKey(COUNT_HEADER)) {
			return Integer.valueOf(response.getHeaders().getFirst(COUNT_HEADER));
		} else {
			// Error
			throw new RestException("Response received with status code: " + response.getStatusCode());
		}
	}

	private List<T> merge(T[] restObjs, boolean shouldCommit) throws RestException {
		List<T> fetchedRestObjs = new ArrayList<T>();
		List<String> currentIds = new ArrayList<String>();

		for (T restObj : restObjs) {
			fetchedRestObjs.add(restObj);

			if (!shouldCommit) {
				continue;
			}

			currentIds.add(restObj.getId());

			if (contains(restObj)) {
				int idx = indexOf(restObj);
				RestObject currentRestObj = get(idx);
				BambouUtils.copyJsonProperties(restObj, currentRestObj);
			} else {
				add(restObj);
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

		return fetchedRestObjs;
	}

	private HttpHeaders prepareHeaders(String filter, String orderBy, String[] groupBy, Integer page, Integer pageSize) {
		HttpHeaders headers = new HttpHeaders();

		if (filter != null) {
			headers.set(FILTER_HEADER, filter);
		}

		if (orderBy != null) {
			headers.set(ORDER_BY_HEADER, orderBy);
		}

		if (page != null) {
			headers.set(PAGE_HEADER, String.valueOf(page));
		}

		if (pageSize != null) {
			headers.set(PAGE_SIZE_HEADER, String.valueOf(pageSize));
		}

		if (groupBy != null && groupBy.length > 0) {
			String header = "";
			for (Iterator<String> iter = Arrays.asList(groupBy).iterator(); iter.hasNext();) {
				header += iter.next();
				if (iter.hasNext()) {
					header += ", ";
				}

			}
			headers.set(GROUP_BY_HEADER, String.valueOf(true));
			headers.set(ATTRIBUTES_HEADER, header);
		}

		return headers;
	}

	private String getResourceUrl(RestSession<?> session) {
		return parentRestObj.getResourceUrlForChildType(session, childRestObjClass);
	}
}
