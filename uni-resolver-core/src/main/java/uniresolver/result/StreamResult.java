package uniresolver.result;

public interface StreamResult {

    public String getContentType();
    public void setContentType(String contentType);
    public byte[] getStream();
    public void setStream(byte[] stream);
}
