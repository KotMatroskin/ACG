import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: irina
 * Date: 14/12/12
 * Time: 2:16 PM
 * To change this template use File | Settings | File Templates.
 */


//The repository to be used by all relevant objectives should be created and then given to each of the objectives
//so that they all can use it
public class VariantRepository {

    //TODO change known_variants and values to ArrayList? ArrayList<int[]> bla;
    //an array of pointers to arrays representing variants
    private int[][] known_variants;
    private double[][] known_variants_values;
    private final String[] objectives; //the list of objectives, the order of objectives corresponds to the order of values in know_variants_values
    private int last = 0;
    private final ArrayList<Resource> res_list; //list of resources
    private int growth_value = 10; //the number of new spaces to add to repository when all existing ones are filled;


    //it is more efficient to give the resource list in the order arranged for at least one objective

    public VariantRepository(ArrayList<Resource> res_list, String[] objectvies) {
        this.res_list = new ArrayList<Resource>(res_list);
        this.objectives = Arrays.copyOf(objectvies, objectvies.length);
        //initialize known_variants to be log n in size + half of that
        //calculate the total number of permutations
        int num_variants = 1;
        int size = 0;
        for (int i = 0; i < res_list.size(); i++) {
            size += Math.round(Math.log(res_list.get(i).getMaxNumBranches()) / Math.log(2));
        }

        size += res_list.size() + 2;  //2 accounts for max/min)  and adding res_list size accounts for variants computed during k-values calculation

        known_variants = new int[size][res_list.size()];
        known_variants_values = new double[size][objectvies.length];
        //initialize the values array with NaN - that way later we can tell which values for which objectives were computed
        //and which not
        for (int i = 0; i < size; i++)
            Arrays.fill(known_variants_values[i], Double.NaN);

    }

    public void setGrowthValue(int growthValue) {
        growth_value = growthValue;
    }

    //returns the unmodifiable list of resources in the order that is used to reference variants in repository
    //this is used to compute masks to convert between variants represented in the arranged orders for particular objectives
    public ArrayList<Resource> getResourceList() {
        ArrayList<Resource> tmp = new ArrayList<Resource>(Collections.unmodifiableList(res_list));
        return tmp;
    }

    //TODO implement binary search instead
    //assuming that the known_variants list is always kept sorted

    //mask array contains indecies that direct how to rearrange variant to match the order of resources in repository
    //for example, if the mask is [3,2,1...] and in the repository the resources are in order [A,B,C, ...] and the variant is
    //[0,8,9...] then after the application of the mask the variant will be converted to [9,8,0,...]

    //returns an array of size 2. The first element of that array is either 0 or 1. 0 meaning that element was not found
    //and 1 meaning that it was found. The second element contains the position at which the element was found or at the
    //position where it should be inserted to maintain the ascending order in the case that element was not found
    public int[] findVariant(int[] variant, int[] mask) {

        int[] masked_variant = apply_mask(variant, mask);
        int[] result = {0, 0};

        if (last > 0) {
            int i;
            for (i = 0; i < known_variants.length; i++) {
                if (compare_variants(masked_variant, known_variants[i]) < 0) { //reached the area of variant larger than current one
                    result[1] = i;
                    System.out.println("I was looking for variant " + Arrays.toString(variant) + "found and result is " + Arrays.toString(result));
                    return result;
                }
                if (compare_variants(masked_variant, known_variants[i]) == 0) {   //found variant and check that the value for given objective was computed
                    result[0] = 1;
                    result[1] = i;
                    System.out.println("I was looking for variant " + Arrays.toString(variant) + "found and result is " + Arrays.toString(result));
                    return result;
                }

            }
            //clearly, the variant is larger than all so far inserted in database
            result[1] = i;
            return result;

        }
        //System.out.println("I was looking for variant " + Arrays.toString(variant) + "found and result is " + Arrays.toString(result));
        //repository is empty (last is pointing to 0) so return result array with initial values
        return result;

    }

    //This method is used to check if a variant at position 'pos' has had
    //the value for objective given by 'objective' parameter computed before
    public boolean checkObjectiveValue(int pos, String objective) {
        int obj_pos = findObjective(objective);
        if (obj_pos < 0)
            throw new IllegalArgumentException(objective + " is not a known objective. Known objectives are: " + Arrays.toString(objectives));
        if (Double.isNaN(known_variants_values[pos][obj_pos])) return false;

        return true;
    }


