package bambou;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.JsonNode;

import bambou.obj.Events;

public class RestPushCenter {
	private static final Logger logger = LoggerFactory.getLogger(RestPushCenter.class);

	private String url;
	private List<RestPushCenterListener> listeners = new ArrayList<RestPushCenterListener>();
	private Thread eventThread;
	private boolean stopPollingEvents;
	private boolean isRunning;
	private RestSession session;

	protected RestPushCenter(RestSession session) {
		this.session = session;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void start() {
		if (isRunning) {
			return;
		}
		
		isRunning = true;
		Runnable exec = new Runnable() {
			public void run() {
				pollEvents();
			}
		};
		eventThread = new Thread(exec);
		eventThread.start();
	}

	public void stop() {
		if (!isRunning) {
			return;
		}
		
		stopPollingEvents = true;
		try {
			eventThread.join();
		} catch (InterruptedException e) {
		}
		isRunning = false;
		eventThread = null;
		
	}

	public void addListener(RestPushCenterListener listener) {
		if (listeners.contains(listener)) {
			return;
		}

		listeners.add(listener);
	}

	public void removeListener(RestPushCenterListener listener) {
		if (!listeners.contains(listener)) {
			return;
		}

		listeners.remove(listener);
	}

	private void pollEvents() {
		stopPollingEvents = false;

		String uuid = null;
		while (!stopPollingEvents) {
			try {
				// Debug
				logger.info("Polling events from VSD using uuid=" + uuid);
				
				// Get the next events
				RestResponse response = sendRequest(uuid);
				if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
					// In case of a 400/Bad Request: re-send request without uuid in order to get a new one
					response = sendRequest(null);
				}
				Events events = (Events) response.getBody();

				if (stopPollingEvents) {
					break;
				}

				// Debug
				logger.info("Received events: " + events);

				// Process the events received
				for (JsonNode event : events.getEvents()) {
					for (RestPushCenterListener listener : listeners) {
						listener.onEvent(event);
					}
				}

				// Get the next UUID to query for
				uuid = events.getUuid();
			} catch (Exception ex) {
				// Error
				logger.error("Error", ex);

				// Pause and try again
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
			}
		}

		// Debug
		logger.info("Polling stopped");
	}
	
	private RestResponse sendRequest(String uuid) throws RestException {
		String eventsUrl = String.format("%s/events", url);
		if (uuid != null) {
			eventsUrl = String.format("%s?uuid=%s", eventsUrl, uuid);
		}
		RestRequest request = new RestRequest(HttpMethod.GET, eventsUrl, null, Events.class, false);
		RestConnection connection = new RestConnection(request);
		connection.start(session);
		return connection.getResponse();
	}

}
