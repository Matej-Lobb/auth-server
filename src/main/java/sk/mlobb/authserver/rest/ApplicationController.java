package sk.mlobb.authserver.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import sk.mlobb.authserver.model.rest.Application;
import sk.mlobb.authserver.service.ApplicationService;

@Slf4j
@RestController
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping(value = "/applications/{uid}")
    public Application getByName(@PathVariable("uid") String uid) {
        log.info("Getting application by uid: {}", uid);
        return applicationService.getApplication(uid);
    }
}