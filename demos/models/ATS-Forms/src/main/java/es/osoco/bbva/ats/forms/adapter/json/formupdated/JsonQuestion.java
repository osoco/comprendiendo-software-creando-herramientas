
package es.osoco.bbva.ats.forms.adapter.json.formupdated;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonQuestion {

    public String id;
    public String type;
    public Boolean required;
    public String key;
    public String subType;
    public Integer maxsize;
    public List<JsonI18n> text = null;
    public List<JsonI18n> helpText = null;
    public JsonResponses responses;
    public Integer maxResponses;
    public String regex;
    public List<JsonI18n> errorMsg;
    public String hint;
    public List<String> accept;
    public List<String> preferredCountries;
    public Integer maxLength;
    public Boolean disabled;
    public Boolean showExpanded;
    public Boolean summary;
    public Boolean shareData;
    public List<JsonI18n> label;
    public Integer cols;
    public String clonesTo;
}
