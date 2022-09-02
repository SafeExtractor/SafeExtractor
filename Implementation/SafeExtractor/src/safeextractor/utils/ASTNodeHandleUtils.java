package safeextractor.utils;

import extractvariable.api.APIFieldRecord;
import extractvariable.api.APIRecorder;
import extractvariable.ast.ASTPart;
import extractvariable.ast.LightASTParser;
import extractvariable.detector.Comparator;
import safeextractor.log.MyLog;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
 

public class ASTNodeHandleUtils {
	public static boolean isParameterizedType(Type type) {
		if(type==null&&type.resolveBinding()!=null) {
			return true;
		}
		//string int byte boolean short long float double char
		//Short  Integer  Long Character Byte Float Boolean Double 
		String[] arr = {"String","char","int","long","float","double","byte","boolean","short",
				"Integer","Short","Long","Charater","Byte","Float","Boolean","Double"}; 
		 
		String name = type.resolveBinding().getName();
		for(String s:arr) {
			if(name.equals(s)) {
				return true;
			}
		} 
		return false;
	}
	
	public static boolean isParameterizedType(String name) {
		 
		String[] arr = {"String","char","int","long","float","double","byte","boolean","short",
				"Integer","Short","Long","Charater","Byte","Float","Boolean","Double"}; 
		  
		for(String s:arr) {
			if(name.equals(s)) {
				return true;
			}
		} 
		return false;
	}
	public static ArrayList<ASTNode> generateNodeVisitArrayList(ASTNode node) {
		ArrayList<ASTNode> nodeArrayList = new ArrayList<ASTNode>();
		ASTNode tempASTNode = node;
		while (tempASTNode != null) {
			nodeArrayList.add(tempASTNode);
			tempASTNode = tempASTNode.getParent();
		}
		return nodeArrayList;
	}

	public static ASTNode findMinimumCommonParentNode(ArrayList<ASTNode> firstNodeVisitArrayList,
			ArrayList<ASTNode> secondNodeVisitArrayList) {
		int len1 = firstNodeVisitArrayList.size();
		int len2 = secondNodeVisitArrayList.size();
		int i = len1 - 1;
		int j = len2 - 1;
		ASTNode resNode = null;
		while (i >= 0 && j >= 0) {
			if (isSameNode(firstNodeVisitArrayList.get(i).hashCode(), secondNodeVisitArrayList.get(j).hashCode())) {
				resNode = firstNodeVisitArrayList.get(i);
				i--;
				j--;
			} else {
				break;
			}
		}
		return resNode;
	}

	private static boolean isSameNode(int a, int b) {
		return a == b;
	}


	public static IMethod findMethodImplementationInHierarchy(ITypeHierarchy hierarchy, IType type, String name,
			String[] paramTypes, boolean isConstructor) throws JavaModelException {
		if(type==null) {
			return null;
		}
		IType[] superTypes = hierarchy.getAllSuperclasses(type);
		if(superTypes==null) {
			return null;
		} 
		for (int i = -1; i < superTypes.length; i++) {
			if(i==-1) {
				IMethod found = JavaModelUtil.findMethod(name, paramTypes, isConstructor, type);
				if (found != null) {
					if (Flags.isAbstract(found.getFlags())) {
						return null;
					}
					return found;
				}
			}else {
				IMethod found = JavaModelUtil.findMethod(name, paramTypes, isConstructor, superTypes[i]);
				if (found != null) {
					if (Flags.isAbstract(found.getFlags())) {
						return null;
					}
					return found;
				}
			}
		}
		return null;
	}

