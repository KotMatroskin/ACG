import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: irina
 * Date: 19/11/12
 * Time: 4:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class Test {
    public static void main(String[] args) {

        //define objectives
        Objective[] objectives = new Objective[1];
        objectives[0] = new Area("bla", "max",70);
        String[] obj = {"area"};
        //test resources
        ResourceVersions adder = new ResourceVersions("adder",7 , obj, false);
        adder.setObjectiveValues(obj[0], new double[]{1.0,3.0,6.0, 7.0, 8.0,13.0, 14.0});
        ResourceCopies mult = new ResourceCopies("multiplier", 2, obj, false);
        mult.setObjectiveValues(obj[0], 20);
        ResourceVersions div = new ResourceVersions("divider", 6, obj, true);
        div.setObjectiveValues(obj[0], new double[]{5.0, 8.0, 9.0, 13.0, 15.0, 25.0});
        //ResourceCopies accum = new ResourceCopies("accum", 2, obj, false);
        ResourceVersions accum = new ResourceVersions("accum", 4, obj, false);
        accum.setObjectiveValues(obj[0], new double[] {20,80.0,90,100});

        objectives[0].addResource(adder);
        objectives[0].addResource(mult);
        objectives[0].addResource(div);
        objectives[0].addResource(accum);

        System.out.println(objectives[0].toString());

        objectives[0].sortResources();
        System.out.println(objectives[0].toString());

        objectives[0].findTightBorderVar();
        System.out.println(Arrays.toString(objectives[0].getTight_border_var()));
    }
}
