package extractvariable.ast;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

import safeextractor.utils.Constants;
import safeextractor.utils.GlobalClass;

import java.io.IOException;
import java.util.*;

public class ASTPart {
	ASTParser astParser;
    CompilationUnit compilationUnit; 
    HashSet<String> allClassFullNameHashSet ;
    HashMap<IBinding,AbstractTypeDeclaration> typeDeclarationHashMap;
    String Path;

    public HashMap<IBinding, AbstractTypeDeclaration> getTypeDeclarationHashMap() {
        return typeDeclarationHashMap;
    }

    public ASTParser getAstParser() {
        return astParser;
    }

    public void setAstParser(ASTParser astParser) {
        this.astParser = astParser;
    }

    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    public void setCompilationUnit(CompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;
    }
 

    private HashMap<IBinding,AbstractTypeDeclaration> getAllClass(CompilationUnit compilationUnit) {
        class TempVisitor extends ASTVisitor{
            public HashMap<IBinding,AbstractTypeDeclaration>  allTypeDeclaration;

            public TempVisitor() {
                this.allTypeDeclaration =new HashMap<IBinding,AbstractTypeDeclaration> ();
            }

            @Override
            public void preVisit(ASTNode node) {
                if(node instanceof TypeDeclaration){
                    this.allTypeDeclaration.put(((TypeDeclaration) node).resolveBinding(),(TypeDeclaration)node);
               
                }else if(node instanceof EnumDeclaration){
                    this.allTypeDeclaration.put(((EnumDeclaration) node).resolveBinding(),(EnumDeclaration)node);
                }
            }
        }
        TempVisitor tempVisitor=new TempVisitor();
        compilationUnit.accept(tempVisitor);
        return tempVisitor.allTypeDeclaration;
    }

    
    public ASTPart(  ICompilationUnit iCompilationUnit ) throws IOException {
        this.Path=iCompilationUnit.getPath().toString();
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
        
        CompilationUnit unit = (CompilationUnit) (astParser.createAST(null)); 
		astParser.setCompilerOptions(compilerOptions); //设置编译选项
		
        allClassFullNameHashSet= new HashSet<String>(); 
        this.compilationUnit=unit; 
        this.typeDeclarationHashMap = getAllClass(this.compilationUnit);
        
    }

    private static MethodDeclaration[] getAllMethod(CompilationUnit cu) {
    	if(cu==null ||cu.types().size()==0 ) {
    		return null;
    	}
        ArrayList<MethodDeclaration> methodsArrayList = new ArrayList<MethodDeclaration>();
    	for(int i=0;i<cu.types().size();++i){
            if(cu.types().get(i) instanceof TypeDeclaration ) {
                TypeDeclaration type =  (TypeDeclaration) cu.types().get(i);
                methodsArrayList.addAll(myNewGetMethods(type));
            }
            else if(cu.types().get(i) instanceof EnumDeclaration) {
                EnumDeclaration type =  (EnumDeclaration) cu.types().get(i);
                methodsArrayList.addAll(myNewGetMethods(type));
            }
        }
        if(methodsArrayList.size()>0){
            return methodsArrayList.toArray(new MethodDeclaration[methodsArrayList.size()]);
        }
    	return new MethodDeclaration[0];
    }


    private static ArrayList<MethodDeclaration> myNewGetMethods(TypeDeclaration type ) {
		List bd = type.bodyDeclarations();
		ArrayList<MethodDeclaration> methodsArrayList= new ArrayList<MethodDeclaration>();
		for (Iterator it = bd.listIterator(); it.hasNext(); ) {
			Object decl = it.next();
			if (decl instanceof MethodDeclaration) {
				methodsArrayList.add((MethodDeclaration) decl);
			}
			else if(decl instanceof TypeDeclaration) {
				methodsArrayList.addAll(myNewGetMethods((TypeDeclaration)decl));
			}
		}
		return methodsArrayList;
	}
    

    private static ArrayList<MethodDeclaration> myNewGetMethods(EnumDeclaration type ) {
		List bd = type.bodyDeclarations();
		ArrayList<MethodDeclaration> methodsArrayList= new ArrayList<MethodDeclaration>();
		for (Iterator it = bd.listIterator(); it.hasNext(); ) {
			Object decl = it.next();
			if (decl instanceof MethodDeclaration) {
				methodsArrayList.add((MethodDeclaration) decl);
			}
			else if(decl instanceof TypeDeclaration) {
				methodsArrayList.addAll(myNewGetMethods((TypeDeclaration)decl));
			}
		}
		return methodsArrayList;
	}

}
