package uniresolver.result;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface Result {

    /*
     * Field methods
     */

    @JsonIgnore
    public Map<String, Object> getFunctionMetadata();

    @JsonIgnore
    public byte[] getFunctionContent() throws IOException;

    @JsonIgnore
    public Map<String, Object> getFunctionContentMetadata();

    /*
     * Serialization
     */

    @JsonIgnore
    public Map<String, Object> toMap();

    @JsonIgnore
    public String toJson();

    @JsonIgnore
    public URI getDefaultContext();

    @JsonIgnore
    public boolean isComplete();

    /*
     * Content type methods
     */

    @JsonIgnore
    default public String getContentType() {
        return (String) this.getFunctionMetadata().get("contentType");
    }

    @JsonIgnore
    default public void setContentType(String contentType) {
        if (contentType != null)
            this.getFunctionMetadata().put("contentType", contentType);
        else
            this.getFunctionMetadata().remove("contentType");
    }

    /*
     * Error methods
     */

    @JsonIgnore
    default public boolean isErrorResult() {
        return this.getError() != null;
    }

    @JsonIgnore
    default public String getError() {
        if (this.getFunctionMetadata() == null) throw new NullPointerException();
        return (String) this.getFunctionMetadata().get("error");
    }

    @JsonIgnore
    default public void setError(String error) {
        if (this.getFunctionMetadata() == null) throw new NullPointerException();
        if (error != null)
            this.getFunctionMetadata().put("error", error);
        else
            this.getFunctionMetadata().remove("error");
    }

    @JsonIgnore
    default public String getErrorMessage() {
        if (this.getFunctionMetadata() == null) throw new NullPointerException();
        return (String) this.getFunctionMetadata().get("errorMessage");
    }

    @JsonIgnore
    default public void setErrorMessage(String error) {
        if (this.getFunctionMetadata() == null) throw new NullPointerException();
        if (error != null)
            this.getFunctionMetadata().put("errorMessage", error);
        else
            this.getFunctionMetadata().remove("errorMessage");
    }

    /*
     * Problem/warning methods
     */

    @JsonIgnore
    default public boolean hasWarnings() {
        return this.getWarnings() != null && !this.getWarnings().isEmpty();
    }

    @JsonIgnore
    default public List<Map<String, Object>> getWarnings() {
        return this.getFunctionMetadata() == null ? null : (List<Map<String, Object>>) this.getFunctionMetadata().get("warnings");
    }

    @JsonIgnore
    default public void addWarning(String message, Map<String, Object> warningMetadata) {
        if (message == null) throw new NullPointerException();
        if (this.getFunctionMetadata() == null) throw new NullPointerException();
        List<Map<String, Object>> warnings = (List<Map<String, Object>>) this.getFunctionMetadata().get("warnings");
        if (warnings == null) { warnings = new ArrayList<>(); this.getFunctionMetadata().put("warnings", warnings); }
        Map<String, Object> warning = new LinkedHashMap<>();
        if (message != null) warning.put("message", message);
        if (warningMetadata != null) warning.putAll(warningMetadata);
        warnings.add(warning);
    }

    @JsonIgnore
    default public void addWarning(String message) {
        this.addWarning(message, null);
    }
}
