import javax.swing.text.StyledEditorKit;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Created by IntelliJ IDEA.
 * User: irina
 * Date: 16/11/12
 * Time: 1:28 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Resource {

    private String name = "no name"; //default name for resource
    private int num; //how many items of this resource are available or how many versions
    private Boolean not_included = true; //when true that means that a combination where this particular resources
    // is not included at all is valid, and when false it means that at least one version or copy of this resources is
    //always included (even if it not actually used)

    private String[] objectives;      // TODO make objectives list static?

    public Resource (int num, String[] objectives) {
        this.num = num;
        this.objectives = objectives;
    }

    public Resource(String name, int num, String[] objectives) {
        this (num, objectives);
        this.name = name;
        System.out.println(this.objectives + "sdf");
    }

    public Resource (int num, String[] objectives, Boolean not_included) {
        this (num, objectives);
        this.not_included = not_included;
    }

    public Resource(String name, int num, String[] objectives, Boolean not_included) {
        this(name, num, objectives);
        this.not_included = not_included;
    }
    
    public void setNoResoursePossible (Boolean value){
        not_included = value;
    }

    public int getNum() {
        return num;
    }

    public String getName() {
        return name;
    }

    public abstract double getValue(String objective, int number);

    public abstract double getNextValue (String objective, int number);

    public int getMin() {
        if (not_included) return 0;
        else return 1;

    }

    /*
    returns the maximum INDEX of number or copies of resource. This does not depend on whether
    it is possible for this particular resource not to be included at all.
    For example, suppose there are 3 copies of resource available, then, the maximum index
    is 3. It may also be that it is possible not to include this resource at all.
    */
    public int getMax() {
        return num;
    }

    public int getMaxIndex (){
        if (not_included) return num;
        else return num-1;
    }
    
    int find(String objctv) {

        for (int i = 0; i < objectives.length; i++) {

            if (objectives[i].equals(objctv))
                return i;
        }
        return -1;
    }
}
