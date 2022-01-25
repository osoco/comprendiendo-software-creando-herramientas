package es.osoco.bbva.ats.forms.domain.service;

import es.osoco.bbva.ats.forms.domain.aggregate.Answer;
import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import es.osoco.bbva.ats.forms.domain.aggregate.ApplicationStatus;
import es.osoco.bbva.ats.forms.domain.aggregate.form.Form;
import es.osoco.bbva.ats.forms.domain.aggregate.form.Question;
import es.osoco.bbva.ats.forms.domain.aggregate.form.QuestionType;
import es.osoco.bbva.ats.forms.domain.aggregate.RecoveryToken;
import es.osoco.bbva.ats.forms.domain.exception.ApplicationAlreadySentException;
import es.osoco.bbva.ats.forms.domain.exception.ApplicationWithOtherKeySentException;
import es.osoco.bbva.ats.forms.domain.exception.ApplicationSubmitForbiddenException;
import es.osoco.bbva.ats.forms.domain.repository.ApplicationRepository;
import es.osoco.bbva.ats.forms.domain.repository.RecoveryTokenRepository;
import es.osoco.bbva.ats.forms.domain.repository.GenericTokenRepository;
import es.osoco.bbva.ats.forms.domain.repository.EmailTokenRepository;
import es.osoco.bbva.ats.forms.domain.util.ApplicationValidator;
import es.osoco.bbva.ats.forms.domain.util.RecoveryKeyGenerator;

import es.osoco.logging.Logging;
import es.osoco.logging.LoggingFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


public abstract class AbstractApplicationSubmitService {

    private final static ApplicationValidator applicationValidator = ApplicationValidator.getInstance();

    final static ApplicationRepository applicationRepository = ApplicationRepository.getInstance();
    final static RecoveryTokenRepository recoveryTokenRepository = RecoveryTokenRepository.getInstance();
    final static GenericTokenRepository genericTokenRepository = GenericTokenRepository.getInstance();
    final static EmailTokenRepository emailTokenRepository = EmailTokenRepository.getInstance();

    String createNewToken(Application originApplication) {
        String recoveryKey;
        recoveryKey = RecoveryKeyGenerator.generateKey();
        RecoveryToken recoveryToken = new RecoveryToken(originApplication.getContestId(), originApplication.getLanguage(), originApplication.getApplicantKey(), recoveryKey);
        recoveryTokenRepository.save(recoveryToken);
        return recoveryKey;
    }

    void validateFields(Application application) {
        validateFields(application, false);
    }


    void validateFields(Application application, boolean strict) {
        Map<Question,Answer> invalidItemsList =  applicationValidator.executeAndGetFails(application, strict);
        if (!invalidItemsList.entrySet().isEmpty()) {
            //TODO implement a custom exception
            throw new RuntimeException("Invalid answers" +  invalidItemsList.entrySet().stream().map(item -> "question: " + item.getKey().getText() + " answer: " + item.getValue().getText()).collect(Collectors.joining(" \n")));
        }
    }

    void validateRequiredFields(Application application) {
        Set<Question> requiredQuestionNotAnswered = applicationValidator.validateRequiredFields(application);
        if (!requiredQuestionNotAnswered.isEmpty()) {
            //TODO implement a custom exception
            throw new RuntimeException("Invalid answers " +  requiredQuestionNotAnswered.stream().map(item -> "question: " + item.getId() + " : " + item.getText()).collect(Collectors.joining(" \n")));
        }
    }

    void renewTokenTTL(RecoveryToken recoveryToken){
        recoveryTokenRepository.save(recoveryToken);
    }

    void checkIfApplicationKeyIsEquals(Application originApplication, Application draftStoredApplication) {
        if (draftStoredApplication != null && !draftStoredApplication.getApplicationKey().equals(originApplication.getApplicationKey())){
            throw new ApplicationWithOtherKeySentException();
        }
    }

    void checkIfApplicationIsComplete(Application draftStoredApplication) {
        if (draftStoredApplication != null && draftStoredApplication.getStatus().equals(ApplicationStatus.FINISHED)){
            throw new ApplicationAlreadySentException();
        }
    }

    void checkTokensAreValid(Application originApplication, Form form) {
        Logging logging = LoggingFactory.getInstance().createLogging();
        logging.info("originApplication.getId: " + originApplication.getId());
        logging.info("form.getVerifyApplicantEmail: " + form.getVerifyApplicantEmail());

        if (originApplication.getEmailToken() != null) {
            if (!form.getVerifyApplicantEmail()) {
                logging.warn("The verify applicant email is inactive " +
                    "however an email token [" + originApplication.getEmailToken() +
                    "] is sent for applicantKey: " + originApplication.getApplicantKey());
            }
            checkEmailTokenIsValid(originApplication);
        } else if (originApplication.getFormToken() != null) {
            checkFormTokenIsValid(originApplication);
        } else {
            if (form.getVerifyApplicantEmail()) {
                logging.warn("Missing Email or Form token and verify applicant email is active for applicantKey: " + originApplication.getApplicantKey());
                throw new ApplicationSubmitForbiddenException();
            }
        }
    }

    void checkFormTokenIsValid(Application application) {
        Logging logging = LoggingFactory.getInstance().createLogging();
        if (!genericTokenRepository.isTokenValid(application.getApplicantKey(), application.getFormToken())) {
            logging.warn("Form token " +
                         application.getEmailToken() +
                         " could not be verified for applicantKey: " +
                         application.getApplicantKey());
            throw new ApplicationSubmitForbiddenException();
        }
    }

    void checkEmailTokenIsValid(Application application) {
        Logging logging = LoggingFactory.getInstance().createLogging();
        String verifiedEmail = emailTokenRepository.verifiedEmailByToken(application.getEmailToken());
        if (!application.getApplicantKey().equals(verifiedEmail)) {
            logging.warn("Email " + application.getApplicantKey() + " could not be verified with email token: " + application.getEmailToken());
            throw new ApplicationSubmitForbiddenException();
        }
    }

    Map<String, Answer> getUpdatedAnswerMap(Application originApplication, Application draftStoredApplication, Form form) {
        Set<String> fileQuestions = form
                .getSections()
                .stream()
                .filter(section -> section.getQuestions() != null)
                .flatMap(section -> section.getQuestions().stream())
                .filter(question -> Objects.nonNull(question) && Objects.nonNull(question.getType()))
                .filter(question -> question.getType().equals(QuestionType.FILE))
                .map(Question::getId)
                .collect(Collectors.toSet());

        Map<String, Answer> answerMap = originApplication.getAnswersById();

        fileQuestions.forEach(fileQuestionId -> {
            Answer newAnswer = originApplication.getAnswersById().get(fileQuestionId);
            Answer storedAnswer = draftStoredApplication.getAnswersById().get(fileQuestionId);
            if ( newAnswer == null && storedAnswer != null)
            {
                answerMap.put(fileQuestionId,storedAnswer);
            }
        });
        return answerMap;
    }
}
