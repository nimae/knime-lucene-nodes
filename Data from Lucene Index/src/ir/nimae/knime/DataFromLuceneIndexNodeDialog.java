package ir.nimae.knime;

import javax.swing.JFileChooser;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentDoubleRange;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentMultiLineString;
import org.knime.core.node.defaultnodesettings.DialogComponentString;

/**
 * <code>NodeDialog</code> for the "DataFromLuceneIndex" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Nima Ebrahimpour
 */
public class DataFromLuceneIndexNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the DataFromLuceneIndex node.
     */
    protected DataFromLuceneIndexNodeDialog() {
    	
		addDialogComponent(new DialogComponentFileChooser(
				DataFromLuceneIndexNodeModel.createIndexDirSettingsModel(),
				DataFromLuceneIndexNodeModel.CONFIG_INDEX_DIR,
				JFileChooser.OPEN_DIALOG,
				true));
		
		addDialogComponent(new DialogComponentDoubleRange(
				DataFromLuceneIndexNodeModel.createDfFromToSettingsModel(),
				0, Integer.MAX_VALUE, 1,
				DataFromLuceneIndexNodeModel.CONFIG_INDEX_DIR));
		
		addDialogComponent(new DialogComponentString(
				DataFromLuceneIndexNodeModel.createDeterminerClassSettingsModel(),
				DataFromLuceneIndexNodeModel.CONFIG_DETERMINER_CLASS));
		
		addDialogComponent(new DialogComponentString(
				DataFromLuceneIndexNodeModel.createEvaluatorClassSettingsModel(),
				DataFromLuceneIndexNodeModel.CONFIG_EVALUATOR_CLASS));
		
		addDialogComponent(new DialogComponentMultiLineString(
				DataFromLuceneIndexNodeModel.createClassPathSettingsModel(),
				DataFromLuceneIndexNodeModel.CONFIG_CLASS_PATH));
		
    }
}

