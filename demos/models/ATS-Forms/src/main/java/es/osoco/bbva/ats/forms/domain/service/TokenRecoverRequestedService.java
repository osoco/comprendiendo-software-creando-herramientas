package es.osoco.bbva.ats.forms.domain.service;

import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import es.osoco.bbva.ats.forms.domain.aggregate.RecoveryToken;
import es.osoco.bbva.ats.forms.domain.events.RecoveryTokenGenerated;
import es.osoco.bbva.ats.forms.domain.events.TokenRecoverRequested;
import es.osoco.bbva.ats.forms.domain.exception.ApplicationNotFoundException;
import es.osoco.bbva.ats.forms.domain.repository.ApplicationRepository;
import es.osoco.bbva.ats.forms.domain.repository.RecoveryTokenRepository;
import es.osoco.bbva.ats.forms.domain.util.RecoveryKeyGenerator;

import java.util.function.Consumer;

public class TokenRecoverRequestedService implements Consumer<TokenRecoverRequested> {

    private final static ApplicationRepository applicationRepository = ApplicationRepository.getInstance();
    private final static RecoveryTokenRepository recoveryTokenRepository = RecoveryTokenRepository.getInstance();

    @Override
    public void accept(TokenRecoverRequested tokenRecoverRequested) {

        Application application = applicationRepository.byID(tokenRecoverRequested.getId());

        if (application != null) {
            recoveryTokenRepository.deleteKey(tokenRecoverRequested.getId());
            String newRecoveryKey = RecoveryKeyGenerator.generateKey();

            RecoveryToken newRecoveryToken = new RecoveryToken(
                    tokenRecoverRequested.getContestId(),
                    tokenRecoverRequested.getLanguage(),
                    tokenRecoverRequested.getApplicantKey(),
                    newRecoveryKey);

            recoveryTokenRepository.save(newRecoveryToken);

            new RecoveryTokenGenerated(newRecoveryToken).emit();
        }else {
            throw new ApplicationNotFoundException();
        }
    }
}
