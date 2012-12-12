import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: irina
 * Date: 16/11/12
 * Time: 5:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResourceCopies extends Resource {

    private double[] objectiveValue; //list of values for each objective (in the same order as objectives list) for one resource

    public ResourceCopies(String name, int num, String[] objectives) {
        super(name, num, objectives);
        objectiveValue = new double[objectives.length];

    }


    public ResourceCopies(int num, String[] objectives) {
        super(num, objectives);
        objectiveValue = new double[objectives.length];
    }

    public ResourceCopies(String name, int num, String[] objectives, Boolean not_included) {
        super(name, num, objectives, not_included);
        objectiveValue = new double[objectives.length];
    }
    //use only when the version of resource is its number of
    public void setObjectiveValues(String objective, double value) {
        objectiveValue[find(objective)] = value;
    }

    //sorts the versions list in the order of values

    //TODO Maybe make getValue not abstract but separate for each type of resources
    public double getValue(String objective, int num) {
        if (num != 0)
            return objectiveValue[find(objective)] * num;
        else return 0;

    }

    public double getNextValue (String objective, int num){
         return getValue(objective, num);

    }
    
//    public void sortByValue(String objective) {
//        Arrays.sort(objectiveValues[find(objective)]);
//
//    }

}



