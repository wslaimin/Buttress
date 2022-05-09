import javax.swing.*;
import java.util.List;

public class ListModel<T> extends AbstractListModel<T> {
    private List<T> nodes;

    public ListModel(List<T> nodes){
        this.nodes=nodes;
    }

    @Override
    public int getSize() {
        return nodes.size();
    }

    @Override
    public T getElementAt(int index) {
        return nodes.get(index);
    }
}
