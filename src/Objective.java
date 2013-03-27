import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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

//it is assumed everywhere in the objective class that the resource copies versions are organized
//such that resource number/versions (and therefore objective value) are growing from *hypethetical* left to right
public abstract class Objective {
    private final String name; //name of the objectives
    private final int goal; //-1 - minimize, +1 - maximize, if constraint is set together with say min goal, then constraint is the upper bound, else it's a lower bound
    private double constraint;     //should be in the same units as what objective computes, it's inclusive, cannot initialize constraint to 0 for maximization objective, 0 may be a valid constraint
    private boolean optimized = false; //this flag is to be set to true for the objective which is to be optimized. The user of this class should take measures to keep only 1 such objective in a design space
    private String units;
    private final ArrayList<Resource> res_list; //list of resources
    private int[] tight_border_var = null;
    private int[] loose_border_var = null;
    private int[] max_var = null;
    private int[] min_var = null;
    private boolean arranged = false; //if false - means that for this objective the design space has not been partially arranged
    //and when it's true that means that design space is arranged and therefore the order of resources will not change any more

    protected int[] mask; //maps the resources' order for this objective to that used in the repository
    //so if mask is applied to a variant in the current objective the resources will be rearranged into the repository's order

    protected VariantRepository rep;

    //if objective is max then the acceptable variants need to be above or equal to constraint,
    //if objective is min then constraint is the maximum acceptable value of objective
    public Objective(String name, String units, String goal, ArrayList<Resource> res_list) {
        this.name = name;
        this.units = units;
        if (goal.equalsIgnoreCase("max")) {
            this.goal = 1;
            this.constraint = Integer.MIN_VALUE;
        } else if (goal.equalsIgnoreCase("min")) {
            this.goal = -1;
            this.constraint = Integer.MAX_VALUE;

        } else {
            throw (new Error("An objective must have a 'min' or 'max' goal"));
        }
        this.res_list = new ArrayList<Resource>(res_list);
        
        //for now set up mask to be corresponding to the current order of resources
        mask = new int[res_list.size()];
        for (int i = 0; i < res_list.size(); i++){
            mask[i] = i;
        }

        //can set the max and min variants

            max_var = new int[res_list.size()];
            min_var = new int[res_list.size()];

            for (int i = 0; i < min_var.length; i++) {
                min_var[i] = res_list.get(i).getMin();
                max_var[i] = res_list.get(i).getMax();
            }
    }

    public Objective(String name, String units, String goal, ArrayList<Resource> res_list, double constraint) {
        this(name, units, goal, res_list);
        this.constraint = constraint;
    }

    public Objective(String name, String units, String goal, ArrayList<Resource> res_list, VariantRepository rep) {
        this(name, units, goal, res_list);
        this.rep = rep;
    }


    public Objective(String name, String units, String goal, ArrayList<Resource> res_list, VariantRepository rep, double constraint) {
        this(name, units, goal, res_list, rep);
        this.constraint = constraint;
    }

    //call this method on the objective that is set to be optimized.
    public void thisObjectiveIsOptimized() {
        optimized = true;
    }

    //returns the unmodifiable resource list for this objective
    //this can be used to check the order of the resources once arranged
    public ArrayList<Resource> getResourceList() {
        ArrayList<Resource> tmp = new ArrayList<Resource>(Collections.unmodifiableList(res_list));
        return tmp;
    }

    public void setConstraint(double constraint) {
        this.constraint = constraint;
    }

    public String getName() {
        return name;
    }

    public int getGoal() {
        return goal;
    }

    //returns a defensive copy of mask
    public int[] getMask() {
        return Arrays.copyOf(mask, mask.length);
    }


    //the max variant that is returned corresponds to the order of resources at the point of calling
    public int[] getMaxVariant() {
        if (!arranged) {
            //make max variant
            for (int i = 0; i < max_var.length; i++) {
                max_var[i] = res_list.get(i).getMax();
            }
        }
        return Arrays.copyOf(max_var, max_var.length);
    }

    //the min variant that is returned corresponds to the order of resources at the point of calling
    public int[] getMinVariant() {
        if (!arranged) {
            //make min variant
            for (int i = 0; i < min_var.length; i++) {
                min_var[i] = res_list.get(i).getMin();
            }
        }
        return Arrays.copyOf(min_var, min_var.length);
    }

    //TODO return a safe copy?
    public int[] getTight_border_var() {
        return tight_border_var;
    }

