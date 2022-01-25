package es.osoco.bbva.ats.forms.domain.repository;

import com.amazonaws.util.IOUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import es.osoco.bbva.ats.forms.domain.exception.InvalidMappingFileLocationException;
import es.osoco.bbva.ats.forms.domain.exception.MappingContestErrorException;
import es.osoco.bbva.ats.forms.domain.exception.MappingContestFileNotFoundException;
import es.osoco.bbva.ats.forms.domain.valueobject.ContestMapping;
import es.osoco.bbva.ats.forms.domain.valueobject.QuestionMapping;
import es.osoco.logging.Logging;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;


public class ConverterContestFileRepository {

    private static final String S3_CONVERT_CONTEST_URL = System.getenv("S3_CONVERT_CONTEST_URL");

    public Map<String, QuestionMapping> getAnswersConvertMapping(String originContestId, String targetContestId) {
        try {
            Type type = new TypeToken<ContestMapping>(){}.getType();
            Gson gson = new GsonBuilder().create();
            String jsonConverterUrl = S3_CONVERT_CONTEST_URL + originContestId + "/" + targetContestId + ".json";
            InputStream urlInputStream = new URL(jsonConverterUrl ).openStream();
            String mappingJson = IOUtils.toString(urlInputStream);
            return ((ContestMapping) gson.fromJson(mappingJson, type)).getQuestions();
        } catch (MalformedURLException e) {
            throw new InvalidMappingFileLocationException();
        } catch (IOException e){
            throw new MappingContestFileNotFoundException();
        } catch (Exception e) {
            throw new MappingContestErrorException(e.getMessage(), e.getCause());
        }
    }

    public void setLogging(Logging logging) {
        this.logging = logging;
    }

    public static ConverterContestFileRepository getInstance() {
        if (ConverterContestFileRepository.INSTANCE == null) {
            ConverterContestFileRepository.build();
        }
        return ConverterContestFileRepository.INSTANCE;
    }

    private static void build() {
        ConverterContestFileRepository.INSTANCE = new ConverterContestFileRepository();
    }

    private static ConverterContestFileRepository INSTANCE;

    private Logging logging;

    private ConverterContestFileRepository(){
    }
}
