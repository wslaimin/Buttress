import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

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

    public void setCheckChangeListener(MouseListener listener){
        checkBox.addMouseListener(listener);
    }
}

interface CheckChangeListener{
    void onChanged(boolean checked);
}
