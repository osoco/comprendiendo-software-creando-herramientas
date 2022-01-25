package utils


import es.osoco.bbva.ats.forms.adapter.json.common.JsonMetaAggregate
import es.osoco.bbva.ats.forms.adapter.json.formupdated.JsonBody
import es.osoco.bbva.ats.forms.adapter.json.formupdated.JsonCheckbox
import es.osoco.bbva.ats.forms.adapter.json.formupdated.JsonConfirmationMessage
import es.osoco.bbva.ats.forms.adapter.json.formupdated.JsonFormUpdated
import es.osoco.bbva.ats.forms.adapter.json.formupdated.JsonGdpr
import es.osoco.bbva.ats.forms.adapter.json.formupdated.JsonHelpText
import es.osoco.bbva.ats.forms.adapter.json.formupdated.JsonI18n
import es.osoco.bbva.ats.forms.adapter.json.formupdated.JsonLegalConditions
import es.osoco.bbva.ats.forms.adapter.json.formupdated.JsonOption
import es.osoco.bbva.ats.forms.adapter.json.formupdated.JsonQuestion
import es.osoco.bbva.ats.forms.adapter.json.formupdated.JsonRecoveryZone
import es.osoco.bbva.ats.forms.adapter.json.formupdated.JsonResponses
import es.osoco.bbva.ats.forms.adapter.json.formupdated.JsonSection
import es.osoco.bbva.ats.forms.adapter.json.formupdated.JsonSummarySentence
import es.osoco.bbva.ats.forms.adapter.json.formupdated.JsonTab
import es.osoco.bbva.ats.forms.adapter.json.formupdated.JsonTimeoutErrorMessage
import es.osoco.bbva.ats.forms.application.parser.externaltodomain.FormParser
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

FormParser formParser = FormParser.getInstance()
File formFile = new File('/home/jripoll/osoco/bbva/BBVA-ATS-Forms/src/test/resources/form.ot19.json')

JsonSlurper jsonSlurper = new JsonSlurper()
Map map = jsonSlurper.parseText(formFile.text)
JsonFormUpdated jsonFormUpdated = new JsonFormUpdated(
    new JsonMetaAggregate(map.meta),
    new JsonBody(
        map.body.id,
        map.body.version,
        map.body.contestId,
        map.body.contestKey,
        map.body.formKey,
        map.body.contestName,
        new JsonLegalConditions(
            map.body.legalConditions.link.collect { new JsonI18n(it) },
            map.body.legalConditions.text.collect { new JsonI18n(it) },
            map.body.legalConditions.linkText.collect { new JsonI18n(it) },
        ),
        map.body.canSaveDraft,
        map.body.languages,
        map.body.defaultLanguage,
        map.body.showAllowShareDataCheck,
        new JsonRecoveryZone(
            map.body.recoveryZone.button.collect { new JsonI18n(it) },
            map.body.recoveryZone.message.collect { new JsonI18n(it) },
            map.body.recoveryZone.title.collect { new JsonI18n(it) },
            new JsonCheckbox(
                map.body.recoveryZone.checkbox.helpText.collect { new JsonI18n(it) },
                map.body.recoveryZone.checkbox.label.collect { new JsonI18n(it) },
                map.body.recoveryZone.checkbox.errorLabel.collect { new JsonI18n(it) },
            )
        ),
        map.body.sendingDataMessage.collect { new JsonI18n(it) },
        new JsonConfirmationMessage(
            map.body.confirmationMessage.header.collect { new JsonI18n(it) },
            map.body.confirmationMessage.body.collect { new JsonI18n(it) },
        ),
        new JsonTimeoutErrorMessage(
            map.body.timeoutErrorMessage.header.collect { new JsonI18n(it) },
            map.body.timeoutErrorMessage.body.collect { new JsonI18n(it) },
        ),
        new JsonGdpr(
            map.body.gdpr.link.collect { new JsonI18n(it) },
            map.body.gdpr.text.collect { new JsonI18n(it) },
            map.body.gdpr.linkText.collect { new JsonI18n(it) },
        ),
        map.body.summarySentence.collect { summarySentence ->
            new JsonSummarySentence(
                summarySentence.questionId,
                summarySentence.text.collect { new JsonI18n(it) },
            )
        },
        map.body.sections.collect { section ->
            new JsonSection(
                section.id,
                section.type,
                section.text.collect { new JsonI18n(it) },
                section.questions.collect { question ->
                    new JsonQuestion(
                        question.id,
                        question.type,
                        question.required,
                        question.key,
                        question.subType,
                        question.maxsize,
                        question.text.collect { new JsonI18n(it) },
                        question.helpText.collect { new JsonI18n(it) },
                        question.responses ?
                            new JsonResponses(
                                question.responses.type,
                                question.responses.options.collect { option ->
                                    new JsonOption(
                                        option.text.collect { new JsonI18n(it) },
                                        option.value
                                    )
                                },
                            ) : null,
                        question.maxResponses,
                        question.regex,
                        question.errorMsg.collect { new JsonI18n(it) },
                        question.hint,
                        question.accept,
                        question.preferredCountries,
                        question.maxLength,
                        question.disabled,
                        question.showExpanded,
                        question.summary,
                        question.shareData,
                        question.label.collect { new JsonI18n(it) },
                        question.cols,
                        question.clonesTo,
                    )
                },
                section.helpText.collect { new JsonI18n(it) },
                section.tabs.collect { tab ->
                    new JsonTab(
                        tab.text.collect { new JsonI18n(it)},
                        tab.helpText.collect {
                            new JsonHelpText(it.lang, it.text)
                        },
                        tab.questions.collect { question ->
                            new JsonQuestion(
                                question.id,
                                question.type,
                                question.required,
                                question.key,
                                question.subType,
                                question.maxsize,
                                question.text.collect { new JsonI18n(it) },
                                question.helpText.collect { new JsonI18n(it) },
                                question.responses ?
                                    new JsonResponses(
                                        question.responses.type,
                                        question.responses.options.collect { option ->
                                            new JsonOption(
                                                option.text.collect { new JsonI18n(it) },
                                                option.value
                                            )
                                        },
                                    ) : null,
                                question.maxResponses,
                                question.regex,
                                question.errorMsg.collect { new JsonI18n(it) },
                                question.hint,
                                question.accept,
                                question.preferredCountries,
                                question.maxLength,
                                question.disabled,
                                question.showExpanded,
                                question.summary,
                                question.shareData,
                                question.label.collect { new JsonI18n(it) },
                                question.cols,
                                question.clonesTo,
                            )
                        },
                    )
                },
            )

        },
    )
)


println JsonOutput.toJson(formParser.createForms(jsonFormUpdated))
map
