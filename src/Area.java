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

    private int mask; //maps the resources' order for this objective with that used in the repository

    public Area(String units, String goal, ArrayList<Resource> res_list, VariantRepository rep) {
        super("area", units, goal, res_list, rep);
    }

    public Area(String units, String goal, double constraint, ArrayList<Resource> res_list, VariantRepository rep) {
        super("area", units, goal, constraint, res_list, rep);
    }


    //how to convert from the order of the list to the order that is used in repository
    public void createMask (ArrayList<Resource> list){
        mask = new int[list.size()];
        for (int i = 0; i < list.size(); i++){

        }

    }

    //specification of the area objective function
    //variant array specifies the numbers/versions in the same order as
    //presently stored in the list of resources
    public double evaluate(int[] variant) {

        //first check if this variant hasn't been evaluated already
        int pos = rep.findVariant(variant, mask);
        if (pos < 0) { //variant already has been computed before and value filed
             return rep.getVariantValue(pos);
        } else  { //computer the value of variant and insert it


            System.out.println("Evalute variant: " + Arrays.toString(variant));

            ArrayList<Resource> resources = super.getResourceList();

            if (resources.size() != variant.length) {
                throw (new Error("the variant of resources must have the same length as number of *types* of resources available"));
            }

            Double area = 0.0;

            for (int i = 0; i < resources.size(); i++) {

                //System.out.println(variant[0] + "-----");
                if (variant[i] != 0) {

                    //System.out.println(super.getName() + " varinat " + Arrays.toString(variant));
                    area += resources.get(i).getValue(super.getName(), variant[i]);
                }

            }
            System.out.println("Area for this variant is: " + area);
            insertVariant(variant, area, pos);
            return area;
        }
    }


    public int find_Variant (int[] var){

    }
}
