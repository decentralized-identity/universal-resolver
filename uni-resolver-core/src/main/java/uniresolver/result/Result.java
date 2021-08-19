package uniresolver.result;

import java.util.Map;

public interface Result {

    /*
     * Serialization
     */

    public Map<String, Object> toMap();
    public String toJson();

    /*
     * Error methods
     */

    public boolean isErrorResult();
    public String getError();
    public void setError(String error);
    public String getErrorMessage();
    public void setErrorMessage(String errorMessage);
}
