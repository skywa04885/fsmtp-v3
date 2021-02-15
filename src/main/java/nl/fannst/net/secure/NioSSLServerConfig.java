package nl.fannst.net.secure;

public class NioSSLServerConfig {
    private String m_ServerKeyFile;
    private String m_ServerStorePass;
    private String m_ServerKeyPass;

    private String m_TrustFile;
    private String m_TrustStorePass;


    public void setServerKeyFile(String file) {
        m_ServerKeyFile = file;
    }

    public void setServerStorePass(String pass) {
        m_ServerStorePass = pass;
    }

    public void setServerKeyPass(String pass) {
        m_ServerKeyPass = pass;
    }

    public void setTrustFile(String file) {
        m_TrustFile = file;
    }

    public void setTrustStorePass(String pass) {
        m_TrustStorePass = pass;
    }


    public String getServerKeyFile() {
        return m_ServerKeyFile;
    }

    public String getServerStorePass() {
        return m_ServerStorePass;
    }

    public String getServerKeyPass() {
        return m_ServerKeyPass;
    }

    public String getTrustFile() {
        return m_TrustFile;
    }

    public String getTrustStorePass() {
        return m_TrustStorePass;
    }
}
