package extractvariable.ast;

import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import safeextractor.utils.Constants;
import safeextractor.utils.GlobalClass;

public class LightASTParser {
	private CompilationUnit compilationUnit;
	public LightASTParser(ICompilationUnit  iCompilationUnit) {
		 ASTParser astParser = ASTParser.newParser(AST.JLS8);  
			astParser.setKind(ASTParser.K_COMPILATION_UNIT);
			astParser.setSource(iCompilationUnit);  
			astParser.setStatementsRecovery(true);
			astParser.setResolveBindings(true);
			astParser.setUnitName(Constants.UNIT_NAME);
			astParser.setProject(GlobalClass.JavaProject);
			
	        Map<String, String> compilerOptions = JavaCore.getOptions();
	        compilerOptions.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
	        compilerOptions.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
	        compilerOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
			astParser.setCompilerOptions(compilerOptions); 
			if( iCompilationUnit==null) {
				this.compilationUnit=null;
			}else {
				 CompilationUnit unit = (CompilationUnit) (astParser.createAST(null));; 
				 this.compilationUnit=unit;
			}
	} 
	
	public CompilationUnit getCompilationUnit() {
		return this.compilationUnit;
	}
	
	
}
