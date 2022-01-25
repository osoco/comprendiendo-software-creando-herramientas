package es.osoco.bbva.ats.forms.application.parser.externaltodomain;

import es.osoco.bbva.ats.forms.adapter.json.formupdated.JsonBody;
import es.osoco.bbva.ats.forms.adapter.json.formupdated.JsonFormUpdated;
import es.osoco.bbva.ats.forms.adapter.json.formupdated.JsonI18n;
import es.osoco.bbva.ats.forms.adapter.json.formupdated.JsonQuestion;
import es.osoco.bbva.ats.forms.adapter.json.formupdated.JsonSection;
import es.osoco.bbva.ats.forms.domain.aggregate.form.Checkbox;
import es.osoco.bbva.ats.forms.domain.aggregate.form.ConfirmationMessage;
import es.osoco.bbva.ats.forms.domain.aggregate.form.Form;
import es.osoco.bbva.ats.forms.domain.aggregate.form.Form.FormBuilder;
import es.osoco.bbva.ats.forms.domain.aggregate.form.Gdpr;
import es.osoco.bbva.ats.forms.domain.aggregate.form.LegalConditions;
import es.osoco.bbva.ats.forms.domain.aggregate.form.Option;
import es.osoco.bbva.ats.forms.domain.aggregate.form.Question;
import es.osoco.bbva.ats.forms.domain.aggregate.form.QuestionType;
import es.osoco.bbva.ats.forms.domain.aggregate.form.RecoveryZone;
import es.osoco.bbva.ats.forms.domain.aggregate.form.Responses;
import es.osoco.bbva.ats.forms.domain.aggregate.form.Section;
import es.osoco.bbva.ats.forms.domain.aggregate.form.SummarySentence;
import es.osoco.bbva.ats.forms.domain.aggregate.form.Tab;
import es.osoco.bbva.ats.forms.domain.aggregate.form.TimeoutErrorMessage;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class FormParser {

    public static FormParser getInstance() {
        return FormParser.ApplicationParserSingletonContainer.SINGLETON;
    }

    protected static final class ApplicationParserSingletonContainer {
        protected static final FormParser SINGLETON = new FormParser();
    }

    public List<Form> createForms(JsonFormUpdated jsonFormUpdated) {

        JsonBody formUpdatedBody = jsonFormUpdated.getBody();
        List<String> languages = formUpdatedBody.getLanguages();

        if ((languages == null)
            || (languages.size() == 0)) {
            List<JsonSection> sections = formUpdatedBody.getSections();
            if ((sections != null)
                && (sections.size() > 0)) {
                List<JsonI18n> i18nList = sections.get(0).getText();
                if ((i18nList != null)
                    && (i18nList.size() > 0)) {
                    languages = i18nList.stream().map(JsonI18n::getLang).collect(Collectors.toList());
                }
            }
        }
        if (languages == null) {
            languages = Arrays.asList("EN");
        }
        return languages.stream().map(language -> {
            final FormBuilder builder = Form.builder();
            builder
                .formId(formUpdatedBody.getId())
                .contestId(formUpdatedBody.getContestId())
                .language(language)
                .contestName(formUpdatedBody.getContestName())
                .canSaveDraft(formUpdatedBody.getCanSaveDraft())
                .verifyApplicantEmail(formUpdatedBody.getVerifyApplicantEmail())
                .showAllowShareDataCheck(formUpdatedBody.getShowAllowShareDataCheck());

            if (formUpdatedBody.getRecoveryZone() != null) {
                builder.recoveryZone(
                    new RecoveryZone(
                        retrieveI18nText(formUpdatedBody.getRecoveryZone().getTitle(), language),
                        retrieveI18nText(formUpdatedBody.getRecoveryZone().getMessage(), language),
                        retrieveI18nText(formUpdatedBody.getRecoveryZone().getButton(), language),
                        new Checkbox(
                            retrieveI18nText(formUpdatedBody.getRecoveryZone().getCheckbox().getHelpText(), language),
                            retrieveI18nText(formUpdatedBody.getRecoveryZone().getCheckbox().getLabel(), language),
                            retrieveI18nText(formUpdatedBody.getRecoveryZone().getCheckbox().getErrorLabel(), language))));
            }
            if (formUpdatedBody.getSendingDataMessage() != null) {
                builder.sendingDataMessage(retrieveI18nText(formUpdatedBody.getSendingDataMessage(), language));
            }
            if ((formUpdatedBody.getConfirmationMessage() != null)
                && (formUpdatedBody.getConfirmationMessage().getHeader() != null)
                && (formUpdatedBody.getConfirmationMessage().getBody() != null)) {
                builder.confirmationMessage(
                    new ConfirmationMessage(
                        retrieveI18nText(formUpdatedBody.getConfirmationMessage().getHeader(), language),
                        retrieveI18nText(formUpdatedBody.getConfirmationMessage().getBody(), language)));
            }
            if (formUpdatedBody.getTimeoutErrorMessage() != null) {
                builder.timeoutErrorMessage(
                    new TimeoutErrorMessage(
                        retrieveI18nText(formUpdatedBody.getTimeoutErrorMessage().getHeader(), language),
                        retrieveI18nText(formUpdatedBody.getTimeoutErrorMessage().getBody(), language)));
            }
            if (formUpdatedBody.getLegalConditions() != null) {
                builder.legalConditions(
                    new LegalConditions(
                        retrieveI18nText(formUpdatedBody.getLegalConditions().getLink(), language),
                        retrieveI18nText(formUpdatedBody.getLegalConditions().getText(), language),
                        retrieveI18nText(formUpdatedBody.getLegalConditions().getLinkText(), language)));
            }
            if (formUpdatedBody.getGdpr() != null) {
                builder.gdpr(
                    new Gdpr(
                        retrieveI18nText(formUpdatedBody.getGdpr().getLink(), language),
                        retrieveI18nText(formUpdatedBody.getGdpr().getText(), language),
                        retrieveI18nText(formUpdatedBody.getGdpr().getLinkText(), language)));
            }
            if (formUpdatedBody.getSummarySentence() != null) {
                builder.summarySentence(
                    formUpdatedBody.summarySentence.stream().map(summaryText ->
                        new SummarySentence(
                            summaryText.getText() != null
                                ? retrieveI18nText(summaryText.getText(), language) : null,
                            summaryText.getQuestionId())).collect(Collectors.toList()));
            }
            if (formUpdatedBody.getSections() != null) {
                builder.sections(
                    formUpdatedBody.getSections().stream().map(section ->
                        Section.builder()
                            .sectionId(section.getId())
                            .text(retrieveI18nText(section.getText(), language))
                            .helpText(retrieveI18nText(section.getHelpText(), language))
                            .questions(retrieveQuestions(section.getQuestions(), language))
                            .type(section.getType())
                            .tabs(retrieveTabs(language, section))
                            .build())
                        .collect(Collectors.toList()));
            }
            return builder.build();
        }).collect(Collectors.toList());
    }

    private List<Tab> retrieveTabs(String language, JsonSection section) {
        List<Tab> result = null;
        if (section.getTabs() != null) {
            result = section.getTabs().stream().map(tab -> new Tab(
                retrieveI18nText(tab.getText(), language),
                retrieveQuestions(tab.getQuestions(), language))).
                collect(Collectors.toList());
        }
        return result;
    }

    private List<Question> retrieveQuestions(List<JsonQuestion> questions, String language) {
        List<Question> result = null;
        if (questions != null && !questions.isEmpty()) {
            result = questions.stream().map(question ->
                Question.builder().
                    id(question.getId()).
                    required(question.getRequired()).
                    type(QuestionType.valueOf(question.getType())).
                    subType(question.getSubType()).
                    text(retrieveI18nText(question.getText(), language)).
                    key(question.getKey()).
                    summary(question.getSummary()).
                    shareData(question.getShareData()).
                    label(retrieveI18nText(question.getLabel(), language)).
                    helpText(retrieveI18nText(question.getHelpText(), language)).
                    maxsize(question.getMaxsize()).
                    accept(question.getAccept()).
                    preferredCountries(question.getPreferredCountries()).
                    hint(question.getHint()).
                    choices(retrieveResponses(language, question)).
                    showExpanded(question.getShowExpanded()).
                    maxLength(question.getMaxLength()).
                    regex(question.getRegex()).
                    cols(question.getCols()).
                    maxResponses(question.getMaxResponses()).
                    errorMsg(retrieveI18nText(question.getErrorMsg(), language)).
                    disabled(question.getDisabled()).
                    clonesTo(question.getClonesTo()).
                    build()
            ).collect(Collectors.toList());
        }
        return result;
    }

    protected Responses retrieveResponses(String language, JsonQuestion question) {
        Responses result = null;
        if ((question.getResponses() != null)
            && (question.getResponses().getOptions() != null)) {
            result = new Responses(
                question.getResponses().getType(),
                question.getResponses().getOptions().stream().map(option -> new Option(
                    retrieveI18nText(option.text, language),
                    option.getValue()
                )).collect(Collectors.toList())
            );
        }
        return result;
    }

    private String retrieveI18nText(List<JsonI18n> i18nList, String language) {

        String result = null;

        if (i18nList != null) {
            Optional<JsonI18n> i18nOptional = i18nList.stream().filter(i18n ->
                i18n.getLang().toUpperCase().equals(language.toUpperCase())).
                findFirst();

            if (i18nOptional.isPresent()) {
                result = i18nOptional.get().getText();
            }
        }
        return result;
    }

}
