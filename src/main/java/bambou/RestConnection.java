package bambou;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import bambou.ssl.NaiveHostnameVerifier;
import bambou.ssl.X509NaiveTrustManager;

public class RestConnection {
	private static final Logger logger = LoggerFactory.getLogger(RestConnection.class);

	private static final String ORGANIZATION_HEADER_KEY = "X-Nuage-Organization";
	private static final String AUTH_HEADER_KEY = "Authorization";

	private static RestOperations restOperations;
	private ObjectMapper objectMapper;
	private RestResponse response;
	private RestRequest request;
	private Map<String, Object> userInfo;

	public RestConnection(RestRequest request) {
		this.request = request;

		if (restOperations == null) {
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setErrorHandler(new ResponseErrorHandler() {
				@Override
				public boolean hasError(ClientHttpResponse arg0) throws IOException {
					return false;
				}
	
				@Override
				public void handleError(ClientHttpResponse arg0) throws IOException {
				}
			});
			restOperations = restTemplate;
		}
		objectMapper = new ObjectMapper();
		disableSslVerification();
	}

	static void setRestOperations(RestOperations restOperations) {
		RestConnection.restOperations = restOperations;
	}
	
	public Map<String, Object> getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(Map<String, Object> userInfo) {
		this.userInfo = userInfo;
	}

	public RestResponse getResponse() {
		return response;
	}

	public void start(RestSession session) throws RestException {
		makeRequest(session);
	}

	private RestConnection makeRequest(RestSession session) throws RestException {
		try {
			return makeRequestInternal(session);
		} catch (HttpClientErrorException ex) {
			if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
				logger.info("HTTP 401/Unauthorized response received");
				// Re-authenticate the session and try to send the same request again
				// a new API key might get issued as a result
				session.reset();
				session.authenticate();
				return makeRequestInternal(session);
			} else {
				throw ex;
			}
		}
	}

	private RestConnection makeRequestInternal(RestSession session) throws RestException {
		RestLoginController loginController = session.getLoginContoller();
		String enterprise = loginController.getEnterprise();
		String username = loginController.getUser();
		String apiKey = loginController.getApiKey();

		HttpHeaders headers = request.getHeaders();
		headers.set(ORGANIZATION_HEADER_KEY, enterprise);
		headers.set(AUTH_HEADER_KEY, loginController.getAuthenticationHeader(username, apiKey, null, null));

		logger.info(String.format("> %s %s %s", request.getMethod(), request.getUrl(), request.getParams() != null ? request.getParams() : ""));
		logger.info(String.format("> headers: %s", headers));
		try {
			logger.info(String.format("> data:\n  %s",
			        request.getRequestRestObject() != null ? objectMapper.writeValueAsString(request.getRequestRestObject()) : ""));
		} catch (JsonProcessingException e) {
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		ResponseEntity response = sendRequest(request.getMethod(), request.getUrl(), request.getParams(),
		        new HttpEntity(request.getRequestRestObject(), headers), request.getResponseRestObjectClass());
		return didReceiveResponse(response);
	}

	private RestConnection didReceiveResponse(ResponseEntity<?> response) {
		this.response = new RestResponse(response.getHeaders(), response.getStatusCode(), response.getBody());

		logger.info(String.format("< %s %s %s", request.getMethod(), request.getUrl(), request.getParams() != null ? request.getParams() : ""));
		logger.info(String.format("< headers: %s", this.response.getHeaders()));
		try {
			logger.info(String.format("< data:\n  %s", (this.response.getBody() != null) ? objectMapper.writeValueAsString(this.response.getBody()) : ""));
		} catch (JsonProcessingException e) {
		}

		return this;
	}

	private <T, U> ResponseEntity<T> sendRequest(HttpMethod method, String uri, String params, HttpEntity<U> request, Class<T> responseType)
	        throws RestException {
		if (params != null) {
			uri += (uri.indexOf('?') >= 0) ? ";" + params : "?" + params;
		}

		ResponseEntity<String> response = restOperations.exchange(uri, method, request, String.class);
		String responseBody = response.getBody();
		HttpStatus statusCode = response.getStatusCode();

		try {
			HttpStatus.Series series = statusCode.series();
			if (series != HttpStatus.Series.CLIENT_ERROR && series != HttpStatus.Series.SERVER_ERROR) {
				T body = (responseBody != null) ? objectMapper.readValue(responseBody, responseType) : null;
				return new ResponseEntity<T>(body, response.getHeaders(), response.getStatusCode());
			} else {
				try {
					// Debug
					logger.error("Response error: {} {} {}", statusCode, statusCode.getReasonPhrase(), responseBody);

					// Try to retrieve an error message from the response content (in JSON format)
					JsonNode responseObj = objectMapper.readTree(responseBody);
					ArrayNode errors = (ArrayNode) responseObj.get("errors");
					if (errors != null) {
						JsonNode error = errors.get(0);
						ArrayNode descriptions = (ArrayNode) error.get("descriptions");
						JsonNode description = descriptions.get(0);
						String descriptionText = description.get("description").asText();
						throw new RestException(descriptionText);
					} else {
						throw new RestException(statusCode + " " + statusCode.getReasonPhrase());
					}
				} catch (JsonParseException | JsonMappingException ex) {
					// No error message available in the response
					// Throw the same exceptions that the default error handler
					// would normally throw in for these types of errors
					switch (statusCode.series()) {
					case CLIENT_ERROR:
						throw new HttpClientErrorException(statusCode);
					case SERVER_ERROR:
						throw new HttpServerErrorException(statusCode);
					default:
						throw new RestClientException("Unknown status code [" + statusCode + "]");
					}
				}
			}
		} catch (IOException ex) {
			throw new RestException(ex);
		}
	}

	private void disableSslVerification() {
		try {
			// Create a trust manager that doesn't validate cert chains
			TrustManager[] trustAllCerts = new TrustManager[] { new X509NaiveTrustManager() };

			// Install the new trust manager
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			// Create host verifier
			HostnameVerifier allHostsValid = new NaiveHostnameVerifier();

			// Install the host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
	}
}
