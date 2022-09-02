package extractvariable.ast.rw.reader;

import extractvariable.ast.EntityVisitor;
import extractvariable.ast.Field;
import extractvariable.ast.rw.AllVisitor;
import extractvariable.ast.rw.RWVisitor;
import extractvariable.detector.Comparator;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.*;

import safeextractor.json.APIJson;
import safeextractor.json.HandleAPIJsonFile;
import safeextractor.log.MyLog;
import safeextractor.utils.ASTNodeHandleUtils;
import safeextractor.utils.Constants;

import static safeextractor.utils.ASTNodeHandleUtils.*;

import java.io.IOException;
import java.util.*;

public class ReaderVisitor extends RWVisitor {
	protected Boolean exceptionFlag;
	protected Boolean confiditonFlag;

	private boolean addElemToReaderHashMap(String name, ASTNode astNode) {
		if (name.startsWith("this.")) {
			name = name.substring(5);
		}
		if (astNode instanceof NumberLiteral) {
			return false;
		} else if (astNode instanceof FieldAccess) {
			IVariableBinding variableBinding = ((FieldAccess) astNode).resolveFieldBinding();
			if (variableBinding == null || variableBinding.isField() == false) {
				return false;
			}
			if (Modifier.isStatic(variableBinding.getModifiers())) {
				name = variableBinding.getDeclaringClass().getQualifiedName() + "." + variableBinding.getName();
			}
			if (Modifier.isStatic(variableBinding.getModifiers()) || findInFieldRecords(astNode, key) || key == owner) {
				name = name.replaceAll("null\\.", "");
				addToAPIList(name, variableBinding);
				return true;
			}
		} else if (astNode instanceof QualifiedName) {
			IBinding iBinding = ((QualifiedName) astNode).resolveBinding();
			if (iBinding instanceof IVariableBinding) {
				IVariableBinding variableBinding = (IVariableBinding) iBinding;
				Name qualifier = ((QualifiedName) astNode).getQualifier();
				while (qualifier instanceof QualifiedName) {
					qualifier = ((QualifiedName) qualifier).getQualifier();
				}
				if (qualifier instanceof SimpleName) {
					SimpleName sn = (SimpleName) (qualifier);
					IBinding svb = sn.resolveBinding();
					if (this.paramHashmap.get(sn.toString()) == null && this.layer != 0 && svb != null
							&& svb.getJavaElement() != null
							&& svb.getJavaElement().getElementType() == IJavaElement.LOCAL_VARIABLE) {

						if (!(svb instanceof IVariableBinding
								&& (((IVariableBinding) (svb)).isParameter() || ((IVariableBinding) (svb)).isField()
										|| ((IVariableBinding) (svb)).isEnumConstant()))) {
							return false;
						}
					}
				}

				if (Modifier.isStatic(variableBinding.getModifiers())) {
					name = variableBinding.getDeclaringClass().getQualifiedName() + "." + variableBinding.getName();
				}
				name = name.replaceAll("null\\.", "");
				if (variableBinding.getType().isArray()) {
					name = name + "[]";
				}
				if (Modifier.isStatic(variableBinding.getModifiers()) || findInFieldRecords(astNode, key)
						|| key == owner) {
					addToAPIList(name, variableBinding);
					return true;
				}
			}
		} else if (astNode instanceof SimpleName) {
			IBinding iBinding = ((SimpleName) astNode).resolveBinding();
			if (iBinding instanceof IVariableBinding) {
				IVariableBinding variableBinding = (IVariableBinding) iBinding;
				if (this.layer != 0 && variableBinding != null && variableBinding.getJavaElement() != null
						&& variableBinding.getJavaElement().getElementType() == IJavaElement.LOCAL_VARIABLE) {
					if (!(variableBinding instanceof IVariableBinding
							&& (((IVariableBinding) (variableBinding)).isParameter()
									|| ((IVariableBinding) (variableBinding)).isField()
									|| ((IVariableBinding) (variableBinding)).isEnumConstant()))) {
						return false;
					}
				}
				if (Modifier.isStatic(variableBinding.getModifiers())) {
					name = variableBinding.getDeclaringClass().getQualifiedName() + "." + variableBinding.getName();
				}
				name = name.replaceAll("null\\.", "");
				if (variableBinding.getType().isArray()) {
					name = name + "[]";
				}
				if (Modifier.isStatic(variableBinding.getModifiers()) || findInFieldRecords(astNode, key)
						|| key == owner) {
					addToAPIList(name, variableBinding);
					return true;
				}
			}
		} else if (astNode instanceof ArrayAccess) {
			Expression arrayExpression = ((ArrayAccess) astNode).getArray();
			if (arrayExpression instanceof SimpleName) {
				IBinding iBinding = ((SimpleName) arrayExpression).resolveBinding();
				if (iBinding instanceof IVariableBinding) {
					IVariableBinding variableBinding = (IVariableBinding) iBinding;
					if (Modifier.isStatic(variableBinding.getModifiers())) {
						name = variableBinding.getDeclaringClass().getQualifiedName() + "." + variableBinding.getName();
					}
					name = name.replaceAll("null\\.", "");
					if (Modifier.isStatic(variableBinding.getModifiers()) || findInFieldRecords(astNode, key)
							|| key == owner) {
						addToAPIList(name, variableBinding);
						return true;
					}
				}
			}
		}
		return false;
	}