    //returns the best variant for this objective if:
    //- objective is being optimized
    // - objective is constrained and the tight border variant has been determined
    //otherwise returns null
    public int[] getOptimalVariant() {
        if (goal > 1) {
            return getMaxVariant();
        } else return getMinVariant();
    }

    public boolean getArranged() {
        return arranged;
    }


    //The mask allows to convert a variant given in the order of resources kept in repository
    // to the order of resources kept in this objective. Repository is specified by parameter 'list'
    //When this method is called the 'list' should be requested from repository
    public final void setMask(ArrayList<Resource> list) {

        mask = new int[list.size()];

        //go through the list order as in repository (this is the "master" order)
        for (int i = 0; i < list.size(); i++) {
            //find the current resource in the list
            for (int j = 0; j < res_list.size(); j++) {
                if (res_list.get(j).equals(list.get(i))) {
                    mask[i] = j;
                    break;
                }
            }
        }
    }

    //Generates a mask that could be used to convert a variant in the current objective to
    //another objective who's mask is given
    //When this method is called the 'list' should be requested from repository
    //This method can be called at any point in time, however before
    //"arranged" tag (returned by getArranged () method) has become true this method has to be
    //called every time a mask is needed.
    //If getArranged() returns true, then the mask that this method returns can be save and reused
    //In case of potential subsequent space rearrangement, check the getArrange() value again
    public int[] makeMaskforObjective(ArrayList<Resource> list, int[] other_objective_mask) {
        int[] converting_mask = new int[res_list.size()];

        //first create the unmask from other objective mask (a mask takes a variant from an objective resource order to
        //repository resource order, the unmask takes repository order to objective order
        int[] unmask = new int[other_objective_mask.length];
        for (int i = 0; i < other_objective_mask.length; i++) {
            unmask[other_objective_mask[i]] = i;
        }
        System.out.println("Other objective mask is :" + Arrays.toString(other_objective_mask));
        System.out.println("unmask is :" + Arrays.toString(unmask));


        /*applying unmask on current mask will do the trick*/
        if (arranged == true) {
            //now apply first the current mask followed by the unmask
            for (int i = 0; i < mask.length; i++) {
                converting_mask[i] = mask[unmask[i]];
            }

        } else {   //the design space for current objective is not sorted yet, so a temporary mask can be generated

            //first create a temporary mask for the current state of resources
            int[] tmp_mask = new int[res_list.size()];
            //go through the list order as in repository (this is the "master" order)
            for (int i = 0; i < list.size(); i++) {
                //find the current resource in the list
                for (int j = 0; j < res_list.size(); j++) {
                    if (res_list.get(j).equals(list.get(i))) {
                        tmp_mask[i] = j;
                        break;
                    }
                }
            }
            System.out.println("Created a temporary mask for unsorted space :" + Arrays.toString(tmp_mask));

            //now apply first the current mask followed by the unmask
            for (int i = 0; i < tmp_mask.length; i++) {
                converting_mask[i] = tmp_mask[unmask[i]];
            }
        }

        return converting_mask;
    }

    //converts a variant from current objective "view" to that of the other objective (given that objective's mask)
    public int[] convertToAnotherObjective(int[] variant, int[] other_objective_mask) {
        System.out.println("-->Trying to mask variant :" + Arrays.toString(variant));
        System.out.println("-->with mask :" + Arrays.toString(other_objective_mask));
        int[] masked_variant = new int[variant.length];
        for (int i = 0; i < variant.length; i++) {
            masked_variant[i] = variant[other_objective_mask[i]];

        }
        System.out.println("-->Finishing masking, got: " + Arrays.toString(masked_variant));
        return masked_variant;
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

            //update the mask again, since resources were swaped in sorted order
            setMask(rep.getResourceList());

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

        //now we're done with design space arrangement, so we can set the flag
        arranged = true;

    }

