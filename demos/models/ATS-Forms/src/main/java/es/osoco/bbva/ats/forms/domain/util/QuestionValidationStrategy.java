package es.osoco.bbva.ats.forms.domain.util;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import es.osoco.bbva.ats.forms.domain.aggregate.Answer;
import es.osoco.bbva.ats.forms.domain.aggregate.form.QuestionType;
import org.apache.commons.validator.routines.AbstractNumberValidator;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.LongValidator;
import org.apache.commons.validator.routines.UrlValidator;

import java.util.regex.Pattern;


public enum QuestionValidationStrategy implements ValidationStrategy {
    TEXT (QuestionType.TEXT) {
        @Override
        public boolean validate(Answer answer) {
            return answer.getChoices() == null;
        }
    },
    LONGTEXT (QuestionType.LONGTEXT) {
        @Override
        public boolean validate(Answer answer) {
            return answer.getChoices() == null;
        }
    },
    SELECT (QuestionType.SELECT) {
        public boolean validate(Answer answer) {
            return answer.getChoices() == null ||  answer.getChoices().size() <=1;
        }
    },
    LINK (QuestionType.LINK) {
        private final Pattern linkRegex = Pattern.compile("(?:(?:(?:[a-zA-Z]+:)?//)?|www\\.|WWW\\.)(?:\\S+(?::\\S*)?@)?(?:localhost|(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]\\d|\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]\\d|\\d)){3}|(?:(?:[a-zA-Z\\u00a1-\\uffff0-9]-*)*[a-zA-Z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-zA-Z\\u00a1-\\uffff0-9]-*)*[a-zA-Z\\u00a1-\\uffff0-9]+)*(?:\\.(?:.*))\\.?)(?::\\d{2,5})?(?:[/?#][^\\s\"]*)?");
        public boolean validate(Answer answer) {
            return linkRegex.matcher(answer.getText()).matches() && answer.getChoices() == null;
        }
    },
    NUMBER (QuestionType.NUMBER) {
        public boolean validate(Answer answer) {
            AbstractNumberValidator abstractNumberValidator = new LongValidator();
            return abstractNumberValidator.isValid(answer.getText()) && answer.getChoices() == null;
        }
    },
    DATE (QuestionType.DATE) {
        public boolean validate(Answer answer) {
            //TODO ISO FORMAT date validator
            //DateValidator dateValidator = new DateValidator(false, DateFormat.FULL);
            //return dateValidator.isValid(answer.getText()) && answer.getChoices() == null;
            return true;
        }
    },
    EMAIL (QuestionType.EMAIL) {
        public boolean validate(Answer answer) {
            EmailValidator emailValidator =EmailValidator.getInstance();
            return emailValidator.isValid(answer.getText()) && answer.getChoices() == null;
        }
    },
    COUNTRY (QuestionType.COUNTRY) {
        public boolean validate(Answer answer) {
            return answer.getChoices() == null  || answer.getChoices().size() <=1;
        }
    },
    MULTISELECT (QuestionType.MULTISELECT) {
        public boolean validate(Answer answer) {
            return true;
        }
    },
    FILE (QuestionType.FILE) {
        public boolean validate(Answer answer) {
            UrlValidator urlValidator = new UrlValidator();
            return urlValidator.isValid(answer.getText()) && answer.getChoices() == null;
        }
    },
    REQUIRED () {
        public boolean validate(Answer answer) {
            return answer.getChoices()!= null && !answer.getChoices().toArray()[0].equals("")  || answer.getText() != null;
        }
    },
    PHONE (QuestionType.PHONE) {
        public boolean validate(Answer answer) {
            try {
                PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
                Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(answer.getText(), "");
                return phoneNumberUtil.isValidNumber(phoneNumber);
            } catch (NumberParseException e) {
                return false;
            }
        }
    },
    HEADER (QuestionType.HEADER){
        @Override
        public boolean validate(Answer input) {
            return true;
        }
    },
    TAGS (QuestionType.TAGS) {
        @Override
        public boolean validate(Answer input) {
            return true;
        }
    };

    private QuestionType questionType;

    private QuestionValidationStrategy(QuestionType questionType) {
        this.questionType = questionType;
    }

    private QuestionValidationStrategy() {
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public static QuestionValidationStrategy valueByQuestionType(QuestionType questionType) {
        for (QuestionValidationStrategy validationStrategy : values()) {
            if (validationStrategy.questionType == questionType) {
                return validationStrategy;
            }
        }
        throw new IllegalArgumentException(questionType.name());
    }
}
