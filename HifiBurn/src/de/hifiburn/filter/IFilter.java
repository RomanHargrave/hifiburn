/*
 * HifiBurn 2012
 *
 * IFilter.java
 */
package de.hifiburn.filter;

import de.hifiburn.model.Project;

public interface IFilter
{
  public void doPreFiltering(Project theProject) throws FilterException;
  public void doPostFiltering(Project theProject) throws FilterException;
  public String getId();
  public String getName();
}
