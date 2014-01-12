package de.lmu.ifi.dbs.elki.algorithm;

/*
 This file is part of ELKI:
 Environment for Developing KDD-Applications Supported by Index-Structures

 Copyright (C) 2013
 Ludwig-Maximilians-Universität München
 Lehr- und Forschungseinheit für Datenbanksysteme
 ELKI Development Team

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.lmu.ifi.dbs.elki.data.BitVector;
import de.lmu.ifi.dbs.elki.data.type.TypeInformation;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.data.type.VectorFieldTypeInformation;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.database.relation.RelationUtil;
import de.lmu.ifi.dbs.elki.logging.Logging;
import de.lmu.ifi.dbs.elki.result.AprioriResult;
import de.lmu.ifi.dbs.elki.utilities.BitsUtil;
import de.lmu.ifi.dbs.elki.utilities.documentation.Description;
import de.lmu.ifi.dbs.elki.utilities.documentation.Reference;
import de.lmu.ifi.dbs.elki.utilities.documentation.Title;
import de.lmu.ifi.dbs.elki.utilities.exceptions.AbortException;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.AbstractParameterizer;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.CommonConstraints;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.OneMustBeSetGlobalConstraint;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.OnlyOneIsAllowedToBeSetGlobalConstraint;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.DoubleParameter;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.IntParameter;

/**
 * Provides the APRIORI algorithm for Mining Association Rules.
 * <p>
 * Reference: <br>
 * R. Agrawal, R. Srikant: Fast Algorithms for Mining Association Rules in Large
 * Databases. <br>
 * In Proc. 20th Int. Conf. on Very Large Data Bases (VLDB '94), Santiago de
 * Chile, Chile 1994.
 * </p>
 * 
 * @author Arthur Zimek
 * @author Erich Schubert
 * 
 * @apiviz.has Itemset
 * @apiviz.uses BitVector
 */
@Title("APRIORI: Algorithm for Mining Association Rules")
@Description("Searches for frequent itemsets")
@Reference(authors = "R. Agrawal, R. Srikant", title = "Fast Algorithms for Mining Association Rules in Large Databases", booktitle = "Proc. 20th Int. Conf. on Very Large Data Bases (VLDB '94), Santiago de Chile, Chile 1994", url = "http://www.acm.org/sigmod/vldb/conf/1994/P487.PDF")
public class APRIORI extends AbstractAlgorithm<AprioriResult> {
  /**
   * The logger for this class.
   */
  private static final Logging LOG = Logging.getLogger(APRIORI.class);

  /**
   * Optional parameter to specify the threshold for minimum frequency, must be
   * a double greater than or equal to 0 and less than or equal to 1.
   * Alternatively to parameter {@link #MINSUPP_ID}).
   */
  public static final OptionID MINFREQ_ID = new OptionID("apriori.minfreq", "Threshold for minimum frequency as percentage value " + "(alternatively to parameter apriori.minsupp).");

  /**
   * Parameter to specify the threshold for minimum support as minimally
   * required number of transactions, must be an integer equal to or greater
   * than 0. Alternatively to parameter {@link #MINFREQ_ID} - setting
   * {@link #MINSUPP_ID} is slightly preferable over setting {@link #MINFREQ_ID}
   * in terms of efficiency.
   */
  public static final OptionID MINSUPP_ID = new OptionID("apriori.minsupp", "Threshold for minimum support as minimally required number of transactions " + "(alternatively to parameter apriori.minfreq" + " - setting apriori.minsupp is slightly preferable over setting " + "apriori.minfreq in terms of efficiency).");

  /**
   * Holds the value of {@link #MINFREQ_ID}.
   */
  private double minfreq = Double.NaN;

  /**
   * Holds the value of {@link #MINSUPP_ID}.
   */
  private int minsupp = Integer.MIN_VALUE;

  /**
   * Constructor with minimum frequency.
   * 
   * @param minfreq Minimum frequency
   */
  public APRIORI(double minfreq) {
    super();
    this.minfreq = minfreq;
  }

  /**
   * Constructor with minimum support.
   * 
   * @param minsupp Minimum support
   */
  public APRIORI(int minsupp) {
    super();
    this.minsupp = minsupp;
  }

