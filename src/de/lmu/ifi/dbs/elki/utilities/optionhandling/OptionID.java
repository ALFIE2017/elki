package de.lmu.ifi.dbs.elki.utilities.optionhandling;

import de.lmu.ifi.dbs.elki.algorithm.Algorithm;
import de.lmu.ifi.dbs.elki.database.connection.DatabaseConnection;
import de.lmu.ifi.dbs.elki.normalization.Normalization;
import de.lmu.ifi.dbs.elki.properties.Properties;
import de.lmu.ifi.dbs.elki.utilities.ConstantObject;
import de.lmu.ifi.dbs.elki.varianceanalysis.EigenPairFilter;

/**
 * An OptionID is used by option handlers as a unique identifier for specific
 * options.
 * There is no option possible without a specific OptionID defined
 * within this class.
 *
 * @author Elke Achtert
 */
public final class OptionID extends ConstantObject<OptionID> {

    /**
     * OptionID for {@link de.lmu.ifi.dbs.elki.KDDTask#HELP_FLAG}
     */
    public static final OptionID HELP = new OptionID("h",
        "Flag to obtain help-message, either for the main-routine or for any specified algorithm. " +
        "Causes immediate stop of the program.");

    /**
     * OptionID for {@link de.lmu.ifi.dbs.elki.KDDTask#HELP_LONG_FLAG}
     */
    public static final OptionID HELP_LONG = new OptionID("help",
        "Flag to obtain help-message, either for the main-routine or for any specified algorithm. " +
        "Causes immediate stop of the program.");

    /**
     * OptionID for {@link de.lmu.ifi.dbs.elki.KDDTask#ALGORITHM_PARAM}
     */
    public static final OptionID ALGORITHM = new OptionID("algorithm",
        "Classname of an algorithm " +
        Properties.KDD_FRAMEWORK_PROPERTIES.restrictionString(Algorithm.class) +
        ". Either full name to identify classpath or only classname, if its package is " +
        Algorithm.class.getPackage().getName() + "."
    );

    /**
     * OptionID for {@link de.lmu.ifi.dbs.elki.KDDTask#DESCRIPTION_PARAM}
     */
    public static final OptionID DESCRIPTION = new OptionID("description",
        "Name of a class to obtain a description " +
//            Properties.KDD_FRAMEWORK_PROPERTIES.restrictionString(Parameterizable.class) +
"- for classes that implement " + Parameterizable.class.getName() + ")" +
" -- no further processing will be performed."
    );

    /**
     * OptionID for {@link de.lmu.ifi.dbs.elki.KDDTask#DATABASE_CONNECTION_PARAM}
     */
    public static final OptionID DATABASE_CONNECTION = new OptionID("dbc",
        "Classname of a database connection " +
        Properties.KDD_FRAMEWORK_PROPERTIES.restrictionString(DatabaseConnection.class) +
        ". Either full name to identify classpath or only classname, if its package is " +
        DatabaseConnection.class.getPackage().getName() + "."
    );

    /**
     * OptionID for {@link de.lmu.ifi.dbs.elki.KDDTask#OUTPUT_PARAM}
     * todo richtige beschreibung? oder sind es directories?
     */
    public static final OptionID OUTPUT = new OptionID("out",
        "Name of the file to write the obtained results in. " +
        "If an algorithm requires several outputfiles, the given filename will be used " +
        "as prefix followed by automatically created markers. " +
        "If this parameter is omitted, per default the output will sequentially be given to STDOUT."
    );

    /**
     * OptionID for {@link de.lmu.ifi.dbs.elki.KDDTask#NORMALIZATION_PARAM}
     */
    public static final OptionID NORMALIZATION = new OptionID("norm",
        "Classname of a normalization in order to use a database with normalized values " +
        Properties.KDD_FRAMEWORK_PROPERTIES.restrictionString(Normalization.class) +
        ". "
    );

    /**
     * OptionID for {@link de.lmu.ifi.dbs.elki.KDDTask#NORMALIZATION_PARAM}
     */
    public static final OptionID NORMALIZATION_UNDO = new OptionID("normUndo",
        "Flag to revert result to original values - " +
        "invalid option if no normalization has been performed.");

    /**
     * OptionID for {@link de.lmu.ifi.dbs.elki.algorithm.AbstractAlgorithm#VERBOSE_FLAG}
     */
    public static final OptionID ALGORITHM_VERBOSE = new OptionID("verbose",
        "Flag to allow verbose messages while performing the algorithm.");

    /**
     * OptionID for {@link de.lmu.ifi.dbs.elki.algorithm.AbstractAlgorithm#TIME_FLAG}
     */
    public static final OptionID ALGORITHM_TIME = new OptionID("time",
        "Flag to request output of performance time.");

    /**
     * OptionID for {@link de.lmu.ifi.dbs.elki.algorithm.clustering.subspace.CLIQUE#PRUNE_FLAG}
     */
    public static final OptionID CLIQUE_PRUNE = new OptionID("clique.prune",
        "Flag to indicate that only subspaces with large coverage " +
        "(i.e. the fraction of the database that is covered by the dense units) " +
        "are selected, the rest will be pruned."
    );

