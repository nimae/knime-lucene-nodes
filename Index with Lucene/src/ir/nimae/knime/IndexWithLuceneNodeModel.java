package ir.nimae.knime;

import ir.nimae.utils.JarLoader;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;


/**
 * This is the model implementation of IndexWithLucene.
 * 
 *
 * @author Nima Ebrahimpour
 */
public class IndexWithLuceneNodeModel extends NodeModel {
	
	private static final NodeLogger LOGGER = NodeLogger.getLogger(IndexWithLuceneNodeModel.class);
	
	private static final FieldType FIELD_TYPE1 = new FieldType();
	private static final FieldType FIELD_TYPE2 = new FieldType();
	
	static {
		FIELD_TYPE1.setStored(true);
		
		FIELD_TYPE2.setIndexed(true);
		//FIELD_TYPE2.setStored(true);
		FIELD_TYPE2.setStoreTermVectors(true);
	}
	
	static final String CONFIG_INDEX_DIR = "indexDir";
	static final String CONFIG_ID_COLUMN = "idColumn";
	static final String CONFIG_TEXT_COLUMN = "textColumn";
	static final String CONFIG_ANALYZER_CLASS = "analyzerClass";
	static final String CONFIG_CLASS_PATH = "classPath";
	static final String CONFIG_LIB_PATH = "libPath";
	
	static SettingsModelString createIndexDirSettingsModel() {
		return new SettingsModelString(CONFIG_INDEX_DIR, null);
	}

	static SettingsModelString createIdColumnSettingsModel() {
		return new SettingsModelString(CONFIG_ID_COLUMN, "");
	}

	static SettingsModelString createTextColumnSettingsModel() {
		return new SettingsModelString(CONFIG_TEXT_COLUMN, "");
	}

	static SettingsModelString createAnalyzerClassSettingsModel() {
		return new SettingsModelString(CONFIG_ANALYZER_CLASS, "");
	}

	static SettingsModelString createClassPathClassSettingsModel() {
		return new SettingsModelString(CONFIG_CLASS_PATH, "");
	}

	static SettingsModelString createLibPathClassSettingsModel() {
		return new SettingsModelString(CONFIG_LIB_PATH, "");
	}

	protected IndexWithLuceneNodeModel() {
		super(1, 0);
		
		LOGGER.info("ctor 1");
		
		exe = Executors.newFixedThreadPool(16);
		
		LOGGER.info("ctor 2");
	}
	
	private SettingsModelString indexDir = createIndexDirSettingsModel();
	private SettingsModelString idColumn = createIdColumnSettingsModel();
	private SettingsModelString textColumn = createTextColumnSettingsModel();
	private SettingsModelString analyzerClass = createAnalyzerClassSettingsModel();
	private SettingsModelString classPath = createClassPathClassSettingsModel();
	private SettingsModelString libPath = createLibPathClassSettingsModel();
	
	private Directory dir;
	
	private Executor exe;
	
	private void index(final IndexWriter writer, final String id, final String text) {
		exe.execute(new Runnable() {
			@Override
			public void run() {
				Document doc = new Document();
				doc.add(new Field("id", id, FIELD_TYPE1));
				doc.add(new Field("text", text, FIELD_TYPE2));
				try {
					writer.addDocument(doc);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
    /**
     * {@inheritDoc}
     */
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception {
		
		LOGGER.info("exec 1");
		
		DataTableSpec spec = inData[0].getSpec();
		int iId = spec.findColumnIndex(idColumn.getStringValue());
		int iText = spec.findColumnIndex(textColumn.getStringValue());
		
		double all = inData[0].getRowCount();
		int i = 0;
		
		Analyzer analyzer;
		
		String className = analyzerClass.getStringValue();
		if (className == null || className.isEmpty()) {
			analyzer = new StandardAnalyzer(Version.LUCENE_47);
		} else {
			Class<?> clazz;
			if (jarLoader == null) clazz = Class.forName(className);
			else clazz = jarLoader.loadClass(className);
			analyzer = (Analyzer)clazz.getConstructor(Version.class)
					.newInstance(Version.LUCENE_47);
		}
		
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, analyzer);
		config.setInfoStream(System.out);
		IndexWriter writer = null;
		
		try {
			
			writer = new IndexWriter(dir, config);
			
			for (DataRow row : inData[0]) {
				String id = ((StringCell)row.getCell(iId)).getStringValue();
				String text = ((StringCell)row.getCell(iText)).getStringValue();
				index(writer, id, text);
				
				i++;
				exec.setMessage(i + " docs");
				exec.setProgress(i / all);
				exec.checkCanceled();
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			LOGGER.info(">>>"+writer);
			if (writer != null) writer.close();
		}
		
		LOGGER.info("exec 2");
		
		return null;
	}
	
	private JarLoader jarLoader = null;
	
    /**
     * {@inheritDoc}
     */
	@Override
	protected void reset() {
		LOGGER.info("reset 1");
	}
	
    /**
     * {@inheritDoc}
     */
	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		
		LOGGER.info("conf 1");
		
		jarLoader = null;
		
		if (indexDir.getStringValue() == null)
			throw new InvalidSettingsException("No index dir");
		if (idColumn.getStringValue() == null)
			throw new InvalidSettingsException("No ID column");
		if (textColumn.getStringValue() == null)
			throw new InvalidSettingsException("No text column");
		
		String cpath = classPath.getStringValue();
		if (cpath != null && !cpath.isEmpty()) {
			try {
				jarLoader = new JarLoader(cpath, libPath.getStringValue());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		String className = analyzerClass.getStringValue();
		if (className != null && !className.isEmpty()) {
			try {
				Class<?> clazz;
				if (jarLoader == null) clazz = Class.forName(className);
				else clazz = jarLoader.loadClass(className);
				if (!Analyzer.class.isAssignableFrom(clazz))
					throw new InvalidSettingsException("Invalid analyzer");
			} catch (ClassNotFoundException e) {
				throw new InvalidSettingsException("No such analyzer");
			}
		}
		
		try {
			dir = FSDirectory.open(new File(indexDir.getStringValue()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		LOGGER.info("conf 2");
		
		return null;
	}
	
    /**
     * {@inheritDoc}
     */
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		indexDir.saveSettingsTo(settings);
		idColumn.saveSettingsTo(settings);
		textColumn.saveSettingsTo(settings);
		analyzerClass.saveSettingsTo(settings);
		classPath.saveSettingsTo(settings);
		libPath.saveSettingsTo(settings);
	}
	
    /**
     * {@inheritDoc}
     */
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		indexDir.loadSettingsFrom(settings);
		idColumn.loadSettingsFrom(settings);
		textColumn.loadSettingsFrom(settings);
		analyzerClass.loadSettingsFrom(settings);
		classPath.loadSettingsFrom(settings);
		libPath.loadSettingsFrom(settings);
	}
	
    /**
     * {@inheritDoc}
     */
	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		indexDir.validateSettings(settings);
		idColumn.validateSettings(settings);
		textColumn.validateSettings(settings);
		analyzerClass.validateSettings(settings);
		classPath.validateSettings(settings);
		libPath.validateSettings(settings);
	}
	
    /**
     * {@inheritDoc}
     */
	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}
	
    /**
     * {@inheritDoc}
     */
	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}
	
}

