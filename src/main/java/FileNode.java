import java.util.List;

public class FileNode {
    private String name;
    private int deep;
    private List<FileNode> nodes;

    public FileNode(String name, int deep) {
        this.name = name;
        this.deep = deep;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDeep() {
        return deep;
    }

    public void setDeep(int deep) {
        this.deep = deep;
    }

    public List<FileNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<FileNode> nodes) {
        this.nodes = nodes;
    }
}
