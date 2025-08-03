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
    default public String getErrorType() {
        if (this.getFunctionMetadata() == null) throw new NullPointerException();
        Map<String, Object> error = (Map<String, Object>) this.getFunctionMetadata().get("error");
        return error == null ? null : (String) error.get("type");
    }

    @JsonIgnore
    default public String getErrorTitle() {
        if (this.getFunctionMetadata() == null) throw new NullPointerException();
        Map<String, Object> error = (Map<String, Object>) this.getFunctionMetadata().get("error");
        return error == null ? null : (String) error.get("title");
    }

    @JsonIgnore
    default public void setError(String errorType, String errorTitle) {
        if (this.getFunctionMetadata() == null) throw new NullPointerException();
        Map<String, Object> error = (Map<String, Object>) this.getFunctionMetadata().computeIfAbsent("error", k -> new LinkedHashMap<>());
        if (errorType != null) error.put("type", errorType); else error.remove("type");
        if (errorTitle != null) error.put("title", errorTitle); else error.remove("title");
    }

    @JsonIgnore
    default public String getErrorDetail() {
        if (this.getFunctionMetadata() == null) throw new NullPointerException();
        Map<String, Object> error = (Map<String, Object>) this.getFunctionMetadata().get("error");
        return error == null ? null : (String) error.get("detail");
    }

    @JsonIgnore
    default public void setErrorDetail(String errorDetail) {
        if (this.getFunctionMetadata() == null) throw new NullPointerException();
        Map<String, Object> error = (Map<String, Object>) this.getFunctionMetadata().computeIfAbsent("error", k -> new LinkedHashMap<>());
        if (errorDetail != null) error.put("type", errorDetail); else error.remove("detail");
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
