/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.system.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author jdowling
 */
/**
 Read and write a file using an explicit encoding.
 Removing the encoding from this code will simply cause the
 system's default encoding to be used instead.
*/
public final class ReadTextFileWithEncoding {


  private final StringBuilder data = new StringBuilder();


  public ReadTextFileWithEncoding(File sourceFile, String encoding)
  throws IOException
  {

//    String NL = System.getProperty("line.separator");
    String NL =  "\r\n";
    Scanner scanner = new Scanner(new FileInputStream(sourceFile), encoding);
    try {
      while (scanner.hasNextLine()){
        data.append(scanner.nextLine()).append(NL);
      }
    }
    finally{
      scanner.close();
    }
  }

  public String getText() {
      return data.toString();
  }

}