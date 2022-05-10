import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.lm.ArchitectContext;
import com.lm.FileUtilKt;
import com.lm.FlatFileNode;
import com.lm.FlatItemNode;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.NullLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static javax.swing.ScrollPaneConstants.*;

public class MainPanel extends JFrame {
    private JPanel panel;
    private JList viewList;
    private JList operationList;
    private JButton button;
    private JTextField textField;
    private JSplitPane splitPane;
    private List<FlatItemNode> checkedList;
    private PsiDirectory directory;
    private String language;

    public MainPanel(PsiDirectory directory) {
        initSplitPane();
        this.directory = directory;
        setContentPane(panel);
        checkedList = new ArrayList<>();

        PlainDocument document = (PlainDocument) textField.getDocument();
        document.setDocumentFilter(new ClassNameFilter());
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setViewList(convertViewList());
                enableButton(textField.getText().length() != 0);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setViewList(convertViewList());
                enableButton(textField.getText().length() != 0);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setViewList(convertViewList());
                enableButton(textField.getText().length() != 0);
            }
        });

        button.setEnabled(false);

        button.addActionListener(e -> {
            final String name = textField.getText();
            checkedList.forEach(item -> {
                WriteCommandAction.runWriteCommandAction(ArchitectContext.project, () -> {
                    String path = FileUtilKt.trimToRelativePath(item.getDir());
                    String suffix = language.isEmpty() ? "" : "." + language;
                    String fileName = name + StringUtils.capitalize(item.getName());

                    String text = null;
                    PsiDirectory dir = FileUtilKt.getTemplateDir();
                    if (dir != null) {
                        VelocityEngine velocityEngine = new VelocityEngine();
                        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
                        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
                        velocityEngine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new NullLogChute());
                        velocityEngine.init();

                        PsiFile file = dir.findFile(item.getName()+".vm");
                        String templateStr = null;
                        if (file != null) {
                            templateStr = FileUtilKt.readFile(file.getVirtualFile());
                        }
                        if (templateStr != null) {
                            StringReader reader = new StringReader(templateStr);
                            StringWriter sw = new StringWriter();
                            VelocityContext context = new VelocityContext();
                            context.put("NAME", fileName);
                            velocityEngine.evaluate(context, sw, "ERROR", reader);
                            text = sw.toString();
                        }
                    }
                    FileUtilKt.createFile(directory, path, fileName + suffix, text);
                });
            });
            setVisible(false);
        });
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setList(List<FlatItemNode> nodes) {
        Optional.ofNullable(nodes).ifPresent(it -> {
            operationList.setModel(new ListModel<>(nodes));
            operationList.setCellRenderer((ListCellRenderer<FlatItemNode>) (list, value, index, isSelected, cellHasFocus) -> {
                if ("item".equals(value.getType())) {
                    LayerItem item = new LayerItem();
                    item.setSelected(value.getSelected());
                    item.setSpace(value.getDeep() * 10);
                    item.setLabel(value.getName());
                    return item.getPanel();
                } else {
                    LabelItem item = new LabelItem();
                    item.setSpace(value.getDeep() * 10);
                    item.setLabel(value.getName());
                    return item.getPanel();
                }
            });

            operationList.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int index = operationList.locationToIndex(e.getPoint());
                    FlatItemNode item = (FlatItemNode) operationList.getModel().getElementAt(index);
                    if (!"item".equals(item.getType())) {
                        return;
                    }

                    if (e.getPoint().x > item.getDeep() * 10 + 25) {
                        JDialog dialog = new TemplateDialog(MainPanel.this, item.getName()+".vm");
                        dialog.setLocationRelativeTo(null);
                        dialog.setVisible(true);
                    } else {
                        item.setSelected(!item.getSelected());
                        operationList.repaint();

                        if (item.getSelected()) {
                            checkedList.add(item);
                        } else {
                            checkedList.remove(item);
                        }

                        setViewList(convertViewList());
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {

                }

                @Override
                public void mouseReleased(MouseEvent e) {

                }

                @Override
                public void mouseEntered(MouseEvent e) {

                }

                @Override
                public void mouseExited(MouseEvent e) {

                }
            });
        });
    }

    private List<FlatFileNode> convertViewList() {
        if (checkedList.size() == 0) {
            return null;
        }
        String suffix = language.isEmpty() ? "" : "." + language;
        FileNode root = new FileNode(ArchitectContext.module.getName());
        FileNode parent = root;
        String dirPath=directory.getVirtualFile().getPath();
        Pattern pattern= Pattern.compile(".*/src");
        dirPath=pattern.matcher(dirPath).replaceFirst("src");
        for (FlatItemNode item : checkedList) {
            String[] parts;
            String path = dirPath;
            if (item.getDir() != null) {
                if (item.getDir().startsWith("/")) {
                    path += item.getDir();
                } else {
                    path += "/" + item.getDir();
                }
            }
            parts = path.split("/");
            for (String str : parts) {
                List<FileNode> children = parent.getNodes();
                FileNode nextNode = null;
                for (int i = 0; children != null && i < children.size(); i++) {
                    FileNode child = children.get(i);
                    if (str.equals(child.getName())) {
                        nextNode = child;
                        break;
                    }
                }
                if (nextNode == null) {
                    nextNode = new FileNode(str);
                    if (parent.getNodes() == null) {
                        parent.setNodes(new ArrayList<>());
                    }
                    parent.getNodes().add(nextNode);
                }
                parent = nextNode;
            }
            if (parent.getNodes() == null) {
                parent.setNodes(new ArrayList<>());
            }
            parent.getNodes().add(new FileNode(item.getName() + suffix));
            parent = root;
        }
        return getViewList(root, 0);
    }

    private List<FlatFileNode> getViewList(FileNode root, int deep) {
        List<FlatFileNode> list = new ArrayList<>();
        if (root != null) {
            List<FileNode> children = root.getNodes();
            list.add(new FlatFileNode(root.getName(), children == null, deep));

            for (int i = 0; children != null && i < children.size(); i++) {
                list.addAll(getViewList(children.get(i), deep + 1));
            }
        }
        return list;
    }

    private void setViewList(List<FlatFileNode> nodes) {
        Optional.ofNullable(nodes).ifPresent(it -> {
            viewList.setModel(new ListModel(nodes));
            viewList.setCellRenderer((ListCellRenderer<FlatFileNode>) (list, value, index, isSelected, cellHasFocus) -> {
                String name = value.getName();
                if (value.isFile()) {
                    name = textField.getText() + StringUtils.capitalize(name);
                }
                FileItem item = new FileItem(name, value.isFile());
                item.setSpace(value.getDeep() * 10);
                return item.getPanel();
            });
        });
    }

    private void enableButton(boolean bool) {
        if (button.isEnabled() == bool) {
            return;
        }
        button.setEnabled(bool);
    }

    private void initSplitPane(){
        viewList=new JBList();
        JScrollPane scrollPane=new JBScrollPane(viewList);
        scrollPane.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
        splitPane.setLeftComponent(scrollPane);

        operationList=new JBList();
        scrollPane=new JBScrollPane(operationList);
        scrollPane.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
        splitPane.setRightComponent(scrollPane);
    }
}

class ClassNameFilter extends DocumentFilter {
    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        string = string.replaceAll("\\s", "");
        super.insertString(fb, offset, string, attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        text = text.replaceAll("\\s", "");
        super.replace(fb, offset, length, text, attrs);
    }
}
