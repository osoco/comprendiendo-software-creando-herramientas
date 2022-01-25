package es.osoco.bbva.ats.forms.domain.service;

import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import es.osoco.bbva.ats.forms.domain.aggregate.EmailToken;
import es.osoco.bbva.ats.forms.domain.events.EmailTokenGenerated;
import es.osoco.bbva.ats.forms.domain.events.EmailTokenRequested;
import es.osoco.bbva.ats.forms.domain.exception.ApplicationNotFoundException;
import es.osoco.bbva.ats.forms.domain.repository.EmailTokenRepository;
import es.osoco.bbva.ats.forms.domain.util.RecoveryKeyGenerator;
import es.osoco.logging.Logging;
import es.osoco.logging.LoggingFactory;

import java.util.function.Consumer;
import java.util.Set;

public class EmailTokenRequestedService implements Consumer<EmailTokenRequested> {

    private final static EmailTokenRepository emailTokenRepository = EmailTokenRepository.getInstance();

    @Override
    public void accept(EmailTokenRequested emailTokenRequested) {
        final Logging logging = LoggingFactory.getInstance().createLogging();
        logging.info("Accept emailTokenRequested: " + emailTokenRequested);

        Set<String> previousKeys = emailTokenRepository.findKeys(emailTokenRequested.getApplicantKey());
        logging.info("Previous keys found to be deleted: " + previousKeys);

        previousKeys.stream().forEach(key -> emailTokenRepository.deleteKey(key));

        String newEmailKey = RecoveryKeyGenerator.generateKey();
        logging.info("Email key generated: " + newEmailKey);

        EmailToken newEmailToken = new EmailToken(
                    emailTokenRequested.getLanguage(),
                    emailTokenRequested.getContestId(),
                    emailTokenRequested.getApplicantKey(),
                    newEmailKey);
        emailTokenRepository.save(newEmailToken);

        new EmailTokenGenerated(newEmailToken).emit();
    }

}
