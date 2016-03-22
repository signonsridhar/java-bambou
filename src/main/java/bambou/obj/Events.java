package bambou.obj;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public class Events {

	private List<JsonNode> events = new ArrayList<JsonNode>();
	private String uuid;

	public List<JsonNode> getEvents() {
		return events;
	}

	public void setEvents(List<JsonNode> events) {
		this.events = events;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public String toString() {
		return "Events [events=" + events + ", uuid=" + uuid + "]";
	}
}
