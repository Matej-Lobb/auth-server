package sk.mlobb.authserver.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import sk.mlobb.authserver.model.rest.response.Application;
import sk.mlobb.authserver.service.ApplicationService;

@Slf4j
@RestController
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping(value = "/applications/{uid}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Application> getByName(@PathVariable("uid") String uid) {
        return new ResponseEntity<>(applicationService.getApplication(uid), HttpStatus.OK);
    }
}