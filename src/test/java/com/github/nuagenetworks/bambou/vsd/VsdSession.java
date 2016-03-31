package com.github.nuagenetworks.bambou.vsd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.github.nuagenetworks.bambou.RestSession;
import com.github.nuagenetworks.bambou.service.RestClientTemplate;
import com.github.nuagenetworks.bambou.spring.SpringConfig;

public class VsdSession extends RestSession<Me> {

	@Autowired
	private RestClientTemplate restClientTemplate;

	public VsdSession() {
		super(Me.class);

		try (AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(SpringConfig.class)) {
			applicationContext.getAutowireCapableBeanFactory().autowireBean(this);
		}
	}

	public VsdSession(String username, String password, String enterprise, String apiUrl) {
		this();

		setUsername(username);
		setPassword(password);
		setEnterprise(enterprise);
		setApiUrl(apiUrl);
		setApiPrefix("nuage/api");
		setVersion(3.2);
	}

	public RestClientTemplate getClientTemplate() {
		return restClientTemplate;
	}

	public Me getMe() {
		return super.getRootObject();
	}

}
