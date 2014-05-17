package ir.nimae.knime;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;

/**
 * <code>NodeDialog</code> for the "LibSVMWriter" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author 
 */
public class LibSVMWriterNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring LibSVMWriter node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    protected LibSVMWriterNodeDialog() {
        super();
        
        addDialogComponent(new DialogComponentFileChooser(
        			LibSVMWriterNodeModel.createOutputFileSettingsModel(),
        			LibSVMWriterNodeModel.CONFIG_OUTPUT_FILE
        		));
        
    }
}

