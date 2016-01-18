package com.rasgrass.alr.server.mp3;

import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.simple.container.SimpleServerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;

public class App {

	private static int getPort(int defaultPort) {
		String port = System.getProperty("jersey.test.port");
		if (null != port) {
			try {
				return Integer.parseInt(port);
			} catch (NumberFormatException e) {
			}
		}
		return defaultPort;
	}

	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost/").port(getPort(80)).build();
	}

	public static final URI BASE_URI = getBaseURI();

	protected static Closeable startServer() throws IOException {
		System.out.println("Starting server...");
		ResourceConfig resourceConfig = new DefaultResourceConfig();
		resourceConfig.getSingletons().add(new MediaResource());
		resourceConfig.getSingletons().add(new StaticContentController());

		resourceConfig.getFeatures().put(LoggingFilter.FEATURE_LOGGING_DISABLE_ENTITY, true);
		resourceConfig.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, LoggingFilter.class.getName());
		resourceConfig.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, LoggingFilter.class.getName());

		return SimpleServerFactory.create(BASE_URI, resourceConfig);
	}

	public static void main(String[] args) throws IOException {
		Closeable httpServer = startServer();
		System.in.read();
		httpServer.close();
	}
}
