package bambou;

import com.fasterxml.jackson.databind.JsonNode;

public interface RestPushCenterListener {

	void onEvent(JsonNode event);
}
