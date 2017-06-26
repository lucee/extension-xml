package org.lucee.xml.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;

public class Utils {

	public static void closeSilent(Reader reader) {
		if(reader!=null) {
			try {
				reader.close();
			}
			catch (IOException e) {}
		}
	}
	public static void closeSilent(Writer writer) {
		if(writer!=null) {
			try {
				writer.close();
			}
			catch (IOException e) {}
		}
	}
	
	public static void closeSilent(InputStream is) {
		if(is!=null) {
			try {
				is.close();
			}
			catch (IOException e) {}
		}
	}
	
	public static void closeSilent(OutputStream os) {
		if(os!=null) {
			try {
				os.close();
			}
			catch (IOException e) {}
		}
	}


	public static String toString(InputStream is, Charset charset) throws IOException {
		return toString(getReader(is, charset));
	}

	public static String toString(File file, Charset charset) throws IOException {
       Reader r = null;
       try {
    	   r = getReader(new FileInputStream(file),charset);
           String str = toString(r);
           return str;
       }
       finally {
           closeSilent(r);
       }
   }
	
	public static String toString(Reader reader) throws IOException {
       StringWriter sw=new StringWriter(512);
       copy(toBufferedReader(reader),sw);
       sw.close();
       return sw.toString();
   }
	
	public static Reader getReader(InputStream is, Charset charset) throws IOException {
		if(charset==null) return new BufferedReader(new InputStreamReader(is));
	    return new BufferedReader(new InputStreamReader(is,charset));
	}
	
	public static BufferedReader toBufferedReader(Reader r) {
		if(r instanceof BufferedReader) return (BufferedReader) r;
		return new BufferedReader(r);
	}
	
	private static final void copy(Reader r, Writer w) throws IOException {
        char[] buffer = new char[0xffff];
        int len;
        while((len = r.read(buffer)) !=-1)
          w.write(buffer, 0, len);
    }

	public static boolean isEmpty(String str) {
		return isEmpty(str,true);
	}
	public static boolean isEmpty(String str, boolean trim) {
		if(str==null) return true;
		return trim?str.trim().length()==0:str.length()==0;
	}
}
