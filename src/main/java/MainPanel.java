import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.lm.ArchitectContext;
import com.lm.FileUtilKt;
import com.lm.FlatItemNode;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.NullLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;

public class MainPanel extends JFrame {
    private JPanel panel;
    private JButton button;
    private JTextField textField;
    private JSplitPane splitPane;
    private final List<FlatItemNode> nodes;
    private final VirtualFile actionDir;
    private final DefaultListModel<FileNode> viewListModel = new DefaultListModel<>();

    public MainPanel(@NotNull VirtualFile actionDir,@NotNull List<FlatItemNode> nodes) {
        this.actionDir = actionDir;
        this.nodes = nodes;
        setContentPane(panel);

        PlainDocument document = (PlainDocument) textField.getDocument();
        document.setDocumentFilter(new ClassNameFilter());
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                enableButton(textField.getText().length() != 0);
                updateLeftPanel();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                enableButton(textField.getText().length() != 0);
                updateLeftPanel();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });

        button.setEnabled(false);

        button.addActionListener(e -> {
            final String name = textField.getText();
            nodes.forEach((node) -> {
                if (node.getSelected()) {
                    String fileName;
                    if (node.getFileHump()) {
                        fileName = StringUtils.capitalize(name) + node.getName();
                    } else {
                        fileName = StringUtils.uncapitalize(name) + "_" + node.getName();
                    }
                    String filePath;
                    if(node.getDir()==null){
                        filePath=fileName;
                    }else{
                        filePath=node.getDir() + "/" + fileName;
                    }
                    VirtualFile templateDir = FileUtilKt.getTemplateDir();
                    if (templateDir != null) {
                        VelocityEngine velocityEngine = new VelocityEngine();
                        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
                        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
                        velocityEngine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new NullLogChute());
                        velocityEngine.init();

                        VirtualFile templateFile = templateDir.findChild(node.getName());
                        if (templateFile != null) {
                            String templateContent = FileUtilKt.readFile(templateFile);
                            StringReader reader = new StringReader(templateContent == null ? "" : templateContent);
                            StringWriter sw = new StringWriter();
                            VelocityContext context = new VelocityContext();
                            VirtualFile file = FileUtilKt.createFile(actionDir, filePath);
                            String className;
                            if (node.getClassHump()) {
                                className = StringUtils.capitalize(name);
                            } else {
                                className = StringUtils.uncapitalize(name);
                            }
                            context.put("NAME", className);
                            context.put("PACKAGE_NAME", getPackageName(file.getPath()));
                            velocityEngine.evaluate(context, sw, "ERROR", reader);
                            FileUtilKt.writeToFile(file, sw.toString());
                        }
                    }
                }
            });
            setVisible(false);
        });

        setPanels(nodes);
    }

