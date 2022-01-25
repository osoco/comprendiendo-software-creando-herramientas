package es.osoco.bbva.ats.forms.domain.repository;

import es.osoco.bbva.ats.forms.domain.aggregate.EmailToken;
import es.osoco.bbva.ats.forms.domain.ports.storage.AggregateStore;
import es.osoco.logging.Logging;

import java.util.Set;

public class EmailTokenRepository implements Repository<EmailToken> {

    @Override
    public EmailToken byID(String id) {
        return (EmailToken) aggregateStore.findByKey(id);
    }

    @Override
    public void deleteKey(String key) {
        aggregateStore.deleteKey(key);
    }

    @Override
    public Set<String> findKeys(String string) {
        return aggregateStore.findKeys(string);
    }

    @Override
    public void save(EmailToken emailToken) {
        aggregateStore.save(emailToken);
    }

    @Override
    public void setAggregateStore(AggregateStore aggregateStore) {
        this.aggregateStore = aggregateStore;
    }

    @Override
    public void setLogging(Logging logging) {
        this.logging = logging;
    }

    public String verifiedEmailByToken(String emailToken) {
        String verifiedEmail = null;
        if (emailToken != null) {
            Set<String> emailTokenKeys = this.findKeys(emailToken);
            if (emailTokenKeys.size() > 0) {
                if (emailTokenKeys.size() > 1) {
                    logging.warn("Email token [" + emailToken + "] found " + emailTokenKeys.size() + " times");
                }
                String emailTokenFullKey = emailTokenKeys.iterator().next();
                String [] emailTokenFullKeyParts = emailTokenFullKey.split(":");
                String emailTokenKey = emailTokenFullKeyParts[1] + ":" + emailTokenFullKeyParts[2];
                EmailToken emailTokenFound = this.byID(emailTokenKey);
                if (emailTokenFound != null) {
                    verifiedEmail = emailTokenFound.getApplicantKey();
                }
            }
        }
        return verifiedEmail;
    }

    public static EmailTokenRepository getInstance() {
        if (EmailTokenRepository.INSTANCE == null) {
            EmailTokenRepository.build();
        }
        return EmailTokenRepository.INSTANCE;
    }

    private static void build() {
        EmailTokenRepository.INSTANCE = new EmailTokenRepository();
    }

    private static EmailTokenRepository INSTANCE;

    private Logging logging;
    private AggregateStore aggregateStore;

    private EmailTokenRepository(){
    }
}
