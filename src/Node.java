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
/*
public abstract class Objective {
    private final String name; //name of the objectives
    private final int goal; //-1 - minimize, +1 - maximize, if constraint is set together with say min goal, then constraint is the upper bound, else it's a lower bound
    private double constraint = 0;     //should be in the same units as what objective computes, it's inclusive
    private String units;
    private ArrayList<Resource> res_list; //list of resources
    private int[] tight_border_var = null;
    private int[] loose_border_var = null;

    //if objective is max then the acceptable variants need to be above or equal to constraint,
    //if objective is min then constraint is the maximum acceptable value of objective

    public Objective(String name, String units, String goal) {
        res_list = new ArrayList<Resource>();
        this.name = name;
        this.units = units;
        if (goal.equalsIgnoreCase("max"))
            this.goal = 1;
        else if (goal.equalsIgnoreCase("min"))
            this.goal = -1;
        else {
            throw (new Error("An objective must have a 'min' or 'max' goal"));
        }


    }

    public Objective(String name, String units, String goal, double constraint) {
        this(name, units, goal);
        this.constraint = constraint;
    }


    public ArrayList<Resource> getResourceList() {
        return res_list;
    }

    public String getName() {
        return name;
    }

    public void addResource(Resource r) {
        res_list.add(r);
    }

    public int[] getTight_border_var() {
        return tight_border_var;
    }

    //sorts the list of resources based on K value
    public void sortResources() {

        double[] kValues = new double[res_list.size()]; //will hold k values for each of resource in the current order

        int[] extrem_variant = new int[res_list.size()];
        int[] critical_variant = new int[res_list.size()];
        ///////extrem_variant[0] = (res_list.get(0)).getMax();

        for (int r = 0; r < res_list.size(); r++) {  //look at each resource to compute k-value


            //place the current resource at the beginning of the list
            Collections.swap(res_list, 0, r);

            //don't forget to update k-values
            double tmp = kValues[0];
            kValues[0] = kValues[r];
            kValues[r] = tmp;
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
        //at this point the resource list is ordered such that most influencial resource is on the top
    }

    //divide each level and follow the max branch
    public boolean findTightBorderVar() {

        //so I'm sort of trying to land max/min onto constraint
        assert (constraint != 0); //it doesn't make sense to search for border variant when objective to max/min


        double value;
        int max, min, border; //indecies
        Resource res;

        //check if constraint is viable
        int result = check_constraint();
        //System.exit(result);
        if (result < 0) return false;
        else if (result == 0 || result == 2) return true;
        else {

            tight_border_var = new int[res_list.size()];

            if (goal > 0) { // MAXIMIZE
                tight_border_var[0] = res_list.get(0).getMax();
                //construct the rest of the variant
                for (int i = 1; i < res_list.size(); i++) {
                    tight_border_var[i] = res_list.get(i).getMin();
                }
            } else { //MINIMIZE
                tight_border_var[0] = res_list.get(0).getMin();
                for (int i = 1; i < res_list.size(); i++) {
                    tight_border_var[i] = res_list.get(i).getMax();
                }
            }


            for (int r = 0; r < res_list.size(); r++) {           //go through all levels of the tree of resources
                System.out.println ("Level: " + (r+1));

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
                    System.out.println ("Just computed regular tight_border it is: " +  tight_border_var[r]);

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


                    //evaluate
                    if (value > constraint) {
                        System.out.println("here");

                        if (goal > 0) {       //maximize
                            //but before we search the reverted to bush lets check if it's worth it
                            //check the max variant on the bush that contains border to see if it is greater than constraints
                            //because if it isn't we're done and there is no point in searching this bush for tighter match

                            System.out.println("Checking if it is worth to continue searching");
                            int[] tmp_variant = Arrays.copyOf(tight_border_var, tight_border_var.length);
                            tmp_variant[r] = border;
                            for (int k = r + 1; k < tmp_variant.length; k++) {
                                tmp_variant[k] = res_list.get(k).getMax();

                            }

                            double tmp_value = evaluate(tmp_variant);
                            if (tmp_value < constraint) {
                                System.out.println("no");
                                break;
                            } else {
                                tight_border_var[r] = border; //the other branch that was just tried gives smallest variant being larger than constraint, so we should revert to what has been tried previously
                                max = tight_border_var[r]; //prune the bush who's smallest branch is larger than constraint

                            }
                            System.out.println("Set");

                        } else { //minimizing

                            //can prune everything to the right and keep tighting
                            max = tight_border_var[r];
                            System.out.println("minimize and my max is : " + max);
                        }

                    } else if (value < constraint) {
                        System.out.println("there");
                        min = tight_border_var[r];
                        System.out.println ("Just reset min to " + min);
                        if (goal > 0) {
                            min = tight_border_var[r];
                            System.out.println("now min is " + min);
                        } else {


                            //check the next that bounds bushes from below/left if it satisfies the constraint
                            //if it doesn't then we can stop and this is the tightest bound
                            System.out.println("Checking if it is worth to continue searching (doing min)");
                            int[] tmp_variant = Arrays.copyOf(tight_border_var, tight_border_var.length);
                            //tmp_variant[r] = border;
                            if (tmp_variant[r]+1 <= max)
                                tmp_variant[r] +=1;//border;
                            for (int k = r + 1; k < tmp_variant.length; k++) {
                                tmp_variant[k] = res_list.get(k).getMin();
                            }
                            double tmp_value = evaluate(tmp_variant);
                            if (tmp_value > constraint) {
                                System.out.println("no");
                                break;
                            } else {
                                System.out.println("yes");
                                tight_border_var[r] = border; //the other branch that was just tried gives smallest variant being larger than constraint, so we should revert to what has been tried previously
                                ////min = tight_border_var[r]; //prune the bush who's smallest branch is larger than constraint

                            }


                        }
                    } else {
                        System.out.println("Right on constraint value");
                        if (r != res_list.size() - 1) {
                            if (goal > 0) { //maximize
                                System.out.println("Checking if it is worth to continue searching");
                                int[] tmp_variant = Arrays.copyOf(tight_border_var, tight_border_var.length);
                                tmp_variant[r] = border;
                                for (int k = r + 1; k < tmp_variant.length; k++) {
                                    tmp_variant[k] = res_list.get(k).getMax();
                                }

                                double tmp_value = evaluate(tmp_variant);
                                if (tmp_value < constraint) {
                                    System.out.println("no");
                                    break;
                                } else {
                                    tight_border_var[r] = border; //the other branch that was just tried gives smallest variant being larger than constraint, so we should revert to what has been tried previously
                                    max = tight_border_var[r]; //prune the bush who's smallest branch is larger than constraint

                                }


                                tight_border_var[r] = border;
                                System.out.println("border is " + border);
                            } else {
                                min = tight_border_var[r];
                            }
                        } else //we're looking at the last resource, so there is no need to adjust max or min, or roll back to the value saved in border
                            //TODO can try searching for a tighter variant with the same value that uses lesser # of resources
                            //TODO that would be if goal is max check next (
                            //break;
                            //I will actually return, not just break here in order not to have the adjustemt by one happen
                            //to this variant.
                            return true;

                    }


                } while (max != min);// ((int) Math.ceil(min + (max - min) / 2.0) != tight_border_var[r]);


            }
            //adjustment
            //if (goal > 0) //maximize
            //  tight_border_var [tight_border_var.length-1] = tight_border_var [tight_border_var.length-1];

            System.out.println("So at the end, the border variant is : " + evaluate(tight_border_var));
            return true;
        }

    }


    public abstract double evaluate(int[] variant);

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
*/