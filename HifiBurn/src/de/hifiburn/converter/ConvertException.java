/*
 * HifiBurn 2012
 * 
 * ConvertException.java
 */
package de.hifiburn.converter;


public class ConvertException extends java.io.IOException
{
  /**
   * 
   */
  public ConvertException()
  {
    super();
  }

  /**
   * @param theMessage
   * @param theCause
   */
  public ConvertException(String theMessage, Throwable theCause)
  {
    super(theMessage, theCause);
  }

  /**
   * @param theMessage
   */
  public ConvertException(String theMessage)
  {
    super(theMessage);
  }

  /**
   * @param theCause
   */
  public ConvertException(Throwable theCause)
  {
    super(theCause);
  }

}
