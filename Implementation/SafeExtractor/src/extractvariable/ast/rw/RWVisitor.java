package extractvariable.ast.rw;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;

import safeextractor.log.MyLog;
import safeextractor.utils.ASTNodeHandleUtils;
import safeextractor.utils.Constants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class RWVisitor extends ASTVisitor {
 
	protected boolean isLocal;
	protected String owner;
	protected int middleFlag;
	protected ASTNode extractVariable;
	protected List<String> APIList;
	protected HashSet<ITypeBinding> allBindings;
	protected HashMap<String, String> paramHashmap;
	protected List<String> argList;

	protected String key;
	protected String methodKey;
	protected int layer;

	public static long start_time = -1;

	@Override
	public int hashCode() {
		return Objects.hash(methodKey);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof RWVisitor)) {
			return false;
		}
		RWVisitor other = (RWVisitor) obj;
		return Objects.equals(methodKey, other.methodKey);
	}

	public RWVisitor(int layer, boolean isLocal, int middleFlag, List<String> argList) {
		super();
		this.layer = layer;
		this.isLocal = isLocal;
		this.middleFlag = middleFlag;
		this.argList = argList;
		this.paramHashmap = new HashMap<String, String>();
		this.time_out_flag = false;
	}

	protected void addToAPIList(String name, IBinding iBinding) {
		if (this.layer > 0 && iBinding.getJavaElement() != null
				&& iBinding.getJavaElement().getElementType() == IJavaElement.LOCAL_VARIABLE
				&& Modifier.isStatic(iBinding.getModifiers()) == false) {
			ITypeBinding type = ((IVariableBinding) (iBinding)).getType();
			if (!(iBinding instanceof IVariableBinding
					&& (((IVariableBinding) (iBinding)).isParameter() || ((IVariableBinding) (iBinding)).isField()
							|| ((IVariableBinding) (iBinding)).isEnumConstant()))) {
				return;
			} else if (iBinding instanceof IVariableBinding 
					&& ((IVariableBinding) (iBinding)).isParameter()
					&& ASTNodeHandleUtils.isParameterizedType(type.getName())) {
				return;
			}
		}
		String s = null;
		if (key == owner || Modifier.isStatic(iBinding.getModifiers())) {
			if (!APIList.contains(name)) {
				s = name;
			}
		} else {
			if (!APIList.contains(owner + "." + name)) {
				if (this.paramHashmap.containsKey(owner)) {
					s = this.paramHashmap.get(owner) + "." + name;
				} else {
					s = owner + "." + name;
				}
			}
		}
		if (s != null)
			APIList.add(s);
	}

	boolean time_out_flag;

	@Override
	public boolean preVisit2(ASTNode node) {
		if (this.middleFlag == -1 && this.APIList.size() > 0) {
			return false;
		} 
		long current_time = System.currentTimeMillis();
		if (this.layer > 0 && current_time - start_time > Constants.MAX_TIME_LIMITATION) {
			if (!time_out_flag) {
				time_out_flag = true;
			}
			return false;
		}
		if (this.layer > Constants.MAX_LAYER) {
			return false;
		}
		if (this.middleFlag != 1 || !node.toString().equals(extractVariable.toString())) {
			if (node instanceof SimpleName) {
				ASTNode tempNode = node.getParent();
				if (tempNode != null && (tempNode instanceof FieldAccess && ((FieldAccess) tempNode).getName() == node
						|| tempNode instanceof QualifiedName && ((QualifiedName) tempNode).getName() == node)) {
					return false;
				}
			}
			return super.preVisit2(node);
		} else {
			return false;
		}
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		List<SingleVariableDeclaration> parameters = methodDeclaration.parameters();
		for (int i = 0; i < parameters.size() && i < argList.size(); ++i) {
			SingleVariableDeclaration svd = parameters.get(i);
			String s = svd.getName().toString();
			String value = argList.get(i);
			Type type = svd.getType();
			if (!ASTNodeHandleUtils.isParameterizedType(type) && !java.util.regex.Pattern.matches("^ *new ", value)) {// new
				this.paramHashmap.put(s, value); 
			}
		}
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationFragment vdf) {
		if (this.middleFlag > 0 && vdf.getInitializer() != null) {
			if (this.paramHashmap.containsKey(vdf.getInitializer().toString())) {
				String s = this.paramHashmap.get(vdf.getInitializer().toString());
				this.paramHashmap.put(vdf.getName().toString(), s);
			}
		}
		return true;
	}

	@Override
	public boolean visit(Assignment am) {
		if (this.layer > 0 && am.getOperator().toString().equals("=")) {
			if (this.paramHashmap.containsKey(am.getLeftHandSide().toString())) {
				this.paramHashmap.remove(am.getLeftHandSide().toString());
			}
		}
		if (this.layer > 0 && am.getOperator().toString().equals("=")) {
			if (this.paramHashmap.containsKey(am.getRightHandSide().toString())) {
				String s = this.paramHashmap.get(am.getRightHandSide().toString());
				this.paramHashmap.put(am.getLeftHandSide().toString(), s);
			}
		}
		return true;
	}

	public List<String> getAPIList() {
		return APIList;
	}
}
