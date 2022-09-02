package extractvariable.detector;

import extractvariable.ast.ASTPart;
import extractvariable.ast.ExceptionMethod;
import extractvariable.ast.middlecode.MiddleCode;
import extractvariable.ast.rw.reader.ReaderSet;
import org.eclipse.jdt.core.dom.*;

import extractvariable.ast.rw.writer.WriterSet;
import safeextractor.json.HandleNativeFieldJson;
import safeextractor.json.NativeFieldJson;
import safeextractor.log.MyLog;
import safeextractor.utils.Constants;
import safeextractor.utils.GlobalClass;

import java.util.*;

public class Comparator {
	String projectPath;
	ASTNode commonParent;
	public static boolean sameFlag;
	public static boolean extractFlag;
	public static ExceptionMethod exceptionMethod;
	public static HashSet<String> visitedNativeMethodSet;
	public static MethodDeclaration methodDeclaration;
	public static Set<String> expressionReaderSet;
	public static Set<String> expressionWriterSet;

	public Comparator(int firstOffset, int secondOffset, ASTNode expression1, ASTNode expression2, ASTNode commonParent)
			throws Exception {
		this.commonParent = commonParent;
		this.projectPath = GlobalClass.JavaProject.getPath().toString();

		ASTPart aSTPart = new ASTPart(GlobalClass.iCompilationUnit);
		if (  methodDeclaration == null) {
			return;
		}
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();

		MiddleCode middleCode = new MiddleCode(firstOffset, secondOffset, expression1, expression2, commonParent,
				aSTPart.getCompilationUnit(), methodBinding.getDeclaringClass().getKey(), expressionWriterSet,
				expressionReaderSet);
		sameFlag = middleCode.valid;  
		 
		if (sameFlag && Comparator.exceptionMethod.isConflict()) {
			MyLog.add("exp conf");
			sameFlag = false;
		}
		return;
	}


	public static boolean isSame() {
		if (!sameFlag) {
			MyLog.add("reject refactor");
		} else {
			MyLog.add("allow refactor");
		}
		return sameFlag;
	}

	public static boolean isExtract() {
		return extractFlag;
	}
 
	private static boolean isHarmfulNativeMethod() {
		try {
			HashMap<String, NativeFieldJson> map = HandleNativeFieldJson.JsonParser(Constants.Native_Method_Label_Json);
			for (String s : visitedNativeMethodSet) { 
				NativeFieldJson temp = map.get(s);
				if (temp != null) {
				
					if (temp.getLabel() == false) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	// 恢复静态变量
	public static void restore() {
		exceptionMethod = new ExceptionMethod();
		visitedNativeMethodSet = new HashSet<>();
		methodDeclaration = null;
		expressionReaderSet = new HashSet<>();
		expressionWriterSet = new HashSet<>();
		extractFlag = true;
		sameFlag = true;
	}

	// 初始化
	public static void init(ASTNode commonExpression, ASTNode commonParent) throws Exception {
		MiddleCode.init(); 
		if (commonExpression instanceof MethodInvocation
				&& ((MethodInvocation) commonExpression).getExpression() instanceof ClassInstanceCreation) {
			MyLog.add("exp side effect");
			extractFlag = false;
			return;
		}
		ASTPart aSTPart = new ASTPart(GlobalClass.iCompilationUnit); 
	 
		ASTNode temp= commonParent;
		while(temp!=null) {
			if(temp instanceof MethodDeclaration  ) {
				methodDeclaration= (MethodDeclaration)temp;
				break;
			} 
			temp=temp.getParent();
		}  
		if (methodDeclaration == null) {
			return;
		}
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		WriterSet preExpWriterSet = new WriterSet(commonExpression, -1, methodBinding.getDeclaringClass().getKey(),
				commonExpression);
		ReaderSet expReaderSet = new ReaderSet(commonExpression, 0, methodBinding.getDeclaringClass().getKey(),
				commonExpression);
		WriterSet expWriterSet = new WriterSet(commonExpression, 0, methodBinding.getDeclaringClass().getKey(),
				commonExpression);
		MyLog.add("-----------------sub exp---------------");
		preExpWriterSet.print();
		expReaderSet.print();
		expWriterSet.print();
		MyLog.add("------------------------------------------");
		expressionWriterSet = new HashSet<String>(expWriterSet.getResList());
		expressionReaderSet = new HashSet<String>(expReaderSet.getResList());
		if (preExpWriterSet.getResList().size() > 0 || isHarmfulNativeMethod()) {
			MyLog.add("exp side effect！");
			extractFlag = false; 
		}
	}

	public static boolean isNoOverLap(List<String> list) {
		for (int i = 0; i < list.size(); ++i) {
			String s = (String) list.get(i);
			if (expressionWriterSet.contains(s) || expressionReaderSet.contains(s)) {
				return false;
			}
		}
		return true;
	} 
}
