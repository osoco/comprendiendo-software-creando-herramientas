package es.osoco.bbva.ats.forms.domain.aggregate

import spock.lang.Specification
import spock.lang.Unroll

class AnswerSpec extends Specification {

    void 'is empty answer'() {
        expect:
        new Answer(null, [new Choice(null, null), new Choice(null, null)].toSet()).isEmpty()
    }

    @Unroll
    void 'is not empty answer'() {
        given:
        Answer answer = new Answer(label, [new Choice(choiceId, choiceLabel), new Choice(null, null)].toSet())

        expect:
        !answer.isEmpty()

        where:
        label   | choiceId   | choiceLabel
        'label' | null       | null
        null    | 'choiceId' | null
        null    | null       | 'choiceLabel'
    }
}