private void setPanels(@NotNull List<FlatItemNode> nodes) {
        setLeftPanel();
        setRightPanel(nodes);
    }

    private void setLeftPanel() {
        JList<FileNode> viewList = new JBList<>();
        JScrollPane scrollPane = new JBScrollPane(viewList);
        scrollPane.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
        viewList.setModel(viewListModel);
        viewList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            FileItem item = new FileItem(value.getName(), value.getName().matches(".*\\..+"));
            item.setSpace(value.getDeep() * 10);
            return item.getPanel();
        });
        splitPane.setLeftComponent(scrollPane);
    }

    private void setRightPanel(@NotNull List<FlatItemNode> nodes) {
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        for (FlatItemNode node : nodes) {
            if ("item".equals(node.getType())) {
                LayerItem layerItem = new LayerItem();
                layerItem.setSelected(node.getSelected());
                layerItem.setSpace(node.getDeep() * 10);
                layerItem.setLabel(node.getName());
                layerItem.setCheckChangeListener(e -> {
                    node.setSelected(e.getStateChange() == ItemEvent.SELECTED);
                    updateLeftPanel();
                });
                layerItem.setLinkClickListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        JDialog dialog = new TemplateDialog(MainPanel.this, node.getName());
                        dialog.setLocationRelativeTo(null);
                        dialog.setVisible(true);
                    }
                });
                rightPanel.add(layerItem.getPanel());
            } else if ("label".equals(node.getType())) {
                LabelItem labelItem = new LabelItem();
                labelItem.setSpace(node.getDeep() * 10);
                labelItem.setLabel(node.getName());
                rightPanel.add(labelItem.getPanel());
            }
        }
        JScrollPane scrollPane = new JBScrollPane(rightPanel);
        splitPane.setRightComponent(scrollPane);
    }

    private void updateLeftPanel() {
        FileNode root = createFileTree(nodes);
        List<FileNode> flatList = new ArrayList<>();
        transformTreeToList(root, flatList);
        viewListModel.clear();
        for (FileNode fileNode : flatList) {
            viewListModel.addElement(fileNode);
        }
    }

    @NotNull
    private FileNode createFileTree(@NotNull List<FlatItemNode> nodes) {
        String packagePath = getPackagePath();
        FileNode root = new FileNode(null, -1);
        final String name = textField.getText();
        nodes.forEach((node) -> {
            if (node.getSelected()) {
                FileNode pointer = root;
                String dir = node.getDir();
                String fileName;
                if (node.getFileHump()) {
                    fileName = StringUtils.capitalize(name) + node.getName();
                } else {
                    fileName = StringUtils.uncapitalize(name) + "_" + node.getName();
                }
                String filePath;
                if (dir == null || dir.isEmpty()) {
                    filePath = fileName;
                } else {
                    filePath = dir + "/" + fileName;
                }
                filePath = packagePath + "/" + filePath;
                String[] parts = filePath.split("/");
                for (int i = 0; i < parts.length; i++) {
                    FileNode nextPointer = null;
                    String part = parts[i];
                    List<FileNode> children = pointer.getNodes();
                    if (children != null) {
                        for (FileNode child : children) {
                            if (part.equals(child.getName())) {
                                nextPointer = child;
                                break;
                            }
                        }
                    }
                    if (nextPointer != null) {
                        pointer = nextPointer;
                    } else {
                        for (int j = i; j < parts.length; j++) {
                            children = pointer.getNodes();
                            if (children == null) {
                                children = new ArrayList<>();
                                pointer.setNodes(children);
                            }
                            FileNode child = new FileNode(parts[j], j);
                            children.add(child);
                            pointer = child;
                        }
                        break;
                    }
                }
            }
        });
        return root;
    }

    private void transformTreeToList(@Nullable FileNode root, @NotNull List<FileNode> list) {
        if (root != null) {
            if (root.getDeep() >= 0) {
                list.add(root);
            }
            List<FileNode> children = root.getNodes();
            if (children != null) {
                for (FileNode child : children) {
                    transformTreeToList(child, list);
                }
            }
        }
    }

    @Nullable
    private String getPackagePath() {
        String path = null;
        VirtualFile moduleDir = FileUtilKt.getModuleDir(ArchitectContext.module);
        if (moduleDir != null) {
            String moduleName = moduleDir.getPath().replaceAll(".*/", "");
            path = actionDir.getPath().replaceFirst(moduleDir.getPath(), moduleName);
        }
        return path;
    }

    @Nullable
    private String getPackageName(@Nullable String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        } else {
            VirtualFile[] sources = ModuleRootManager.getInstance(ArchitectContext.module).getSourceRoots();
            String packageName = null;
            for (VirtualFile source : sources) {
                String sourcePath = source.getPath();
                int j, k;
                for (j = 0, k = 0; j < sourcePath.length() && k < filePath.length(); ) {
                    if (sourcePath.charAt(j) == filePath.charAt(k)) {
                        j++;
                        k++;
                    } else {
                        j++;
                        k = 0;
                    }
                }
                if (j == sourcePath.length() && k > 0) {
                    packageName = filePath.replaceFirst(sourcePath.substring(j - k) + "/?", "").replaceAll("/[^/]*$", "").replaceAll("/",".");
                    break;
                }
            }
            return packageName;
        }
    }

    private void enableButton(boolean bool) {
        if (button.isEnabled() == bool) {
            return;
        }
        button.setEnabled(bool);
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
