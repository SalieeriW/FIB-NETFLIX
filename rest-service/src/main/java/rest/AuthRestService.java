package rest;

import database.UserDAO;
import security.JwtProvider;
import util.JsonSerializer;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class AuthRestService {

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response login(@FormParam("username") String username,
                          @FormParam("password") String password) {
        if (isBlank(username)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(JsonSerializer.errorResponse("Bad Request", "Username is required", 400))
                    .build();
        }
        if (isBlank(password)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(JsonSerializer.errorResponse("Bad Request", "Password is required", 400))
                    .build();
        }

        boolean valid = UserDAO.validateUser(username, password);
        if (valid) {
            String token = JwtProvider.createToken(username);
            String payload = JsonSerializer.loginResponseWithToken(token, username);
            return Response.ok(payload).build();
        }
        String payload = JsonSerializer.loginResponse(false, username);
        return Response.status(Response.Status.UNAUTHORIZED).entity(payload).build();
    }

    @POST
    @Path("registerUser")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response registerUser(@FormParam("username") String username,
                                 @FormParam("password") String password,
                                 @FormParam("confirmPassword") String confirmPassword) {
        if (isBlank(username) || isBlank(password) || isBlank(confirmPassword)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(JsonSerializer.errorResponse("Bad Request", "All fields are required", 400))
                    .build();
        }
        if (!password.equals(confirmPassword)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(JsonSerializer.errorResponse("Bad Request", "Passwords must match", 400))
                    .build();
        }

        if (UserDAO.userExists(username)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(JsonSerializer.errorResponse("Conflict", "Username already exists", 409))
                    .build();
        }

        boolean registered = UserDAO.insertUser(username, password);
        if (registered) {
            String token = JwtProvider.createToken(username);
            String payload = JsonSerializer.loginResponseWithToken(token, username);
            return Response.status(Response.Status.CREATED).entity(payload).build();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(JsonSerializer.errorResponse("Internal Server Error", "Failed to register user", 500))
                .build();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
