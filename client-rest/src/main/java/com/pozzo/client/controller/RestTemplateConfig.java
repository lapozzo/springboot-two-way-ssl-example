package com.pozzo.client.controller;

import java.io.InputStream;
import java.security.KeyStore;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

	@Bean
	public RestTemplate getRestTemplate() {
		RestTemplate restTemplate = new RestTemplate();

		KeyStore keyStore;
		HttpComponentsClientHttpRequestFactory requestFactory = null;
		try {
			keyStore = KeyStore.getInstance("jks");
			ClassPathResource classPathResource = new ClassPathResource("client-rest.jks");
			InputStream inputStream = classPathResource.getInputStream();
			keyStore.load(inputStream, "client-rest".toCharArray());
			SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
					new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy())
							.loadKeyMaterial(keyStore, "client-rest".toCharArray()).build());

			HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).setMaxConnTotal(5)
					.setMaxConnPerRoute(5).build();

			requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
			requestFactory.setReadTimeout(5000);
			requestFactory.setConnectTimeout(5000);

			restTemplate.setRequestFactory(requestFactory);

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Exception creating restTemplate with SSL", e);
		}
		return restTemplate;
	}

}
