package ir.nimae.knime;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;


/**
 * This is the model implementation of LibSVMWriter.
 * 
 *
 * @author 
 */
public class LibSVMWriterNodeModel extends NodeModel {
    
    //private static final NodeLogger LOGGER = NodeLogger.getLogger(LibSVMWriterNodeModel.class);
    
	static final String CONFIG_OUTPUT_FILE = "outputFile";
	
	static SettingsModelString createOutputFileSettingsModel() {
		return new SettingsModelString(CONFIG_OUTPUT_FILE, null);
	}
	
	private SettingsModelString outputFile = createOutputFileSettingsModel();
	
    protected LibSVMWriterNodeModel() {
        super(1, 0);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
    	
    	BufferedDataTable inTable = inData[0];
    	
    	FileWriter w = new FileWriter(new File(outputFile.getStringValue()));
    	
    	double crows = inTable.getRowCount();
    	exec.setProgress(0);
    	
		// number of feature cells
		int ccols = inTable.getSpec().getNumColumns() - 1;
		
		int c = 1;
    	for (DataRow row : inTable) {
    		// last cell is label
    		String label = ((StringCell)row.getCell(ccols)).getStringValue();
    		w.write( label.substring(4) );
    		
    		for (int i = 0; i < ccols; i++) {
    			double v = ((DoubleCell)row.getCell(i)).getDoubleValue();
    			if (v != 0) w.write("\t" + (i+1) + ":" + v);
    		}
    		w.write("\n");
    		w.flush();
    		
    		exec.checkCanceled();
    		exec.setMessage(c + " of " + crows);
    		exec.setProgress(c / crows);
    		c++;
    	}
    	
    	w.close();
    	
        return new BufferedDataTable[0];
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
        return new DataTableSpec[0];
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	outputFile.saveSettingsTo(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	outputFile.loadSettingsFrom(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	outputFile.validateSettings(settings);
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