  /**
   * Performs the APRIORI algorithm on the given database.
   * 
   * @param relation the Relation to process
   * @return the AprioriResult learned by this APRIORI
   */
  public AprioriResult run(Relation<BitVector> relation) {
    List<Itemset> solution = new ArrayList<>();
    final int size = relation.size();
    VectorFieldTypeInformation<BitVector> meta = RelationUtil.assumeVectorField(relation);
    if(size > 0) {
      final int dim = meta.getDimensionality();
      // Generate initial candidates of length 1.
      List<Itemset> candidates = new ArrayList<>(dim);
      for(int i = 0; i < dim; i++) {
        candidates.add(new OneItemset(i));
      }
      for(int length = 1; candidates.size() > 0; length++) {
        StringBuilder msg = LOG.isVerbose() ? new StringBuilder() : null;
        List<Itemset> supported = frequentItemsets(candidates, relation);
        if(msg != null) {
          if(LOG.isDebuggingFinest() && length > 2) {
            msg.append("\ncandidates (").append(candidates.size()).append("):");
            for(Itemset itemset : candidates) {
              msg.append(" [");
              itemset.appendTo(msg, meta);
              msg.append(']');
            }
          }
          msg.append("\nfrequentItemsets: (").append(supported.size()).append("):");
          for(Itemset itemset : supported) {
            msg.append(" [");
            itemset.appendTo(msg, meta);
            msg.append(']');
          }
        }
        solution.addAll(supported);
        // Join to get the new candidates
        candidates = aprioriGenerate(supported, length + 1, dim);
        if(msg != null) {
          if(length > 2) {
            msg.append("\ncandidates after pruning (").append(candidates.size()).append("):");
            for(Itemset itemset : candidates) {
              msg.append(" [");
              itemset.appendTo(msg, meta);
              msg.append(']');
            }
          }
          LOG.verbose(msg.toString());
        }
      }
    }
    return new AprioriResult("APRIORI", "apriori", solution, meta);
  }

  /**
   * Returns the frequent BitSets out of the given BitSets with respect to the
   * given database.
   * 
   * @param support Support map.
   * @param candidates the candidates to be evaluated
   * @param database the database to evaluate the candidates on
   * @return Itemsets with sufficient support
   */
  protected List<Itemset> frequentItemsets(List<Itemset> candidates, Relation<BitVector> database) {
    for(DBIDIter iditer = database.iterDBIDs(); iditer.valid(); iditer.advance()) {
      BitVector bv = database.get(iditer);
      // TODO can we exploit that the candidate set it sorted?
      for(Itemset candidate : candidates) {
        if(candidate.containedIn(bv)) {
          candidate.support++;
        }
      }
    }
    // Retain only those with minimum support:
    final int needed = (minfreq >= 0.) ? (int) Math.ceil(minfreq * database.size()) : minsupp;
    List<Itemset> supported = new ArrayList<>(candidates.size());
    for(Iterator<Itemset> iter = candidates.iterator(); iter.hasNext();) {
      final Itemset candidate = iter.next();
      if(candidate.support >= needed) {
        supported.add(candidate);
      }
    }
    return supported;
  }

