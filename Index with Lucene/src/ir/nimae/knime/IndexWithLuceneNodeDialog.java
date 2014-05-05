package ir.nimae.knime;

import javax.swing.JFileChooser;

import org.knime.core.data.IntValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentMultiLineString;
import org.knime.core.node.defaultnodesettings.DialogComponentString;

/**
 * <code>NodeDialog</code> for the "IndexWithLucene" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Nima Ebrahimpour
 */
public class IndexWithLuceneNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring IndexWithLucene node dialog. This is just a
	 * suggestion to demonstrate possible default dialog components.
	 */
	@SuppressWarnings("unchecked")
	protected IndexWithLuceneNodeDialog() {
		super();
		
		addDialogComponent(new DialogComponentFileChooser(
				IndexWithLuceneNodeModel.createIndexDirSettingsModel(),
				IndexWithLuceneNodeModel.CONFIG_INDEX_DIR,
				JFileChooser.OPEN_DIALOG,
				true));
		
		addDialogComponent(new DialogComponentColumnNameSelection(
				IndexWithLuceneNodeModel.createIdColumnSettingsModel(),
				"Id column",
				0,
				true,
				IntValue.class,
				StringValue.class));
		
		addDialogComponent(new DialogComponentColumnNameSelection(
				IndexWithLuceneNodeModel.createTextColumnSettingsModel(),
				"Text column",
				0,
				true,
				StringValue.class));
		
		addDialogComponent(new DialogComponentString(
				IndexWithLuceneNodeModel.createAnalyzerClassSettingsModel(),
				"Analyzer class"));
		
		addDialogComponent(new DialogComponentMultiLineString(
				IndexWithLuceneNodeModel.createClassPathClassSettingsModel(),
				"Class path"));
		
		addDialogComponent(new DialogComponentMultiLineString(
				IndexWithLuceneNodeModel.createLibPathClassSettingsModel(),
				"Lib path"));
		
	}
}
