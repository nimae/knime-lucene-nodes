package ir.nimae.knime;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "LibSVMWriter" Node.
 * 
 *
 * @author 
 */
public class LibSVMWriterNodeFactory 
        extends NodeFactory<LibSVMWriterNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public LibSVMWriterNodeModel createNodeModel() {
        return new LibSVMWriterNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<LibSVMWriterNodeModel> createNodeView(final int viewIndex,
            final LibSVMWriterNodeModel nodeModel) {
        return new LibSVMWriterNodeView(nodeModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new LibSVMWriterNodeDialog();
    }

}

