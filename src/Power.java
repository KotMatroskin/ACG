import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: irina
 * Date: 21/12/12
 * Time: 4:42 PM
 * To change this template use File | Settings | File Templates.
 */
    public class Power extends Objective{


        public Power(String units, String goal, ArrayList<Resource> res_list) {
            super("Power", units, goal, res_list);
        }

        public Power(String units, String goal, ArrayList<Resource> res_list, double constraint) {
            super("Power", units, goal, res_list);
        }

        public Power(String units, String goal, ArrayList<Resource> res_list, VariantRepository rep) {
            super("Power", units, goal, res_list, rep);
        }

        public Power(String units, String goal, ArrayList<Resource> res_list, VariantRepository rep, double constraint) {
            super("Power", units, goal, res_list, rep, constraint);
        }

        //to evaluate power we need to know the area and the wattage of the clock
        public double evaluate(int[] variant) {
            return 2.0;
        }

        public void setMask(ArrayList<Resource> list) {}

        //returns a defensive copy of mask
        public int[] getMask() {
            return Arrays.copyOf(mask, mask.length);
        }
    }


