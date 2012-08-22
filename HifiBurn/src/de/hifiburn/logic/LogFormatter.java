/*
 * HifiBurn 2012
 *
 * LogFormatter.java
 */
package de.hifiburn.logic;

import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class LogFormatter extends SimpleFormatter
{

  @Override
  public String format(LogRecord theRecord)
  {
    String _ret = super.format(theRecord);
    if (System.getProperty("os.name").toLowerCase().indexOf("win")>=0)
    {
      System.out.println("REPLACE");
      return _ret.replace("\r\n", "\n").replace("\n", "\r\n");
    }
    
    return _ret;
  }
}
