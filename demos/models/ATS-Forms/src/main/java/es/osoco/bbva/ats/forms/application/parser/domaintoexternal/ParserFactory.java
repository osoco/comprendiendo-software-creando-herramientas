package es.osoco.bbva.ats.forms.application.parser.domaintoexternal;

import es.osoco.bbva.ats.forms.application.parser.Parser;
import es.osoco.bbva.ats.forms.domain.events.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ParserFactory<T> {

    private static final Map<String, Parser> factoryParserByDomainEvent = new HashMap<>();

    static {
        factoryParserByDomainEvent.put(ApplicationSubmitted.class.getSimpleName(), ApplicationDomainEventParser.getInstance());
        factoryParserByDomainEvent.put(PartialApplicationRecovered.class.getSimpleName(), PartialApplicationRecoveredParser.getInstance());
        factoryParserByDomainEvent.put(ApplicationStored.class.getSimpleName(), ApplicationDomainEventParser.getInstance());
        factoryParserByDomainEvent.put(ApplicationDraftStored.class.getSimpleName(), ApplicationDomainEventParser.getInstance());
        factoryParserByDomainEvent.put(ApplicationUpdateStored.class.getSimpleName(), ApplicationUpdatedDomainEventParser.getInstance());
        factoryParserByDomainEvent.put(RecoveryTokenGenerated.class.getSimpleName(), NewTokenGeneratedEventParser.getInstance());
        factoryParserByDomainEvent.put(EmailTokenGenerated.class.getSimpleName(), EmailTokenGeneratedParser.getInstance());
        factoryParserByDomainEvent.put(GenericTokenGenerated.class.getSimpleName(), GenericTokenGeneratedEventParser.getInstance());
        factoryParserByDomainEvent.put(EntityRecovered.class.getSimpleName(), EntityRecoveredParser.getInstance());
        factoryParserByDomainEvent.put(EntitySubmitted.class.getSimpleName(), ApplicationDomainEventParser.getInstance());
        factoryParserByDomainEvent.put(EntityStored.class.getSimpleName(), ApplicationDomainEventParser.getInstance());
        factoryParserByDomainEvent.put(GenericTokenWithNoRecoverPermitRequested.class.getSimpleName(), GenericTokenWithNoRecoverPermitRequestedParser.getInstance());
        factoryParserByDomainEvent.put(GenericTokenForNonexistentApplicantRequested.class.getSimpleName(), GenericTokenForNonexistentApplicantRequestedParser.getInstance());
    }

    public static ParserFactory getInstance() {
        return new ParserFactory();
    }

    public Optional<Parser> getParser(T event) {
        return Optional.ofNullable(factoryParserByDomainEvent.get(event.getClass().getSimpleName()));
    }
}
