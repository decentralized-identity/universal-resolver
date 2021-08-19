package uniresolver.result;

public interface StreamResult extends Result {

    public String getContentType();
    public void setContentType(String contentType);
    public byte[] getContentStream();
    public void setContentStream(byte[] stream);
}
