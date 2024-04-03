import com.intellij.util.ui.JBUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;

public class FileItem {
    private JPanel panel;
    private JLabel lb;

    public FileItem(String name,boolean fileType){
        InputStream inputStream;

        if(fileType){
            inputStream=getClass().getResourceAsStream("/icons/file.png");
        }else{
            inputStream=getClass().getResourceAsStream("/icons/directory.png");
        }
        if(inputStream!=null) {
            try {
                ImageIcon icon = new ImageIcon(ImageIO.read(inputStream));
                lb.setIcon(icon);
                lb.setText(name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setSpace(int space){
        lb.setBorder(JBUI.Borders.emptyLeft(space));
    }

    public JPanel getPanel(){
        return panel;
    }

    public void setPanel(JPanel panel) {
        this.panel = panel;
    }

    public JLabel getLb() {
        return lb;
    }

    public void setLb(JLabel lb) {
        this.lb = lb;
    }
}
