package org.traccar.rest.config;

import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

/**
 * Created by niko on 2/6/16.
 */
@ApplicationPath("api")
public class ApplicationConfiguration extends ResourceConfig {
    public ApplicationConfiguration() {
       packages("org.traccar.rest");
    }
}