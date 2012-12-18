import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: irina
 * Date: 16/11/12
 * Time: 1:44 PM
 * To change this template use File | Settings | File Templates.
 */

//TODO replace function calls such as res_list.size() by variable reference ??

    //constraint and goal can change, however, the changing list of resources will result in the need to recompute everything
    //for example, changing goal from max to min will merely result in switching around of tight and loose border variants
    //changing constraint also just refines the search i.e. we already know something about the tree, no need to do as much work
public abstract class Objective {
    private final String name; //name of the objectives
    private final int goal; //-1 - minimize, +1 - maximize, if constraint is set together with say min goal, then constraint is the upper bound, else it's a lower bound
    private double constraint;     //should be in the same units as what objective computes, it's inclusive, cannot initialize constraint to 0 for maximization objective, 0 may be a valid constraint
    private String units;
    private final ArrayList<Resource> res_list; //list of resources
    private int[] tight_border_var = null;
    private int[] loose_border_var = null;
    private int[] max_var;
    private int[] min_var;
    
    protected VariantRepository rep;

    //if objective is max then the acceptable variants need to be above or equal to constraint,
    //if objective is min then constraint is the maximum acceptable value of objective

    public Objective(String name, String units, String goal, ArrayList<Resource> res_list) {
        this.name = name;
        this.units = units;
        if (goal.equalsIgnoreCase("max")) {
            this.goal = 1;
            this.constraint = Integer.MIN_VALUE;
        }
        else if (goal.equalsIgnoreCase("min")){
            this.goal = -1;

        }        else {
            throw (new Error("An objective must have a 'min' or 'max' goal"));
        }
        this.res_list = new ArrayList<Resource>(res_list);

    }

    public Objective(String name, String units, String goal, ArrayList<Resource> res_list, double constraint ) {
       this (name,units, goal, res_list);
        this.constraint = constraint;
    }

    public Objective(String name, String units, String goal, ArrayList<Resource> res_list, VariantRepository rep) {
        this (name,units, goal, res_list);
        this.rep = rep;
    }


    public Objective(String name, String units, String goal, ArrayList<Resource> res_list,  VariantRepository rep, double constraint) {
        this(name, units, goal, res_list, rep);
        this.constraint = constraint;
    }

    //TODO return a safe copy?
    public ArrayList<Resource> getResourceList() {
        return res_list;
    }

    public void setConstraint (double constraint){
        this.constraint = constraint;
    }
    
    public String getName() {
        return name;
    }

    //TODO return a safe copy?
    public int[] getTight_border_var() {
        return tight_border_var;
    }

    //sorts the list of resources based on K value
    public void sortResources() {

        double[] kValues = new double[res_list.size()]; //will hold k values for each of resource in the current order

        int[] extrem_variant = new int[res_list.size()];
        int[] critical_variant = new int[res_list.size()];

        for (int r = 0; r < res_list.size(); r++) {  //look at each resource to compute k-value


            //place the current resource at the beginning of the list
            Collections.swap(res_list, 0, r);

            //don't forget to update k-values
            double tmp = kValues[0];
            kValues[0] = kValues[r];
            kValues[r] = tmp;

            //since resource order was swapped, need to update the mask
            setMask(rep.getResourceList());

            System.out.println("---------");
            System.out.println("Now the resources are in this order (computing k value):");
            for (int m = 0; m < res_list.size(); m++) {
                System.out.println(res_list.get(m).getName());
            }
            System.out.println("And kValues are: " + Arrays.toString(kValues));
            System.out.println("---------");

            //construct 2 variants needed to compute the k value
            System.out.println("Min value for resource " + res_list.get(0).getName() + (res_list.get(0)).getMin());
            System.out.println("Max value for resource " + res_list.get(0).getName() + (res_list.get(0)).getMax());

            critical_variant[0] = (res_list.get(0)).getMin();
            extrem_variant[0] = (res_list.get(0)).getMax();

            //go through the rest of the resources and choose maximum copies/versions to complete the variants' construction
            for (int i = 1; i < res_list.size(); i++) {

                critical_variant[i] = res_list.get(i).getMax();
                extrem_variant[i] = critical_variant[i];

            }
            System.out.println("Critical variant: " + Arrays.toString(critical_variant));
            System.out.println("Extrem variant: " + Arrays.toString(extrem_variant));
            kValues[0] = (evaluate(extrem_variant) - evaluate(critical_variant)) / ((res_list.get(0)).getNum());
            System.out.println("And k value is " + kValues[0]);

        }
        System.out.println("Starting sorting resources according to k-values, k values are:");
        System.out.println(Arrays.toString(kValues));
        System.out.println("Corresponding order of resourse is: ");
        for (int m = 0; m < res_list.size(); m++) {
            System.out.println(res_list.get(m).getName());
        }
        //at this point the kValues contain the k-values corresponding to the order of resource.
        //The resources need to be sorted
        int largest, j;
        assert (kValues.length == res_list.size());
        System.out.println(kValues.length);
        double tmp;
        for (int i = 0; i < kValues.length; i++) {
            largest = i;
            for (j = i + 1; j < kValues.length; j++) {
                if (kValues[j] > kValues[largest]) {
                    largest = j;
                }

            }
            //swap k-values and resources
            tmp = kValues[i];
            kValues[i] = kValues[largest];
            kValues[largest] = tmp;
            Collections.swap(res_list, i, largest);
            System.out.println("Now resources are in this order: ");
            for (int m = 0; m < res_list.size(); m++) {
                System.out.println(res_list.get(m).getName());
            }
            System.out.println("And k values are: ");
            System.out.println(Arrays.toString(kValues));

        }
        //now that the resources are in the final appropriate order update the mask again
        setMask(rep.getResourceList());


        //at this point the resource list is ordered such that most influencial resource is on the top

        //can also fill in max and min variant composition - just for easiness
        //construct the rest of the variant
        min_var = new int[res_list.size()];
        max_var = new int[res_list.size()];
        for (int i = 0; i < res_list.size(); i++) {
            min_var[i] = res_list.get(i).getMin();
        }

        for (int i = 0; i < res_list.size(); i++) {
            max_var[i] = res_list.get(i).getMax();
        }
    }

