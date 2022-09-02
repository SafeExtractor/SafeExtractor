package safeextractor.json;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

public class HandleNativeFieldJson {
	public static JSONArray jsonData = new JSONArray();

	public HandleNativeFieldJson(NativeFieldJson nfj) {  
		JSONObject jsonObject = new JSONObject(new LinkedHashMap()); 
		jsonObject.put("name", nfj.getMethodName());
		jsonObject.put("label", nfj.getLabel());
		jsonData.add(jsonObject);
	}
  
	public static HashMap<String, NativeFieldJson> JsonParser(String path) throws Exception {

		File file = new File(path);
		if (!file.exists()) { 
			return null;
		}
		String fileStr = FileUtils.readFileToString(file, "UTF-8"); 
		JSONArray jsonArray = JSONObject.parseArray(fileStr);
		List<Map<String, Object>> jsonListMap = JSON.parseObject(fileStr,
				new TypeReference<List<Map<String, Object>>>() {
				});
		HashMap<String,NativeFieldJson> jsonHashMap = new HashMap<String, NativeFieldJson>(); 
		 
		for (int i = 0; i < jsonArray.size(); i++) {
			Map<String, Object> temp = jsonListMap.get(i);  
			String methodName = temp.get("name").toString(); 
			boolean label =(Integer)(temp.get("label")) == 0 ;  
			NativeFieldJson json = new NativeFieldJson( methodName,label );
			jsonHashMap.put(methodName, json);
		}
		return jsonHashMap;
	}
 
}
