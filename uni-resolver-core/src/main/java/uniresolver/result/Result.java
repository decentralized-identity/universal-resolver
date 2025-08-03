package uniresolver.result;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.IOException;
import java.net.URI;
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
        return this.getFunctionMetadata().containsKey("error");
    }

    @JsonIgnore
    default public String getErrorType() {
        if (this.getFunctionMetadata() == null) throw new NullPointerException();
        Map<String, Object> error = (Map<String, Object>) this.getFunctionMetadata().get("error");
        if (error == null) return null;
        return (String) error.get("type");
    }

    @JsonIgnore
    default public String getErrorTitle() {
        if (this.getFunctionMetadata() == null) throw new NullPointerException();
        Map<String, Object> error = (Map<String, Object>) this.getFunctionMetadata().get("error");
        if (error == null) return null;
        return (String) error.get("title");
    }

    @JsonIgnore
    default public String getErrorDetail() {
        if (this.getFunctionMetadata() == null) throw new NullPointerException();
        Map<String, Object> error = (Map<String, Object>) this.getFunctionMetadata().get("error");
        if (error == null) return null;
        return (String) error.get("detail");
    }

    @JsonIgnore
    default public Map<String, Object> getErrorMetadata() {
        if (this.getFunctionMetadata() == null) throw new NullPointerException();
        Map<String, Object> error = (Map<String, Object>) this.getFunctionMetadata().get("error");
        if (error == null) return null;
        Map<String, Object> errorMetadata = new LinkedHashMap<>(error);
        errorMetadata.remove("type");
        errorMetadata.remove("title");
        errorMetadata.remove("detail");
        return errorMetadata;
    }

    @JsonIgnore
    default public void setError(String errorType, String errorTitle) {
        if (this.getFunctionMetadata() == null) throw new NullPointerException();
        Map<String, Object> error = (Map<String, Object>) this.getFunctionMetadata().computeIfAbsent("error", k -> new LinkedHashMap<>());
        if (errorType != null) error.put("type", errorType); else error.remove("type");
        if (errorTitle != null) error.put("title", errorTitle); else error.remove("title");
    }

    @JsonIgnore
    default public void setErrorDetail(String errorDetail) {
        if (this.getFunctionMetadata() == null) throw new NullPointerException();
        Map<String, Object> error = (Map<String, Object>) this.getFunctionMetadata().computeIfAbsent("error", k -> new LinkedHashMap<>());
        if (errorDetail != null) error.put("detail", errorDetail); else error.remove("detail");
    }

    @JsonIgnore
    default public void setErrorMetadata(Map<String, Object> errorMetadata) {
        if (this.getFunctionMetadata() == null) throw new NullPointerException();
        Map<String, Object> error = (Map<String, Object>) this.getFunctionMetadata().computeIfAbsent("error", k -> new LinkedHashMap<>());
        if (errorMetadata != null) error.putAll(errorMetadata);
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
    default public void addWarning(String warningType, String warningTitle, String warningDetail, Map<String, Object> warningMetadata) {
        if (warningType == null) throw new NullPointerException();
        if (warningTitle == null) throw new NullPointerException();
        if (this.getFunctionMetadata() == null) throw new NullPointerException();
        List<Map<String, Object>> warnings = (List<Map<String, Object>>) this.getFunctionMetadata().computeIfAbsent("warnings", k -> new LinkedHashMap<>());
        Map<String, Object> warning = new LinkedHashMap<>();
        warning.put("type", warningType);
        warning.put("title", warningTitle);
        if (warningDetail != null) warning.put("detail", warningDetail);
        if (warningMetadata != null) warning.putAll(warningMetadata);
        warnings.add(warning);
    }
}
