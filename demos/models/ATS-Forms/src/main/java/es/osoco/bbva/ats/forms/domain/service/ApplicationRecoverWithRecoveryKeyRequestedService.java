package es.osoco.bbva.ats.forms.domain.service;

import es.osoco.bbva.ats.forms.application.util.ConstantTimeStringEquals;
import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import es.osoco.bbva.ats.forms.domain.aggregate.RecoveryToken;
import es.osoco.bbva.ats.forms.domain.events.ApplicationRecoverWithRecoveryKeyRequested;
import es.osoco.bbva.ats.forms.domain.events.PartialApplicationRecovered;
import es.osoco.bbva.ats.forms.domain.exception.ApplicationNotFoundException;
import es.osoco.bbva.ats.forms.domain.repository.ApplicationRepository;
import es.osoco.bbva.ats.forms.domain.repository.RecoveryTokenRepository;

import java.util.function.Consumer;

public class ApplicationRecoverWithRecoveryKeyRequestedService implements Consumer<ApplicationRecoverWithRecoveryKeyRequested> {

    private static final ApplicationRepository applicationRepository = ApplicationRepository.getInstance();
    private static final RecoveryTokenRepository recoveryTokenRepository = RecoveryTokenRepository.getInstance();

    @Override
    public void accept(ApplicationRecoverWithRecoveryKeyRequested applicationRecoverWithRecoveryKeyRequested) {

        RecoveryToken recoveryToken = recoveryTokenRepository.byID(applicationRecoverWithRecoveryKeyRequested.getID());
        Application application = applicationRepository.byID(applicationRecoverWithRecoveryKeyRequested.getID());

        if (recoveryToken != null
                && application != null
                && ConstantTimeStringEquals.safeEqual(recoveryToken.getRecoveryKey(), applicationRecoverWithRecoveryKeyRequested.getRecoveryKey())){
            recoveryTokenRepository.save(recoveryToken);
            new PartialApplicationRecovered(application).emit();
        }
        else {
            throw new ApplicationNotFoundException();
        }
    }
}
