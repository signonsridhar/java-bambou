package com.github.nuagenetworks.bambou.vsd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.nuagenetworks.bambou.RestRootObject;
import com.github.nuagenetworks.bambou.annotation.RestEntity;

@RestEntity(restName = "me", resourceName = "me")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Me extends RestRootObject {
}