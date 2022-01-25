package es.osoco.bbva.ats.forms.domain.service;

import es.osoco.bbva.ats.forms.domain.aggregate.Answer;
import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import es.osoco.bbva.ats.forms.domain.events.EntityStored;
import es.osoco.bbva.ats.forms.domain.events.EntitySubmitted;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.function.Consumer;

public class EntitySubmittedService extends AbstractApplicationSubmitService implements Consumer<EntitySubmitted>  {

    @Override
    public void accept(EntitySubmitted domainEvent) {

        Application originApplication = domainEvent.getApplication();
        Application storedApplication = applicationRepository.byID(originApplication.getId());

        ZonedDateTime firstSubmissionDate = ZonedDateTime.now();

        Map<String, Answer> updatedAnswerMap = originApplication.getAnswersById();

        Integer version = 0;
        if (storedApplication != null){
            firstSubmissionDate = storedApplication.getFirstSubmissionDate();
        }

        Application application = Application.builder()
                .version(version)
                .answersById(updatedAnswerMap)
                .language(originApplication.getLanguage())
                .applicationKey(originApplication.getApplicationKey())
                .applicantKey(originApplication.getApplicantKey())
                .status(originApplication.getStatus())
                .contestId(originApplication.getContestId())
                .formId(originApplication.getFormId())
                .origin(originApplication.getOrigin())
                .submissionDate(ZonedDateTime.now())
                .firstSubmissionDate(firstSubmissionDate )
                .entityId(originApplication.getEntityId())
                .build();

        validateFields(application, false);
        validateRequiredFields(application);

        applicationRepository.save(application);

        new EntityStored(application).emit();
    }
}
