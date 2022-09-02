package extractvariable.ast.middlecode;

import extractvariable.ast.rw.RWVisitor;
import extractvariable.ast.rw.reader.ReaderSet;
import extractvariable.ast.rw.writer.WriterSet;
import extractvariable.ast.rw.writer.WriterVisitor;
import safeextractor.log.MyLog;

import org.eclipse.jdt.core.dom.*;

import java.util.*;

public class MiddleCode {
	ASTNode loopNode;
	ASTNode commonNode;
	ASTNode node;// 到node结尾
	ASTNode expression1;
	ASTNode expression2;
	public MiddleCodeVisitor middleCodeVisitor;
	public static HashSet<ASTNode> allStatement;
	int endPosition;
	public boolean valid;


	public MiddleCode(int startPosition, int endPostion, ASTNode expression1, ASTNode expression2, ASTNode commonNode,
			CompilationUnit compilationUnit, String key, Set<String> tempWriterSet, Set<String> tempReaderSet) {
		this.commonNode = commonNode;
		this.expression1 = expression1;
		this.expression2 = expression2;
		this.endPosition = endPostion;
		this.valid = true;
		HashSet<ASTNode> excludeSet = excludeIFELSEBranch(expression2);
		if (expression1 != null)
			excludeSet.add(expression1);
		if (expression2 != null)
			excludeSet.add(expression2);
		excludeSet.addAll(excludeSwitchBranch());

		middleCodeVisitor = new MiddleCodeVisitor(compilationUnit, startPosition, endPostion, excludeSet,
				this.expression1, this.expression2);
		compilationUnit.accept(middleCodeVisitor);
		for (ASTNode node : middleCodeVisitor.getAllStatement()) {
			// 要排除的节点
			boolean v = true;
			for (ASTNode astnode : allStatement) {
				if (astnode.getStartPosition() == node.getStartPosition() && astnode.getLength() == node.getLength()) {
					v = false;
					break;
				}
			}
			if (v == false) {
				continue;
			}     
			allStatement.add(node);
			WriterSet writerSet = new WriterSet(node, 1, key, expression1);
			LinkedHashSet<String> writerHashSet = new LinkedHashSet<>(writerSet.getResList());
			ArrayList<String> list = new ArrayList<>(writerHashSet);
			if (extractvariable.detector.Comparator.isNoOverLap(list) == false) {
				this.valid = false;
				break;
			}
		}
	}

	private HashSet<ASTNode> excludeSwitchBranch() {
		HashSet<ASTNode> excludeSet = new HashSet<ASTNode>();
		
		if (!(this.commonNode instanceof SwitchStatement)) {
			ASTNode temp2 = this.expression2;
			boolean switchFlag = false;
			while (temp2 != null && temp2 != this.commonNode) {
				if (temp2 instanceof SwitchStatement) {
					switchFlag = true;
					break;
				}
				temp2 = temp2.getParent();
			}
			if (switchFlag) {
				SwitchStatement sw = (SwitchStatement) temp2;
				int index2 = -1;
				boolean isbreak=false;
 
				for (int k = sw.statements().size() - 1; k >= 0; --k) {
					Object obj = sw.statements().get(k);
					if (!(obj instanceof ASTNode)) {
						continue;
					}
					ASTNode node = (ASTNode) obj;
					if (index2 != -1) {
						BreakVisitor bv = new BreakVisitor(node);
						ReturnVisitor rv = new ReturnVisitor(node);
						node.accept(bv);
						node.accept(rv);
						if (bv.breakFlag || rv.returnFlag) {
							isbreak = true;
						}
						if (isbreak) {
							excludeSet.add(node); 
						}
					} 
					if (index2 == -1 && this.expression2.getStartPosition() >= node.getStartPosition()
							&& this.expression2.getStartPosition()
									+ this.expression2.getLength() <= node.getStartPosition() + node.getLength()) {
						index2 = k;
					}
				}
			}
			return excludeSet;
		}

		SwitchStatement sw = (SwitchStatement) commonNode;
		int index1 = -1;
		int index2=-1;
		int caseIndex=-1;
		boolean isbreak = false; 
		
		if(this.expression1.getStartPosition()!=this.expression2.getStartPosition()) {
			for (int k = 0; k < sw.statements().size(); ++k) {
				Object obj = sw.statements().get(k);
				if (!(obj instanceof ASTNode) ) {
					continue;
				} 
				ASTNode node = (ASTNode) obj;
				if(index2==-1 && (node instanceof SwitchCase)) {
					caseIndex= k;
				}
				if (index1==-1&&this.expression1.getStartPosition() >= node.getStartPosition()
						&& this.expression1.getStartPosition() + this.expression1.getLength() <= node.getStartPosition() + node.getLength()) {
					index1 = k;  
				}
				if (index2==-1&&this.expression2.getStartPosition() >= node.getStartPosition()
						&& this.expression2.getStartPosition() + this.expression2.getLength() <= node.getStartPosition() + node.getLength()) {
					index2 = k; 
				}
				if (index1 != -1) {
					BreakVisitor bv = new BreakVisitor(node);
					ReturnVisitor rv = new ReturnVisitor(node);
					node.accept(bv);
					node.accept(rv);
					if (bv.breakFlag || rv.returnFlag) { 
						isbreak = true;
					} 
				}
				if (isbreak) {
					excludeSet.add(node); 
				}
			} 
			if(index1!=-1&&index2!=-1 && isbreak ) {
				 
				for (int k = 0; k <= caseIndex; ++k) {
					Object obj = sw.statements().get(k);
					if (!(obj instanceof ASTNode) || obj == null ) {
						continue;
					}
					excludeSet.add(( ASTNode)obj); 
				}
			}
		}else {
			for (int k = sw.statements().size()-1; k >=0; --k) {
				Object obj = sw.statements().get(k);
				if (!(obj instanceof ASTNode)) {
					continue;
				}
				ASTNode node = (ASTNode) obj;				 
				if (index1 != -1) {
					BreakVisitor bv = new BreakVisitor(node);
					ReturnVisitor rv = new ReturnVisitor(node);
					node.accept(bv);
					node.accept(rv);
					if (bv.breakFlag || rv.returnFlag) {
						isbreak = true;
					}
				}
				if (isbreak) {
					excludeSet.add(node); 
				}
				if (index1==-1&&this.expression1.getStartPosition() >= node.getStartPosition()
						&& this.expression1.getStartPosition() +this.expression1.getLength() <= node.getStartPosition() + node.getLength()) {
					index1 = k;
				} 
			}
		}
		  
		return excludeSet;
	}

