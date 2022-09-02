package safeextractor.utils;

import java.util.HashMap;

public final class Constants {
	
	//----------- need to configure ----------- 
	public static final String Project_Path="/home/safe/SafeExtractor/Implementation/SafeExtractor/"; 
	public static final String EXP1_ROOT = "/home/safe/SafeExtractor/CaseStudy/"; 
    public static final String EXP2_ROOT = "/home/safe/SafeExtractor/Evaluation/"; 
    //----------------------------------------- 
	 
    public static final String EXP1_PROJECT_ROOT = EXP1_ROOT+"/projects/"; 
    public static final String EXP1_RESULT_PATH = EXP1_ROOT+"patch/"; 
    public static final String EXP1_EVALUATE_PATH = EXP1_ROOT+"eval/";
    public static final String EXP1_RUNTIME_PATH = EXP1_ROOT+"runtime/";
    
 
    public static final String EXP2_PROJECT_ROOT =  EXP2_ROOT + "projects/";
    public static final String EXP2_RESULT_PATH = EXP2_ROOT +"patch/"; 
    public static final String EXP2_RUNTIME_PATH = EXP2_ROOT+"runtime/";
    
    
    public static final String JDK_Prefix= Project_Path+"APIData/classes/"; 
	public static final String Native_Method_Label_Json=Project_Path+"/APIData/native.json"; 
	public static final String DATASET_ROOT = Project_Path+"/dataset/"; 
	public static final String USER_DIR =  System.getProperty("user.dir");
    public static final String UNIT_NAME = "myUnit"; 
    
    // 
    public final static int MAX_LINE_LIMITATION = 10000; 
   	public final static long MAX_TIME_LIMITATION = 1000*60*1; 
   	public final static int MAX_LAYER = 5; 
   	public static final HashMap<String, String> CaseStudyHashMap ;
   	static {
   		CaseStudyHashMap = new HashMap<>();
   		CaseStudyHashMap.put("Codec", "commons-codec");
   		CaseStudyHashMap.put("Compress", "commons-compress");
   		CaseStudyHashMap.put("commons-math", "commons-math");
   		CaseStudyHashMap.put("httpcomponents_core_h2", "httpcomponents-core");
   		CaseStudyHashMap.put("jackrabbit-oak", "jackrabbit-oak");
   		CaseStudyHashMap.put("joda-time", "joda-time");
   		CaseStudyHashMap.put("Johnzon_core", "johnzon");
   		CaseStudyHashMap.put("Jsoup", "jsoup");
   		CaseStudyHashMap.put("nifi", "nifi");
   		CaseStudyHashMap.put("Storm_client", "storm");
   	}
}