    //divide each level and follow the max brancht rying to land max/min onto constraint
    //returns true if border variant exists and false otherwise (i.e. if constrains are such that there are no variants that satisfy)
    public boolean findTightBorderVar() {


        if (constraint == Integer.MAX_VALUE)
            tight_border_var = max_var; //it doesn't make sense to search for border variant when objective to max/min
        if (constraint == Integer.MIN_VALUE)
            tight_border_var = min_var;

        //check if constraint is viable
        int result = check_constraint();

        if (result < 0) return false; //constraints are unsatisfiable
        else if (result == 0 || result == 2)
            return true; //either the whole tree is viable or exactly one max or min variant is viable, either way check_constraint would have set the border correctly
        else {

            double value;
            int max, min, border; //indecies
            Resource res;
            tight_border_var = new int[res_list.size()];
            int[] last_acceptable_var;

            if (goal > 0) { // MAXIMIZE
                tight_border_var = Arrays.copyOf(min_var, min_var.length);
                tight_border_var[0] = -1; //just to take care of the case when first level has two branches, therefore resulting in rounding down branch being executed and it shouldn't be on the first level. This is only necessary for this specific case and only has to do with rounding (which has to be corrected with rounding down when appropriate)
            } else { //MINIMIZE
                tight_border_var = Arrays.copyOf(max_var, max_var.length);
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

                        if (goal > 0) {
                            //Save this point as last acceptable variant
                            last_acceptable_var = Arrays.copyOf(tight_border_var, tight_border_var.length);
                        }
                        max = tight_border_var[r];
                        System.out.println("Now max is " + max + " and min is " + min);
                        System.out.println("Border is now " + border + " and variant is " + Arrays.toString(tight_border_var));

                    } else if (value < constraint) {
                        System.out.println(value + " < " + constraint);
                        if (goal < 0) {
                            last_acceptable_var = Arrays.copyOf(tight_border_var, tight_border_var.length);
                            ;
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
                            if (goal > 0) {
                                max = tight_border_var[r];
                            } else
                                min = tight_border_var[r];

                        } else //we're looking at the last resource, so there is no need to adjust max or min, or roll back to the value saved in border
                            //TODO can try searching for a tighter variant with the same value that uses lesser # of resources
                            //TODO that would be if goal is max check next (
                            return true;  //since I'm returning don't have to worry about adjusting the variant

                    }

                    if (max - min == 1 && ((goal > 0 && border == max) || (goal < 0 && border == min))) {
                        max = min;
                    }

                    System.out.println("--------------------------");
                    rep.printRepository();
                    System.out.println("--------------------------");


                } while (max != min);


            }
            System.out.println("So at the end, the border variant is : " + evaluate(last_acceptable_var));
            tight_border_var = last_acceptable_var;


