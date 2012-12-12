import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: irina
 * Date: 16/11/12
 * Time: 5:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResourceVersions extends Resource {
    private double[][] objectiveValues; //array of values, each row is a list of values for a particular objective for each of the versions

    //num - number of version of this resource that are avaialble
    public ResourceVersions(String name, int num, String[] objectives) {
        super(name, num, objectives);
        objectiveValues = new double[objectives.length][num];
    }

    public ResourceVersions(String name, int num, String[] objectives, Boolean not_included) {
        super(name, num, objectives, not_included);
        objectiveValues = new double[objectives.length][num];
    }

    public ResourceVersions(int num, String[] objectives) {
        super(num, objectives);
        objectiveValues = new double[objectives.length][num];
    }

    public ResourceVersions(int num, String[] objectives, Boolean not_included) {
        super(num, objectives, not_included);
        objectiveValues = new double[objectives.length][num];
    }

    //objective name would be pulled out from objective
    public void setObjectiveValues(String objective, double[] values) {
        objectiveValues[find(objective)] = values;
    }


    public double getValue(String objective, int number) {
        if (number == 0) return 0;
        else {
            //System.out.println("->" + super.getName() + " " + objective + " " + number + " " + objectiveValues[0].length);
            return objectiveValues[find(objective)][number - 1];

        }

    }
    
    
    public double getNextValue (String objective, int num){
        assert (num < objectiveValues[find(objective)].length);
        if (num == 0) return 0;
        else {
            //System.out.println("->" + super.getName() + " " + objective + " " + number + " " + objectiveValues[0].length);
            return objectiveValues[find(objective)][num];

        }

    }

}
