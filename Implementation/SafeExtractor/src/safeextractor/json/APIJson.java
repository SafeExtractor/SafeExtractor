package safeextractor.json;

import extractvariable.ast.Field;

import java.util.List;
import java.util.Objects;

public class APIJson {
    private String className;
    private String methodName;
    private String methodKey;
    private List<Field> readList;
    private List<Field> writeList;
    private List<Field> nativeList;

    public APIJson(String className, String methodName, String methodKey, List<Field> readList, List<Field> writeList, List<Field> nativeList) {
        this.className = className;
        this.methodName = methodName;
        this.methodKey = methodKey;
        this.readList = readList;
        this.writeList = writeList;
        this.nativeList=nativeList;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        APIJson apiJson  = (APIJson) o;
        String str1= methodKey;
        String str2= apiJson.getMethodKey();
        if (!Objects.equals(str1, str2)) {
            return false;
        }
        return true;
    }
    @Override
    public int hashCode() {
        return  Objects.hash(methodKey);
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodKey() {
        return methodKey;
    }

    public List<Field> getReadList() {
        return readList;
    }

    public List<Field> getWriteList() {
        return writeList;
    }

    public List<Field> getNativeList() {
        return nativeList;
    }

    public void setNativeList(List<Field> nativeList) {
        this.nativeList = nativeList;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setMethodKey(String methodKey) {
        this.methodKey = methodKey;
    }

    public void setReadList(List<Field> readList) {
        this.readList = readList;
    }

    public void setWriteList(List<Field> writeList) {
        this.writeList = writeList;
    }

    public void setALL(APIJson apiJson){
        this.className = apiJson.className;
        this.methodName = apiJson.methodName;
        this.methodKey = apiJson.methodKey;
        this.readList = apiJson.readList;
        this.writeList = apiJson.writeList;
        this.nativeList=apiJson.nativeList;
    }


}
