package app;

import java.io.Serializable;

public class MetaFile implements Serializable {
    private static final long serialVersionUID = 135677L;
    private String path;
    private int ownerPort;
    private boolean isPublic;

    public MetaFile(String path, int ownerPort, boolean isPublic) {
        this.path = path;
        this.ownerPort = ownerPort;
        this.isPublic = isPublic;
    }

    public String getPath() {
        return path;
    }

    public int getOwnerPort() {
        return ownerPort;
    }

    public boolean isPublic() {
        return isPublic;
    }
}
