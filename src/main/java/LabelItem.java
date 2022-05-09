import com.intellij.util.ui.JBUI;

import javax.swing.*;

public class LabelItem {
    private JPanel panel;
    private JLabel label;

    public void setSpace(int space){
        label.setBorder(JBUI.Borders.emptyLeft(space));
    }

    public void setLabel(String str){
        label.setText(str);
    }

    public JPanel getPanel(){
        return panel;
    }
}