    // a safe copy of a variant parameter is made
    //will insert at any valid position pos no additional checks are made to keep
    //known_variants list sorted or non-sparse, presumable, findVariant has been called before this method to determine correct position
    //value - value of the variant
    //mask will be used to arrange the variant in the same order of resources as used in repository
    public void insertVariant(int[] variant_to_insert, int[] mask, double value, String objective, int pos) {

        System.out.println("INSERTING  variant " + Arrays.toString(variant_to_insert) + "at position " + pos);
        //first apply the mask
        int[] variant = apply_mask(variant_to_insert, mask);

        int obj_num = findObjective(objective);
        if (obj_num < 0) throw new IllegalArgumentException(objective + " does not exist for this system");

        //check if the variant_to_insert is at the position
        if (!Arrays.equals(variant, known_variants[pos])) { //the variant has not been inserted, need to make room to insert it

            //check that shifting can be done (to make up room for insertion
            if (last >= known_variants.length - 1) {//grow the array first
                System.out.println("%%%%%%% Growing");
                int[][] newArray = new int[known_variants.length + growth_value][];
                double[][] newValues = new double[known_variants.length + growth_value][objectives.length];
                //fill the newly created cells with NaNs
                for (int i = known_variants_values.length; i < newValues.length; i++)
                    Arrays.fill(newValues[i], Double.NaN);
                //copy into new array
                for (int i = 0; i < known_variants.length; i++) {
                    newArray[i] = Arrays.copyOf(known_variants[i], known_variants[i].length);
                    newValues[i] = Arrays.copyOf(known_variants_values[i], known_variants_values[i].length);
                }
                known_variants = newArray;
                known_variants_values = newValues;
            }


            //System.out.println("What is last? " + last + " and position is " + pos);
            //shift the variant up to free up the space at pos
            for (int j = last; j > pos; j--) {

                known_variants[j] = Arrays.copyOf(known_variants[j - 1], known_variants[j - 1].length);
                known_variants_values[j] = Arrays.copyOf(known_variants_values[j - 1], known_variants_values[j - 1].length);
                /*System.out.println("###########");
                System.out.println(Arrays.toString(known_variants_values[j-1]));
                System.out.println(Arrays.toString(known_variants_values[j]));
                System.out.println("############");*/
            }
            //empty out the insertion slot
            known_variants[pos]=null;
            known_variants_values [pos][obj_num] = Double.NaN;
        }
        //the variant has been inserted before (either way before or just above),
        // check if value for specified objective has been computed before

        if (!checkObjectiveValue(pos, objective)) {  //wasn't computed and inserted before
            //proceed with insertion, either slot at pos is empty or it wasn't and space was prepared above
            //System.out.println("Insert!");
            known_variants[pos] = Arrays.copyOf(variant, variant.length);
            known_variants_values[pos][obj_num] = value;
            //don't forget to update the last mark
            last++;
        }
        //else the value for specified objective has been previously computed and saved for the specified variant, do nothing


        for (int n = 0; n < known_variants_values.length; n++) {
            System.out.print(Arrays.toString(known_variants[n]));
            System.out.print(Arrays.toString(known_variants_values[n]) + "\n");
        }


    }


    //pos - must be a non-negative integer less than size known_variants array
    //This method is inteded to be used after findVariant method
    //Returns: NaN if no value is stored for the specified objective or the value for that objective
    public double getVariantValue(int pos, String objective) {
        System.out.println(">>>> retrieving this value" + known_variants_values[pos][findObjective(objective)]);
        return known_variants_values[pos][findObjective(objective)];
    }

    //returns 1 if variant1 is *lexographically* (i.e. in the order of tree branches where left is less and right is more)
    //larger and -1 if variant 2 is larger
    //and 0 if variants are the same
    //the numeric value returned does not bear any information on the "amount" of difference between variants.
    public static int compare_variants(int[] variant1, int[] variant2) {

        for (int i = 0; i < variant1.length; i++) {
            if (variant1[i] > variant2[i]) return 1;
            else if (variant1[i] < variant2[i]) return -1;
            //else keep checking
        }
        return 0;
    }


    public void printRepository() {
        for (int i = 0; i < known_variants.length; i++) {

            System.out.print(Arrays.toString(known_variants[i]) + " " + Arrays.toString(known_variants_values[i]) + "\n");

        }
    }

    //returns a new array that is a masked copy of parameter array
    private static int[] apply_mask(int[] variant, int[] mask) {
        int[] masked_variant = new int[variant.length];

        for (int i = 0; i < variant.length; i++) {
            masked_variant[i] = variant[mask[i]];

        }
        return masked_variant;
    }


    private int findObjective(String objective) {
        for (int i = 0; i < objectives.length; i++) {
            if (objectives[i] == objective) return i;

        }
        return -1;
    }


    public static void main(String[] args) {
        int[] m = {2, 4, 6, 3, 1};
        int[] mask = {2, 0, 1, 4, 3};
        System.out.println(Arrays.toString(apply_mask(m, mask)));

        System.out.println(compare_variants(m, mask));

    }
}