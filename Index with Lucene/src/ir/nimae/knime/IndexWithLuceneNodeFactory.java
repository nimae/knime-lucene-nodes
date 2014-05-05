package ir.nimae.knime;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "IndexWithLucene" Node.
 * 
 *
 * @author Nima Ebrahimpour
 */
public class IndexWithLuceneNodeFactory 
        extends NodeFactory<IndexWithLuceneNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public IndexWithLuceneNodeModel createNodeModel() {
        return new IndexWithLuceneNodeModel();
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
    public NodeView<IndexWithLuceneNodeModel> createNodeView(final int viewIndex,
            final IndexWithLuceneNodeModel nodeModel) {
        return new IndexWithLuceneNodeView(nodeModel);
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
        return new IndexWithLuceneNodeDialog();
    }

}

