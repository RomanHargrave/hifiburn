/*
 * HifiBurn 2012
 * 
 * Project.java
 */
package de.hifiburn.model;



public class Project extends AbstractModelObject
{
  protected String name = null;
  
  protected Disc disc = null;
  
  /**
   * 
   */
  public Project()
  {
    super();
    name = "";
    disc = new Disc();
  }
  
  /**
   * @return Returns the name.
   */
  public String getName()
  {
    return name;
  }

  /**
   * @param theName The name to set.
   */
  public void setName(String theName)
  {
    firePropertyChange("name", name, name = theName);
  }

  /**
   * @return Returns the disc.
   */
  public Disc getDisc()
  {
    return disc;
  }

  /**
   * @param theDisc The disc to set.
   */
  public void setDisc(Disc theDisc)
  {
    firePropertyChange("disc", disc, disc= theDisc);
  }
}