    //divide each level and follow the max branchtrying to land max/min onto constraint
    public boolean findTightBorderVar() {


        if (constraint == Integer.MAX_VALUE)
            tight_border_var = max_var; //it doesn't make sense to search for border variant when objective to max/min
        if (constraint == Integer.MIN_VALUE)
            tight_border_var= min_var;

        //check if constraint is viable
        int result = check_constraint();
        //System.exit(result);
        if (result < 0) return false;
        else if (result == 0 || result == 2) return true;
        else {

            double value;
            int max, min, border; //indecies
            Resource res;
            tight_border_var = new int[res_list.size()];
            int [] last_acceptable_var;

            if (goal > 0) { // MAXIMIZE
                    tight_border_var = Arrays.copyOf(min_var, min_var.length);
                    tight_border_var[0] = -1; //just to take care of the case when first level has two branches, therefore resulting in rounding down branch being executed and it shouldn't be on the first level. This is only necessary for this specific case and only has to do with rounding (which has to be corrected with rounding down when appropriate)
            } else { //MINIMIZE
                    tight_border_var = Arrays.copyOf(max_var,max_var.length);
                    tight_border_var[0] = -1;
            }

            last_acceptable_var = Arrays.copyOf(tight_border_var, tight_border_var.length);

            for (int r = 0; r < res_list.size(); r++) {           //go through all levels of the tree of resources
                System.out.println("Level: " + (r + 1));

                //divide the current level
                res = res_list.get(r);
                min = res.getMin();
                max = res.getMax();

                //carry out binary division of current level
                do {
                    //border is always one step behind, and into tight_border_variant[r] is saved and tried
                    //an index of resource

                    border = tight_border_var[r];
                    tight_border_var[r] = (int) Math.ceil(min + (max - min) / 2.0);
                    System.out.println("Just computed regular tight_border it is: " + tight_border_var[r]);

                    if (tight_border_var[r] == border) { //account for always rounding up when computing the middle above
                        tight_border_var[r] -= 1;
                        System.out.println("ROUNDING DOWN");
                    }
     
                    System.out.println("Calculation: " + tight_border_var[r] + " and border is right now " + border);
                    System.out.println("Looking at resource " + res.getName());
                    System.out.println(max);
                    System.out.println(min);
                    System.out.println("evaluating " + tight_border_var[r]);

                    value = evaluate(tight_border_var);
                  
                    if (value > constraint) {
                        System.out.println(value + " > " + constraint);

                        if (goal > 0){
                            //Save this point as last acceptable variant
                            last_acceptable_var = Arrays.copyOf (tight_border_var, tight_border_var.length);
                        }
                        max = tight_border_var[r];
                        System.out.println("Now max is " + max + " and min is " + min);
                        System.out.println("Border is now " + border + " and variant is " + Arrays.toString(tight_border_var));

                    } else if (value < constraint) {
                        System.out.println(value + " < " + constraint);
                        if (goal  < 0 ){
                            last_acceptable_var = Arrays.copyOf (tight_border_var, tight_border_var.length);;
                        }

                        min = tight_border_var[r];
                        System.out.println("Just reset min to " + min);
                        System.out.println("Now max is " + max + " and min is " + min);
                        System.out.println("Border is now " + border + " and variant is " + Arrays.toString(tight_border_var));

                    } else {   //being right on value of constraint is special in a sense that it should be
                        //included whether objective is being maximized or minimized.
                        System.out.println("Right on constraint value");
                        if (r != res_list.size() - 1) {
                            
                            last_acceptable_var = Arrays.copyOf(tight_border_var, tight_border_var.length);
                            if (goal > 0){
                                max = tight_border_var[r];
                            }
                            else
                                min = tight_border_var[r];
                            
                        } else //we're looking at the last resource, so there is no need to adjust max or min, or roll back to the value saved in border
                            //TODO can try searching for a tighter variant with the same value that uses lesser # of resources
                            //TODO that would be if goal is max check next (
                            return true;  //since I'm returning don't have to worry about adjusting the variant

                    }

                if (max - min == 1 && ((goal > 0 && border == max) || (goal < 0 && border == min))){                
                    max = min;
                }
                } while (max != min);


            }
            System.out.println("So at the end, the border variant is : " + evaluate(last_acceptable_var));
            tight_border_var = last_acceptable_var;
            return true;
        }

    }


