package es.osoco.bbva.ats.forms.domain.service;

import es.osoco.bbva.ats.forms.application.util.ConstantTimeStringEquals;
import es.osoco.bbva.ats.forms.domain.aggregate.Answer;
import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import es.osoco.bbva.ats.forms.domain.aggregate.Choice;
import es.osoco.bbva.ats.forms.domain.aggregate.GenericToken;
import es.osoco.bbva.ats.forms.domain.events.ApplicationDataRecoverRequested;
import es.osoco.bbva.ats.forms.domain.events.ApplicationDataRecovered;
import es.osoco.bbva.ats.forms.domain.events.PartialApplicationRecovered;
import es.osoco.bbva.ats.forms.domain.exception.ApplicationNotFoundException;
import es.osoco.bbva.ats.forms.domain.repository.ApplicationRepository;
import es.osoco.bbva.ats.forms.domain.repository.ConverterContestFileRepository;
import es.osoco.bbva.ats.forms.domain.repository.GenericTokenRepository;
import es.osoco.bbva.ats.forms.domain.valueobject.QuestionMapping;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Map.Entry;

public class ApplicationDataRecoverRequestedService implements Consumer<ApplicationDataRecoverRequested> {

    private static final ApplicationRepository applicationRepository = ApplicationRepository.getInstance();
    private static final GenericTokenRepository genericTokenRepository = GenericTokenRepository.getInstance();
    private static final ConverterContestFileRepository converterContestFileRepository = ConverterContestFileRepository.getInstance();

    @Override
    public void accept(ApplicationDataRecoverRequested applicationDataRecoverRequested) {

        GenericToken genericToken = genericTokenRepository.byID(applicationDataRecoverRequested.getApplicantKey());

        if (genericToken != null && ConstantTimeStringEquals.safeEqual(genericToken.getToken(), applicationDataRecoverRequested.getRecoveryKey())) {
            genericTokenRepository.deleteKey(applicationDataRecoverRequested.getApplicantKey());
            Application application = applicationRepository.byID(applicationDataRecoverRequested.getID());
            if (application != null) {
                new PartialApplicationRecovered(application).emit();
            } else {
                Set<String> applicationKeys = applicationRepository.findKeys(applicationDataRecoverRequested.getApplicantKey());
                if (applicationKeys.size() > 0) {
                    application = applicationKeys.stream()
                            .map(applicationRepository::byID)
                            .max(Comparator.comparing(Application::getSubmissionDate)).get();

                    Application convertedApplication = applicationDataContestConvert(applicationDataRecoverRequested.getContestId(), application.getContestId(), application);
                    new ApplicationDataRecovered(convertedApplication).emit();

                } else {
                    throw new ApplicationNotFoundException();
                }
            }
        } else {
            throw new ApplicationNotFoundException();
        }
    }

    private Application applicationDataContestConvert(String originContestId, String targetContestId, Application application) {

        Map<String, QuestionMapping> answerConvertMap = converterContestFileRepository.getAnswersConvertMapping(
            originContestId,
            targetContestId
        );

        Map <String, Answer> newAnswerMap = answerConvertMap
                .entrySet()
                .stream()
                .filter(answerMapEntry -> hasConverterAnswer(application, answerMapEntry))
                .collect(Collectors.toMap(
                        x -> x.getValue().getId(),
                        x -> isAnswerWithChoices(application, x) ?
                            buildAnswerWithChoices(application, answerConvertMap, x) :
                            buildAnswerWithText(application, x)
                        )
                );

        Map <String, Answer> answerMapWithoutEmptyAnswers = newAnswerMap.entrySet()
            .stream()
            .filter(x -> !x.getValue().isEmpty())
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));



        System.out.println("answerMapWithoutEmptyAnswers: " + answerMapWithoutEmptyAnswers );

        return Application.builder()
                .version(application.getVersion())
                .answersById(answerMapWithoutEmptyAnswers)
                .recoveryKey(application.getRecoveryKey())
                .language("EN") //TODO replace for `.language(application.getLanguage())` when https://osocojira.atlassian.net/browse/BATS-1785 is done
            .applicationKey(application.getApplicationKey())
                .applicantKey(application.getApplicantKey())
                .status(application.getStatus())
                .contestId(application.getContestId())
                .formId(application.getFormId())
                .origin(application.getOrigin())
                .submissionDate(ZonedDateTime.now())
                .firstSubmissionDate(application.getFirstSubmissionDate())
                .build();
    }

    private Answer buildAnswerWithChoices(
            Application application,
            Map<String, QuestionMapping> answerConvertMap,
            Entry<String, QuestionMapping> questionMappingEntry
    ) {
        return new Answer(null,
                application.getAnswersById().get(questionMappingEntry.getKey()).getChoices()
                .stream()
                .map( y -> {
                    Map<String, String> choices = answerConvertMap.get(questionMappingEntry.getKey()).getChoices();
                    Map<String, String> revertedMap = new HashMap<String, String>();
                    choices.forEach((k, v) -> {
                        if (revertedMap.get(v) == null) {
                            revertedMap.put(v, k);
                        }
                    });
                    return new Choice(choices.get(y.getLabel()), revertedMap.get(choices.get(y.getLabel())));
                })
                .filter(x -> (x.getLabel() != null && x.getId() != null))
                .collect(Collectors.toSet()));
    }

    private boolean hasConverterAnswer(Application application, Entry<String, QuestionMapping> answerMapEntry) {
        return application.getAnswersById().get(answerMapEntry.getKey()) != null;
    }

    private Answer buildAnswerWithText(Application application, Entry<String, QuestionMapping> x) {
        return new Answer(application.getAnswersById().get(x.getKey()).getText(), null);
    }

    private boolean isAnswerWithChoices(Application application, Entry<String, QuestionMapping> x) {
        return application.getAnswersById().get(x.getKey()).getText() == null;
    }
}
