package es.osoco.bbva.ats.forms.domain.service;

import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import es.osoco.bbva.ats.forms.domain.events.ApplicationRecoverRequested;
import es.osoco.bbva.ats.forms.domain.events.PartialApplicationRecovered;
import es.osoco.bbva.ats.forms.domain.exception.ApplicationNotFoundException;
import es.osoco.bbva.ats.forms.domain.repository.ApplicationRepository;

import java.util.function.Consumer;

public class ApplicationRecoverRequestedService implements Consumer<ApplicationRecoverRequested> {

    private static final ApplicationRepository applicationRepository = ApplicationRepository.getInstance();

    @Override
    public void accept(ApplicationRecoverRequested applicationRecoverRequested) {

        Application application = applicationRepository.byID(applicationRecoverRequested.getID());

        if (application != null) {
            new PartialApplicationRecovered(application).emit();
        } else {
            throw new ApplicationNotFoundException();
        }
    }
}
