package safeextractor.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import extractvariable.api.APIRecorder;
import extractvariable.ast.ASTPart;
import safeextractor.json.Exp1Record;

public class GlobalClass {
	public static IJavaProject JavaProject=null; 
	public static ICompilationUnit iCompilationUnit=null;
	public static HashMap<String, APIRecorder> recorderHashMap; 
	
	public static void buildRecorderHashMap() throws JavaModelException, IOException {
		recorderHashMap= new HashMap<>();
		IPackageFragment[] fragments = GlobalClass.JavaProject.getPackageFragments(); 
		for (int i = 0; i < fragments.length; i++) {
			 
			IPackageFragment fragment = fragments[i]; 
			ICompilationUnit[] iCompilationUnits = fragment.getCompilationUnits();
			if(fragment.getPath().toString().contains("/test/")) {
				continue;
			}   
			for (int j = 0; j < iCompilationUnits.length; j++) {
				ICompilationUnit iCompilationUnit = iCompilationUnits[j];
				if(!iCompilationUnit.exists()) {
					continue;
				}
				ASTPart astPart = new ASTPart(iCompilationUnit); 
				Set<Map.Entry<IBinding, AbstractTypeDeclaration>> entrySet = astPart.getTypeDeclarationHashMap()
						.entrySet();
				for (Map.Entry entry : entrySet) {
					Object obj = entry.getValue();
					if (obj instanceof TypeDeclaration) {
						TypeDeclaration typeDeclaration = (TypeDeclaration) obj;
						ITypeBinding currentClass = typeDeclaration.resolveBinding().getTypeDeclaration();
						APIRecorder apiRecorder = new APIRecorder(typeDeclaration, astPart.getTypeDeclarationHashMap());
						recorderHashMap.put(currentClass.getKey(), apiRecorder);
					} else if (obj instanceof EnumDeclaration) {
						EnumDeclaration enumDeclaration = (EnumDeclaration) obj;
						ITypeBinding currentClass = enumDeclaration.resolveBinding();
						APIRecorder apiRecorder = new APIRecorder(enumDeclaration, astPart.getTypeDeclarationHashMap());
						recorderHashMap.put(currentClass.getKey(), apiRecorder);
					}
				} 
				astPart = null;
			}
		}
	}
}
