package de.lmu.ifi.dbs.preprocessing;

import de.lmu.ifi.dbs.data.RealVector;
import de.lmu.ifi.dbs.database.Database;
import de.lmu.ifi.dbs.distance.DoubleDistance;
import de.lmu.ifi.dbs.utilities.QueryResult;
import de.lmu.ifi.dbs.utilities.optionhandling.AttributeSettings;
import de.lmu.ifi.dbs.utilities.optionhandling.NoParameterValueException;
import de.lmu.ifi.dbs.utilities.optionhandling.OptionHandler;
import de.lmu.ifi.dbs.utilities.optionhandling.UnusedParameterException;

import java.util.ArrayList;
import java.util.List;

/**
 * Computes the correlation dimension of objects of a certain database.
 * The PCA is based on k nearest neighbor queries.
 *
 * @author Elke Achtert (<a href="mailto:achtert@dbs.ifi.lmu.de">achtert@dbs.ifi.lmu.de</a>)
 */
public class KnnQueryBasedCorrelationDimensionPreprocessor extends CorrelationDimensionPreprocessor {

  /**
   * Undefined value for k.
   */
  public static final int UNDEFINED_K = -1;

  /**
   * Option string for parameter k.
   */
  public static final String K_P = "k";

  /**
   * Description for parameter k.
   */
  public static final String K_D = "<k>a integer specifying the number of " +
                                   "nearest neighbors considered in the PCA. " +
                                   "If this value is not defined, k ist set to three " +
                                   "times of the dimensionality of the query object";

  /**
   * The number of nearest neighbors considered in the PCA.
   */
  private int k;

  /**
   * Provides a new Preprocessor that computes the correlation dimension of
   * objects of a certain database based on a k nearest neighbor query.
   */
  public KnnQueryBasedCorrelationDimensionPreprocessor() {
    super();
    parameterToDescription.put(K_P + OptionHandler.EXPECTS_VALUE, K_D);
    optionHandler = new OptionHandler(parameterToDescription, getClass().getName());
  }

  /**
   * @see CorrelationDimensionPreprocessor#objectIDsForPCA(Integer, de.lmu.ifi.dbs.database.Database, boolean)
   */
  protected List<Integer> objectIDsForPCA(Integer id, Database<RealVector> database, boolean verbose) {
    if (k == UNDEFINED_K) {
      RealVector obj = database.get(id);
      k = 3 * obj.getDimensionality();
    }

    pcaDistanceFunction.setDatabase(database, verbose);
    List<QueryResult<DoubleDistance>> knns = database.kNNQueryForID(id, k, pcaDistanceFunction);

    List<Integer> ids = new ArrayList<Integer>(knns.size());
    for (QueryResult knn : knns) {
      ids.add(knn.getID());
    }

    return ids;
  }

  /**
   * Sets the value for the parameter k. If k is not specified, the default value is used.
   *
   * @see de.lmu.ifi.dbs.utilities.optionhandling.Parameterizable#setParameters(String[])
   */
  public String[] setParameters(String[] args) throws IllegalArgumentException {
    String[] remainingParameters = super.setParameters(args);

    if (optionHandler.isSet(K_P)) {
      try {
        k = Integer.parseInt(optionHandler.getOptionValue(K_P));
        if (k < 0)
          throw new IllegalArgumentException("KnnQueryBasedCorrelationDimensionPreprocessor: k has to be greater than zero!");
      }
      catch (UnusedParameterException e) {
        throw new IllegalArgumentException(e);
      }
      catch (NoParameterValueException e) {
        throw new IllegalArgumentException(e);
      }
    }
    else {
      k = UNDEFINED_K;
    }

    return remainingParameters;
  }

  /**
   * Returns the parameter setting of the attributes.
   *
   * @return the parameter setting of the attributes
   */
  public List<AttributeSettings> getAttributeSettings() {
    List<AttributeSettings> result = super.getAttributeSettings();

    AttributeSettings attributeSettings = result.get(0);
    attributeSettings.addSetting(K_P, Integer.toString(k));

    return result;
  }

  /**
   * Returns a description of the class and the required parameters.
   * <p/>
   * This description should be suitable for a usage description.
   *
   * @return String a description of the class and the required parameters
   */
  public String description() {
    StringBuffer description = new StringBuffer();
    description.append(KnnQueryBasedCorrelationDimensionPreprocessor.class.getName());
    description.append(" computes the correlation dimension of objects of a certain database.\n");
    description.append("The PCA is based on k nearest neighbor queries.\n");
    description.append(optionHandler.usage("", false));
    return description.toString();
  }
}
