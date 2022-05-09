import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.lm.ArchitectContext;
import com.lm.FileUtilKt;

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

    public TemplateDialog(Frame frame, String fileName) {
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

        WriteCommandAction.runWriteCommandAction(ArchitectContext.project, () -> {
            PsiDirectory dir = FileUtilKt.getTemplateDir();
            if (dir == null) {
                dir = FileUtilKt.createTemplateDir();
            }
            if (dir != null) {
                PsiFile file = dir.findFile(fileName);
                if (file != null) {
                    final String text = FileUtilKt.readFile(file.getVirtualFile());
                    ApplicationManager.getApplication().invokeLater(() -> {
                        textArea.setText(text);
                    });
                }
            }
        });
    }

    private void onOK() {
        PsiDirectory dir = FileUtilKt.getTemplateDir();
        if (dir != null) {
            PsiFile file = dir.findFile(templateFileName);
            if (file != null) {
                WriteCommandAction.runWriteCommandAction(ArchitectContext.project, () -> {
                    FileUtilKt.writeToFile(file.getVirtualFile(), textArea.getText());
                });
            } else {
                WriteCommandAction.runWriteCommandAction(ArchitectContext.project, () -> {
                    FileUtilKt.createFile(dir, null, templateFileName, textArea.getText());
                });
            }
        }
        dispose();
    }

    private void onCancel() {
        dispose();
    }
}
