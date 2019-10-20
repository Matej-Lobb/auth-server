package sk.mlobb.authserver.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sk.mlobb.authserver.db.ApplicationsRepository;
import sk.mlobb.authserver.model.Application;
import sk.mlobb.authserver.model.Role;
import sk.mlobb.authserver.model.exception.NotFoundException;
import sk.mlobb.authserver.model.rest.request.UpdateApplicationDetailsRequest;

import java.util.Set;

@Slf4j
@Service
public class ApplicationService {

    private final ApplicationsRepository applicationsRepository;
    private final RoleService roleService;

    @Autowired
    public ApplicationService(ApplicationsRepository applicationsRepository, RoleService roleService) {
        this.applicationsRepository = applicationsRepository;
        this.roleService = roleService;
    }

    @Transactional
    public Application getApplicationByUid(String applicationUid) {
        log.debug("Getting Application by uid {} !", applicationUid);
        return checkApplication(applicationsRepository.findByUid(applicationUid));
    }

    @Transactional
    public Application updateApplicationDetails(String applicationUid, UpdateApplicationDetailsRequest request) {
        log.debug("Updating application: {}", applicationUid);
        boolean nameUpdated = false;
        Application application = getApplicationByUid(applicationUid);
        if (StringUtils.isEmpty(request.getName()) && !request.getName().equals(application.getName())) {
            application.setName(request.getName());
            nameUpdated = true;
        }
        boolean defaultRoleUpdated = false;
        if (StringUtils.isEmpty(request.getDefaultRoleName()) && !StringUtils.isEmpty(request.getDefaultRoleName())
                && !request.getDefaultRoleName().equals(application.getDefaultUserRole().getRole())) {
            Role role = roleService.getRoleByName(request.getDefaultRoleName().toUpperCase());
            application.setDefaultUserRole(role);
            defaultRoleUpdated = true;
        }
        if (nameUpdated || defaultRoleUpdated) {
            return applicationsRepository.save(application);
        }
        return application;
    }

    @Transactional
    public Set<Role> getApplicationRoles(String uid) {
        Application application = getApplicationByUid(uid);
        return application.getApplicationRoles();
    }

    private Application checkApplication(Application application) {
        if (application == null) {
            throw new NotFoundException("Application not found !");
        }
        return application;
    }
}
