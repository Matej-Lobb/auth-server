package sk.mlobb.authserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.mlobb.authserver.db.ApplicationsRepository;
import sk.mlobb.authserver.model.Application;

import java.util.List;

@Slf4j
@Service
public class ApplicationService {

    private final ApplicationsRepository applicationsRepository;

    @Autowired
    public ApplicationService(ApplicationsRepository applicationsRepository) {
        this.applicationsRepository = applicationsRepository;
    }

    public Application getByUid(String applicationUid) {
        log.debug("Getting Application by uid {} !", applicationUid);
        return applicationsRepository.findByUid(applicationUid);
    }
}
