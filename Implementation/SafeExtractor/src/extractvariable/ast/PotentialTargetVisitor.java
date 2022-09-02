package extractvariable.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.corext.Corext;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.fragments.ASTFragmentFactory;
import org.eclipse.jdt.internal.corext.dom.fragments.IASTFragment;
import org.eclipse.jdt.internal.corext.dom.fragments.IExpressionFragment;
import org.eclipse.jdt.internal.corext.refactoring.Checks;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.base.RefactoringStatusCodes;
import org.eclipse.jdt.internal.corext.refactoring.code.CodeRefactoringUtil;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import safeextractor.refactoring.MyExtractTempRefactoring.ForStatementChecker;
import safeextractor.utils.Constants;

public class PotentialTargetVisitor extends ASTVisitor { 
	List<Record> potentialTargetList;
	CompilationUnit cu;
	ICompilationUnit icu;

	public PotentialTargetVisitor(CompilationUnit cu, ICompilationUnit icu) {
		this.potentialTargetList = new ArrayList<>();
		this.cu = cu;
		this.icu = icu;
	}

	public boolean visit(MethodDeclaration md) {
		ExpressionCounterVisitor ecv = new ExpressionCounterVisitor(icu,cu);
		md.accept(ecv);
		ecv.expressionCounterHashMap.forEach((k, v) -> {
			if (v.getCount() > 1 && v.getMax_line()-v.getMin_line()<= Constants.MAX_LINE_LIMITATION ) {
				this.potentialTargetList.add(v);
			}else if(v.getCount() > 1) {
				System.out.println("too long "+ v.getName());
			}
		});
		return false;
	}
	
	public boolean visit(Initializer init) {
		ExpressionCounterVisitor ecv = new ExpressionCounterVisitor(icu,cu);
		init.accept(ecv);
		ecv.expressionCounterHashMap.forEach((k, v) -> {
			if (v.getCount() > 1 && v.getMax_line()-v.getMin_line()<= Constants.MAX_LINE_LIMITATION ) {
				this.potentialTargetList.add(v);
			}else if(v.getCount() > 1) {
				System.out.println("too long "+ v.getName());
			}
		});
		return false;
	}

	/**
	 * @return the potentialTargetList
	 */
	public List<Record> getPotentialTargetList() {
		return potentialTargetList;
	}

	/**
	 * @param potentialTargetList the potentialTargetList to set
	 */
	public void setPotentialTargetList(List<Record> potentialTargetList) {
		this.potentialTargetList = potentialTargetList;
	}

	class ExpressionCounterVisitor extends ASTVisitor {
		HashMap<String, Record> expressionCounterHashMap;
		ICompilationUnit icu;
		CompilationUnit cu;
		 