    /**
     * OptionID for {@link de.lmu.ifi.dbs.elki.algorithm.clustering.subspace.CLIQUE#TAU_PARAM}
     */
    public static final OptionID CLIQUE_TAU = new OptionID("clique.tau",
        "The density threshold for the selectivity of a unit, where the selectivity is" +
        "the fraction of total feature vectors contained in this unit."
    );

    /**
     * OptionID for {@link de.lmu.ifi.dbs.elki.algorithm.clustering.subspace.CLIQUE#XSI_PARAM}
     */
    public static final OptionID CLIQUE_XSI = new OptionID("clique.xsi",
        "The number of intervals (units) in each dimension."
    );

    /**
     * OptionID for {@link de.lmu.ifi.dbs.elki.algorithm.DependencyDerivator#RANDOM_SAMPLE_FLAG}
     */
    public static final OptionID DEPENDENCY_DERIVATOR_RANDOM_SAMPLE = new OptionID("derivator.randomSample",
        "Flag to use random sample (use knn query around centroid, if flag is not set)."
    );

    /**
     * OptionID for {@link de.lmu.ifi.dbs.elki.algorithm.clustering.subspace.DiSH#EPSILON_PARAM}
     */
    public static final OptionID DISH_EPSILON = new OptionID("dish.epsilon",
        "The maximum radius of the neighborhood " +
        "to be considered in each dimension for determination of " +
        "the preference vector."
    );

    /**
     * OptionID for {@link de.lmu.ifi.dbs.elki.algorithm.clustering.subspace.DiSH#MU_PARAM}
     */
    public static final OptionID DISH_MU = new OptionID("dish.mu",
        "The minimum number of points as a smoothing factor to avoid the single-link-effekt."
    );

    /**
     * OptionID for {@link de.lmu.ifi.dbs.elki.preprocessing.KnnQueryBasedHiCOPreprocessor#K_PARAM}
     */
    public static final OptionID KNN_HICO_PREPROCESSOR_K = new OptionID("hicopreprocessor.k",
        "<The number of nearest neighbors considered in the PCA. " +
        "If this parameter is not set, k ist set to three " +
        "times of the dimensionality of the database objects."
    );

    /**
     * OptionID for {@link de.lmu.ifi.dbs.elki.algorithm.clustering.correlation.ORCLUS#ALPHA_PARAM}
     */
    public static final OptionID ORCLUS_ALPHA = new OptionID("orclus.alpha",
        "The factor for reducing the number of current clusters in each iteration."
    );

    /**
     * OptionID for {@link de.lmu.ifi.dbs.elki.varianceanalysis.AbstractPCA#EIGENPAIR_FILTER_PARAM}
     */
    public static final OptionID PCA_EIGENPAIR_FILTER = new OptionID("pca.filter",
        "Classname of the filter to determine the strong and weak eigenvectors " +
        Properties.KDD_FRAMEWORK_PROPERTIES.restrictionString(EigenPairFilter.class) +
        "."
    );

    /**
     * OptionID for {@link de.lmu.ifi.dbs.elki.algorithm.clustering.subspace.PROCLUS#M_I_PARAM}
     */
    public static final OptionID PROCLUS_M_I = new OptionID("proclus.mi",
        "The multiplier for the initial number of medoids."
    );

    /**
     * The description of the OptionID.
     */
    private String description;

    /**
     * Provides a new OptionID of the given name and description. <p/> All
     * OptionIDs are unique w.r.t. their name. An OptionID provides
     * additionally a description of the option.
     *
     * @param name        the name of the option
     * @param description the description of the option
     */
    private OptionID(final String name, final String description) {
        super(name);
        this.description = description;
    }


    /**
     * Returns the description of this OptionID.
     *
     * @return the description of this OptionID
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this OptionID.
     *
     * @param description the description to be set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets or creates the OptionID for the given class and given name.
     * The OptionID usually is named as the classes name (lowercase) as name-prefix
     * and the given name as suffix of the complete name, separated by a dot.
     * For example, the parameter {@code epsilon} for the class {@link de.lmu.ifi.dbs.elki.algorithm.clustering.DBSCAN}
     * will be named {@code dbscan.epsilon}.
     *
     * @param name        the name
     * @param description the description is also set if the named OptionID does exist already
     * @return the OptionID for the given name
     */
    public static OptionID getOrCreateOptionID(final String name, final String description) {
        OptionID optionID = getOptionID(name);
        if (optionID == null) {
            optionID = new OptionID(name, description);
        }
        else {
            optionID.setDescription(description);
        }
        return optionID;
    }

    /**
     * Returns the OptionID for the given name
     * if it exists, null otherwise.
     *
     * @param name name of the desired OptionID
     * @return the OptionID for the given name
     */
    public static OptionID getOptionID(final String name) {
        return OptionID.lookup(OptionID.class, name);
    }
}
