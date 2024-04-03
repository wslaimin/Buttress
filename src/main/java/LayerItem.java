import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;

public class LayerItem {
    private JPanel itemPanel;
    private JCheckBox checkBox;
    private JLabel link;

    public void setLabel(String label){
        link.setText(label);
    }

    public void setSpace(int space){
        checkBox.setBorder(JBUI.Borders.emptyLeft(space));
    }

    public JPanel getPanel(){
        return itemPanel;
    }

    public void setSelected(boolean bool){
        checkBox.setSelected(bool);
    }

    public void setCheckChangeListener(ItemListener listener){
        checkBox.addItemListener(listener);
    }

    public void setLinkClickListener(MouseAdapter mouseAdapter){
        link.addMouseListener(mouseAdapter);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}