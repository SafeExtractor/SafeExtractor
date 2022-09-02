package extractvariable.api;

import org.eclipse.jdt.core.dom.*;

import java.util.HashSet;
import java.util.Objects;

public class APIFieldRecord{
    String name;
    private IVariableBinding iVariableBinding;
    boolean addFlag;
    boolean isContainsNative;

    public APIFieldRecord(IVariableBinding iVariableBinding)  {
        this.iVariableBinding =iVariableBinding;
        this.name=iVariableBinding.getName().toString();
        this.addFlag=true;
        this.isContainsNative=false;
        if(Modifier.isStatic(iVariableBinding.getModifiers())||Modifier.isFinal(iVariableBinding.getModifiers())){
            this.addFlag=false;
            return;
        }
        if(iVariableBinding.getType().isArray()){
            this.name=this.name+"[]";
        }
        if(iVariableBinding.getType().isPrimitive()){
        } 
    }

    public boolean isContainsNative() {
        return isContainsNative;
    }

    public String getName() {
        return name;
    }

    public IVariableBinding getIVariableBinding() {
        return iVariableBinding;
    }

    @Override
    public boolean equals(Object o) {
        //传进来的是string
        if (o == null || !( o instanceof APIFieldRecord)) return false;

        String str1= this.getIVariableBinding().getKey();
        String str2=((APIFieldRecord) o).getIVariableBinding().getKey();

        if (!Objects.equals(str1, str2)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return this.getIVariableBinding().getKey().hashCode();
    }
}