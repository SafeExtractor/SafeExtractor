package safeextractor.json;

import java.util.Objects;

public class NativeFieldJson { 
    private String methodName; 
    private boolean label;

    public NativeFieldJson( String methodName ,boolean label) { 
        this.methodName = methodName;
        this.label=label;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NativeFieldJson nfj  = ( NativeFieldJson) o;
        String str1= methodName;
        String str2= nfj.getMethodName();
        if (!Objects.equals(str1, str2)) {
            return false;
        }
        return true;
    }
    @Override
    public int hashCode() {
        return  Objects.hash(methodName);
    }
 
    public String getMethodName() {
        return methodName;
    }
    public boolean getLabel() {
        return label;
    }
 

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    public void setLabel(boolean label) {
        this.label = label;
    }
 
    public void setALL(NativeFieldJson nfj){
        this.methodName =nfj.getMethodName();
        this.label=nfj.getLabel();
    }
}
