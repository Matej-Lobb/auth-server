package sk.mlobb.authserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sk.mlobb.authserver.db.ApplicationsRepository;
import sk.mlobb.authserver.model.Application;
import sk.mlobb.authserver.model.exception.NotFoundException;

@Slf4j
@Component
public class ApplicationHelper {

    private final ApplicationsRepository applicationsRepository;

    private static final String APPLICATION_NOT_EXISTS = "Application not exists !";

    public ApplicationHelper(ApplicationsRepository applicationsRepository) {
        this.applicationsRepository = applicationsRepository;
    }

    public Application checkIfApplicationExists(String applicationUid) {
        Application dbApplication = applicationsRepository.findByUid(applicationUid);
        validateIfObjectExists(dbApplication == null, APPLICATION_NOT_EXISTS);
        return dbApplication;
    }

    private void validateIfObjectExists(boolean exists, String message) {
        if (exists) {
            throw new NotFoundException(message);
        }
    }
}
