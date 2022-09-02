package safeextractor.json;

import extractvariable.ast.Field;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;

import org.apache.commons.io.FileUtils;
import java.io.*;
import java.util.*;

public class HandleAPIJsonFile {
	public static JSONArray jsonData = new JSONArray();

	public HandleAPIJsonFile(APIJson apiJson) {

		JSONObject jsonObject = new JSONObject(new LinkedHashMap());
		jsonObject.put("class name", apiJson.getClassName());
		jsonObject.put("method name", apiJson.getMethodName());
		jsonObject.put("method key", apiJson.getMethodKey());

		JSONArray readerArray = new JSONArray();
		for (Field field : apiJson.getReadList()) {
			JSONObject tempJsonObject = new JSONObject(new LinkedHashMap());
			tempJsonObject.put("name", field.getName());
			tempJsonObject.put("static label", field.getIsStatic());
			readerArray.add(tempJsonObject);
		}
		JSONArray writerArray = new JSONArray();
		for (Field field : apiJson.getWriteList()) {
			JSONObject tempJsonObject = new JSONObject(new LinkedHashMap());
			tempJsonObject.put("name", field.getName());
			tempJsonObject.put("static label", field.getIsStatic());
			writerArray.add(tempJsonObject);
		}

		JSONArray nativeArray = new JSONArray();
		for (Field field : apiJson.getNativeList()) {
			JSONObject tempJsonObject = new JSONObject(new LinkedHashMap());
			tempJsonObject.put("name", field.getName());
			tempJsonObject.put("static label", field.getIsStatic());
			nativeArray.add(tempJsonObject);
		}

		jsonObject.put("read list", readerArray);
		jsonObject.put("write list", writerArray);
		jsonObject.put("native method list", nativeArray);
		jsonData.add(jsonObject);
	}
	public static boolean createJsonFile(String filePath) throws FileNotFoundException, UnsupportedEncodingException {
		String content = JSON.toJSONString(jsonData, SerializerFeature.PrettyFormat,
				SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat);
		
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

	public static HashMap<String, APIJson> JsonParser(String path) throws Exception {

		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		String fileStr = FileUtils.readFileToString(file, "UTF-8");
		JSONArray jsonArray = JSONObject.parseArray(fileStr);
		List<Map<String, Object>> jsonListMap = JSON.parseObject(fileStr,
				new TypeReference<List<Map<String, Object>>>() {
				});
		HashMap<String, APIJson> apiJsonHashMap = new HashMap<String, APIJson>(); 
		 
		for (int i = 0; i < jsonArray.size(); i++) {
			Map<String, Object> temp = jsonListMap.get(i);
			String className = temp.get("class name").toString();
			String methodName = temp.get("method name").toString();
			String methodKey = temp.get("method key").toString();
			List<Field> readList= parserList(temp,"read list");
			List<Field> writeList =parserList(temp,"write list");
			List<Field> nativeList =parserList(temp,"native method list");
 
			APIJson apiJson = new APIJson(className, methodName, methodKey, readList, writeList, nativeList);
			apiJsonHashMap.put(methodKey, apiJson);
		}
		return apiJsonHashMap;
	}

	/**
	 * @param temp
	 * @param readList
	 */
	public static  List<Field> parserList(Map<String, Object> temp,String type) {
		 List<Field> list= new ArrayList<Field>();
		for (Object tempObj : ((JSONArray) temp.get(type))) {
			JSONObject jsonObj = (JSONObject) tempObj;
			Field field = new Field(jsonObj.get("name").toString(), jsonObj.getIntValue("static label") == 1);
			list.add(field);
		}
		return list;
	}

	public static void DataJsonParser(String path) throws Exception {

		File file = new File(path);

		String fileStr = FileUtils.readFileToString(file, "UTF-8");
		JSONArray jsonArray = JSONObject.parseArray(fileStr);
		List<Map<String, Object>> jsonListMap = JSON.parseObject(fileStr,
				new TypeReference<List<Map<String, Object>>>() {
				}); 
	}
}
