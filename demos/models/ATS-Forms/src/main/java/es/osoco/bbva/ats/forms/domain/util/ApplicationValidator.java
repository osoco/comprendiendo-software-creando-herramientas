package es.osoco.bbva.ats.forms.domain.util;

import es.osoco.bbva.ats.forms.domain.aggregate.Answer;
import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import es.osoco.bbva.ats.forms.domain.aggregate.form.Form;
import es.osoco.bbva.ats.forms.domain.aggregate.form.Question;
import es.osoco.bbva.ats.forms.domain.aggregate.form.QuestionType;
import es.osoco.bbva.ats.forms.domain.aggregate.form.Section;
import es.osoco.bbva.ats.forms.domain.repository.FormRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ApplicationValidator implements Validator<Application, Map<Question,Answer>> {

    public static ApplicationValidator getInstance() {
        return ApplicationValidator.ApplicationValidatorSingletonContainer.SINGLETON;
    }

    protected static final class ApplicationValidatorSingletonContainer {
        protected static final ApplicationValidator SINGLETON = new ApplicationValidator();
    }

    public Map<Question,Answer> executeAndGetFails(Application application, boolean strict) {

        Form form = FormRepository.getInstance().byID(application.getFormId() + ":" + application.getLanguage());
        Map<Question,Answer> questionAnswerMap = new HashMap<>();

        Map<String, Question> questionMap = form.getSections().stream().filter(section -> section.getQuestions() != null)
                .map(Section::getQuestions)
                .flatMap(List::stream)
                .collect(Collectors.toConcurrentMap(Question::getId, Function.identity()));

        application.getAnswersById().forEach((key, answer) -> {
                    if (questionMap.get(key) != null) {
                        QuestionType questionType = questionMap.get(key).getType();
                        if (!QuestionValidationStrategy.valueByQuestionType(questionType).validate(answer)) {
                            questionAnswerMap.put(questionMap.get(key), answer);
                        }
                    } else if (strict){
                        throw new RuntimeException("JsonQuestion not found for answer: " + key + answer.getText() + answer.getChoices());
                    }
                }
        );
        return questionAnswerMap;
    }

    public Set<Question> validateRequiredFields(Application application) {

        Form form = FormRepository.getInstance().byID(application.getFormId() + ":" + application.getLanguage());
        Set<Question> questions = new HashSet<>();

        Map<String, Question> questionMap = form.getSections().stream()
                .filter(section -> section.getQuestions() != null)
                .flatMap(section -> section.getQuestions().stream())
                .collect(Collectors.toConcurrentMap(Question::getId, Function.identity()));

        application.getAnswersById().forEach((key, answer) ->{
                    if (!QuestionValidationStrategy.REQUIRED.validate(answer)){
                        questions.add(questionMap.get(key));
                    }
                }
        );
        return questions;
    }
}
