package sk.mlobb.authserver.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import sk.mlobb.authserver.model.Application;
import sk.mlobb.authserver.rest.auth.RestAuthenticationHandler;
import sk.mlobb.authserver.service.ApplicationService;

import java.util.List;

@Slf4j
@RestController
public class ApplicationController {

    private final RestAuthenticationHandler restAuthenticationHandler;
    private final ApplicationService applicationService;

    public ApplicationController(RestAuthenticationHandler restAuthenticationHandler,
                                 ApplicationService applicationService) {
        this.restAuthenticationHandler = restAuthenticationHandler;
        this.applicationService = applicationService;
    }

    @Secured("ADMIN")
    @GetMapping(value = "/applications/all",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity getAllUsers() {
        restAuthenticationHandler.checkAccess();
        final List<Application> applications = applicationService.getAll();
        if (applications.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(applications, HttpStatus.OK);
    }

    @Secured("ADMIN")
    @GetMapping(value = "/applications/{uid}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity getByName(@PathVariable("uid") String uid) {
        restAuthenticationHandler.checkAccess();
        final Application applications = applicationService.getByUid(uid);
        if (applications == null) {
            return ResponseEntity.notFound().build();
        }
        return new ResponseEntity<>(applications, HttpStatus.OK);
    }
}
