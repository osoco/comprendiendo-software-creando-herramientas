package es.osoco.bbva.ats.forms.domain.service;

import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import es.osoco.bbva.ats.forms.domain.aggregate.GenericToken;
import es.osoco.bbva.ats.forms.domain.aggregate.RecoveryPermit;
import es.osoco.bbva.ats.forms.domain.config.DataRecoverConfiguration;
import es.osoco.bbva.ats.forms.domain.events.GenericTokenGenerated;
import es.osoco.bbva.ats.forms.domain.events.GenericTokenRequested;
import es.osoco.bbva.ats.forms.domain.exception.ApplicationNotFoundException;
import es.osoco.bbva.ats.forms.domain.exception.ApplicationToRecoverNotFoundException;
import es.osoco.bbva.ats.forms.domain.exception.DataRecoveryNotAllowedException;
import es.osoco.bbva.ats.forms.domain.repository.ApplicationRepository;
import es.osoco.bbva.ats.forms.domain.repository.GenericTokenRepository;
import es.osoco.bbva.ats.forms.domain.repository.RecoveryPermitRepository;
import es.osoco.bbva.ats.forms.domain.util.RecoveryKeyGenerator;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Set;
import java.util.function.Consumer;

public class GenericTokenRequestedService implements Consumer<GenericTokenRequested> {

    private final static ApplicationRepository applicationRepository = ApplicationRepository.getInstance();
    private final static GenericTokenRepository genericTokenRepository = GenericTokenRepository.getInstance();
    private final static RecoveryPermitRepository recoveryPermitRepository = RecoveryPermitRepository.getInstance();

    @Override
    public void accept(GenericTokenRequested genericTokenRequested) {
        Set<String> applicationKeys = applicationRepository.findKeys(genericTokenRequested.getApplicantKey());
        Boolean tokenRequestIsForTheLastContest;

        if (applicationKeys.size() <= 0) {
            throw new ApplicationToRecoverNotFoundException(
                genericTokenRequested.getApplicantKey(),
                genericTokenRequested.getContestId(),
                genericTokenRequested.getLanguage());
        }

        String lastContestId = applicationKeys.stream()
            .map(applicationRepository::byID)
            .filter(application -> DataRecoverConfiguration.contestsAllowed(
                genericTokenRequested.getContestId()).contains(application.getContestId())
            ).max(Comparator.comparing(Application::getSubmissionDate))
            .orElseThrow(()-> new ApplicationToRecoverNotFoundException(
                genericTokenRequested.getApplicantKey(),
                genericTokenRequested.getContestId(),
                genericTokenRequested.getLanguage()))
            .getContestId();

        tokenRequestIsForTheLastContest = lastContestId.equals(genericTokenRequested.getContestId());

        if (!tokenRequestIsForTheLastContest && !genericTokenRequested.getAllowRecoverData()) {
            throw new DataRecoveryNotAllowedException(
                genericTokenRequested.getApplicantKey(),
                genericTokenRequested.getContestId(),
                genericTokenRequested.getLanguage());
        }

        if (genericTokenRequested.getForceNewToken()) {
            generateNewToken(genericTokenRequested, lastContestId);
        } else {
            GenericToken recoveredToken = genericTokenRepository.byID(genericTokenRequested.getApplicantKey());
            if (recoveredToken != null) {
                Boolean userHasToken = true;
                new GenericTokenGenerated(recoveredToken, genericTokenRequested.getContestId(), lastContestId, userHasToken).emit();
            } else {
                generateNewToken(genericTokenRequested, lastContestId);
            }
        }

        if (!tokenRequestIsForTheLastContest) {
            saveRecoveryPermit(genericTokenRequested);
        }
    }

    private void generateNewToken(GenericTokenRequested genericTokenRequested, String lastContestId) {
        genericTokenRepository.deleteKey(genericTokenRequested.getApplicantKey());
        String genericTokenKey = RecoveryKeyGenerator.generateKey();

        GenericToken genericToken = new GenericToken(
            genericTokenRequested.getLanguage(),
            genericTokenRequested.getApplicantKey(),
            genericTokenKey
        );

        genericTokenRepository.save(genericToken);

        Boolean userHasToken = false;
        new GenericTokenGenerated(genericToken, genericTokenRequested.getContestId(), lastContestId, userHasToken).emit();
    }

    private void saveRecoveryPermit(GenericTokenRequested genericTokenRequested) {
        recoveryPermitRepository.deleteKey(
            genericTokenRequested.getContestId() + ':'
                + genericTokenRequested.getApplicantKey()
        );

        RecoveryPermit recoveryPermit = new RecoveryPermit(
            genericTokenRequested.getContestId(),
            genericTokenRequested.getApplicantKey(),
            genericTokenRequested.getAllowRecoverData(),
            genericTokenRequested.getAllowRecoverDataText(),
            ZonedDateTime.now()
        );

        recoveryPermitRepository.save(recoveryPermit);
    }
}
