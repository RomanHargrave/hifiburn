/*
 * HifiBurn 2012
 * 
 * ConvertException.java
 */
package de.hifiburn.filter;


public class FilterException extends java.io.IOException
{
  /**
   * 
   */
  public FilterException()
  {
    super();
  }

  /**
   * @param theMessage
   * @param theCause
   */
  public FilterException(String theMessage, Throwable theCause)
  {
    super(theMessage, theCause);
  }

  /**
   * @param theMessage
   */
  public FilterException(String theMessage)
  {
    super(theMessage);
  }

  /**
   * @param theCause
   */
  public FilterException(Throwable theCause)
  {
    super(theCause);
  }

}
