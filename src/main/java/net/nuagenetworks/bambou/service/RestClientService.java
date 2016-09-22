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
package net.nuagenetworks.bambou.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import net.nuagenetworks.bambou.RestException;
import net.nuagenetworks.bambou.RestStatusCodeException;
import net.nuagenetworks.bambou.util.BambouUtils;

@Service
public class RestClientService {
    private static final Logger logger = LoggerFactory.getLogger(RestClientService.class);

    @Autowired
    private RestOperations restOperations;

    public <T, U> ResponseEntity<T> sendRequest(HttpMethod method, String url, HttpHeaders headers, U requestObject, Class<T> responseType)
            throws RestException {
        logger.info(String.format("> %s %s", method, url));
        logger.info(String.format("> headers: %s", headers));
        logger.info(String.format("> data:\n  %s", BambouUtils.toString(requestObject)));

        ResponseEntity<T> response = sendRequest(method, url, new HttpEntity<U>(requestObject, headers), responseType);

        logger.info(String.format("< %s %s [%s]", method, url, response.getStatusCode()));
        logger.info(String.format("< headers: %s", response.getHeaders()));
        logger.info(String.format("< data:\n  %s", BambouUtils.toString(response.getBody())));

        return response;
    }

    private <T, U> ResponseEntity<T> sendRequest(HttpMethod method, String uri, HttpEntity<U> content, Class<T> responseType) throws RestException {
        ResponseEntity<String> response = restOperations.exchange(uri, method, content, String.class);
        String responseBody = response.getBody();
        HttpStatus statusCode = response.getStatusCode();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            HttpStatus.Series series = statusCode.series();
            if (series != HttpStatus.Series.CLIENT_ERROR && series != HttpStatus.Series.SERVER_ERROR) {
                T body = (responseBody != null) ? objectMapper.readValue(responseBody, responseType) : null;
                return new ResponseEntity<T>(body, response.getHeaders(), response.getStatusCode());
            } else {
                try {
                    // Debug
                    logger.error("Response error: {} {} {}", statusCode, statusCode.getReasonPhrase(), responseBody);

                    // Try to retrieve an error message from the response
                    // content (in JSON format)
                    String errorMessage = null;
                    JsonNode responseObj = objectMapper.readTree(responseBody);
                    ArrayNode errorsNode = (ArrayNode) responseObj.get("errors");
                    if (errorsNode != null) {
                        JsonNode error = errorsNode.get(0);
                        ArrayNode descriptionsNode = (ArrayNode) error.get("descriptions");
                        JsonNode descriptionNode = descriptionsNode.get(0);
                        errorMessage = descriptionNode.get("description").asText();
                        JsonNode propertyNode = error.get("property");
                        if (propertyNode != null) {
                            errorMessage = propertyNode.asText() + ": " + errorMessage;
                        } else {
                            errorMessage = statusCode + " " + statusCode.getReasonPhrase();
                        }
                    } else {
                        errorMessage = statusCode + " " + statusCode.getReasonPhrase();
                    }
                    
                    // Try to retrieve an error code from the response 
                    // content (in JSON format)
                    String internalErrorCode = null;
                    JsonNode internalErrorCodeNode = responseObj.get("internalErrorCode");
                    if (internalErrorCodeNode != null) {
                        internalErrorCode = internalErrorCodeNode.textValue();
                    }
                    
                    // Raise an exception with status code, description and internal error code
                    throw new RestStatusCodeException(statusCode, errorMessage, internalErrorCode);
                } catch (JsonParseException | JsonMappingException ex) {
                    // No error message available in the response
                    switch (statusCode.series()) {
                    case CLIENT_ERROR:
                        throw new RestStatusCodeException(statusCode);
                    case SERVER_ERROR:
                        throw new RestStatusCodeException(statusCode);
                    default:
                        throw new RestClientException("Unknown status code [" + statusCode + "]");
                    }
                }
            }
        } catch (IOException ex) {
            throw new RestException(ex);
        }
    }
}
