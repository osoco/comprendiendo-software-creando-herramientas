package es.osoco.bbva.ats.forms.domain.service;

import es.osoco.bbva.ats.forms.domain.aggregate.Answer;
import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import es.osoco.bbva.ats.forms.domain.aggregate.form.Form;
import es.osoco.bbva.ats.forms.domain.aggregate.RecoveryToken;
import es.osoco.bbva.ats.forms.domain.events.ApplicationStored;
import es.osoco.bbva.ats.forms.domain.events.ApplicationSubmitted;
import es.osoco.bbva.ats.forms.domain.repository.FormRepository;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.function.Consumer;

public class ApplicationSubmittedService extends AbstractApplicationSubmitService implements Consumer<ApplicationSubmitted>  {

    @Override
    public void accept(ApplicationSubmitted domainEvent) {

        Application originApplication = domainEvent.getApplication();
        Form form = FormRepository.getInstance().byID(originApplication.getFormId() + ":" + originApplication.getLanguage());

        Application draftStoredApplication = applicationRepository.byID(originApplication.getId());

        checkIfApplicationIsComplete(draftStoredApplication);
        checkIfApplicationKeyIsEquals(originApplication, draftStoredApplication);
        checkTokensAreValid(originApplication, form);

        String recoveryKey;
        RecoveryToken applicationToken = null;
        ZonedDateTime firstSubmissionDate = ZonedDateTime.now();
        
        originApplication.removeTokens();
        Map<String, Answer> updatedAnswerMap = originApplication.getAnswersById();

        Integer version = 0;
        if (draftStoredApplication != null){
            version = 1;
            firstSubmissionDate = draftStoredApplication.getFirstSubmissionDate();
            applicationToken = recoveryTokenRepository.byID(originApplication.getId());
            updatedAnswerMap = getUpdatedAnswerMap(originApplication, draftStoredApplication, form);
        }

        if (applicationToken == null) {
            recoveryKey = createNewToken(originApplication);
        }else {
            renewTokenTTL(applicationToken);
            recoveryKey = applicationToken.getRecoveryKey();
        }

        Application application = Application.builder()
                .version(version)
                .answersById(updatedAnswerMap)
                .recoveryKey(recoveryKey)
                .language(originApplication.getLanguage())
                .applicationKey(originApplication.getApplicationKey())
                .applicantKey(originApplication.getApplicantKey())
                .status(originApplication.getStatus())
                .contestId(originApplication.getContestId())
                .formId(originApplication.getFormId())
                .origin(originApplication.getOrigin())
                .submissionDate(ZonedDateTime.now())
                .firstSubmissionDate(firstSubmissionDate )
                .build();

        validateFields(application);
        validateRequiredFields(application);

        applicationRepository.save(application);

        new ApplicationStored(application).emit();
    }
}
