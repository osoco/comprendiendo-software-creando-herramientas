package es.osoco.bbva.ats.forms.application;

import es.osoco.bbva.ats.forms.adapter.AgentRecoverRequestApiGatewayAdapter;
import es.osoco.bbva.ats.forms.adapter.json.JsonApplicationRecoverRequested;
import es.osoco.bbva.ats.forms.adapter.json.formanswered.JsonFormAnswered;
import es.osoco.bbva.ats.forms.application.parser.domaintoexternal.PartialApplicationRecoveredParser;
import es.osoco.bbva.ats.forms.domain.aggregate.Answer;
import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import es.osoco.bbva.ats.forms.domain.aggregate.form.Form;
import es.osoco.bbva.ats.forms.domain.aggregate.form.Question;
import es.osoco.bbva.ats.forms.domain.aggregate.form.QuestionType;
import es.osoco.bbva.ats.forms.domain.events.ApplicationRecoverRequested;
import es.osoco.bbva.ats.forms.domain.events.DomainEvent;
import es.osoco.bbva.ats.forms.domain.events.PartialApplicationRecovered;
import es.osoco.bbva.ats.forms.domain.repository.FormRepository;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class RecoverPartialApplicationUseCase extends UseCase<JsonApplicationRecoverRequested> {

    public static RecoverPartialApplicationUseCase getInstance() {
        return SubmitFormUseCaseSingletonContainer.SINGLETON;
    }

    protected static final class SubmitFormUseCaseSingletonContainer {
        protected static final RecoverPartialApplicationUseCase SINGLETON = new RecoverPartialApplicationUseCase();
    }

    @Override
    public void process(JsonApplicationRecoverRequested applicationRecoverRequested) {
        domainInit(applicationRecoverRequested);

        ApplicationRecoverRequested recoverRequested= new ApplicationRecoverRequested(
                applicationRecoverRequested.getContestId(),
                applicationRecoverRequested.getApplicantKey().toLowerCase());

        domainEventService.subscribe(this);
        domainEventService.receive(recoverRequested);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onEvent(final DomainEvent event) {
        if (event instanceof PartialApplicationRecovered) {
            PartialApplicationRecovered partialApplicationRecovered = (PartialApplicationRecovered) event;

            Application application = partialApplicationRecovered.getApplication();
            Form form = FormRepository.getInstance().byID(application.getFormId() + ":" +application.getLanguage());

            partialApplicationRecovered = new PartialApplicationRecovered(updateFileName(partialApplicationRecovered.getApplication(), form));

            JsonFormAnswered jsonFormAnswered = (JsonFormAnswered) PartialApplicationRecoveredParser.getInstance().toExternalEvent(partialApplicationRecovered);
            AgentRecoverRequestApiGatewayAdapter.getInstance().onOutputEvent(jsonFormAnswered);
        }
    }

    private Application updateFileName(Application application, Form form){
            Set<String> fileQuestions = form
                    .getSections()
                    .stream()
                    .filter(section -> section.getQuestions() != null)
                    .flatMap(section -> section.getQuestions().stream())
                    .filter(question -> Objects.nonNull(question) && Objects.nonNull(question.getType()))
                    .filter(question -> question.getType().equals(QuestionType.FILE))
                    .map(Question::getId)
                    .collect(Collectors.toSet());

            Map<String, Answer> answerMap = application
                    .getAnswersById()
                    .entrySet()
                    .stream()
                    .filter(entry -> Objects.nonNull(entry.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                        if (fileQuestions.contains(entry.getKey()) && entry.getValue().getText() != null) {
                            return new Answer(getFileNameFromS3Url(entry.getValue().getText()),null);
                        } else {
                            return entry.getValue();
                        }
                    }));

            return copyApplicationWithNewAnswers(application, answerMap);
        }

        private String getFileNameFromS3Url(String s3Url){
           return s3Url.substring(s3Url.indexOf("_") + 1);
        }

        private Application copyApplicationWithNewAnswers(Application originApplication, Map<String,Answer> answerMap){
            return Application.builder()
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

