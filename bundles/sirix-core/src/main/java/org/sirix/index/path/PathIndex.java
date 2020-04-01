package org.sirix.index.path;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import org.sirix.api.PageReadOnlyTrx;
import org.sirix.api.PageTrx;
import org.sirix.index.ChangeListener;
import org.sirix.index.Filter;
import org.sirix.index.IndexDef;
import org.sirix.index.IndexFilterAxis;
import org.sirix.index.SearchMode;
import org.sirix.index.avltree.AVLNode;
import org.sirix.index.avltree.AVLTreeReader;
import org.sirix.index.avltree.keyvalue.NodeReferences;
import org.sirix.index.path.summary.PathSummaryReader;
import org.sirix.node.interfaces.DataRecord;
import org.sirix.page.UnorderedKeyValuePage;
import org.sirix.settings.Fixed;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;

public interface PathIndex<B, L extends ChangeListener> {
  B createBuilder(PageTrx<Long, DataRecord, UnorderedKeyValuePage> pageWriteTrx, PathSummaryReader pathSummaryReader,
      IndexDef indexDef);

  L createListener(PageTrx<Long, DataRecord, UnorderedKeyValuePage> pageWriteTrx, PathSummaryReader pathSummaryReader,
      IndexDef indexDef);

  default Iterator<NodeReferences> openIndex(final PageReadOnlyTrx pageRtx, final IndexDef indexDef,
      final PathFilter filter) {
    final AVLTreeReader<Long, NodeReferences> reader =
        AVLTreeReader.getInstance(pageRtx, indexDef.getType(), indexDef.getID());

    if (filter != null && filter.getPCRs().size() == 1) {
      final Optional<NodeReferences> optionalNodeReferences =
          reader.get(filter.getPCRs().iterator().next(), SearchMode.EQUAL);
      return Iterators.forArray(optionalNodeReferences.orElse(new NodeReferences()));
    } else {
      final Iterator<AVLNode<Long, NodeReferences>> iter =
          reader.new AVLNodeIterator(Fixed.DOCUMENT_NODE_KEY.getStandardProperty());
      final Set<Filter> setFilter = filter == null
          ? ImmutableSet.of()
          : ImmutableSet.of(filter);

      return new IndexFilterAxis<>(iter, setFilter);
    }
  }
}
