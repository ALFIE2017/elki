package de.lmu.ifi.dbs.elki.utilities.datastructures.heap;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import de.lmu.ifi.dbs.elki.database.ids.ArrayDBIDs;
import de.lmu.ifi.dbs.elki.database.ids.DBID;
import de.lmu.ifi.dbs.elki.database.query.DistanceResultPair;
import de.lmu.ifi.dbs.elki.distance.distancevalue.Distance;

/**
 * Finalized KNN List.
 * 
 * @author Erich Schubert
 * 
 * @apiviz.composedOf DBIDItr
 * @apiviz.composedOf DBIDView
 * @apiviz.composedOf DistanceItr
 * @apiviz.composedOf DistanceView
 * 
 * @param <D>
 */
public class KNNList<D extends Distance<D>> extends ArrayList<DistanceResultPair<D>> {
  /**
   * Serial ID
   */
  private static final long serialVersionUID = 1L;

  /**
   * The value of k this was materialized for.
   */
  private final int k;

  /**
   * The maximum distance to return if size() &lt; k
   */
  private final D maxdist;

  /**
   * Constructor, to be called from KNNHeap only!
   * 
   * @param heap Calling heap.
   * @param maxdist infinite distance to return.
   */
  protected KNNList(KNNHeap<D> heap, D maxdist) {
    super(heap.size());
    this.k = heap.getK();
    this.maxdist = maxdist;
    // Get sorted data from heap; but in reverse.
    int i;
    for(i = 0; i < heap.size(); i++) {
      super.add(null);
    }
    while(!heap.isEmpty()) {
      i--;
      assert (i >= 0);
      super.set(i, heap.poll());
    }
    assert (heap.size() == 0);
  }

  /**
   * Get the K parameter.
   * 
   * @return K
   */
  public int getK() {
    return k;
  }

  /**
   * Get the distance to the k nearest neighbor, or maxdist otherwise.
   * 
   * @return Maximum distance
   */
  public D getKNNDistance() {
    if(size() < getK()) {
      return maxdist;
    }
    return get(getK() - 1).getDistance();
  }

  /**
   * Get maximum distance in list
   */
  public D getMaximumDistance() {
    if(isEmpty()) {
      return maxdist;
    }
    return get(size() - 1).getDistance();
  }

  /**
   * View as ArrayDBIDs
   * 
   * @return Static DBIDs
   */
  public ArrayDBIDs asDBIDs() {
    return new DBIDView(this);
  }

  /**
   * View as list of distances
   * 
   * @return List of distances view
   */
  public List<D> asDistanceList() {
    return new DistanceView<D>(this);
  }

  /* Make the list unmodifiable! */

  @Override
  public boolean add(@SuppressWarnings("unused") DistanceResultPair<D> e) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add(@SuppressWarnings("unused") int index, @SuppressWarnings("unused") DistanceResultPair<D> element) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(@SuppressWarnings("unused") Collection<? extends DistanceResultPair<D>> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(@SuppressWarnings("unused") int index, @SuppressWarnings("unused") Collection<? extends DistanceResultPair<D>> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public DistanceResultPair<D> remove(@SuppressWarnings("unused") int index) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean remove(@SuppressWarnings("unused") Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public DistanceResultPair<D> set(@SuppressWarnings("unused") int index, @SuppressWarnings("unused") DistanceResultPair<D> element) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void trimToSize() {
    throw new UnsupportedOperationException();
  }

  /**
   * Proxy iterator for accessing DBIDs.
   * 
   * @author Erich Schubert
   */
  protected static class DBIDItr implements Iterator<DBID> {
    /**
     * The real iterator.
     */
    Iterator<? extends DistanceResultPair<?>> itr;

    /**
     * Constructor.
     */
    protected DBIDItr(Iterator<? extends DistanceResultPair<?>> itr) {
      super();
      this.itr = itr;
    }

    @Override
    public boolean hasNext() {
      return itr.hasNext();
    }

    @Override
    public DBID next() {
      return itr.next().getDBID();
    }

    @Override
    public void remove() {
      itr.remove();
    }
  }

  /**
   * A view on the DBIDs of the result
   * 
   * @author Erich Schubert
   */
  protected static class DBIDView extends AbstractList<DBID> implements ArrayDBIDs {
    /**
     * The true list.
     */
    final List<? extends DistanceResultPair<?>> parent;

    /**
     * Constructor.
     * 
     * @param parent Owner
     */
    public DBIDView(List<? extends DistanceResultPair<?>> parent) {
      super();
      this.parent = parent;
    }

    @Override
    public DBID get(int i) {
      return parent.get(i).getDBID();
    }

    @Override
    public Collection<DBID> asCollection() {
      return this;
    }

    @Override
    public Iterator<DBID> iterator() {
      return new DBIDItr(parent.iterator());
    }

    @Override
    public int size() {
      return parent.size();
    }
  }

  /**
   * Proxy iterator for accessing DBIDs.
   * 
   * @author Erich Schubert
   */
  protected static class DistanceItr<D extends Distance<D>> implements Iterator<D> {
    /**
     * The real iterator.
     */
    Iterator<? extends DistanceResultPair<D>> itr;

    /**
     * Constructor.
     */
    protected DistanceItr(Iterator<? extends DistanceResultPair<D>> itr) {
      super();
      this.itr = itr;
    }

    @Override
    public boolean hasNext() {
      return itr.hasNext();
    }

    @Override
    public D next() {
      return itr.next().getDistance();
    }

    @Override
    public void remove() {
      itr.remove();
    }
  }

  /**
   * A view on the Distances of the result
   * 
   * @author Erich Schubert
   */
  protected static class DistanceView<D extends Distance<D>> extends AbstractList<D> implements List<D> {
    /**
     * The true list.
     */
    final List<? extends DistanceResultPair<D>> parent;

    /**
     * Constructor.
     * 
     * @param parent Owner
     */
    public DistanceView(List<? extends DistanceResultPair<D>> parent) {
      super();
      this.parent = parent;
    }

    @Override
    public D get(int i) {
      return parent.get(i).getDistance();
    }

    @Override
    public Iterator<D> iterator() {
      return new DistanceItr<D>(parent.iterator());
    }

    @Override
    public int size() {
      return parent.size();
    }
  }

  /**
   * View as ArrayDBIDs
   * 
   * @return Static DBIDs
   */
  public static ArrayDBIDs asDBIDs(List<? extends DistanceResultPair<?>> list) {
    return new DBIDView(list);
  }

  /**
   * View as list of distances
   * 
   * @return List of distances view
   */
  public static <D extends Distance<D>> List<D> asDistanceList(List<? extends DistanceResultPair<D>> list) {
    return new DistanceView<D>(list);
  }
}