import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: irina
 * Date: 27/03/13
 * Time: 11:46 AM
 * To change this template use File | Settings | File Templates.
 */


/*
This class carries out a test run of exhaustive search into objective space and report the best variant.
 */
public class Exhaust {


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
        //repository.printRepository();

        Objective[] objectives = new Objective[2];
        objectives[0] = new Area("units", "max",res_list, repository, 50);
        objectives[1] = new Power(clock, "mW", "min", res_list, repository, (Area) objectives[0]);
        
        int[] variant = new int[res_list.size()];
        variant = objectives[0].getMinVariant();

        while (variant != null){
            objectives[0].evaluate(variant);
            objectives[1].evaluate(variant);
            variant = objectives[0].getNextVariant(variant,1);

        }

        repository.printRepository();


    }

}
