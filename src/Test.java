import java.util.ArrayList;
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


        ArrayList<Resource> res_list = new ArrayList<Resource>();
        String[] obj = {"area", "Power"};


        //create resources
        ResourceVersions adder = new ResourceVersions("adder",7 , obj, false);
        adder.setObjectiveValues(obj[0], new double[]{1.0,3.0,6.0, 7.0, 8.0,13.0, 14.0});

        ResourceCopies mult = new ResourceCopies("multiplier", 2, obj, false);
        mult.setObjectiveValues(obj[0], 20);

        ResourceVersions div = new ResourceVersions("divider", 6, obj, true);
        div.setObjectiveValues(obj[0], new double[]{5.0, 8.0, 9.0, 13.0, 15.0, 25.0});
        //ResourceCopies accum = new ResourceCopies("accum", 2, obj, false);

        ResourceVersions accum = new ResourceVersions("accum", 4, obj, false);
        accum.setObjectiveValues(obj[0], new double[] {20,80.0,90,100});

        Clock clock = new Clock("clock", 3, obj, false);
        clock.setObjectiveValues(obj[0], new double[] {2.0,3.0,9.0});
        clock.setObjectiveValues(obj[1], new double[] {100.0,140.0,800.0});

        res_list.add(adder);
        res_list.add(mult);
        res_list.add(div);
        res_list.add(accum);
        res_list.add(clock);

        VariantRepository repository = new VariantRepository(res_list, obj);
        repository.printRepository();
        //define objectives
        Objective[] objectives = new Objective[2];
        objectives[0] = new Area("units", "max",res_list, repository, 50);
        objectives[1] = new Power(clock, "mW", "min", res_list, repository, (Area) objectives[0]);

        //------- Arrange ACG space for Area objective --------
        System.out.println(objectives[0].toString());
        objectives[0].setMask(res_list);
        objectives[0].sortResources();
        System.out.println(objectives[0].toString());

        objectives[0].findTightBorderVar();
        System.out.println(Arrays.toString(objectives[0].getTight_border_var()));

        repository.printRepository();

        //System.out.println(Arrays.toString(objectives[0].getResourceList().toArray()));




        //----- Arrange ACG space for Power objective -------
        //Theoretically, the value for power can be flooded into repository,
        // since it'sarea*power, but here it's not assumed so the full process is carried out
        System.out.println("\n\n"+objectives[1].toString());
        //objectives[1].setMask(res_list); //TODO check - I think not needed
        objectives[1].sortResources();
        System.out.println(objectives[1].toString());
        objectives[1].findTightBorderVar();


        //save trees
        //objectives[0].toYGraph(null);
        //objectives[1].toYGraph(null);

        System.out.println(Arrays.toString(objectives[0].getResourceList().toArray()));

        int[] opt_var = Objective.optimize(objectives,objectives[1],repository);
        System.out.println(Arrays.toString (opt_var));
        System.out.println(Arrays.toString(objectives[1].getResourceList().toArray()));
        System.out.println("#########Border for area is " + Arrays.toString(objectives[0].getTight_border_var()));
        System.out.println("#########Border for power is " + Arrays.toString(objectives[1].getTight_border_var()));


    }
}
