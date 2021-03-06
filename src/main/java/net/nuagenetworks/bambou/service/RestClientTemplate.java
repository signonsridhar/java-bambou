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

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import net.nuagenetworks.bambou.ssl.NaiveHostnameVerifier;
import net.nuagenetworks.bambou.ssl.X509NaiveTrustManager;

public class RestClientTemplate extends RestTemplate {

    private static final int DEFAULT_SOCKET_TIMEOUT_IN_MS = 60 * 1000;

    public RestClientTemplate() {
        super(new SimpleClientHttpRequestFactory());

        setSocketTimeout(DEFAULT_SOCKET_TIMEOUT_IN_MS);
        ResponseErrorHandlerImpl responseErrorHandler = new ResponseErrorHandlerImpl();
        setErrorHandler(responseErrorHandler);
        disableSslVerification();
    }

    public void setSocketTimeout(int socketTimeout) {
        if (socketTimeout > 0) {
            // Debug
            logger.debug("Using socket timeout for REST connection: " + socketTimeout);

            // Set connect and read timeouts
            SimpleClientHttpRequestFactory requestFactory = (SimpleClientHttpRequestFactory) getRequestFactory();
            requestFactory.setConnectTimeout(socketTimeout);
            requestFactory.setReadTimeout(socketTimeout);
        }
    }

    public void setHttpProxy(String host, int port) {
        if (host != null && !host.isEmpty()) {
            // Debug
            logger.debug("Using HTTP proxy for REST connection: " + host + ":" + port);

            // Set proxy
            SimpleClientHttpRequestFactory requestFactory = (SimpleClientHttpRequestFactory) getRequestFactory();
            Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress(host, port));
            requestFactory.setProxy(proxy);
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
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        } catch (KeyManagementException ex) {
            ex.printStackTrace();
        }
    }
}