		ExpressionCounterVisitor(ICompilationUnit icu,CompilationUnit cu) {
			this.icu=icu;
			this.cu=cu;
			expressionCounterHashMap = new HashMap<>();
			 
		}  
		@Override
		public boolean preVisit2(ASTNode astNode) {
			try {
				if (!checkSelection(astNode)) {
					return true;
				}  
				if (astNode instanceof MethodInvocation
						|| astNode instanceof FieldAccess || astNode instanceof QualifiedName
						|| astNode instanceof ArrayAccess) {
					Record record = this.expressionCounterHashMap.get(astNode.toString());
					if (record == null) {
						record= new Record(icu.getPath().toString(),astNode.toString(), astNode.getStartPosition(),astNode.getLength());
						this.expressionCounterHashMap.put(astNode.toString(),record);
					} else {
						record.getExpLocList().add(new ExpessionLocation(astNode.getStartPosition(),astNode.getLength()));
						record.addCount();
					}  	
					record.setMax_line(Math.max(record.getMax_line(), this.cu.getLineNumber(astNode.getStartPosition())));
					record.setMin_line(Math.min(record.getMin_line(), this.cu.getLineNumber(astNode.getStartPosition())));
					
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
			return true;
		}

	}

	public class ExpessionLocation {
		int offset;
		int length;
		ExpessionLocation(int offset, int length){
			this.offset=offset;
			this.length=length;
		}
		/**
		 * @return the offset
		 */
		public int getOffset() {
			return offset;
		}
		/**
		 * @param offset the offset to set
		 */
		public void setOffset(int offset) {
			this.offset = offset;
		}
		/**
		 * @return the length
		 */
		public int getLength() {
			return length;
		}
		/**
		 * @param length the length to set
		 */
		public void setLength(int length) {
			this.length = length;
		}
		
	}
	
	public class Record {
		int count;
		String name;
		List<ExpessionLocation> expLocList; 
		String path;
		int max_line;
		int min_line;

		Record(String path,String name, int offset,int length) {
			this.name = name;
			this.count = 1;
			expLocList = new ArrayList<ExpessionLocation>(); 
			expLocList.add(new ExpessionLocation(offset,length));
			this.path=path;
			this.max_line=Integer.MIN_VALUE;
			this.min_line=Integer.MAX_VALUE;
		}

		/**
		 * @return the count
		 */
		public int getCount() {
			return count;
		}

		/**
		 * @param count the count to set
		 */
		public void addCount() {
			this.count++;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		} 
		/**
		 * @param count the count to set
		 */
		public void setCount(int count) {
			this.count = count;
		}
 
		/**
		 * @return the path
		 */
		public String getPath() {
			return path;
		}

		/**
		 * @param path the path to set
		 */
		public void setPath(String path) {
			this.path = path;
		}

		/**
		 * @return the max_line
		 */
		public int getMax_line() {
			return max_line;
		}

		/**
		 * @param max_line the max_line to set
		 */
		public void setMax_line(int max_line) {
			this.max_line = max_line;
		}

		/**
		 * @return the min_line
		 */
		public int getMin_line() {
			return min_line;
		}

		/**
		 * @param min_line the min_line to set
		 */
		public void setMin_line(int min_line) {
			this.min_line = min_line;
		}

		/**
		 * @return the expLocList
		 */
		public List<ExpessionLocation> getExpLocList() {
			return expLocList;
		}

		/**
		 * @param expLocList the expLocList to set
		 */
		public void setExpLocList(List<ExpessionLocation> expLocList) {
			this.expLocList = expLocList;
		}
		
		

	}

	private IExpressionFragment getSelectedExpression(ASTNode node) throws JavaModelException {
		IExpressionFragment fSelectedExpression = null;
		IASTFragment selectedFragment = ASTFragmentFactory
				.createFragmentForSourceRange(new SourceRange(node.getStartPosition(), node.getLength()), cu, icu);
		if (selectedFragment instanceof IExpressionFragment
				&& !Checks.isInsideJavadoc(selectedFragment.getAssociatedNode())) {
			fSelectedExpression = (IExpressionFragment) selectedFragment;
		} else if (selectedFragment != null) {
			if (selectedFragment.getAssociatedNode() instanceof ExpressionStatement) {
				ExpressionStatement exprStatement = (ExpressionStatement) selectedFragment.getAssociatedNode();
				Expression expression = exprStatement.getExpression();
				fSelectedExpression = (IExpressionFragment) ASTFragmentFactory.createFragmentForFullSubtree(expression);
			} else if (selectedFragment.getAssociatedNode() instanceof Assignment) {
				Assignment assignment = (Assignment) selectedFragment.getAssociatedNode();
				fSelectedExpression = (IExpressionFragment) ASTFragmentFactory.createFragmentForFullSubtree(assignment);
			}
		}

		if (fSelectedExpression != null
				&& Checks.isEnumCase(fSelectedExpression.getAssociatedExpression().getParent())) {
			fSelectedExpression = null;
		}

		return fSelectedExpression;
	}

	private boolean checkSelection(ASTNode node) throws JavaModelException {

		IExpressionFragment selectedExpression = getSelectedExpression(node);

		if (selectedExpression == null) {
			return false;
		}
		if (isUsedInExplicitConstructorCall(node))
			return false;

		ASTNode associatedNode = selectedExpression.getAssociatedNode();
		if (getEnclosingBodyNode(node) == null || ASTNodes.getParent(associatedNode, Annotation.class) != null)
			return false;

		if (associatedNode instanceof Name && associatedNode.getParent() instanceof ClassInstanceCreation
				&& associatedNode.getLocationInParent() == ClassInstanceCreation.TYPE_PROPERTY)
			return false;
		if (!checkExpression(node))
			return false; 
		if (!checkExpressionFragmentIsRValue(node))
			return false; 

		if (isUsedInForInitializerOrUpdater(getSelectedExpression(node).getAssociatedExpression()))
			return false;

		if (isReferringToLocalVariableFromFor(getSelectedExpression(node).getAssociatedExpression()))
			return false;
		return true;
	}
	
	private boolean isUsedInExplicitConstructorCall(ASTNode node ) throws JavaModelException {
		Expression selectedExpression = getSelectedExpression(node).getAssociatedExpression();
		if (ASTNodes.getParent(selectedExpression, ConstructorInvocation.class) != null)
			return true;
		if (ASTNodes.getParent(selectedExpression, SuperConstructorInvocation.class) != null)
			return true;
		return false;
	}
	
	private ASTNode getEnclosingBodyNode(ASTNode astNode) throws JavaModelException {
		ASTNode node = getSelectedExpression(astNode).getAssociatedNode();

		// expression must be in a method, lambda or initializer body
		// make sure it is not in method or parameter annotation
		StructuralPropertyDescriptor location = null;
		while (node != null && !(node instanceof BodyDeclaration)) {
			location = node.getLocationInParent();
			node = node.getParent();
			if (node instanceof LambdaExpression) {
				break;
			}
		}
		if (location == MethodDeclaration.BODY_PROPERTY 
				|| location == Initializer.BODY_PROPERTY
				|| (location == LambdaExpression.BODY_PROPERTY
						&& ((LambdaExpression) node).resolveMethodBinding() != null)
			) {
			return (ASTNode) node.getStructuralProperty(location);
		}
		return null;
	}

	
	private boolean checkExpression(ASTNode node) throws JavaModelException {
		Expression selectedExpression = getSelectedExpression(node).getAssociatedExpression();
		if (selectedExpression != null) {
			final ASTNode parent = selectedExpression.getParent();
			if (selectedExpression instanceof NullLiteral) {
				return false;
			} else if (selectedExpression instanceof ArrayInitializer) {
				return false;
			} else if (selectedExpression instanceof Assignment) {
				if (parent instanceof Expression && !(parent instanceof ParenthesizedExpression))
					return false;
				else
					return true;
			} else if (selectedExpression instanceof SimpleName) {
				if ((((SimpleName) selectedExpression)).isDeclaration())
					return false;
				if (parent instanceof QualifiedName
						&& selectedExpression.getLocationInParent() == QualifiedName.NAME_PROPERTY
						|| parent instanceof FieldAccess
								&& selectedExpression.getLocationInParent() == FieldAccess.NAME_PROPERTY)
					return false;
			} else if (selectedExpression instanceof VariableDeclarationExpression && parent instanceof TryStatement) {
				return false;
			}
		}

		return true;
	}
	
	private boolean checkExpressionFragmentIsRValue(ASTNode node) throws JavaModelException {
		switch (Checks.checkExpressionIsRValue(getSelectedExpression(node).getAssociatedExpression())) {
		case Checks.NOT_RVALUE_MISC:
			return false;
		case Checks.NOT_RVALUE_VOID:
			return false;
		case Checks.IS_RVALUE_GUESSED:
		case Checks.IS_RVALUE:
			return true;//
		default: 
			return true;
		}
	}
	
	private static boolean isUsedInForInitializerOrUpdater(Expression expression) {
		ASTNode parent = expression.getParent();
		if (parent instanceof ForStatement) {
			ForStatement forStmt = (ForStatement) parent;
			return forStmt.initializers().contains(expression) || forStmt.updaters().contains(expression);
		}
		return false;
	}
	
	private static boolean isReferringToLocalVariableFromFor(Expression expression) {
		ASTNode current = expression;
		ASTNode parent = current.getParent();
		while (parent != null && !(parent instanceof BodyDeclaration)) {
			if (parent instanceof ForStatement) {
				ForStatement forStmt = (ForStatement) parent;
				if (forStmt.initializers().contains(current) || forStmt.updaters().contains(current)
						|| forStmt.getExpression() == current) {
					List<Expression> initializers = forStmt.initializers();
					if (initializers.size() == 1 && initializers.get(0) instanceof VariableDeclarationExpression) {
						List<IVariableBinding> forInitializerVariables = getForInitializedVariables(
								(VariableDeclarationExpression) initializers.get(0));
						ForStatementChecker checker = new ForStatementChecker(forInitializerVariables);
						expression.accept(checker);
						if (checker.isReferringToForVariable())
							return true;
					}
				}
			}
			current = parent;
			parent = current.getParent();
		}
		return false;
	}
	
	private static List<IVariableBinding> getForInitializedVariables(
			VariableDeclarationExpression variableDeclarations) {
		List<IVariableBinding> forInitializerVariables = new ArrayList<>(1);
		for (Iterator<VariableDeclarationFragment> iter = variableDeclarations.fragments().iterator(); iter
				.hasNext();) {
			VariableDeclarationFragment fragment = iter.next();
			IVariableBinding binding = fragment.resolveBinding();
			if (binding != null)
				forInitializerVariables.add(binding);
		}
		return forInitializerVariables;
	}

	
}
