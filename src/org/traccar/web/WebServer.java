/*
 * Copyright 2012 - 2015 Anton Tananaev (anton.tananaev@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.web;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.GzipFilter;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.glassfish.jersey.servlet.ServletContainer;
import org.traccar.Config;
import org.traccar.helper.Log;
import org.traccar.rest.PositionEventEndpoint;

import javax.naming.InitialContext;
import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.util.EnumSet;

/**
 * Integrated HTTP server
 */
public class WebServer {

    private Server server;
    private final Config config;
    private final DataSource dataSource;
    private final HandlerList handlers = new HandlerList();

    private void initServer() {

        String address = config.getString("web.address");
        int port = config.getInteger("web.port", 8082);
        if (address == null) {
            server = new Server(port);
        } else {
            server = new Server(new InetSocketAddress(address, port));
        }
    }

    public WebServer(Config config, DataSource dataSource) {
        this.config = config;
        this.dataSource = dataSource;

        initServer();
        switch (config.getString("web.type", "new")) {
            case "api":
                initOldApi();
                break;
            case "new":
                initApi();
                if (config.getBoolean("web.console")) {
                    initConsole();
                }
                initWebApp();
                break;
            case "old":
                initApi();
                initOldWebApp();
                break;
            default:
                Log.error("Unsupported web application type: " + config.getString("web.type"));
                break;
        }
        server.setHandler(handlers);

        server.addBean(new ErrorHandler() {
            @Override
            protected void handleErrorPage(
                    HttpServletRequest request, Writer writer, int code, String message) throws IOException {
                writer.write("<!DOCTYPE<html><head><title>Error</title></head><html><body>"
                        + code + " - " + HttpStatus.getMessage(code) + "</body></html>");
            }
        });
    }

    private void initWebApp() {
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase(config.getString("web.path"));
        if (config.getBoolean("web.debug")) {
            resourceHandler.setWelcomeFiles(new String[] {"debug.html"});
        } else {
            resourceHandler.setWelcomeFiles(new String[] {"index.html"});
        }
        handlers.addHandler(resourceHandler);
    }

    private void initOldWebApp() {
        try {
            javax.naming.Context context = new InitialContext();
            context.bind("java:/DefaultDS", dataSource);
        } catch (Exception error) {
            Log.warning(error);
        }

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setWar(config.getString("web.application"));
        handlers.addHandler(webapp);
    }

    private void initApi() {
        switch (config.getString("api.provider", "old")) {
            case "old":
                initOldApi();
                break;
            case "rest":
                initRestApi();
                break;
            default:
                Log.error("Unsupported api provider: " + config.getString("api.provider"));
                break;
        }

    }
    private void initOldApi() {
        ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletHandler.setContextPath("/api");
        servletHandler.addServlet(new ServletHolder(new AsyncServlet()), "/async/*");
        servletHandler.addServlet(new ServletHolder(new ServerServlet()), "/server/*");
        servletHandler.addServlet(new ServletHolder(new UserServlet()), "/user/*");
        servletHandler.addServlet(new ServletHolder(new DeviceServlet()), "/device/*");
        servletHandler.addServlet(new ServletHolder(new PositionServlet()), "/position/*");
        servletHandler.addServlet(new ServletHolder(new CommandServlet()), "/command/*");
        servletHandler.addServlet(new ServletHolder(new MainServlet()), "/*");
        handlers.addHandler(servletHandler);
    }

    private void initRestApi() {
        ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletHandler.setContextPath("/api");
        ServletHolder servletHolder = new ServletHolder(new ServletContainer());
        servletHolder.getInitParameters().put("jersey.config.server.provider.packages", "org.traccar.rest");

        //add servlets
        WebSocketHandler webSocketHandler = new WebSocketHandler() {
            @Override
            public void configure(WebSocketServletFactory webSocketServletFactory) {
                webSocketServletFactory.getPolicy().setIdleTimeout(0);
                webSocketServletFactory.register(PositionEventEndpoint.class);
            }
        };

        servletHandler.addServlet(servletHolder, "/*");
        FilterHolder filterHolder = new FilterHolder(GzipFilter.class);
        filterHolder.getInitParameters().put("checkGzExists", "true");
        filterHolder.getInitParameters().put("mimeTypes",
                "text/html,text/css,application/javascript,application/json");
        servletHandler.addFilter(filterHolder, "/*", EnumSet.of(DispatcherType.INCLUDE, DispatcherType.REQUEST));

        handlers.addHandler(webSocketHandler);
        handlers.addHandler(servletHandler);

    }

    private void initConsole() {
        ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletHandler.setContextPath("/console");
        servletHandler.addServlet(new ServletHolder(new ConsoleServlet()), "/*");
        handlers.addHandler(servletHandler);
    }

    public void start() {
        try {
            server.start();
        } catch (Exception error) {
            Log.warning(error);
        }
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception error) {
            Log.warning(error);
        }
    }

}