  /**
   * Prunes a given set of candidates to keep only those BitSets where all
   * subsets of bits flipping one bit are frequent already.
   * 
   * @param supported Support map
   * @param length Itemset length
   * @param dim Dimensionality
   * @return itemsets that cannot be pruned by apriori
   */
  protected List<Itemset> aprioriGenerate(List<Itemset> supported, int length, int dim) {
    List<Itemset> candidateList = new ArrayList<>();
    if(supported.size() <= 0) {
      return candidateList;
    }
    // At length 2, we don't need to check.
    if(length == 2) {
      final int ssize = supported.size();
      for(int i = 0; i < ssize; i++) {
        OneItemset ii = (OneItemset) supported.get(i);
        for(int j = i + 1; j < ssize; j++) {
          OneItemset ij = (OneItemset) supported.get(j);
          candidateList.add(new SparseItemset(ii.item, ij.item));
        }
      }
      return candidateList;
    }
    Itemset ref = supported.get(0);
    if(ref instanceof SparseItemset) {
      // TODO: we currently never switch to DenseItemSet. This may however be
      // beneficial when we have few dimensions and many candidates.
      // E.g. when length > 32 and dim < 100. But this needs benchmarking!
      // For length < 5 and dim > 3000, SparseItemset unsurprisingly was faster

      // Scratch item to use for searching.
      SparseItemset scratch = new SparseItemset(new int[length - 1]);

      final int ssize = supported.size();
      for(int i = 0; i < ssize; i++) {
        SparseItemset ii = (SparseItemset) supported.get(i);
        prefix: for(int j = i + 1; j < ssize; j++) {
          SparseItemset ij = (SparseItemset) supported.get(j);
          for(int k = length - 3; k >= 0; k--) {
            if(ii.indices[k] != ij.indices[k]) {
              continue prefix; // Prefix doesn't match
            }
          }
          // Test subsets (re-) using scratch object
          System.arraycopy(ii.indices, 1, scratch.indices, 0, length - 2);
          scratch.indices[length - 2] = ij.indices[length - 2];
          for(int k = length - 3; k >= 0; k--) {
            scratch.indices[k] = ii.indices[k + 1];
            int pos = Collections.binarySearch(supported, scratch);
            if(pos < 0) {
              continue prefix;
            }
          }
          int[] items = new int[length];
          System.arraycopy(ii.indices, 0, items, 0, length - 1);
          items[length - 1] = ij.indices[length - 2];
          candidateList.add(new SparseItemset(items));
        }
      }
      return candidateList;
    }
    if(ref instanceof DenseItemset) {
      // Scratch item to use for searching.
      DenseItemset scratch = new DenseItemset(BitsUtil.zero(dim), length - 1);

      final int ssize = supported.size();
      for(int i = 0; i < ssize; i++) {
        DenseItemset ii = (DenseItemset) supported.get(i);
        prefix: for(int j = i + 1; j < ssize; j++) {
          DenseItemset ij = (DenseItemset) supported.get(j);
          // Prefix test via "|i1 ^ i2| = 2"
          System.arraycopy(ii.items, 0, scratch.items, 0, ii.items.length);
          BitsUtil.xorI(scratch.items, ij.items);
          if(BitsUtil.cardinality(scratch.items) != 2) {
            break prefix; // No prefix match; since sorted, no more can follow!
          }
          // Ensure that the first difference is the last item in ii:
          int first = BitsUtil.nextSetBit(scratch.items, 0);
          if(BitsUtil.nextSetBit(ii.items, first + 1) > -1) {
            break prefix; // Different overlap by chance?
          }
          BitsUtil.orI(scratch.items, ij.items);

          // Test subsets.
          for(int l = length, b = BitsUtil.nextSetBit(scratch.items, 0); l > 2; l--, b = BitsUtil.nextSetBit(scratch.items, b + 1)) {
            BitsUtil.clearI(scratch.items, b);
            int pos = Collections.binarySearch(supported, scratch);
            if(pos < 0) {
              continue prefix;
            }
            BitsUtil.setI(scratch.items, b);
          }
          candidateList.add(new DenseItemset(scratch.items.clone(), length));
        }
      }
      return candidateList;
    }
    throw new AbortException("Unexpected itemset type " + ref.getClass());
  }

  @Override
  public TypeInformation[] getInputTypeRestriction() {
    return TypeUtil.array(TypeUtil.BIT_VECTOR_FIELD);
  }

  @Override
  protected Logging getLogger() {
    return LOG;
  }

  /**
   * APRIORI itemset.
   * 
   * @author Erich Schubert
   */
  public static abstract class Itemset implements Comparable<Itemset> {
    /**
     * Support for this itemset.
     */
    int support;

    /**
     * Get item support.
     * 
     * @return Support
     */
    public int getSupport() {
      return support;
    }

    /**
     * Test whether the itemset is contained in a bit vector.
     * 
     * @param bv Bit vector
     * @return {@code true} when the itemset is contained in this vector.
     */
    abstract public boolean containedIn(BitVector bv);

    /**
     * Itemset length.
     * 
     * @return Itemset length
     */
    abstract public int length();

    /**
     * Get the items.
     * 
     * @return Itemset contents.
     */
    abstract public long[] getItems();

    @Override
    public String toString() {
      return appendTo(new StringBuilder(), null).toString();
    }

    /**
     * Append to a string buffer.
     * 
     * @param buf Buffer
     * @param labels Item labels
     * @return String buffer for chaining.
     */
    abstract public StringBuilder appendTo(StringBuilder buf, VectorFieldTypeInformation<BitVector> meta);
  }

  /**
   * APRIORI itemset.
   * 
   * @author Erich Schubert
   */
  public static class OneItemset extends Itemset {
    /**
     * Trivial item.
     */
    int item;

    /**
     * Constructor of 1-itemset.
     * 
     * @param item Item
     */
    public OneItemset(int item) {
      this.item = item;
    }

    @Override
    public int length() {
      return 1;
    }

    @Override
    public boolean containedIn(BitVector bv) {
      // TODO: add a booleanValue method to BitVector?
      return bv.longValue(item) != 0L;
    }

    @Override
    public long[] getItems() {
      long[] bits = BitsUtil.zero(item);
      BitsUtil.setI(bits, item);
      return bits;
    }

    @Override
    public int hashCode() {
      return item;
    }

