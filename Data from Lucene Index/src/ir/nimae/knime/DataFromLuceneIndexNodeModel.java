package ir.nimae.knime;

import ir.nimae.utils.Determiner;
import ir.nimae.utils.Evaluator;
import ir.nimae.utils.JarLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleRange;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * This is the model implementation of DataFromLuceneIndex.
 * 
 *
 * @author Nima Ebrahimpour
 */
public class DataFromLuceneIndexNodeModel extends NodeModel {
	
	private static class TfIdfEvaluator implements Evaluator {
		@Override
		public double evaluate(double... inputs) {
			return inputs[0] / inputs[1];
		}
	}
	
	private static final NodeLogger LOGGER = NodeLogger.getLogger(DataFromLuceneIndexNodeModel.class);
    
	static final String CONFIG_INDEX_DIR = "indexDir";
	static final String CONFIG_DF_FROM_TO = "dfFrom";
	static final String CONFIG_DETERMINER_CLASS = "determinerClass";
	static final String CONFIG_EVALUATOR_CLASS = "evaluatorClass";
	static final String CONFIG_CLASS_PATH = "classPath";
	
	static SettingsModelString createIndexDirSettingsModel() {
		return new SettingsModelString(CONFIG_INDEX_DIR, null);
	}
	
	static SettingsModelDoubleRange createDfFromToSettingsModel() {
		return new SettingsModelDoubleRange(CONFIG_DF_FROM_TO, 0, Integer.MAX_VALUE);
	}
	
	static SettingsModelString createDeterminerClassSettingsModel() {
		return new SettingsModelString(CONFIG_DETERMINER_CLASS, null);
	}
	
	static SettingsModelString createEvaluatorClassSettingsModel() {
		return new SettingsModelString(CONFIG_EVALUATOR_CLASS, null);
	}
	
	static SettingsModelString createClassPathSettingsModel() {
		return new SettingsModelString(CONFIG_CLASS_PATH, null);
	}
	
	
    /**
     * Constructor for the node model.
     */
    protected DataFromLuceneIndexNodeModel() {
        super(0, 1);
    }
    
    private SettingsModelString indexDir = createIndexDirSettingsModel();
    private SettingsModelDoubleRange dfFromTo = createDfFromToSettingsModel();
    private SettingsModelString determinerClass = createDeterminerClassSettingsModel();
    private SettingsModelString evaluatorClass = createEvaluatorClassSettingsModel();
    private SettingsModelString classPath = createClassPathSettingsModel();
    
    private JarLoader jarLoader;
    
    private static class ProgressMonitor {
    	public ProgressMonitor(ExecutionContext exec, ExecutorService service, int rows) {
    		this.exec = exec;
    		this.service = service;
    		this.rows = rows;
		}
    	private final ExecutionContext exec;
    	private final ExecutorService service;
    	private final double rows;
    	private int c = 0;
    	public void inc() throws CanceledExecutionException {
    		c++;
			exec.setMessage(c + " docs processed");
			exec.setProgress(c / rows);
			try {
				exec.checkCanceled();
			} catch (CanceledExecutionException e) {
				service.shutdown();
				throw e;
			}
    	}
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
    	
    	Determiner determiner = null;
    	final Evaluator evaluator;
    	
		String className = determinerClass.getStringValue();
		if (className != null && !className.isEmpty()) {
			Class<?> clazz;
			if (jarLoader == null) clazz = Class.forName(className);
			else clazz = jarLoader.loadClass(className);
			determiner = (Determiner)clazz.getConstructor().newInstance();
		}
		
		className = evaluatorClass.getStringValue();
		if (className == null || className.isEmpty()) {
			evaluator = new TfIdfEvaluator();
		} else {
			Class<?> clazz;
			if (jarLoader == null) clazz = Class.forName(className);
			else clazz = jarLoader.loadClass(className);
			evaluator = (Evaluator)clazz.getConstructor().newInstance();
		}
		
		// reading all terms
    	File path = new File(indexDir.getStringValue());
    	DirectoryReader reader = DirectoryReader.open(FSDirectory.open(path));
    	
    	TermsEnum tenum = MultiFields.getTerms(reader, "text").iterator(null);
    	final List<String> usableTerms = new ArrayList<String>();
    	final Map<String, Integer> docFreqs = new HashMap<String, Integer>();
    	BytesRef term;
    	while ((term = tenum.next()) != null) {
    		int df = tenum.docFreq();
    		if (determiner == null) {
    			if (df < dfFromTo.getMinRange() || df > dfFromTo.getMaxRange()) continue;
    		} else {
    			if (!determiner.determine(df)) continue;
    		}
    		String t = term.utf8ToString();
    		usableTerms.add(t);
    		docFreqs.put(t, df);
    	}
    	
    	// dimensions
    	final int ccols = usableTerms.size();
    	int crows = reader.getDocCount("text");
    	
