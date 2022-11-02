package uniresolver.result;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface StreamResult extends Result {

    /*
     * Content stream methods
     */

    @JsonIgnore
    public byte[] getFunctionContentStream();

    @JsonIgnore
    public void setFunctionContentStream(byte[] functionContentStream);

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
}
