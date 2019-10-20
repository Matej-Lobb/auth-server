package sk.mlobb.authserver.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import sk.mlobb.authserver.model.Role;
import sk.mlobb.authserver.model.annotation.DefaultPermission;
import sk.mlobb.authserver.model.annotation.PermissionAlias;
import sk.mlobb.authserver.model.rest.request.CreateRoleRequest;
import sk.mlobb.authserver.model.rest.request.UpdateRoleRequest;
import sk.mlobb.authserver.rest.auth.AuthorizationHandler;
import sk.mlobb.authserver.service.RoleService;

import javax.validation.Valid;

import static sk.mlobb.authserver.model.enums.RequiredAccess.READ_ALL;
import static sk.mlobb.authserver.model.enums.RequiredAccess.READ_SELF;
import static sk.mlobb.authserver.model.enums.RequiredAccess.WRITE_ALL;

@Slf4j
@RestController
public class RoleController {

    private final AuthorizationHandler authorizationHandler;
    private final RoleService roleService;

    public RoleController(AuthorizationHandler authorizationHandler, RoleService roleService) {
        this.authorizationHandler = authorizationHandler;
        this.roleService = roleService;
    }

    @DefaultPermission(readSelf = true)
    @PermissionAlias("get-role")
    @GetMapping(value = {"/applications/{applicationUid}/roles/{roleName}"},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity getRoleByName(@PathVariable("applicationUid") String applicationUid,
                                        @PathVariable("roleName") String roleName) {
        if (authorizationHandler.checkIfAccessingOwnUserData(roleName)) {
            authorizationHandler.validateAccess(READ_SELF);
        } else {
            authorizationHandler.validateAccess(READ_ALL);
        }
        return getRole(roleService.getRoleByName(applicationUid, roleName));
    }

    @DefaultPermission
    @PermissionAlias("create-role")
    @PostMapping(value = {"/applications/{applicationUid}/roles"},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity createRole(@PathVariable("applicationUid") String applicationUid,
                                     @Valid @RequestBody CreateRoleRequest request, UriComponentsBuilder ucBuilder) {
        authorizationHandler.validateAccess(READ_ALL, WRITE_ALL);
        final Role dbRole = roleService.addRole(applicationUid, request);
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/applications/{applicationUid}/roles/{roleName}")
                .buildAndExpand(applicationUid, dbRole.getRole()).toUri());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @DefaultPermission
    @PermissionAlias("update-role")
    @PutMapping(value = {"/applications/{applicationUid}/roles/{roleName}"},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity updateRoleByName(@PathVariable("applicationUid") String applicationUid,
                                           @PathVariable("roleName") String roleName,
                                           @Valid @RequestBody UpdateRoleRequest updateRoleRequest) {
        authorizationHandler.validateAccess(READ_ALL, WRITE_ALL);
        return getRole(roleService.updateRole(applicationUid, roleName, updateRoleRequest));
    }

    @DefaultPermission
    @PermissionAlias("delete-role")
    @DeleteMapping(value = {"/applications/{applicationUid}/roles/{identifier}"},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity deleteRoleByName(@PathVariable("applicationUid") String applicationUid,
                                           @PathVariable("identifier") String identifier) {
        authorizationHandler.validateAccess(READ_ALL, WRITE_ALL);
        roleService.deleteRole(applicationUid, identifier);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private ResponseEntity getRole(Role role) {
        if (role == null) {
            log.debug("Role not found");
            return ResponseEntity.notFound().build();
        }
        return new ResponseEntity<>(role, HttpStatus.OK);
    }
}
