package es.osoco.bbva.ats.forms.application;

import es.osoco.bbva.ats.forms.adapter.ApplicantRecoverRequestApiGatewayAdapter;
import es.osoco.bbva.ats.forms.adapter.json.JsonApplicationRecoverWithRecoveryKeyRequested;
import es.osoco.bbva.ats.forms.adapter.json.formanswered.JsonFormAnswered;
import es.osoco.bbva.ats.forms.application.parser.domaintoexternal.PartialApplicationRecoveredParser;
import es.osoco.bbva.ats.forms.domain.aggregate.Answer;
import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import es.osoco.bbva.ats.forms.domain.events.ApplicationRecoverWithRecoveryKeyRequested;
import es.osoco.bbva.ats.forms.domain.events.DomainEvent;
import es.osoco.bbva.ats.forms.domain.events.PartialApplicationRecovered;

import java.util.Map;

public class RecoverPartialApplicationWithRecoveryKeyUseCase extends UseCase<JsonApplicationRecoverWithRecoveryKeyRequested> {

    public static RecoverPartialApplicationWithRecoveryKeyUseCase getInstance() {
        return SubmitFormUseCaseSingletonContainer.SINGLETON;
    }

    protected static final class SubmitFormUseCaseSingletonContainer {
        protected static final RecoverPartialApplicationWithRecoveryKeyUseCase SINGLETON = new RecoverPartialApplicationWithRecoveryKeyUseCase();
    }

    @Override
    public void process(JsonApplicationRecoverWithRecoveryKeyRequested applicationRecoverRequested) {
        domainInit(applicationRecoverRequested);

        ApplicationRecoverWithRecoveryKeyRequested recoverRequested = new ApplicationRecoverWithRecoveryKeyRequested(
                applicationRecoverRequested.getContestId(),
                applicationRecoverRequested.getApplicantKey().toLowerCase(),
                applicationRecoverRequested.getApplicantRecoveryKey());

        domainEventService.subscribe(this);
        domainEventService.receive(recoverRequested);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onEvent(final DomainEvent event) {
        if (event instanceof PartialApplicationRecovered) {
            PartialApplicationRecovered partialApplicationRecovered = (PartialApplicationRecovered) event;
            JsonFormAnswered jsonFormAnswered = (JsonFormAnswered) PartialApplicationRecoveredParser.getInstance().toExternalEvent(partialApplicationRecovered);
            ApplicantRecoverRequestApiGatewayAdapter.getInstance().onOutputEvent(jsonFormAnswered);
        }
    }

    private String getFileNameFromS3Url(String s3Url){
        return s3Url.substring(s3Url.indexOf("_") + 1);
    }

    private Application copyApplicationWithNewAnswers(Application originApplication, Map<String,Answer> answerMap){
        return
            Application.builder()
                .answersById(answerMap)
                .recoveryKey(originApplication.getRecoveryKey())
                .language(originApplication.getLanguage())
                .applicationKey(originApplication.getApplicationKey())
                .applicantKey(originApplication.getApplicantKey())
                .status(originApplication.getStatus())
                .contestId(originApplication.getContestId())
                .formId(originApplication.getFormId())
                .origin(originApplication.getOrigin())
                .submissionDate(originApplication.getSubmissionDate())
                .firstSubmissionDate(originApplication.getFirstSubmissionDate())
                .build();
    }
}
