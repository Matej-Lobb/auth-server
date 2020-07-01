package sk.mlobb.authserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.mlobb.authserver.db.ApplicationsRepository;
import sk.mlobb.authserver.model.ApplicationEntity;
import sk.mlobb.authserver.model.rest.response.Application;
import sk.mlobb.authserver.service.mappers.ApplicationMapper;

@Slf4j
@Service
public class ApplicationService {

    private final ApplicationsRepository applicationsRepository;
    private final ApplicationMapper mapper;

    @Autowired
    public ApplicationService(ApplicationsRepository applicationsRepository, ApplicationMapper mapper) {
        this.applicationsRepository = applicationsRepository;
        this.mapper = mapper;
    }

    public Application getApplication(String applicationUid) {
        return mapper.mapApplication(getApplicationByUid(applicationUid));
    }

    ApplicationEntity getApplicationByUid(String applicationUid) {
        log.debug("Getting Application by uid {} !", applicationUid);
        return applicationsRepository.findByUid(applicationUid);
    }
}
