package sk.mlobb.authserver.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sk.mlobb.authserver.model.exception.InvalidRequestException;
import sk.mlobb.authserver.model.exception.UnauthorizedException;
import sk.mlobb.authserver.model.rest.auth.AuthRequest;
import sk.mlobb.authserver.model.rest.auth.AuthResponse;
import sk.mlobb.authserver.service.AuthorizationService;

@Slf4j
@RestController
@RequestMapping("/api")
public class AuthorizationController {

    private final AuthorizationService service;

    @Autowired
    public AuthorizationController(AuthorizationService service) {
        this.service = service;
    }

    @PostMapping(value = {"/authorize"}, produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE
    })
    public AuthResponse authorizeLicense(@RequestBody AuthRequest request) throws UnauthorizedException,
            InvalidRequestException {
        return service.authorize(request);
    }

    @GetMapping(value = {"/token/check"}, produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE
    })
    public AuthResponse checkToken(@RequestParam("token") String token) throws InvalidRequestException,
            UnauthorizedException {
        return service.checkToken(token);
    }

    @GetMapping(value = {"/token/refresh"}, produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE
    })
    public AuthResponse refreshToken(@RequestParam("refreshToken") String refreshToken)
            throws InvalidRequestException, UnauthorizedException {
        return service.refreshToken(refreshToken);
    }
}
