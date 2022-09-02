package safeextractor.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;

import safeextractor.dataset.ElemInfo;
import safeextractor.dataset.ExtractVariableRecord;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HandleDataSetJsonFile {

    static JSONArray jsonData= new JSONArray();
    
    public HandleDataSetJsonFile(ExtractVariableRecord extractVariableRecord) {

        JSONObject jsonObject =new JSONObject(new LinkedHashMap());
        jsonObject.put("before commit", extractVariableRecord.getBeforeCommit());
        jsonObject.put("after commit",extractVariableRecord.getAfterCommit());
        jsonObject.put("file path",extractVariableRecord.getFilePath());
        jsonObject.put("old name",extractVariableRecord.getOldName());
        jsonObject.put("new name",extractVariableRecord.getNewName());
        jsonObject.put("type",extractVariableRecord.getType());

        JSONObject variablePlaceJsonObject = new JSONObject(new LinkedHashMap());
        variablePlaceJsonObject.put("start line",extractVariableRecord.getVariablePlace().getStartLine());
        variablePlaceJsonObject.put("start column",extractVariableRecord.getVariablePlace().getStartColumn());
        variablePlaceJsonObject.put("end line",extractVariableRecord.getVariablePlace().getEndLine());
        variablePlaceJsonObject.put("end column",extractVariableRecord.getVariablePlace().getEndColumn());
        jsonObject.put("variable place",variablePlaceJsonObject);

        JSONArray beforeArray = new JSONArray();
        for (ElemInfo elemInfo:extractVariableRecord.getBeforeSet()) {
            JSONObject tempJsonObject = new JSONObject(new LinkedHashMap());
            tempJsonObject.put("start line",elemInfo.getStartLine());
            tempJsonObject.put("start column",elemInfo.getStartColumn());
            tempJsonObject.put("end line",elemInfo.getEndLine());
            tempJsonObject.put("end column",elemInfo.getEndColumn());
            beforeArray.add( tempJsonObject);
        }

        JSONArray afterArray = new JSONArray();
        for (ElemInfo elemInfo:extractVariableRecord.getAfterSet()) {
            JSONObject tempJsonObject = new JSONObject(new LinkedHashMap());
            tempJsonObject.put("start line",elemInfo.getStartLine());
            tempJsonObject.put("start column",elemInfo.getStartColumn());
            tempJsonObject.put("end line",elemInfo.getEndLine());
            tempJsonObject.put("end column",elemInfo.getEndColumn());
            afterArray.add( tempJsonObject);
       }

        jsonObject.put("before location list",beforeArray );
        jsonObject.put("after location list",afterArray );
        jsonData.add(jsonObject);
    }
    
    public HandleDataSetJsonFile(Exp2Record exp2Record) { 
        JSONObject jsonObject =new JSONObject(new LinkedHashMap());
        jsonObject.put("no", exp2Record.getNo());
        jsonObject.put("project name",exp2Record.getProjectName());
        jsonObject.put("old name",exp2Record.getOldName());
        jsonObject.put("new name",exp2Record.getNewName());
        jsonObject.put("path",exp2Record.getPath());
        jsonObject.put("offset",exp2Record.getOffset());
        jsonObject.put("length",exp2Record.getLength()); 
        jsonData.add(jsonObject);
    }
    public static boolean createJsonFile( String filePath) throws FileNotFoundException, UnsupportedEncodingException {
        String content = JSON.toJSONString(jsonData, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteDateUseDateFormat);
        boolean flag = true;
        try {
            File file = new File(filePath);
            if (!file.getParentFile().exists()) { 
                file.getParentFile().mkdirs();
            }
            if (file.exists()) { 
                file.delete();
            }
            file.createNewFile();
            Writer write = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            write.write(content);
            write.flush();
            write.close();
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }

    public static void clear(){ 
        jsonData=new JSONArray();
    }
    public  static int sum=0;
    public static ArrayList<Exp1Record> JsonParserExp1(String path) throws Exception{
        File file = new File(path);
        if(!file.exists()) {
        	 
        }
        String projectName=file.getName().replace(".json", "");
        String fileStr = FileUtils.readFileToString(file,"UTF-8");//前面两行是读取文件
        JSONArray jsonArray =  JSONObject.parseArray(fileStr);
        List<Map<String, Object>> jsonListMap = JSON.parseObject(fileStr, new TypeReference<List<Map<String,Object>>>(){});
        ArrayList<Exp1Record> res = new ArrayList<>();
        for (int i=0;i<jsonArray.size();i++) {
          Map<String, Object> temp = jsonListMap.get(i); 
          JSONObject obj=((JSONArray)temp.get("before location list")).getJSONObject(0);
          ElemInfo tempElemInfo= new ElemInfo(obj.getIntValue("start line"),obj.getIntValue("end column"),
        		  obj.getIntValue("start column"),obj.getIntValue("end column"));
          
//          Record tempRecord=new Record(projectName,temp.get("after commit").toString(), temp.get("old name").toString(),
//        		  temp.get("new name").toString(),temp.get("file path").toString(), tempElemInfo.getStartLine(), tempElemInfo.getStartColumn());
          Exp1Record tempRecord=new Exp1Record(projectName,i+1,temp.get("after commit").toString(), temp.get("old name").toString(),
        		  temp.get("new name").toString(),temp.get("file path").toString(), tempElemInfo.getStartLine(), tempElemInfo.getStartColumn());
          
          int line=tempRecord.getLine();
          int column=-1;//tempRecord.getColumn();
          for(Object tempObj:((JSONArray)temp.get("before location list"))) {
        	  JSONObject jsonObj=(JSONObject)tempObj;
        	  line=Math.min(line,jsonObj.getIntValue("start line"));
        	  column=Math.min(column,jsonObj.getIntValue("column line"));
        	  ElemInfo myTempElemInfo= new ElemInfo(jsonObj.getIntValue("start line"),jsonObj.getIntValue("end column"),
        			  jsonObj.getIntValue("start column"),jsonObj.getIntValue("end column"));
        	  tempRecord.getElemList().add(myTempElemInfo);
          }
         
          tempRecord.setLine(line);
          tempRecord.setColumn(column);
          
          res.add(tempRecord);
        }
        return res;
    } 
    public static ArrayList<Exp2Record> JsonParserExp2(String path) throws Exception{
        File file = new File(path);
        if(!file.exists()) { 
        } 
        String fileStr = FileUtils.readFileToString(file,"UTF-8"); 
        JSONArray jsonArray =  JSONObject.parseArray(fileStr);
        List<Map<String, Object>> jsonListMap = JSON.parseObject(fileStr, new TypeReference<List<Map<String,Object>>>(){});
        ArrayList<Exp2Record> res = new ArrayList<>();
        for (int i=0;i<jsonArray.size();i++) {
          Map<String, Object> temp = jsonListMap.get(i);   
          Exp2Record tempRecord=new Exp2Record(temp.get("project name").toString(),
        		 ( (Integer)(temp.get("no"))).intValue(),temp.get("old name").toString(),
        		  temp.get("new name").toString(), temp.get("path").toString(), 
        		  ( (Integer)(temp.get("offset"))).intValue(),
        		  ( (Integer)(temp.get("length"))).intValue()); 
          res.add(tempRecord);
        }
        return res;
    }
} 