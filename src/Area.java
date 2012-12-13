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

    public Area(String units, String goal) {
        super("area", units, goal);
    }

    public Area(String units, String goal, double constraint) {
        super("area", units, goal, constraint);
    }

    //specification of the area objective function
    //variant array specifies the numbers/versions in the same order as
    //presently stored in the list of resources
    public double evaluate(int[] variant) {

        //first check if this variant hasn't been evaluated already
        int pos = findVariant(variant);
        if (pos < 0) { //variant already has been computed before and value filed
             return getVariantValue(pos);
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
}
