package safeextractor.utils;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
   

	public static CompilationUnit getCompilationUnit(ICompilationUnit iCompilationUnit) {
		ASTParser astParser = ASTParser.newParser(AST.JLS8);
		astParser.setKind(ASTParser.K_COMPILATION_UNIT);
		astParser.setSource(iCompilationUnit);
		astParser.setResolveBindings(true);
		astParser.setBindingsRecovery(true);
		CompilationUnit unit = (CompilationUnit) (astParser.createAST(null));
		return unit;
	}

	public static ASTParser getNewASTParser(String[] sourcepathEntries, String[] encodings) {
		ASTParser astParser;
		astParser = ASTParser.newParser(AST.JLS8);
		astParser.setKind(ASTParser.K_COMPILATION_UNIT);
		astParser.setEnvironment(null, sourcepathEntries, encodings, true);
//	        astParser.setEnvironment(classpathEntries, sourcepathEntries, encodings, true);
		astParser.setResolveBindings(true);
		astParser.setBindingsRecovery(true);
		astParser.setUnitName("");
		Map options = JavaCore.getOptions();
		astParser.setCompilerOptions(options);
		return astParser;
	}

	public static ASTParser getNewASTParser() {
		ASTParser astParser;
		astParser = ASTParser.newParser(AST.JLS8);
		astParser.setKind(ASTParser.K_COMPILATION_UNIT);
		return astParser;
	}

	static public HashSet<String> getSourceTypeNames(ITypeBinding type) {
		HashSet<String> nomes = new HashSet<String>();

		if (type.isFromSource()) {
			nomes.add(type.getQualifiedName());
		}

		// A[] returns if A is from Source
		if (type.isArray() && type.getElementType().isFromSource())
			nomes.add(type.getElementType().getQualifiedName());

		// T<A>, T<A,B> - true if A is from Source or if A or B are source types and its
		// a Collection
		// Collections with source as parameters are considered because represents a
		// relationship with a source type
		if (type.isParameterizedType()) {
			ITypeBinding[] interfaces = type.getInterfaces();
			boolean isCollection = false;
			for (ITypeBinding iTypeBinding : interfaces) {
				isCollection = isCollection || iTypeBinding.getBinaryName().equals("java.util.Collection");
			}

			if (isCollection) {
				for (ITypeBinding typeArg : type.getTypeArguments()) {
					if (typeArg.isFromSource())
						nomes.add(typeArg.getQualifiedName());
				}
			}
		}
		return nomes;
	}

	static public boolean isStatic(Object obj) {
		HashSet<Modifier> modifiers = new HashSet<Modifier>();
		if (obj instanceof FieldDeclaration) {
			FieldDeclaration field = (FieldDeclaration) obj;
			for (Object o : field.modifiers()) {
				if (o instanceof Modifier) {
					Modifier modifier = (Modifier) o;
					modifiers.add(modifier);
				}
			}
		}
		if (obj instanceof MethodDeclaration) {
			MethodDeclaration method = (MethodDeclaration) obj;
			for (Object o : method.modifiers()) {
				if (o instanceof Modifier) {
					Modifier modifier = (Modifier) o;
					modifiers.add(modifier);
				}
			}
		}
		for (Modifier modifier : modifiers)
			if (modifier.isStatic())
				return true;
		return false;
	}

	static public <E> Collection<E> getIntersection(Collection<E> a, Collection<E> b) {
		Collection<E> res = new HashSet<E>();
		res.addAll(a);
		res.retainAll(b);
		return res;
	}

	static public <E> Collection<E> getUnion(Collection<E> a, Collection<E> b) {
		Collection<E> res = new HashSet<E>();
		res.addAll(a);
		res.addAll(b);
		return res;
	}

	static public String getCodeFromFile(File javaFile) throws IOException {
		BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(javaFile));
		byte[] input = new byte[bufferedInputStream.available()];
		bufferedInputStream.read(input);
		bufferedInputStream.close();
		return new String(input);
	}
 

	public static String getVariableQualifiedName(VariableDeclaration variableDeclaration) {
		String qualifiedName = "";
		if (variableDeclaration.resolveBinding() instanceof IVariableBinding) {
			IVariableBinding variableBinding = variableDeclaration.resolveBinding();
			ITypeBinding typeBinding = variableBinding.getDeclaringClass();
			if (typeBinding != null) {
				qualifiedName = typeBinding.getQualifiedName() + "." + variableBinding.getName();
			}
		}
		return qualifiedName;
	}

	public static String getMethodQualifiedName(MethodDeclaration methodDeclaration) {
		String qualifiedName = "";
		if (methodDeclaration.resolveBinding() instanceof IMethodBinding) {
			IMethodBinding methodBinding = methodDeclaration.resolveBinding();
			ITypeBinding typeBinding = methodBinding.getDeclaringClass();
			if (typeBinding != null) {
				qualifiedName = typeBinding.getQualifiedName() + "." + methodBinding.getName();
			}
		}
		return qualifiedName;
	}

	public static String getMethodSignature(MethodDeclaration method) {
		String signature = "";
		signature += method.getName().toString() + "(";
		int parameterSize = method.parameters().size();
		for (int i = 0; i < parameterSize; i++) {
			Object obj = method.parameters().get(i);
			if (obj instanceof SingleVariableDeclaration) {
				SingleVariableDeclaration parameter = (SingleVariableDeclaration) obj;
				signature += parameter.getType().toString() + " " + parameter.getName().toString();
				if (i != parameterSize - 1) {
					signature += ", ";
				}
			}

		}
		return signature + ")";
	}

	public static String getMethodSignature(IMethodBinding methodBinding) {
		String signature = "";
		signature += methodBinding.getName() + "(";
		ITypeBinding[] typeBindings = methodBinding.getParameterTypes();
		int parameterSize = typeBindings.length;
		for (int i = 0; i < parameterSize; i++) {
			signature += typeBindings[i].getName();
			if (i != parameterSize - 1) {
				signature += ", ";
			}
		} 
		return signature + ")";
	}
 
 
	public static boolean isStartWithNumber(String str) {
	    Pattern pattern = Pattern.compile("[0-9]*");
	    Matcher isNum = pattern.matcher(str.charAt(0)+"");
	    if (!isNum.matches()) {
	        return false;
	    }
	    return true;
	}
}
