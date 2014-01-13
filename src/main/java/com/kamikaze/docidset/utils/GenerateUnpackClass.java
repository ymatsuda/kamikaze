package com.kamikaze.docidset.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Generate the hardwired unpack code to read every k bits from 128 integers .
 * This is highly optimized to speed up the decompression speed of
 * decoding b-bit slots (that is,  non-exceptions) in PForDelta algorithm
 * @author hyan
 *
 */
public class GenerateUnpackClass {
  
  public static void main(String[] args)
  {
    try
    {
      generatePForDeltaUnpackClass("com.kamikaze.pfordelta", 128, "./PForDeltaUnpack128.txt");
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  
  static public void generatePForDeltaUnpackClass(String packageName, int blockSize, String filename) throws IOException
  {
    PrintWriter pw = new PrintWriter(new FileOutputStream(filename));
    pw.println("package " +  packageName + ";");
    pw.println(" ");
    pw.println(" ");    
    
    pw.println("public class PForDeltaUnpack128{");
    pw.println(" ");
    generatePForDeltaFunctionSelectionFile(pw);
    int HEADER_BITS = 32 ; // two int header
    for(int i=0; i<POSSIBLE_B.length; ++i)
    {
      pw.println(" ");
      generatePForDeltaUnpackFileEach32(pw, HEADER_BITS, blockSize, POSSIBLE_B[i]);
    }
    
    pw.println("}");
    pw.close();
   
  }
  
  private static int[] POSSIBLE_B =  {0, 1,2,3,4,5,6,7,8,9,10,11,12,13,16,20,28}; 
  
  private static final int[] MASK = {0x00000000,
    0x00000001, 0x00000003, 0x00000007, 0x0000000f, 0x0000001f, 0x0000003f,
    0x0000007f, 0x000000ff, 0x000001ff, 0x000003ff, 0x000007ff, 0x00000fff,
    0x00001fff, 0x00003fff, 0x00007fff, 0x0000ffff, 0x0001ffff, 0x0003ffff,
    0x0007ffff, 0x000fffff, 0x001fffff, 0x003fffff, 0x007fffff, 0x00ffffff,
    0x01ffffff, 0x03ffffff, 0x07ffffff, 0x0fffffff, 0x1fffffff, 0x3fffffff,
    0x7fffffff, 0xffffffff};
  
  static private void generatePForDeltaUnpackFileEach32(PrintWriter pw, int inOffset, int n, int bits)
  {
    pw.println("  static private void unpack" + bits + "(int[] out, int[] in)");
    pw.println("  {");
    if(bits>0)
    {
    pw.println("  int i, w;");
    pw.println("  int outOffset = " +  0 + ";");
    pw.println("  final int mask = " +  MASK[bits] + ";");
    int index,skip;
    pw.println("  for(i=0, w=" + (inOffset>>>5) + "; i<" + (n/32) + "; ++i, w+=" + bits + "){");
      int localInOffset = 0;
      
      int prevIndex = -1;
      for(int i=0 ; i<32; ++i, localInOffset+=bits)
      {
        index = localInOffset >>> 5;
        if(index != prevIndex)
        {
          if(index == 0)
          {
            pw.println("    int curInputValue" + index + " = in[w];");
          }
          else
          {
            pw.println("    int curInputValue" + index + " = in[w+" + index +"];");
          }
          prevIndex = index;
        }
      }
      
      prevIndex = -1;
      localInOffset = 0;
      for(int i=0 ; i<32; ++i, localInOffset+=bits)
      {
        index = localInOffset >>> 5;
        skip = localInOffset & 0x1f;
        if(skip == 0)
          pw.println("    out[" + i + "+outOffset] = curInputValue" + index + " & mask;");
        else
        {
          if (32 - skip < bits) {
            pw.println("    out[" + i + "+outOffset] = ((curInputValue" + index + " >>> " + (skip) +  ") | (curInputValue" + (index+1) + " << " +  (32-skip) + ")) & mask;");
          }
          else if(32-skip == bits)
          {
            pw.println("    out[" + i + "+outOffset] = curInputValue" + index + " >>> " + (skip) +  ";");
          }
          else
          {
            pw.println("    out[" + i + "+outOffset] = (curInputValue" + index + " >>> " + (skip) + ") & mask;");
          }
        }
       
      }
      pw.println("    outOffset += 32;");
      pw.println("  }");
    }
    pw.println("  }");
  }

  static private void generatePForDeltaFunctionSelectionFile(PrintWriter pw)
  {
     pw.println("  static public void unpack(int[] out, int[] in, int bits) {" );
     pw.println("    switch (bits) {");
     
     for(int i=0; i<POSSIBLE_B.length; i++)
     {
       int bits = POSSIBLE_B[i];  
       pw.println("      case " + bits + ":  " + "unpack" + bits + "(out, in); break;");
     }
     pw.println("      default: break;");
     pw.println("    }");
     pw.println("  }");
  }
}
