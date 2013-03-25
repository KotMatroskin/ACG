import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: irina
 * Date: 18/12/12
 * Time: 5:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class Performance extends Objective{



    public Performance(String units, String goal, ArrayList<Resource> res_list) {
        super("performance", units, goal, res_list);
    }

    public Performance(String units, String goal, ArrayList<Resource> res_list, double constraint) {
        super("performance", units, goal, res_list);
    }

    public Performance(String units, String goal, ArrayList<Resource> res_list, VariantRepository rep) {
        super("performance", units, goal, res_list, rep);
    }

    public Performance(String units, String goal, ArrayList<Resource> res_list, VariantRepository rep, double constraint) {
        super("performance", units, goal, res_list, rep, constraint);
    }


    public double evaluate(int[] variant) {
       return 2.0;
    }

    //public void setMask(ArrayList<Resource> list) {}

    //returns a defensive copy of mask
    public int[] getMask() {
        return Arrays.copyOf(mask, mask.length);
    }
}
