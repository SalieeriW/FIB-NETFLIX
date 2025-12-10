package rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Configures base path for REST endpoints.
 * All resources will be available under /resources.
 */
@ApplicationPath("/resources")
public class RestApplication extends Application {
}