    public abstract double evaluate(int[] variant);


    public abstract void setMask (ArrayList<Resource> list);

    public String toString() {

        String output = "Objective name: " + name + "\n";
        if (goal > 0) {
            output += "is being maximized.\n";
        } else {
            output += "is being minimized\n";
        }
        if (constraint != 0) {
            output += "constrained by " + constraint + "\n";
        }
        output += "Present order of resources is: \n";
        for (int i = 0; i < res_list.size(); i++) {
            output += res_list.get(i).getName() + "\n";
        }
        return output;
    }

    //left_right is -: left or + to the right
    private void increase_border_variant (int left_right){

        if (left_right > 0){
            System.out.println("Increasing");
            for (int i = res_list.size()-1; i >=0; i--){
               if ( tight_border_var[i] == res_list.get(i).getMax()){ //cannot increase
                    //so set this one to smallest and increase next level
                   tight_border_var[i] = res_list.get(i).getMin();
               }
               else {
                   //increase and finish
                   tight_border_var[i] +=1;
                   break;
               }
            }
        }
        else {
            System.out.println("Decreasing");
            System.out.println(Arrays.toString(tight_border_var));
            for (int i = res_list.size()-1; i >=0; i--){
                if ( tight_border_var[i] == res_list.get(i).getMin()){ //cannot decrease
                    System.out.println(tight_border_var[i]);
                    //so set this one to largest and decrease next level
                    tight_border_var[i] = res_list.get(i).getMax();
                }
                else {
                    //increase and finish
                    tight_border_var[i] -=1;
                    System.out.println("here");
                    break;
                }
            }

            }

    }


    //0 means that there is there is only 1 variant satisfying constraint so no point in engaging in further search
    //2 means that the whole tree satifies the constraint, also no point in searching
    private int check_constraint() {
        System.out.println("Checking constraint for viability");

        int[] max_var = new int[res_list.size()];
        int[] min_var = new int[res_list.size()];

        //make max variant
        for (int i = 0; i < max_var.length; i++) {
            max_var[i] = res_list.get(i).getMax();
        }

        //make min variant
        for (int i = 0; i < min_var.length; i++) {
            min_var[i] = res_list.get(i).getMin();
        }

        double min_value, max_value;
        max_value = evaluate(max_var);
        min_value = evaluate(min_var);
        System.out.println(max_value);
        if (goal > 0) { //maximize

            if (max_value < constraint) return -1;

            else if (max_value > constraint) {
                //check where the minimum is too
                if (min_value > constraint) { //the whole tree satisfies constraint and border is right on the minimum
                    tight_border_var = min_var;
                    return 2;

                }
                return 1;
            } else {  //the maximum variant IS the border
                //save this variant
                tight_border_var = max_var;
                return 0;
            }


        } else {


            System.out.println(min_value);
            if (min_value > constraint) return -1;
            else if (min_value < constraint) {
                if (max_value < constraint) { //the whole tree satisfies constraint and border is right on the minimum
                    tight_border_var = max_var;
                    return 2;
                }
                return 1;
            } else {
                //save this variant
                tight_border_var = min_var;
                return 0;
            }
        }


    }
}
