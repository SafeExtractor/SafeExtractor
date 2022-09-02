package extractvariable.ast;

public class Field {
    private String name;
    private int isStatic;

    public Field(String name) {
        this.name = name;
        this.isStatic=0;
    }

    public Field(String name,boolean isStatic) {
        this.name = name;
        if(isStatic){
            this.isStatic = 1;
        }else {
            this.isStatic = 0;
        }
    }

    public String getName() {
        return name;
    }

    public int getIsStatic() {
        return isStatic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Field)) return false;

        Field field = (Field) o;

        if (getIsStatic() != field.getIsStatic()) return false;
        return getName().equals(field.getName());
    }

    @Override
    public int hashCode() {
        int result = getName().hashCode();
        result = 31 * result + getIsStatic();
        return result;
    }
}
