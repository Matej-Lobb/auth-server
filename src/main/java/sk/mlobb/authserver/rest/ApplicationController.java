package sk.mlobb.authserver.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import sk.mlobb.authserver.model.annotation.DefaultPermission;
import sk.mlobb.authserver.model.annotation.PermissionAlias;
import sk.mlobb.authserver.rest.auth.RestAuthenticationHandler;
import sk.mlobb.authserver.service.ApplicationService;

import static sk.mlobb.authserver.model.enums.RequiredAccess.*;

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

    @DefaultPermission
    @PermissionAlias("get-application")
    @GetMapping(value = "/applications/{uid}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity getByName(@PathVariable("uid") String uid) {
        if (restAuthenticationHandler.checkIfAccessingOwnApplicationData(uid)) {
            restAuthenticationHandler.validateAccess(READ_SELF);
        } else {
            restAuthenticationHandler.validateAccess(READ_ALL);
        }
        return new ResponseEntity<>(applicationService.getByUid(uid), HttpStatus.OK);
    }
}