	private HashSet<ASTNode> excludeIFELSEBranch(ASTNode astNode) {
		HashSet<ASTNode> excludeSet = new HashSet<ASTNode>();
		ASTNode temp = astNode;
		IfStatement largestIfStat = null;
		ASTNode fiirstShowLocate = null;
		while (temp != null && temp != this.commonNode) {
			ASTNode pre = temp;
			temp = temp.getParent();
			if (temp instanceof IfStatement) {
				largestIfStat = (IfStatement) temp;
				if (((IfStatement) temp).getElseStatement() == pre) {
					AllNodeVisitor av = new AllNodeVisitor(astNode);
					((IfStatement) temp).getThenStatement().accept(av);
					if (av.occurFlag == false) {
						excludeSet.add(((IfStatement) temp).getThenStatement());
					}
				}   
			}
		}
		return excludeSet;
	}

	public static void init() {
		allStatement = new HashSet<ASTNode>();
	}

	public static void restore() {
		allStatement = new HashSet<ASTNode>();
	}

	class AllNodeVisitor extends ASTVisitor {
		ASTNode expression;
		boolean occurFlag;

		AllNodeVisitor(ASTNode node) {
			this.expression = node;
			this.occurFlag = false;
		}

		@Override
		public boolean preVisit2(ASTNode node) {
			if (node == expression) {
				occurFlag = true;
			}
			return super.preVisit2(node);
		}

	}

	class BreakVisitor extends ASTVisitor {
		ASTNode expression;
		boolean breakFlag;

		BreakVisitor(ASTNode node) {
			this.expression = node;
			this.breakFlag = false;
		}

		@Override
		public boolean preVisit2(ASTNode node) {
			if (node instanceof EnhancedForStatement || node instanceof WhileStatement || node instanceof ForStatement
					|| node instanceof DoStatement) {
				return false;
			}
			if (node instanceof BreakStatement) {
				breakFlag = true;
			}
			if (breakFlag)
				return false;
			return super.preVisit2(node);
		}

	}

	class ReturnVisitor extends ASTVisitor {
		ASTNode expression;
		boolean returnFlag;

		ReturnVisitor(ASTNode node) {
			this.expression = node;
			this.returnFlag = false;
		}

		@Override
		public boolean preVisit2(ASTNode node) {
			if (returnFlag)
				return false;
			if (node instanceof ReturnStatement) {
				returnFlag = true;
			}
			return super.preVisit2(node);
		}

	} 
}
