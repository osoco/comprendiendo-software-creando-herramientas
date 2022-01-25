package es.osoco.bbva.ats.forms.domain.service;

import es.osoco.bbva.ats.forms.domain.aggregate.Answer;
import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import es.osoco.bbva.ats.forms.domain.aggregate.ApplicationStatus;
import es.osoco.bbva.ats.forms.domain.aggregate.form.Form;
import es.osoco.bbva.ats.forms.domain.aggregate.RecoveryToken;
import es.osoco.bbva.ats.forms.domain.events.ApplicationUpdateStored;
import es.osoco.bbva.ats.forms.domain.events.ApplicationUpdated;
import es.osoco.bbva.ats.forms.domain.exception.ApplicationNotFoundException;
import es.osoco.bbva.ats.forms.domain.repository.FormRepository;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.function.Consumer;

public class ApplicationUpdatedService extends AbstractApplicationSubmitService implements Consumer<ApplicationUpdated>  {

    @Override
    public void accept(ApplicationUpdated domainEvent) {

        Application originApplication = domainEvent.getApplication();
        Form form = FormRepository.getInstance().byID(originApplication.getFormId() + ":" + originApplication.getLanguage());

        Application storedApplication = applicationRepository.byID(originApplication.getId());

        if (storedApplication != null){
            ZonedDateTime firstSubmissionDate = storedApplication.getFirstSubmissionDate();
            RecoveryToken applicationToken = recoveryTokenRepository.byID(originApplication.getId());
            Map<String, Answer> updatedAnswerMap = getUpdatedAnswerMap(originApplication, storedApplication, form);
            ApplicationStatus applicationStatus = storedApplication.getStatus();
            checkIfApplicationKeyIsEquals(originApplication, storedApplication);

            String recoveryKey = null;
            if (applicationToken != null) {
                recoveryKey = applicationToken.getRecoveryKey();
            }

            Application application = Application.builder()
                    .version(1)
                    .answersById(updatedAnswerMap)
                    .recoveryKey(recoveryKey)
                    .language(originApplication.getLanguage())
                    .applicationKey(originApplication.getApplicationKey())
                    .applicantKey(originApplication.getApplicantKey())
                    .status(applicationStatus)
                    .contestId(originApplication.getContestId())
                    .formId(originApplication.getFormId())
                    .origin(originApplication.getOrigin())
                    .submissionDate(ZonedDateTime.now())
                    .firstSubmissionDate(firstSubmissionDate )
                    .build();

            validateFields(application);

            applicationRepository.save(application);

            new ApplicationUpdateStored(application).emit();
        }
        else {
            throw new ApplicationNotFoundException();
        }
    }
}
