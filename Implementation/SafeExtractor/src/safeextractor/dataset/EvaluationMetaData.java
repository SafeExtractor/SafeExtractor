package safeextractor.dataset;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import safeextractor.log.MyLog;

public class EvaluationMetaData {
	 public static List<String> metadatalist=new ArrayList<>();
	 public static void writeToCsv(String path) {
	        File writeFile = new File(path); 
	        if (!writeFile.getParentFile().exists()) { 
				writeFile.getParentFile().mkdirs();
			}
			if (writeFile.exists()) { 
				writeFile.delete();
			}
	        
	        try{
	            BufferedWriter writeText = new BufferedWriter(new FileWriter(writeFile)); 
	            writeText.write("ID,Project Name,SHA,New Name,Label,Approach");
	            MyLog.add("ID,Project Name,SHA,New Name,Label,Approach");
	            for(String s :metadatalist){
	                writeText.newLine();    
	                MyLog.add(s);
	                writeText.write(s); 
	            }
	            writeText.flush();
	            writeText.close();
	        }catch (FileNotFoundException e){
	        	MyLog.add("error");
	        }catch (IOException e){
	        	MyLog.add("error");
	        }
	    }
}