    @Override
    public boolean equals(Object obj) {
      if(this == obj) {
        return true;
      }
      if(obj == null) {
        return false;
      }
      if(!(obj instanceof Itemset) || ((Itemset) obj).length() != 1) {
        return false;
      }
      if(getClass() != obj.getClass()) {
        return false;
      }
      OneItemset other = (OneItemset) obj;
      return item == other.item;
    }

    @Override
    public int compareTo(Itemset o) {
      int cmp = Integer.compare(1, o.length());
      if(cmp != 0) {
        return cmp;
      }
      if(o instanceof OneItemset) {
        return Integer.compare(item, ((OneItemset) o).item);
      }
      throw new AbortException("Itemset of length 1 not using OneItemset!");
    }

    @Override
    public StringBuilder appendTo(StringBuilder buf, VectorFieldTypeInformation<BitVector> meta) {
      String lbl = (meta != null) ? meta.getLabel(item) : null;
      if(lbl == null) {
        buf.append(item);
      }
      else {
        buf.append(lbl);
      }
      return buf.append(": ").append(support);
    }
  }

  /**
   * APRIORI itemset.
   * 
   * @author Erich Schubert
   */
  public static class SparseItemset extends Itemset {
    /**
     * Items, as indexes.
     */
    private int[] indices;

    /**
     * Constructor.
     * 
     * @param items Items
     */
    public SparseItemset(int... indices) {
      this.indices = indices;
    }

    @Override
    public int length() {
      return indices.length;
    }

    @Override
    public boolean containedIn(BitVector bv) {
      for(int item : indices) {
        // TODO: add a booleanValue method to BitVector?
        if(bv.longValue(item) == 0L) {
          return false;
        }
      }
      return true;
    }

    @Override
    public long[] getItems() {
      long[] bits = BitsUtil.zero(indices[indices.length - 1]);
      for(int item : indices) {
        BitsUtil.setI(bits, item);
      }
      return bits;
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(indices);
    }

    @Override
    public boolean equals(Object obj) {
      if(this == obj) {
        return true;
      }
      if(obj == null) {
        return false;
      }
      if(!(obj instanceof Itemset) || ((Itemset) obj).length() != 1) {
        return false;
      }
      // TODO: allow comparing to DenseItemset etc?
      if(getClass() != obj.getClass()) {
        return false;
      }
      return Arrays.equals(indices, ((SparseItemset) obj).indices);
    }

    @Override
    public int compareTo(Itemset o) {
      int cmp = Integer.compare(indices.length, o.length());
      if(cmp != 0) {
        return cmp;
      }
      SparseItemset other = (SparseItemset) o;
      for(int i = 0; i < indices.length; i++) {
        int c = Integer.compare(indices[i], other.indices[i]);
        if(c != 0) {
          return c;
        }
      }
      return 0;
    }

    @Override
    public StringBuilder appendTo(StringBuilder buf, VectorFieldTypeInformation<BitVector> meta) {
      for(int j = 0; j < indices.length; j++) {
        if(j > 0) {
          buf.append(", ");
        }
        String lbl = (meta != null) ? meta.getLabel(indices[j]) : null;
        if(lbl == null) {
          buf.append(indices[j]);
        }
        else {
          buf.append(lbl);
        }
      }
      buf.append(": ").append(support);
      return buf;
    }
  }

  /**
   * APRIORI itemset.
   * 
   * @author Erich Schubert
   */
  public static class DenseItemset extends Itemset {
    /**
     * Items, as a bitmask.
     */
    long[] items;

    /**
     * Itemset length.
     */
    int length;

    /**
     * Constructor.
     * 
     * @param items Items
     * @param length Length (Cardinality of itemset)
     */
    public DenseItemset(long[] items, int length) {
      this.items = items;
      this.length = length;
    }

    @Override
    public int length() {
      return length;
    }

    @Override
    public boolean containedIn(BitVector bv) {
      return bv.contains(items);
    }

    @Override
    public long[] getItems() {
      return items;
    }

    @Override
    public int hashCode() {
      return BitsUtil.hashCode(items);
    }

    @Override
    public boolean equals(Object obj) {
      if(this == obj) {
        return true;
      }
      if(obj == null) {
        return false;
      }
      if(!(obj instanceof Itemset) || ((Itemset) obj).length() != 1) {
        return false;
      }
      // TODO: allow comparison to DenseItemset?
      if(getClass() != obj.getClass()) {
        return false;
      }
      return BitsUtil.compare(items, ((DenseItemset) obj).items) == 0;
    }

