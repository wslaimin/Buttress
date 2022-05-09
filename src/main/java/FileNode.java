import java.util.List;

public class FileNode {
    private String name;
    private List<FileNode> nodes;

    public FileNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<FileNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<FileNode> nodes) {
        this.nodes = nodes;
    }
}
