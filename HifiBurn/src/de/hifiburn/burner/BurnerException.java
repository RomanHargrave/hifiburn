/*
 * HifiBurn 2012
 * 
 * ConvertException.java
 */
package de.hifiburn.burner;


public class BurnerException extends java.io.IOException
{
  /**
   * 
   */
  public BurnerException()
  {
    super();
  }

  /**
   * @param theMessage
   * @param theCause
   */
  public BurnerException(String theMessage, Throwable theCause)
  {
    super(theMessage, theCause);
  }

  /**
   * @param theMessage
   */
  public BurnerException(String theMessage)
  {
    super(theMessage);
  }

  /**
   * @param theCause
   */
  public BurnerException(Throwable theCause)
  {
    super(theCause);
  }

}
