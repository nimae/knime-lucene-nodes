package ir.nimae.knime;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "DataFromLuceneIndex" Node.
 * 
 *
 * @author Nima Ebrahimpour
 */
public class DataFromLuceneIndexNodeFactory 
        extends NodeFactory<DataFromLuceneIndexNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public DataFromLuceneIndexNodeModel createNodeModel() {
        return new DataFromLuceneIndexNodeModel();
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
    public NodeView<DataFromLuceneIndexNodeModel> createNodeView(final int viewIndex,
            final DataFromLuceneIndexNodeModel nodeModel) {
        return new DataFromLuceneIndexNodeView(nodeModel);
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
        return new DataFromLuceneIndexNodeDialog();
    }

}

