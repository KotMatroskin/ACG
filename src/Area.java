import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: irina
 * Date: 16/11/12
 * Time: 2:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class Area extends Objective {



    public Area(String units, String goal, ArrayList<Resource> res_list) {
        super("area", units, goal, res_list);
    }

    public Area(String units, String goal, ArrayList<Resource> res_list, double constraint) {
        super("area", units, goal, res_list);
    }

    public Area(String units, String goal, ArrayList<Resource> res_list, VariantRepository rep) {
        super("area", units, goal, res_list, rep);
    }

    public Area(String units, String goal, ArrayList<Resource> res_list, VariantRepository rep, double constraint) {
        super("area", units, goal, res_list, rep, constraint);
    }




    //specification of the area objective function
    //variant array specifies the numbers/versions in the same order as
    //presently stored in the list of resources
    public double evaluate(int[] variant) {

        System.out.println("Calling area evaluate with variant " + Arrays.toString(variant));
        //first check if this variant hasn't been evaluated already
        int pos;
        System.out.println ("Evaluating and mask is " + Arrays.toString(mask));
        int[] result = rep.findVariant(variant, mask, super.getName());
        pos = result[1];
        if (result[0] == 1) { //found the variant
            System.out.println("Found variant " + Arrays.toString(variant) + " at position " + pos);
            //variant already has been computed before and value filed
            return rep.getVariantValue(pos, super.getName());
        } else { //computer the value of variant and insert it


            System.out.println("Evaluate variant: " + Arrays.toString(variant));

            ArrayList<Resource> resources = super.getResourceList();

            if (resources.size() != variant.length) {
                throw (new Error("the variant of resources must have the same length as number of *types* of resources available"));
            }

            Double area = 0.0;

            for (int i = 0; i < resources.size(); i++) {

                //System.out.println(variant[0] + "-----");
                if (variant[i] != 0) {

                    System.out.println(super.getName() + " varinat " + Arrays.toString(variant));
                    area += resources.get(i).getValue(super.getName(), variant[i]);
                }

            }
            System.out.println("Area for this variant is: " + area);
            rep.insertVariant(variant, mask, area, super.getName(), pos);
            return area;
        }
    }


}