	public ReaderVisitor(HashSet<ITypeBinding> allBindings, String key, String owner, String methodKey, int middleFlag,
			ASTNode extractVariable, int layer, boolean isLocal, List<String> argList) {
		super(layer, isLocal, middleFlag, argList);
		this.methodKey = methodKey;
		this.allBindings = allBindings;
		this.key = key;
		this.APIList = new ArrayList<>();
		this.owner = owner;
		this.exceptionFlag = false;
		this.confiditonFlag = false;
		this.extractVariable = extractVariable;
	}

	@Override
	public boolean visit(Assignment assignment) {
		super.visit(assignment);
		if (assignment.getOperator().toString().equals("=")) {
			addElemToReaderHashMap(assignment.getRightHandSide().toString(), assignment.getRightHandSide());
		} else {
			if (this.middleFlag == 1) {
				addElemToReaderHashMap(assignment.getLeftHandSide().toString(), assignment.getLeftHandSide());
			}
			addElemToReaderHashMap(assignment.getRightHandSide().toString(), assignment.getRightHandSide());

		}
		return true;
	}

	@Override
	public boolean visit(FieldAccess fieldAccess) {
		IBinding iBinding = fieldAccess.resolveFieldBinding();
		if (iBinding instanceof IVariableBinding) {
			IVariableBinding iVariableBinding = (IVariableBinding) iBinding;
			if (iVariableBinding.isParameter() || key == owner) {
				addElemToReaderHashMap(fieldAccess.toString(), fieldAccess);
			}
		}
		return true;
	}

	@Override
	public boolean visit(SingleVariableDeclaration singleVariableDeclaration) {
		IVariableBinding iVariableBinding = singleVariableDeclaration.resolveBinding();
		if (iVariableBinding.isParameter()) {
		}
		return true;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		IBinding iBinding = simpleName.resolveBinding();
		if (iBinding instanceof IVariableBinding) {
			IVariableBinding iVariableBinding = (IVariableBinding) iBinding;
			if (iVariableBinding.isParameter() || key == owner) {
				addElemToReaderHashMap(simpleName.toString(), simpleName);
			}
		}
		return true;
	}

	@Override
	public boolean visit(PrefixExpression prefixExpression) {
		if (safeextractor.utils.Utils.isStartWithNumber(prefixExpression.getOperand().toString())
				|| prefixExpression.getOperand().toString().startsWith(".")) {
			return false;
		} else {
			addElemToReaderHashMap(prefixExpression.getOperand().toString(), prefixExpression.getOperand());
		}
		return true;
	}

	@Override
	public boolean visit(PostfixExpression postfixExpression) {
		if (safeextractor.utils.Utils.isStartWithNumber(postfixExpression.getOperand().toString())
				|| postfixExpression.getOperand().toString().startsWith(".")) {
			return false;
		} else {
			addElemToReaderHashMap(postfixExpression.getOperand().toString(), postfixExpression.getOperand());
		}
		return true;
	}