	public static MethodDeclaration findFunctionDefinition(ITypeBinding iTypeBinding, IMethodBinding methodBinding)
			throws IOException {
		if (methodBinding == null) {
			return null;
		}
		IType it;
		if(iTypeBinding!=null&& iTypeBinding.getJavaElement() instanceof IType)
			it = (IType) (iTypeBinding.getJavaElement());
		else  { 

			return null;
		}
		try { 
			ITypeHierarchy ith = it.newTypeHierarchy(GlobalClass.JavaProject, null);
			IMethod iMethod = (IMethod) methodBinding.getJavaElement();
			
			if ( iMethod==null) {
				return null;
			}
			
			IType realType =  null;
			if (ith != null ) {
				for(int j=0;j<ith.getAllClasses().length;++j) {
					IType iType = ith.getAllClasses()[j];    
					if(iType.getFullyQualifiedName().equals(it.getFullyQualifiedName() )) {
						realType=iType;
						break;
					}  
					if(iType.exists()) {
						for(String s: iType.getSuperInterfaceNames()) { 
							if(realType!=null )
								break; 
							if(s!=null&&s.equals(it.getElementName())) {
								realType = iType; 
								break;
							}
						}    
					}
				}
				if(realType==null && ith.getAllClasses().length==2 ) {
					if(ith.getAllClasses()[0].getFullyQualifiedName().equals("java.lang.Object")){
						realType=ith.getAllClasses()[1];
					}else if(ith.getAllClasses()[1].getFullyQualifiedName().equals("java.lang.Object")){
						realType=ith.getAllClasses()[0];
					} 
				}
				if(realType==null && ith.getAllClasses().length==1 ) {
					realType=ith.getAllClasses()[0];
				}
			}  
			if(realType==null) {
				return null;
			}
			IMethod im = findMethodImplementationInHierarchy(ith, realType, iMethod.getElementName(),
					iMethod.getParameterTypes(), false);
			if (im != null) {
				LightASTParser lightASTParser = new LightASTParser(im.getCompilationUnit());
				if(lightASTParser.getCompilationUnit()==null) { 
					return null;
				}
				NodeFinder finder = new NodeFinder(lightASTParser.getCompilationUnit(), im.getSourceRange().getOffset(),
						im.getSourceRange().getLength());
				ASTNode node = finder.getCoveredNode();
				while (node != null && !(node instanceof MethodDeclaration)) {
					node = node.getParent();
				}
				return (MethodDeclaration) node;
			}
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null; 
	} 
	 
	public static Boolean findInFieldRecords(ASTNode astNode, String key) { 
		HashSet<APIFieldRecord> apiFieldRecorder = new HashSet<>(); 
		if (safeextractor.utils.GlobalClass.recorderHashMap != null && safeextractor.utils.GlobalClass.recorderHashMap.get(key) != null) {
			APIRecorder apiRecorder = safeextractor.utils.GlobalClass.recorderHashMap.get(key);
 
			apiFieldRecorder = apiRecorder.getApiFieldRecordHashSet(); 
		} else {
			return false;
		} 
		if (astNode instanceof FieldAccess) {
			FieldAccess fieldAccess = (FieldAccess) astNode;
			IBinding iBinding = fieldAccess.resolveFieldBinding();  
			if (iBinding instanceof IVariableBinding) {
				IVariableBinding variableBinding = (IVariableBinding) iBinding;
				if (variableBinding.isField()) {
					APIFieldRecord apiFieldRecord = new APIFieldRecord(variableBinding);
					if (apiFieldRecord.getIVariableBinding().getVariableDeclaration() != null) {
						for (APIFieldRecord afr : apiFieldRecorder) {
							String tatget=apiFieldRecord.getIVariableBinding().getVariableDeclaration().getKey();
							if (apiFieldRecord.getIVariableBinding().getDeclaringClass() != null && afr.getIVariableBinding()
									.getVariableDeclaration().getKey().equals(tatget)) {
								return true;
							}
						}
					}
				}
			}
		} else if (astNode instanceof QualifiedName) {
			QualifiedName qualifiedName = (QualifiedName) astNode;
			IBinding iBinding = qualifiedName.getQualifier().resolveBinding();
			if (iBinding instanceof IVariableBinding) {
				IVariableBinding variableBinding = (IVariableBinding) iBinding;
				if (variableBinding.isField()) {
					APIFieldRecord apiFieldRecord = new APIFieldRecord(variableBinding);
					if (apiFieldRecord.getIVariableBinding().getVariableDeclaration() != null) {
						for (APIFieldRecord afr : apiFieldRecorder) {
							String tatget=apiFieldRecord.getIVariableBinding().getVariableDeclaration().getKey();
							if (apiFieldRecord.getIVariableBinding().getDeclaringClass() != null && afr.getIVariableBinding()
									.getVariableDeclaration().getKey().equals(tatget)) {
								return true;
							}
						}
					}
				}
			}
		} else if (astNode instanceof SimpleName) {
			SimpleName simpleName = (SimpleName) astNode;
			IBinding iBinding = simpleName.resolveBinding();
			if (iBinding instanceof IVariableBinding) {
				IVariableBinding variableBinding = (IVariableBinding) iBinding;
				APIFieldRecord apiFieldRecord = new APIFieldRecord(variableBinding);

				if (variableBinding.isField()) {
					if (apiFieldRecord.getIVariableBinding().getVariableDeclaration() != null) {
						for (APIFieldRecord afr : apiFieldRecorder) {
							String tatget=apiFieldRecord.getIVariableBinding().getVariableDeclaration().getKey();
							if (apiFieldRecord.getIVariableBinding().getDeclaringClass() != null && afr.getIVariableBinding()
									.getVariableDeclaration().getKey().equals(tatget)) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

}
