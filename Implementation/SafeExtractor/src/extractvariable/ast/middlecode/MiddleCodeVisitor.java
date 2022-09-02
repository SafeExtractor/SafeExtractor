package extractvariable.ast.middlecode;

import org.eclipse.jdt.core.dom.*;

import safeextractor.log.MyLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MiddleCodeVisitor extends ASTVisitor {
	int startPosition;
	int endPosition;
	CompilationUnit compilationUnit;
	HashSet<ASTNode> allStatement;
	HashSet<ASTNode> excludeASTNodeSet;
	ASTNode expression1;
	ASTNode expression2;

	public MiddleCodeVisitor(CompilationUnit cu, int startPosition, int endPosition, HashSet<ASTNode> excludeASTNodeSet,
			ASTNode expression1, ASTNode expression2) {
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.compilationUnit = cu;
		this.allStatement = new HashSet<ASTNode>();
		this.excludeASTNodeSet = excludeASTNodeSet;
		this.expression1 = expression1;
		this.expression2 = expression2;
		findLoop();
		findAssignment();
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		for (ASTNode astnode : excludeASTNodeSet) {
			if (astnode.getStartPosition() == node.getStartPosition() && astnode.getLength() == node.getLength()) {
				return false;
			}
		}
		for (ASTNode astnode : this.allStatement) {
			if (astnode.getStartPosition() == node.getStartPosition() && astnode.getLength() == node.getLength()) {
				return false;
			}
		}

		if (node instanceof Type || node instanceof SimpleName || node instanceof StringLiteral) {
			return false;
		}
		ASTNode temp = node;
		int sl = node.getStartPosition();
		int el = node.getStartPosition() + node.getLength();
		if (sl >= startPosition && el <= endPosition) {
			this.allStatement.add(temp);
			return false;
		}
		return super.preVisit2(node);
	}

	public HashSet<ASTNode> getAllStatement() {
		return allStatement;
	}

	public void setAllStatement(HashSet<ASTNode> allStatement) {
		this.allStatement = allStatement;
	}

	private void findAssignment() {
		if (this.expression1 == this.expression2) {
			return;
		}
		ASTNode temp1 = this.expression1; 
		for (ASTNode node : this.excludeASTNodeSet) {
			ASTNode temp2 = this.expression1.getParent(); 
			while (temp2 != null && !(temp2 instanceof MethodDeclaration)) { 
				if (node.getStartPosition() == temp2.getStartPosition() && node.getLength() == temp2.getLength()) {
					return;
				}
				temp2 = temp2.getParent();
			} 
		}
		while (temp1 != null && !(temp1 instanceof Statement)) {
			if (temp1 instanceof Assignment) {
				this.allStatement.add(temp1);
			}
			temp1 = temp1.getParent();
		}
	}

	private void findLoop() {
		ASTNode temp1 = this.expression1;
		ASTNode temp2 = this.expression2;
		if (this.expression1 == this.expression2) {
			NodeFinder finder = new NodeFinder(this.compilationUnit, this.startPosition, 0);
			temp1 = finder.getCoveringNode();
		}
		List<ASTNode> list1 = new ArrayList<>();
		List<ASTNode> list2 = new ArrayList<>();
		while (temp1 != null && !(temp1 instanceof MethodDeclaration)) {
			list1.add(temp1);
			temp1 = temp1.getParent();
		}
		while (temp2 != null && !(temp2 instanceof MethodDeclaration)) {
			list2.add(temp2);
			temp2 = temp2.getParent();
		}
		int counti = -1;
		int countj = -1;
		for (int i = list1.size() - 1, j = list2.size() - 1; i >= 0 && j >= 0; i--, j--) {
			if (list1.get(i).getStartPosition() == list2.get(j).getStartPosition()
					&& list1.get(i).getLength() == list2.get(j).getLength()) {
				counti = i;
				countj = j;
			} else {
				break;
			}
		}
		for (int i = counti - 1; i >= 0; --i) {
			ASTNode temp = list1.get(i);
			if (temp instanceof EnhancedForStatement || temp instanceof WhileStatement
					|| temp instanceof ForStatement) {
				this.allStatement.add(temp);
				break;
			}
		}
		for (int i = countj - 1; i >= 0; --i) {
			ASTNode temp = list2.get(i);
			if (temp instanceof EnhancedForStatement || temp instanceof WhileStatement
					|| temp instanceof ForStatement) {
				this.allStatement.add(temp);
				break;
			}
		}
	}
}