	@Override
	public boolean visit(ArrayAccess arrayAccess) {
		if (!(arrayAccess.getIndex() instanceof NumberLiteral)) {
			addElemToReaderHashMap(arrayAccess.getIndex().toString(), arrayAccess.getIndex());
		}
		addElemToReaderHashMap(arrayAccess.getArray().toString(), arrayAccess.getArray());
		addElemToReaderHashMap(arrayAccess.toString(), arrayAccess);
		return true;
	}

	@Override
	public boolean visit(EnhancedForStatement enhancedForStatement) {

		if (enhancedForStatement.getExpression() instanceof SimpleName) {
			IBinding iBinding = ((SimpleName) enhancedForStatement.getExpression()).resolveBinding();
			if (iBinding instanceof IVariableBinding) {
				IVariableBinding variableBinding = (IVariableBinding) iBinding;
				if (variableBinding.isField()) {
					Expression expression = (SimpleName) enhancedForStatement.getExpression();
					addElemToReaderHashMap(expression.toString(), expression);

				}
			}
		}

		return true;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {

		IMethodBinding iMethodBinding = methodInvocation.resolveMethodBinding();
		List<ASTNode> methodInvocationArgList = methodInvocation.arguments();
		List<String> args = new ArrayList<String>();
		// 应该是对参数都遍历
		for (int i = 0; i < methodInvocationArgList.size(); ++i) {
			ASTNode astNode = methodInvocationArgList.get(i);
			args.add(astNode.toString());
			if (key == owner) {
			} else {
				if (findInFieldRecords(astNode, key)) {
					addElemToReaderHashMap(methodInvocationArgList.get(i).toString(), methodInvocationArgList.get(i));
				}
			}
		}

		if (iMethodBinding == null || iMethodBinding.getKey().equals(this.methodKey)) {
			return true;
		}
		if (Modifier.isNative(iMethodBinding.getModifiers()) && this.layer == 0 && this.middleFlag != 1) {
			String s = iMethodBinding.getDeclaringClass().getQualifiedName() + "." + iMethodBinding.getName();
			Comparator.visitedNativeMethodSet.add(s);
			return true;
		}
		if (methodInvocation.getExpression() instanceof SimpleName) {
			SimpleName sn = (SimpleName) (methodInvocation.getExpression());
			if (this.layer != 0 && sn.resolveBinding() != null && sn.resolveBinding().getJavaElement() != null
					&& sn.resolveBinding().getJavaElement().getElementType() == IJavaElement.LOCAL_VARIABLE) {
				// 如果是参数则也加入
				if (!(sn.resolveBinding() instanceof IVariableBinding
						&& (((IVariableBinding) (sn.resolveBinding())).isParameter()
								|| ((IVariableBinding) (sn.resolveBinding())).isField()
								|| ((IVariableBinding) (sn.resolveBinding())).isEnumConstant()))) {
					return true;
				}
			}
		}
		MethodDeclaration targetFunctionDefinition = null;
		try {
			ITypeBinding itb = null;
			if (methodInvocation.getExpression() != null) {
				itb = methodInvocation.getExpression().resolveTypeBinding();
			} else {
				itb = iMethodBinding.getDeclaringClass();
			}
			targetFunctionDefinition = ASTNodeHandleUtils.findFunctionDefinition(itb, iMethodBinding);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (methodInvocation.getExpression() instanceof SimpleName) {
			SimpleName sn = (SimpleName) (methodInvocation.getExpression());
			if (this.layer != 0 && sn.resolveBinding() != null && sn.resolveBinding().getJavaElement() != null
					&& sn.resolveBinding().getJavaElement().getElementType() == IJavaElement.LOCAL_VARIABLE) {
				if (!(sn.resolveBinding() instanceof IVariableBinding
						&& (((IVariableBinding) (sn.resolveBinding())).isParameter()
								|| ((IVariableBinding) (sn.resolveBinding())).isField()
								|| ((IVariableBinding) (sn.resolveBinding())).isEnumConstant()))) {
					return true;
				}
			}
		}

		if (targetFunctionDefinition != null && targetFunctionDefinition.resolveBinding() != null) {
			IMethodBinding resolveBinding = targetFunctionDefinition.resolveBinding();
			if (resolveBinding == null) {
				return true;
			}
			EntityVisitor entityVisitor = new EntityVisitor(resolveBinding.getDeclaringClass());
			targetFunctionDefinition.accept(entityVisitor);
			String tempOwner = methodInvocation.getExpression() == null ? this.owner
					: methodInvocation.getExpression().toString();
			if (this.paramHashmap.containsKey(tempOwner)) {
				tempOwner = this.paramHashmap.get(tempOwner);
			}
			String tempKey = methodInvocation.getExpression() == null ? this.key
					: resolveBinding.getDeclaringClass().getKey();
			ReaderVisitor readerVisitor = new ReaderVisitor(entityVisitor.getTargetClass(), tempKey, tempOwner,
					iMethodBinding.getKey(), this.middleFlag, this.extractVariable, this.layer + 1, false, args);
			readerVisitor.confiditonFlag = isInCondition(methodInvocation);
			targetFunctionDefinition.accept(readerVisitor);
			readerVisitor.exceptionFlag = targetFunctionDefinition.thrownExceptionTypes().size() == 0
					? readerVisitor.exceptionFlag
					: true;
			List<String> readList = readerVisitor.getAPIList();
			readList.forEach(k -> APIList.add(k));

			if (readerVisitor.exceptionFlag) {
				this.exceptionFlag = true;
				// 如果访问的语句在条件当中
				if (readerVisitor.confiditonFlag && readerVisitor.middleFlag == 1) {
					this.confiditonFlag = true;
					Comparator.exceptionMethod.middleCodeReaderList.add(readList);
				}
			}

		} else if (this.layer <= 1) {
			String apiPath = iMethodBinding.getMethodDeclaration().getDeclaringClass().getBinaryName().replace(".", "/")
					+ ".json";
			HashMap<String, APIJson> apiJsonHashMap;
			try {
				apiJsonHashMap = HandleAPIJsonFile.JsonParser(Constants.JDK_Prefix + apiPath);
				if (apiJsonHashMap == null
						|| apiJsonHashMap.get(iMethodBinding.getMethodDeclaration().getKey()) == null) {
					return true;
				}
				APIJson apiJson = apiJsonHashMap.get(iMethodBinding.getMethodDeclaration().getKey());
				List<Field> readList = apiJson.getReadList();
				if (this.middleFlag != 1)
					apiJson.getNativeList().forEach(k -> Comparator.visitedNativeMethodSet.add(k.getName()));

				readList.forEach(k -> {
					if (k.getIsStatic() == 1) {
					} else {
						APIList.add(methodInvocation.getExpression() + "." + k.getName());
					}
				});
				if (apiJson.getWriteList().size() == 0 && apiJson.getReadList().size() == 0
						&& apiJson.getNativeList().size() == 0 && isInExtractVariable(methodInvocation)
						&& this.layer == 0) {
					Comparator.visitedNativeMethodSet
							.add(iMethodBinding.getMethodDeclaration().getDeclaringClass().getBinaryName() + "."
									+ iMethodBinding.getMethodDeclaration().getKey());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public boolean visit(ReturnStatement returnStatement) {
		Expression expression = returnStatement.getExpression();
		AllVisitor allVisitor = new AllVisitor();
		if (expression != null) {
			expression.accept(allVisitor);
			for (int i = 0; i < allVisitor.getAstNodes().size(); ++i) {
				addElemToReaderHashMap(allVisitor.getAstNodes().get(i).toString(), allVisitor.getAstNodes().get(i));
			}
		}
		return super.visit(returnStatement);
	}

	@Override
	public boolean visit(QualifiedType qualifiedType) {
		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation classInstanceCreation) {
		IMethodBinding iMethodBinding = classInstanceCreation.resolveConstructorBinding();
		if (iMethodBinding != null && iMethodBinding.getKey().equals(this.methodKey)) {
			return true;
		}
		List<String> args = new ArrayList<String>();
		List<ASTNode> methodInvocationArgList = classInstanceCreation.arguments();
		for (int i = 0; i < methodInvocationArgList.size(); ++i) {
			ASTNode astNode = methodInvocationArgList.get(i);
			args.add(astNode.toString());
			if (key == owner) {
			} else if (findInFieldRecords(methodInvocationArgList.get(i), key)) {
				addElemToReaderHashMap(methodInvocationArgList.get(i).toString(), methodInvocationArgList.get(i));
			}
		}
		if (iMethodBinding == null) {
			return true;
		}
		MethodDeclaration targetFunctionDefinition = null;
		ITypeBinding itb = null;
		if (classInstanceCreation.getExpression() != null) {
			itb = classInstanceCreation.getExpression().resolveTypeBinding();
		} else {// 如果没有前缀，则是当前类
			itb = iMethodBinding.getDeclaringClass();
		}
		try {
			targetFunctionDefinition = ASTNodeHandleUtils.findFunctionDefinition(itb, iMethodBinding);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (classInstanceCreation.getExpression() instanceof SimpleName) {
			SimpleName sn = (SimpleName) (classInstanceCreation.getExpression());
			if (this.layer != 0 && sn.resolveBinding() != null && sn.resolveBinding().getJavaElement() != null
					&& sn.resolveBinding().getJavaElement().getElementType() == IJavaElement.LOCAL_VARIABLE) {
				// 如果是参数则也加入
				if (!(sn.resolveBinding() instanceof IVariableBinding
						&& (((IVariableBinding) (sn.resolveBinding())).isParameter()
								|| ((IVariableBinding) (sn.resolveBinding())).isField()
								|| ((IVariableBinding) (sn.resolveBinding())).isEnumConstant()))) {
					return true;
				}
			}
		}
		if (targetFunctionDefinition != null) {

			EntityVisitor entityVisitor = new EntityVisitor(
					targetFunctionDefinition.resolveBinding().getDeclaringClass());
			targetFunctionDefinition.accept(entityVisitor);

			String tempOwner = classInstanceCreation.getExpression() == null ? null
					: classInstanceCreation.getExpression().toString();
			if (this.paramHashmap.containsKey(tempOwner)) {
				tempOwner = this.paramHashmap.get(tempOwner);
			}
			String tempKey = classInstanceCreation.getExpression() == null ? this.key
					: classInstanceCreation.getExpression().toString();
			ReaderVisitor readerVisitor = new ReaderVisitor(entityVisitor.getTargetClass(), tempKey, tempOwner,
					iMethodBinding.getKey(), this.middleFlag, this.extractVariable, this.layer + 1, false, args);
			targetFunctionDefinition.accept(readerVisitor);
			List<String> readList = readerVisitor.getAPIList();
			readList.forEach(k -> APIList.add(k));
		}
		return true;
	}

	@Override
	public boolean visit(InfixExpression infixExpression) {
		addElemToReaderHashMap(infixExpression.getRightOperand().toString(), infixExpression.getRightOperand());
		addElemToReaderHashMap(infixExpression.getLeftOperand().toString(), infixExpression.getLeftOperand());
		// 1+2+3+4 这种情况
		if (infixExpression.hasExtendedOperands()) {
			infixExpression.extendedOperands().forEach(node -> addElemToReaderHashMap(node.toString(), (ASTNode) node));
		}
		return true;
	}

	@Override
	public boolean visit(ThrowStatement throwStatement) {
		this.exceptionFlag = true;
		return true;
	}

	@Override
	public boolean visit(QualifiedName qualifiedName) {
		if (qualifiedName.getQualifier() instanceof SimpleName) {
			SimpleName sn = (SimpleName) (qualifiedName.getQualifier());
			if (sn.resolveBinding() == null || sn.resolveBinding().getJavaElement() == null
					|| sn.resolveBinding().getJavaElement().getElementType() == IJavaElement.LOCAL_VARIABLE
							&& this.layer != 0) {
				return false;
			}
		}
		return true;
	}

	public Boolean isInCondition(ASTNode astNode) {
		ASTNode temp = astNode;
		while (temp != null && temp.getParent() != null) {
			ASTNode tempParent = temp.getParent();
			if (tempParent instanceof IfStatement) {
				IfStatement ifs = (IfStatement) tempParent;
				if (ifs.getExpression() == temp) {
					return true;
				} else {
					return false;
				}
			}
			temp = tempParent;
		}
		return false;
	}

	private boolean isInExtractVariable(ASTNode node) {
		ASTNode temp = node;
		while (temp != null && !(temp instanceof Statement)) {
			if (temp == this.extractVariable) {
				return true;
			}
			temp = temp.getParent();
		}
		return false;
	}
}
