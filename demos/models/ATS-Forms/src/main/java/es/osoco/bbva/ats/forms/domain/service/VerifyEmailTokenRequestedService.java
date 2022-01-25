package es.osoco.bbva.ats.forms.domain.service;

import es.osoco.bbva.ats.forms.domain.aggregate.EmailToken;
import es.osoco.bbva.ats.forms.domain.events.EmailTokenVerified;
import es.osoco.bbva.ats.forms.domain.events.VerifyEmailTokenRequested;
import es.osoco.bbva.ats.forms.domain.exception.EmailTokenNotFoundException;
import es.osoco.bbva.ats.forms.domain.repository.EmailTokenRepository;
import es.osoco.logging.Logging;
import es.osoco.logging.LoggingFactory;

import java.util.function.Consumer;
import java.util.Set;

public class VerifyEmailTokenRequestedService implements Consumer<VerifyEmailTokenRequested> {

    private final static EmailTokenRepository emailTokenRepository = EmailTokenRepository.getInstance();

    @Override
    public void accept(VerifyEmailTokenRequested verifyEmailTokenRequested) {
        final Logging logging = LoggingFactory.getInstance().createLogging();
        String emailToken = verifyEmailTokenRequested.getEmailToken();

        String verifiedEmail = emailTokenRepository.
            verifiedEmailByToken(verifyEmailTokenRequested.getEmailToken());

        if (verifiedEmail == null) {
            throw new EmailTokenNotFoundException(emailToken);
        } else {
            new EmailTokenVerified(verifiedEmail).emit();
        }
    }

}
