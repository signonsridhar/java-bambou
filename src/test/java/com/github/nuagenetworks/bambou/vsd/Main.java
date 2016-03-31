package com.github.nuagenetworks.bambou.vsd;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.nuagenetworks.bambou.RestException;
import com.github.nuagenetworks.bambou.RestPushCenter;
import com.github.nuagenetworks.bambou.RestPushCenterListener;

public class Main {
	public static void main(String[] args) throws RestException {
		VsdSession session = new VsdSession("csproot", "csproot", "csp", "https://135.121.118.83:8443");
		session.getClientTemplate().setSocketTimeout(30000);
		session.getClientTemplate().setHttpProxy("global.proxy.alcatel-lucent.com", 8000);
		session.start();

		System.out.println("api key" + session.getMe().getApiKey());

		RestPushCenter pushCenter = session.getPushCenter();
		pushCenter.addListener(new RestPushCenterListener() {
			@Override
			public void onEvent(JsonNode event) {
				System.out.println("Event: " + event);
			}
		});
		pushCenter.start();
	}
}