    @Override
    public int compareTo(Itemset o) {
      int cmp = Integer.compare(length, o.length());
      if(cmp != 0) {
        return cmp;
      }
      DenseItemset other = (DenseItemset) o;
      for(int i = 0; i < items.length; i++) {
        if(items[i] != other.items[i]) {
          return -Long.compare(Long.reverse(items[i]), Long.reverse(other.items[i]));
        }
      }
      return 0;
    }

    @Override
    public StringBuilder appendTo(StringBuilder buf, VectorFieldTypeInformation<BitVector> meta) {
      int i = BitsUtil.nextSetBit(items, 0);
      while(true) {
        String lbl = (meta != null) ? meta.getLabel(i) : null;
        if(lbl == null) {
          buf.append(i);
        }
        else {
          buf.append(lbl);
        }
        i = BitsUtil.nextSetBit(items, i + 1);
        if(i < 0) {
          break;
        }
        buf.append(", ");
      }
      buf.append(": ").append(support);
      return buf;
    }
  }

  /**
   * APRIORI itemset.
   * 
   * @author Erich Schubert
   */
  public static class SmallDenseItemset extends Itemset {
    /**
     * Items, as a bitmask.
     */
    long items;

    /**
     * Itemset length.
     */
    int length;

    /**
     * Constructor.
     * 
     * @param items Items
     * @param length Length (Cardinality of itemset)
     */
    public SmallDenseItemset(long items, int length) {
      this.items = items;
      this.length = length;
    }

    @Override
    public int length() {
      return length;
    }

    @Override
    public boolean containedIn(BitVector bv) {
      return bv.contains(new long[] { items });
    }

    @Override
    public long[] getItems() {
      return new long[] { items };
    }

    @Override
    public int hashCode() {
      return BitsUtil.hashCode(items);
    }

    @Override
    public boolean equals(Object obj) {
      if(this == obj) {
        return true;
      }
      if(obj == null) {
        return false;
      }
      if(!(obj instanceof Itemset) || ((Itemset) obj).length() != 1) {
        return false;
      }
      // TODO: allow comparison to DenseItemset?
      if(getClass() != obj.getClass()) {
        return false;
      }
      return items == ((SmallDenseItemset) obj).items;
    }

    @Override
    public int compareTo(Itemset o) {
      int cmp = Integer.compare(length, o.length());
      if(cmp != 0) {
        return cmp;
      }
      SmallDenseItemset other = (SmallDenseItemset) o;
      return -Long.compare(Long.reverse(items), Long.reverse(other.items));
    }

    @Override
    public StringBuilder appendTo(StringBuilder buf, VectorFieldTypeInformation<BitVector> meta) {
      int i = BitsUtil.nextSetBit(items, 0);
      while(true) {
        String lbl = (meta != null) ? meta.getLabel(i) : null;
        if(lbl == null) {
          buf.append(i);
        }
        else {
          buf.append(lbl);
        }
        i = BitsUtil.nextSetBit(items, i + 1);
        if(i < 0) {
          break;
        }
        buf.append(", ");
      }
      buf.append(": ").append(support);
      return buf;
    }
  }

  /**
   * Parameterization class.
   * 
   * @author Erich Schubert
   * 
   * @apiviz.exclude
   */
  public static class Parameterizer extends AbstractParameterizer {
    /**
     * Parameter for minFreq.
     */
    protected Double minfreq = null;

    /**
     * Parameter for minSupp.
     */
    protected Integer minsupp = null;

    @Override
    protected void makeOptions(Parameterization config) {
      super.makeOptions(config);
      DoubleParameter minfreqP = new DoubleParameter(MINFREQ_ID);
      minfreqP.setOptional(true);
      minfreqP.addConstraint(CommonConstraints.GREATER_EQUAL_ZERO_DOUBLE);
      minfreqP.addConstraint(CommonConstraints.LESS_EQUAL_ONE_DOUBLE);
      if(config.grab(minfreqP)) {
        minfreq = minfreqP.getValue();
      }

      IntParameter minsuppP = new IntParameter(MINSUPP_ID);
      minsuppP.setOptional(true);
      minsuppP.addConstraint(CommonConstraints.GREATER_EQUAL_ZERO_INT);
      if(config.grab(minsuppP)) {
        minsupp = minsuppP.getValue();
      }

      // global parameter constraints
      config.checkConstraint(new OnlyOneIsAllowedToBeSetGlobalConstraint(minfreqP, minsuppP));
      config.checkConstraint(new OneMustBeSetGlobalConstraint(minfreqP, minsuppP));
    }

    @Override
    protected APRIORI makeInstance() {
      if(minfreq != null) {
        return new APRIORI(minfreq);
      }
      if(minsupp != null) {
        return new APRIORI(minsupp);
      }
      return null;
    }
  }
}