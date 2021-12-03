package uniresolver.result;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface StreamResult extends Result {

    /*
     * Content stream methods
     */

    public byte[] getFunctionContentStream();
    public void setFunctionContentStream(byte[] functionContentStream);

    /*
     * Content type methods
     */

    @JsonIgnore
    default public String getContentType() {
        return (String) this.getFunctionProcessMetadata().get("contentType");
    }

    @JsonIgnore
    default public void setContentType(String contentType) {
        if (contentType != null)
            this.getFunctionProcessMetadata().put("contentType", contentType);
        else
            this.getFunctionProcessMetadata().remove("contentType");
    }
}
