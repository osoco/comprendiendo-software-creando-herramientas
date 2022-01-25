package es.osoco.bbva.ats.forms.application.parser.externaltodomain;

import es.osoco.bbva.ats.forms.adapter.json.applicationsubmitted.JsonAnswer;
import es.osoco.bbva.ats.forms.adapter.json.formanswered.JsonFormAnswered;
import es.osoco.bbva.ats.forms.application.exception.FormMandatoryKeyNotFoundException;
import es.osoco.bbva.ats.forms.domain.aggregate.Answer;
import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import es.osoco.bbva.ats.forms.domain.aggregate.ApplicationStatus;
import es.osoco.bbva.ats.forms.domain.aggregate.Choice;
import es.osoco.bbva.ats.forms.domain.aggregate.form.Form;
import es.osoco.bbva.ats.forms.domain.aggregate.form.Question;
import es.osoco.bbva.ats.forms.domain.aggregate.form.Section;
import es.osoco.bbva.ats.forms.domain.repository.FormRepository;

import java.util.*;
import java.util.stream.Collectors;

public class ApplicationParser {


    public static ApplicationParser getInstance() {
        return ApplicationParser.ApplicationParserSingletonContainer.SINGLETON;
    }

    protected static final class ApplicationParserSingletonContainer {
        protected static final ApplicationParser SINGLETON = new ApplicationParser();
    }

    public Application createApplication(JsonFormAnswered formAnswered, ApplicationStatus status) {

        FormRepository formRepository = FormRepository.getInstance();
        Form form = formRepository.byID(formAnswered.formId + ":" + formAnswered.language.toUpperCase());

        String applicantKeyQuestionId = getAnswerTextByKey(form, formAnswered, "APPLICANT_KEY");
        String applicationKey = getAnswerTextByKey(form, formAnswered, "APPLICATION_KEY");

        Application.ApplicationBuilder applicationBuilder = Application
                .builder()
                .contestId(formAnswered.getContestId())
                .formId(formAnswered.getFormId())
                .applicantKey(applicantKeyQuestionId.toLowerCase())
                .applicationKey(applicationKey)
                .status(status)
                .origin(formAnswered.getOrigin())
                .language(formAnswered.getLanguage())
                .entityId(formAnswered.getEntityId());

        Map<String, Answer> answersByQuestionId = new HashMap<>();
        formAnswered.getAnswers().forEach(answer ->
                answersByQuestionId.put(answer.getQuestionId(),
                        new Answer(
                                answer.getText(),
                                getChoicesFromAnswer(answer))));

        applicationBuilder.answersById(answersByQuestionId);
        return applicationBuilder.build();
    }

    private List<Section> getSectionsFromForm(Form form) {
        return form.getSections();
    }

    private String getAnswerTextByKey(Form form, JsonFormAnswered formAnswered, String key) {
        String questionId = getQuestionIdByKey(form, key);
        return formAnswered
                .getAnswers().stream()
                .filter(answer -> answer.getQuestionId().equals(questionId))
                .findFirst().orElseThrow(FormMandatoryKeyNotFoundException::new)
                .getText();
    }

    private String getQuestionIdByKey(Form form, String key) {
        List<Question> questions = form.getSections().stream()
                .filter(Objects::nonNull)
                .filter(section -> section.getQuestions() != null)
                .map(Section::getQuestions)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return questions.stream()
                .filter(question -> question.getKey() != null)
                .filter(question -> question.getKey().equals(key))
                .findFirst()
                .orElseThrow(FormMandatoryKeyNotFoundException::new).getId();
    }


    private Set<Choice> getChoicesFromAnswer(JsonAnswer answer){
        Set<Choice> result = null;
        if (answer.choices !=  null){
            result = answer.getChoices().stream().
                    map(jsonChoice -> new Choice(jsonChoice.getId(), jsonChoice.getLabel())).
                    collect(Collectors.toSet());
        }
        return result;
    }
}
