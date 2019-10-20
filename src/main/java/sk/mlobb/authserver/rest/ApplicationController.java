package sk.mlobb.authserver.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sk.mlobb.authserver.model.annotation.DefaultPermission;
import sk.mlobb.authserver.model.annotation.PermissionAlias;
import sk.mlobb.authserver.model.rest.request.UpdateApplicationDetailsRequest;
import sk.mlobb.authserver.rest.auth.AuthorizationHandler;
import sk.mlobb.authserver.service.ApplicationService;

import static sk.mlobb.authserver.model.enums.RequiredAccess.READ_ALL;
import static sk.mlobb.authserver.model.enums.RequiredAccess.READ_SELF;
import static sk.mlobb.authserver.model.enums.RequiredAccess.WRITE_ALL;
import static sk.mlobb.authserver.model.enums.RequiredAccess.WRITE_SELF;

@Slf4j
@RestController
public class ApplicationController {

    private final AuthorizationHandler authorizationHandler;
    private final ApplicationService applicationService;

    public ApplicationController(AuthorizationHandler authorizationHandler,
                                 ApplicationService applicationService) {
        this.authorizationHandler = authorizationHandler;
        this.applicationService = applicationService;
    }

    @DefaultPermission
    @PermissionAlias("get-application")
    @GetMapping(value = "/applications/{uid}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity getApplicationByUid(@PathVariable("uid") String uid) {
        if (authorizationHandler.checkIfAccessingOwnApplicationData(uid)) {
            authorizationHandler.validateAccess(READ_SELF);
        } else {
            authorizationHandler.validateAccess(READ_ALL);
        }
        return new ResponseEntity<>(applicationService.getApplicationByUid(uid), HttpStatus.OK);
    }

    @DefaultPermission
    @PermissionAlias("get-application-roles")
    @GetMapping(value = "/applications/{uid}/roles",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity getApplicationRoles(@PathVariable("uid") String uid) {
        if (authorizationHandler.checkIfAccessingOwnApplicationData(uid)) {
            authorizationHandler.validateAccess(READ_SELF);
        } else {
            authorizationHandler.validateAccess(READ_ALL);
        }
        return new ResponseEntity<>(applicationService.getApplicationRoles(uid), HttpStatus.OK);
    }

    @DefaultPermission
    @PermissionAlias("update-application-details")
    @PutMapping(value = "/applications/{uid}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity updateApplicationDetails(
            @PathVariable("uid") String uid,
            @RequestBody UpdateApplicationDetailsRequest updateApplicationDetailsRequest) {
        if (authorizationHandler.checkIfAccessingOwnApplicationData(uid)) {
            authorizationHandler.validateAccess(WRITE_SELF);
        } else {
            authorizationHandler.validateAccess(WRITE_ALL);
        }
        return new ResponseEntity<>(applicationService.updateApplicationDetails(uid, updateApplicationDetailsRequest),
                HttpStatus.OK);
    }
}
