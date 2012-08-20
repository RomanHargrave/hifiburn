/*
 * HifiBurn 2012
 * 
 * ConvertException.java
 */
package de.hifiburn.logic;


public class InitializeException extends java.io.IOException
{
  /**
   * 
   */
  public InitializeException()
  {
    super();
  }

  /**
   * @param theMessage
   * @param theCause
   */
  public InitializeException(String theMessage, Throwable theCause)
  {
    super(theMessage, theCause);
  }

  /**
   * @param theMessage
   */
  public InitializeException(String theMessage)
  {
    super(theMessage);
  }

  /**
   * @param theCause
   */
  public InitializeException(Throwable theCause)
  {
    super(theCause);
  }

}
