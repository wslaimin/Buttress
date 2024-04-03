import com.intellij.openapi.vfs.VirtualFile;
import com.lm.FileUtilKt;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class TemplateDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea textArea;
    private final String templateFileName;

    public TemplateDialog(Frame frame,@NotNull String fileName) {
        super(frame);
        templateFileName = fileName;
        setSize(600, 400);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        VirtualFile templateDir=FileUtilKt.getTemplateDir();
        if(templateDir!=null){
            VirtualFile file=templateDir.findChild(templateFileName);
            if(file!=null){
                textArea.setText(FileUtilKt.readFile(file));
            }
        }
    }

    private void onOK() {
        VirtualFile templateDir=FileUtilKt.getTemplateDir();
        if(templateDir==null){
            templateDir=FileUtilKt.getTemplateDir();
        }
        VirtualFile file=templateDir.findChild(templateFileName);
        if(file==null){
            file=FileUtilKt.createFile(templateDir,templateFileName);
        }
        FileUtilKt.writeToFile(file,textArea.getText());
        dispose();
    }

    private void onCancel() {
        dispose();
    }
}