    	// creating columns
    	DataColumnSpec[] columns = new DataColumnSpec[ccols + 1];
    	columns[0] = new DataColumnSpecCreator("Document ID",
    			DataType.getType(StringCell.class)).createSpec();
    	DataColumnSpecCreator creator = new DataColumnSpecCreator("",
    			DataType.getType(DoubleCell.class));
    	for (int i = 0; i < usableTerms.size(); i++) {
    		creator.setName(usableTerms.get(i));
    		columns[i+1] = creator.createSpec();
    	}
    	
    	// create output table
    	final BufferedDataContainer container = exec.createDataContainer(
    			new DataTableSpec(columns));
    	
    	exec.setProgress(0);
    	
    	ExecutorService service = Executors.newFixedThreadPool(16);
    	final Semaphore semaphore = new Semaphore(0);
    	int count = 0;
    	
    	final ProgressMonitor monitor = new ProgressMonitor(exec, service, crows);
    	
    	// for each atomic reader
    	for (AtomicReaderContext context : reader.leaves()) {
    		final AtomicReader r = context.reader();
    		service.execute(new Runnable() {
    			@Override
    			public void run() {
    				try {
    					TermsEnum tenum = null;
    					BytesRef term;
    		    		// for each term in each doc
    		    		for (int docId = 0; docId < r.numDocs(); docId++) {
    						String id = r.document(docId).get("id");
    						
    		    	    	// create table rows
    		    	    	DataCell[] cells = new DataCell[ccols + 1];
    			    		cells[0] = new StringCell(id);
    			    		
    						tenum = r.getTermVector(docId, "text").iterator(tenum);
    			        	while ((term = tenum.next()) != null) {
    			        		String t = term.utf8ToString();
    			        		if (usableTerms.contains(t)) {
    			        			DocsEnum denum = tenum.docs(null, null);
    			        			denum.nextDoc();
    			        			double v = evaluator.evaluate(denum.freq(), docFreqs.get(t));
    		    	    			cells[usableTerms.indexOf(t) + 1] = new DoubleCell(v);
    			        		}
    			        	}
    			        	
    			        	// filling other cells with zero
    			        	for (int i = 1; i < ccols + 1; i++)
    			        		if (cells[i] == null)
    			        			cells[i] = new DoubleCell(0);
    			        	
    			    		DefaultRow row = new DefaultRow(id, cells);
    			    		synchronized (container) {
    			    			container.addRowToTable(row);
    			    		}
    			    		
    			    		monitor.inc();
    		    		}
    				} catch (CanceledExecutionException e) {
    					return;
    				} catch (Exception e) {
    				} finally {
    					semaphore.release();
    				}
    			}
    		});
    		count++;
    	}
    	
		semaphore.acquire(count);
    	reader.close();
    	
    	container.close();
        return new BufferedDataTable[]{ container.getTable() };
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
    	
    	jarLoader = null;
    	
		if (indexDir.getStringValue() == null)
			throw new InvalidSettingsException("No index dir");
		
		String cpath = classPath.getStringValue();
		if (cpath != null && !cpath.isEmpty()) {
			try {
				jarLoader = new JarLoader(cpath, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    	
		String className = determinerClass.getStringValue();
		if (className != null && !className.isEmpty()) {
			try {
				Class<?> clazz;
				if (jarLoader == null) clazz = Class.forName(className);
				else clazz = jarLoader.loadClass(className);
				if (!Determiner.class.isAssignableFrom(clazz))
					throw new InvalidSettingsException("Invalid determiner");
			} catch (ClassNotFoundException e) {
				throw new InvalidSettingsException("No such determiner");
			}
		}
		
		className = evaluatorClass.getStringValue();
		if (className != null && !className.isEmpty()) {
			try {
				Class<?> clazz;
				if (jarLoader == null) clazz = Class.forName(className);
				else clazz = jarLoader.loadClass(className);
				if (!Evaluator.class.isAssignableFrom(clazz))
					throw new InvalidSettingsException("Invalid evaluator");
			} catch (ClassNotFoundException e) {
				throw new InvalidSettingsException("No such evaluator");
			}
		}
		
        return new DataTableSpec[]{null};
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	indexDir.saveSettingsTo(settings);
    	dfFromTo.saveSettingsTo(settings);
    	determinerClass.saveSettingsTo(settings);
    	evaluatorClass.saveSettingsTo(settings);
    	classPath.saveSettingsTo(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	indexDir.loadSettingsFrom(settings);
    	dfFromTo.loadSettingsFrom(settings);
    	determinerClass.loadSettingsFrom(settings);
    	evaluatorClass.loadSettingsFrom(settings);
    	classPath.loadSettingsFrom(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	indexDir.validateSettings(settings);
    	dfFromTo.validateSettings(settings);
    	determinerClass.validateSettings(settings);
    	evaluatorClass.validateSettings(settings);
    	classPath.validateSettings(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }
    
}

