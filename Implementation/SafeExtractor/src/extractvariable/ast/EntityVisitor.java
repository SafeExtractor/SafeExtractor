package extractvariable.ast;
import org.eclipse.jdt.core.dom.*;

import java.util.*;

public class EntityVisitor extends ASTVisitor {
    private ITypeBinding currentClass;
    private HashSet<ITypeBinding> targetClass;
    private ArrayList<ArrayAccess> accessArrayList;
    private ArrayList<MethodInvocation> methodInvocationList;
    private HashSet<IBinding> allBindings;

    public ArrayList<ArrayAccess> getAccessArrayList() {
        return accessArrayList;
    }

    public HashSet<IBinding> getAllBindings() {
        return allBindings;
    }

    public void setAllBindings(HashSet<IBinding> allBindings) {
        this.allBindings = allBindings;
    }


    public EntityVisitor(ITypeBinding currentClass) {
        targetClass = new HashSet<>();
        accessArrayList = new ArrayList<ArrayAccess>();
        methodInvocationList = new ArrayList<MethodInvocation>();
        allBindings=new HashSet<IBinding>();
        Collections.sort(accessArrayList,new Comparator<ArrayAccess>(){
            @Override
            public int compare(ArrayAccess o1, ArrayAccess o2) {
                int v=o1.getArray().toString().compareTo(o2.getArray().toString());
                if( v!=0)
                    return v;
                else {
                    return o1.getIndex().toString().compareTo(o2.getIndex().toString());
                }
            }
        });
        this.currentClass = currentClass;
    }



    public HashSet<ITypeBinding> getTargetClass() {
        return targetClass;
    }

    public boolean visit(MethodInvocation node) {
        IMethodBinding methodBinding = node.resolveMethodBinding();
        if( node == null ) return true;
        else {
            methodInvocationList.add(node);
        }
        if (methodBinding == null ) return true;
        ITypeBinding typeBinding = methodBinding.getDeclaringClass();
        if (typeBinding == null || typeBinding.isInterface()) return true;
        if (!typeBinding.equals(currentClass) && typeBinding.isFromSource()) {
            targetClass.add(typeBinding);
        } 

        return true;
    }

    public boolean visit(FieldAccess node) {
        IVariableBinding variableBinding = node.resolveFieldBinding();
        if (variableBinding == null) return true;
        ITypeBinding typeBinding = variableBinding.getDeclaringClass();
        if (typeBinding == null) return true;
        if (!typeBinding.equals(currentClass) && typeBinding.isFromSource()) {
            targetClass.add(typeBinding);
        }
        return true;
    }

    public boolean visit(QualifiedName node) {
        if (node.resolveBinding() instanceof IVariableBinding) {
            IVariableBinding variableBinding = (IVariableBinding) node.resolveBinding();
            if (variableBinding.isField()) {
                ITypeBinding typeBinding = variableBinding.getDeclaringClass();
                if (typeBinding == null)
                    return true;
                if (!typeBinding.equals(currentClass) && typeBinding.isFromSource()) {
                    targetClass.add(typeBinding);
                }
            }

        }
        return true;
    }

    public boolean visit(ArrayAccess  node) {
        accessArrayList.add(node);
        return true;
    }

    public boolean visit(FieldDeclaration  node) {
        return true;
    }

    public ArrayList<MethodInvocation> getMethodInvocationList() {
        return methodInvocationList;
    }
}
