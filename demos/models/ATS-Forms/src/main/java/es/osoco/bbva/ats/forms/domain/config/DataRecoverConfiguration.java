package es.osoco.bbva.ats.forms.domain.config;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataRecoverConfiguration {

    private static final String OMP = "80cffcea-3f29-0d00-ac17-bbeb0f1d3b77";
    private static final String OT19 = "e2644837-7a42-0d00-98df-937900b12e3b";

    private static final Map<String, List<String>> configuration = Stream.of(
        new AbstractMap.SimpleImmutableEntry<>(OMP, Collections.singletonList(OT19)),
        new AbstractMap.SimpleImmutableEntry<>(OT19, Collections.singletonList(OMP))
    ).collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    public static List<String> contestsAllowed(String contestId) {
        List<String> configured = configuration.get(contestId);
        List<String> result = new ArrayList<>(Collections.singletonList(contestId));
        if (configured != null) {
            result.addAll(configuration.get(contestId));
        }
        return result;
    }

}
