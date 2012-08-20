/*
 * HifiBurn 2012
 * 
 * IConverter.java
 */
package de.hifiburn.converter;

import java.io.File;
import java.util.List;

import de.hifiburn.logic.InitializeException;

public interface IConverter
{
  public void initialize() throws ConvertException, InitializeException;
  
  public void convert(File theInput, File theOutput, de.hifiburn.converter.Format theWav, int theBitrate, int theSamplerate) 
      throws ConvertException;

  public boolean supportFormat(File theInput, de.hifiburn.converter.Format theOutputFormat);
  
  public String getName();
  
  public String getId();
  
  public List<String> getExtension();
  
  public boolean canConvert();
}
