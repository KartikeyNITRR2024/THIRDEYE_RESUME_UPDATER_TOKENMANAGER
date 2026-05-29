package com.thirdeye30.resumehelper.tokenmanager.configs;


import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;

public class HttpConnectorConfig {

    @Value("${server.http.port:8080}")
    private static int httpPort;

    public static Connector createHttpConnector() {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(httpPort);
        return connector;
    }
}