            return true;
        }

    }

    //Method fo semantic filtering
    //check if the given parameter is to the right of the border if objective is constraint from below
    //and to the left if objective constained from above
    //the order of resrouces reference by variant is assumed to be in the order of this objective 
    //precondition: objective space of current objective must be sorted and border_variant computed 
    public boolean checkIfSatisfies(int[] variant) {
        if (goal > 0) { //maximize
            for (int i = 0; i < variant.length; i++) {
                if (variant[i] > tight_border_var[i]) return true;
                else if (variant[i] < tight_border_var[i]) return false;
                //else keep checking
            }
        } else {
            for (int i = 0; i < variant.length; i++) {
                if (variant[i] < tight_border_var[i]) return true;
                else if (variant[i] > tight_border_var[i]) return false;
                //else keep checking
            }
        }
        return true;
    }

    //objectiveList must contain objectives that are connected with the same repository and using the same set of
    //resources
    //itoOptimize objective can be part objectiveList or not ???
    //Returns: null if no variant that satisfies all objectives' constraints exits or an optimal variant
    //PRecondition: objectives must all be sorted and have their border variants determined by the time of call to this method
    //              objective list must contain at least one objective
    public static int[] optimize(Objective[] objectiveList, Objective toOptimize, VariantRepository repository) {

        int[] opt_variant;

        //verify that tight border variants for all objectives are not excluding of each other
        for (int s = 0; s < objectiveList.length; s++) {
            for (int t = s; t < objectiveList.length; t++) {

            }

        }

        //get the optimal variant for the objective being optimized and start filtering it
        opt_variant = toOptimize.getOptimalVariant(); //initialize to the best variant
        System.out.println(Arrays.toString(opt_variant));


        int[] masked_var;
        boolean pass = false;

        //semantic filtering, will break out of the loop soon as current opt_variant is within
        //limits of border variants for all objectives, else will keep moving opt_variant and trying

        System.out.println("going into while");

        while (!(toOptimize.getGoal() == -1 && Arrays.equals(opt_variant, toOptimize.getMaxVariant()))) { //|| (toOptimize.getGoal() == +1 && Arrays.equals(opt_variant, toOptimize.getMinVariant())))) {

            //System.out.println("in while");

            pass = true;

            for (int i = 0; i < objectiveList.length; i++) {   //go through all constrained objectives
                if (objectiveList[i].equals(toOptimize)) continue;

                //System.out.println("going into for");

                //take the current optimal variant and try to fit into the allowable area of every objective
                //allowable means that it is below or above border variant for that objective as applicable

                //mask the optimal variant into the current objective
                masked_var = toOptimize.convertToAnotherObjective(opt_variant, toOptimize.makeMaskforObjective(repository.getResourceList(), (objectiveList[i]).getMask()));
                System.out.println ("This variant is "+Arrays.toString(opt_variant) + " value: " + objectiveList[i].evaluate(masked_var));

                //now see if this variant fits semantically into the area bordered by tight_border variant of the objective i
                if (!objectiveList[i].checkIfSatisfies(masked_var)) {
                    pass = false;
                    break;
                }


            }
            if (pass) break; //so all constraints are satisfied get out of while
            //move to the next variant and start checking objectives all over

            opt_variant = toOptimize.getNextVariant(opt_variant, toOptimize.getGoal() * (-1));
            System.out.println("Got next variant : " + Arrays.toString(opt_variant));


        }

        //so if pass is false that means that while loop above has exhausted all variants of the to be optimized objective
        if (pass)
            return opt_variant;
        else return null;

    }

    //returns the next variant to the left or to the right of the specified parameter variant
    //        or null if current variant is either maximum and going right or min and going left
    //it is assumed everywhere in the objective class that the resource copies versions are organized
    //such that resource number/versions (and therefore objective value) are growing from *hypothetical* left to right
    //left_right - negative means left, and positive means right
    //Precondition: the order of resources in this_variant parameter is assumed to match the current order of
    //resources in objective. No additional checks are therefore done.
    public int[] getNextVariant(int[] this_variant, int left_right) {
        int[] next_variant = Arrays.copyOf(this_variant, this_variant.length);
        int i;
        if (left_right < 0) { //move to the left in the tree
            if (Arrays.equals(this_variant, min_var)) {
                //System.out.println("equal min var");
                return null; //already at the very left of the tree, nowhere to go
            }
            for (i = this_variant.length - 1; i >= 0; i--) {
                if (next_variant[i] != res_list.get(i).getMin()) {
                    next_variant[i] = next_variant[i] - 1;
                    System.out.println("here");
                    break;
                }
                //System.out.println("set max");
                next_variant[i] = res_list.get(i).getMax();
                //the resource on current level already has the smallest possible value, so set to max and go to the level up
            }

        } else {    //move to the right in the tree
            if (Arrays.equals(this_variant, max_var)) {
                System.out.println("equal max var");
                return null;
            }
            for (i = this_variant.length - 1; i >= 0; i--) {
                if (next_variant[i] != res_list.get(i).getMax()) {
                    next_variant[i] = next_variant[i] + 1;
                    break;
                }
                next_variant[i] = res_list.get(i).getMin();
            }
        }

        return next_variant;
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


    /*
    writes a .tgf file specified by path, or if path is null, to default location
    the file represents the entire objective tree with actual values for all variants
    that have been computed so far or just number of the variant for those that were not computed
     */
    public void toYGraph(String path) {
        try {
            BufferedWriter out;
            if (path != null)
                out = new BufferedWriter(new FileWriter(path + ".tgf"));
            else
                out = new BufferedWriter(new FileWriter(name + ".tgf"));

            //write resources
            
            int prev_num = 1;
            int num_nodes_pres_level = 1;
            
            //write first resource
            out.write("0 " + res_list.get(0).getName() + "\n");
            int[] variant;
            int[] result;
            for (int i = 0; i < res_list.size(); i++) {
                for (int j = 0; j < num_nodes_pres_level * res_list.get(i).getMaxNumBranches(); j++) {

                    if (i < res_list.size() - 1) {

                        out.write((prev_num + j) + " " + res_list.get(i + 1).getName() + "\n");
                    } else {
                        variant = variantNumberToSignature(j+1);
                        result = rep.findVariant(variant,mask);


                        if (result[0] == 0)
                            //out.write((prev_num + j) + " " + (j+1) + "\n");
                            out.write((prev_num + j) + " .\n");
                        else {//so it has been evaluated before for *some objective*)
                            //now check if it was evaluated for this objective
                            double value = rep.getVariantValue(result[1], name);
                            if (!Double.isNaN(value) )
                            out.write((prev_num + j) + " " +rep.getVariantValue(result[1], name) + "\n");
                            else
                                out.write((prev_num + j) + " .\n");
                        }

                    }
                }
                num_nodes_pres_level = num_nodes_pres_level * res_list.get(i).getMaxNumBranches();
                prev_num += num_nodes_pres_level;
            }
            out.write("#\n");

            for (int i = 1; i <= res_list.get(0).getMaxNumBranches(); i++) {
                out.write("0 " + i + "\n");
            }

            num_nodes_pres_level = 1;
            prev_num = 1;
            for (int i = 0; i < res_list.size()-1; i++) {
                int branches = num_nodes_pres_level * res_list.get(i).getMaxNumBranches();
                for (int j = 0; j < num_nodes_pres_level * res_list.get(i).getMaxNumBranches(); j++) {

                    for (int k = 0; k < res_list.get(i + 1).getMaxNumBranches(); k++) {
                        out.write((prev_num + j) + " " + (prev_num + j + branches + j * (res_list.get(i + 1).getMaxNumBranches() - 1) + k) + "\n");
                    }

                }
                num_nodes_pres_level = num_nodes_pres_level * res_list.get(i).getMaxNumBranches();
                prev_num += num_nodes_pres_level;
            }


            out.close();
        } catch (IOException e) {
        }

    }


    //left_right is -: left or + to the right
    private void increase_border_variant(int left_right) {

        if (left_right > 0) {
            System.out.println("Increasing");
            for (int i = res_list.size() - 1; i >= 0; i--) {
                if (tight_border_var[i] == res_list.get(i).getMax()) { //cannot increase
                    //so set this one to smallest and increase next level
                    tight_border_var[i] = res_list.get(i).getMin();
                } else {
                    //increase and finish
                    tight_border_var[i] += 1;
                    break;
                }
            }
        } else {
            System.out.println("Decreasing");
            System.out.println(Arrays.toString(tight_border_var));
            for (int i = res_list.size() - 1; i >= 0; i--) {
                if (tight_border_var[i] == res_list.get(i).getMin()) { //cannot decrease
                    System.out.println(tight_border_var[i]);
                    //so set this one to largest and decrease next level
                    tight_border_var[i] = res_list.get(i).getMax();
                } else {
                    //increase and finish
                    tight_border_var[i] -= 1;
                    System.out.println("here");
                    break;
                }
            }

        }

    }

    //If the variants would be numbered from left to right (strating from 1) in a tree then each such number
    //of a variant can be converted back to variant signature
    //precondition 1: although the num can be long, none of the resources have more than
    //size of int versions/copies - everything is cast into int when storing into variant signature
    //precondition 2: the parameter num has to be less than total number of variants in current objective space
    //there are no checks done in the method to verify this.
        public int[] variantNumberToSignature (long num){
                int[] variant = new int[res_list.size()];
        long remainder;
        int zero_branch = 0; //to account for cases when there is "zeroth" branch, i.e. resource can be ommited
        for (int i = res_list.size()-1; i >0; i--){
            remainder = num % res_list.get(i).getMaxNumBranches();
            if (remainder == 0){
                variant[i] = res_list.get(i).getMaxNumBranches();
                num =  (num /res_list.get(i).getMaxNumBranches());
            }
            else {
                variant[i] = (int)remainder;
                num =  (num /res_list.get(i).getMaxNumBranches()) + 1;
            }
            //to account for cases when there is "zeroth" branch, i.e. resource can be ommited
            if (res_list.get(i).getNotIncludedValue())
                variant[i] -= 1;


        }
        //set root
        variant[0] = (int)num;
            

         return variant;
    }
    
    
    //0 means that there is there is only 1 variant satisfying constraint so no point in engaging in further search
    //1 means that part of the tree (but more than one variant) satisfies the constraint and border variant has to be searched for
    //2 means that the whole tree satisfies the constraint, also no point in searching
    //-1 means that the whole tree doesn't satisfy the constraint
    //the method sets the tight border variant (if applicable)
    private int check_constraint() {
        System.out.println("Checking constraint for viability");

        double min_value, max_value;
        max_value = evaluate(max_var);
        min_value = evaluate(min_var);


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

            if (min_value > constraint) {
                return -1;
            } else if (min_value < constraint) {
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