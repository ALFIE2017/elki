/*
 * This file is part of ELKI:
 * Environment for Developing KDD-Applications Supported by Index-Structures
 *
 * Copyright (C) 2017
 * ELKI Development Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.lmu.ifi.dbs.elki.visualization;

import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.result.Result;
import de.lmu.ifi.dbs.elki.visualization.visualizers.VisFactory;

/**
 * Container class, with ugly casts to reduce generics crazyness.
 *
 * @author Erich Schubert
 * @since 0.4.0
 *
 * @apiviz.landmark
 * @apiviz.has VisualizerContext
 * @apiviz.has VisFactory
 */
public class VisualizationTask implements VisualizationItem, Comparable<VisualizationTask> {
  /**
   * Rendering flags enum.
   *
   * @author Erich Schubert
   */
  public static enum RenderFlag {
    /** Do not use thumbnails for easy visualizations. */
    NO_THUMBNAIL(1),
    /** Do not allow full-screening this task */
    NO_DETAIL(2),
    /** Do not include when exporting */
    NO_EXPORT(4),
    /** Do not embed */
    NO_EMBED(8);

    /**
     * Bit.
     */
    public final int bit;

    /**
     * Constructor.
     *
     * @param bit Bit
     */
    private RenderFlag(int bit) {
      this.bit = bit;
    }
  }

  /**
   * Update flags enum.
   * 
   * TODO: can these be replaced by listeners?
   *
   * @author Erich Schubert
   */
  public static enum UpdateFlag {
    /** When data changes */
    ON_DATA(1),
    /** When selection changes */
    ON_SELECTION(2),
    /** When style changes */
    ON_STYLEPOLICY(4),
    /** When sample changes */
    ON_SAMPLE(8);

    /**
     * Bit.
     */
    public final int bit;

    /**
     * Constructor.
     *
     * @param bit Bit
     */
    private UpdateFlag(int bit) {
      this.bit = bit;
    }
  }

  /**
   * Meta data key: Level for visualizer ordering
   *
   * Returns an integer indicating the "height" of this Visualizer. It is
   * intended to impose an ordering on the execution of Visualizers as a
   * Visualizer may depend on another Visualizer running earlier. <br>
   * Lower numbers should result in a earlier use of this Visualizer, while
   * higher numbers should result in a later use. If more Visualizers have the
   * same level, no ordering is guaranteed. <br>
   * Note that this value is only a recommendation, as it is totally up to the
   * framework to ignore it.
   */
  public int level = 0;

  /**
   * Flag to control visibility.
   */
  public boolean visible = true;

  /**
   * Render capabilities
   */
  private int flags;

  /**
   * Flag to signal default visibility of a visualizer.
   */
  public boolean default_visibility = true;

  /**
   * Flag to mark the visualizer as tool.
   */
  public boolean tool = false;

  /**
   * Background layer
   */
  public static final int LEVEL_BACKGROUND = 0;

  /**
   * Data layer
   */
  public static final int LEVEL_DATA = 100;

  /**
   * Static plot layer
   */
  public static final int LEVEL_STATIC = 200;

  /**
   * Passive foreground layer
   */
  public static final int LEVEL_FOREGROUND = 300;

  /**
   * Active foreground layer (interactive elements)
   */
  public static final int LEVEL_INTERACTIVE = 1000;

  /**
   * The update event mask. See {@link #ON_DATA}, {@link #ON_SELECTION},
   * {@link #ON_STYLEPOLICY}, {@link #ON_SAMPLE}.
   */
  public int updatemask;

  /**
   * Name
   */
  String name;

  /**
   * The factory
   */
  VisFactory factory;

  /**
   * The result we are attached to
   */
  Object result;

  /**
   * The main representation
   */
  Relation<?> relation;

  /**
   * Width request
   */
  public double reqwidth = 1.0;

  /**
   * Height request
   */
  public double reqheight = 1.0;

  /**
   * Visualization task.
   * 
   * @param factory Factory
   * @param name Name
   * @param result Result
   * @param relation Relation to use
   */
  public VisualizationTask(VisFactory factory, String name, Object result, Relation<?> relation) {
    super();
    this.name = name;
    this.result = result;
    this.relation = relation;
    this.factory = factory;
  }

  /**
   * Set (OR) the update flags.
   *
   * @param bits Bits to set
   * @return {@code this}, for method chaining.
   */
  public VisualizationTask with(UpdateFlag f) {
    updatemask |= f.bit;
    return this;
  }

  /**
   * Update if any oft these bits is set.
   *
   * @param bits Bits to check.
   * @return {@code true} if any bit is set.
   */
  public boolean has(UpdateFlag f) {
    return (updatemask & f.bit) != 0;
  }

  /**
   * Set a task flag.
   *
   * @param bits Flag to set
   * @return {@code this}, for method chaining.
   */
  public VisualizationTask with(RenderFlag f) {
    flags |= f.bit;
    return this;
  }

  /**
   * Update if any oft these flags is set.
   *
   * @param bits Bits to check.
   * @return {@code true} if any bit is set.
   */
  public boolean has(RenderFlag f) {
    return (flags & f.bit) != 0;
  }

  /**
   * Set the level of a visualization.
   * 
   * @param level Level
   * @return {@code this}, for method chaining.
   */
  public VisualizationTask level(int level) {
    this.level = level;
    return this;
  }

  /**
   * Flag as tool visualizer.
   * 
   * TODO: don't use a separate boolean for this, but e.g. interface?
   * 
   * @param t Tool flag
   * @return {@code this}, for method chaining.
   */
  public VisualizationTask tool(boolean t) {
    this.tool = t;
    return this;
  }

  /**
   * Set the size request.
   *
   * @param w Width
   * @param h Height
   * @return {@code this}, for method chaining.
   */
  public VisualizationTask requestSize(double w, double h) {
    this.reqwidth = w;
    this.reqheight = h;
    return this;
  }

  /**
   * Init the default visibility of a task.
   *
   * @param vis Visibility.
   * @return {@code this}, for method chaining.
   */
  public VisualizationTask defaultVisibility(boolean vis) {
    visible = vis;
    default_visibility = vis;
    return this;
  }

  /**
   * Get the visualizer factory.
   *
   * @return Visualizer factory
   */
  public VisFactory getFactory() {
    return factory;
  }

  @SuppressWarnings("unchecked")
  public <R> R getResult() {
    return (R) result;
  }

  @SuppressWarnings("unchecked")
  public <R extends Relation<?>> R getRelation() {
    return (R) relation;
  }

  @Override
  public String getMenuName() {
    return name;
  }

  @Override
  public int compareTo(VisualizationTask other) {
    // sort by levels first
    if(this.level != other.level) {
      return this.level - other.level;
    }
    // sort by name otherwise.
    String name1 = this.getMenuName();
    String name2 = other.getMenuName();
    if(name1 != null && name2 != null && name1 != name2) {
      return name1.compareTo(name2);
    }
    return 0;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("VisTask: ").append(factory.getClass().getName()).append(' ');
    if(result != null && result instanceof Result) {
      buf.append("Result: ").append(((Result) result).getLongName()).append(' ');
    }
    buf.append(super.toString());
    return buf.toString();
  }

  @Override
  public int hashCode() {
    // We can't have our hashcode change with the map contents!
    return System.identityHashCode(this);
  }

  @Override
  public boolean equals(Object o) {
    // Also don't inherit equals based on list contents!
    return (this == o);
  }
}
