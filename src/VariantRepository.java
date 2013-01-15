import java.util.ArrayList;
import java.util.Arrays;

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
        for (int i = 0; i < objectvies.length; i++)
            Arrays.fill(known_variants_values[i], Double.NaN);

    }

    //it's ok to return the list itself since it is final
    public ArrayList<Resource> getResourceList() {
        return res_list;
    }

    //TODO implement binary search instead
    //assuming that the known_variants list is always kept sorted

    //mask array contains indecies that direct how to rearrange variant to match the order of resources in repository
    //for example, if the mask is [3,2,1...] and in the repository the resources are in order [A,B,C, ...] and the variant is
    //[0,8,9...] then after the application of the mask the variant will be converted to [9,8,0,...]

    //returns an array of size 2. The first element of that array is either 0 or 1. 0 meaning that element was not found
    //and 1 meaning that it was found. The second element contains the position at which the element was found or at the
    //position where it should be inserted to maintain the ascending order in the case that element was not found
    public int[] findVariant(int[] variant, int[] mask, String objective) {
        int obj_num = findObjective(objective);

        int[] masked_variant = apply_mask(variant, mask);
        int[] result = {0, 0};
        if (last > 0) {
            for (int i = 0; i < known_variants.length; i++) {
                if (compare_variants(masked_variant, known_variants[i]) < 0) { //reached the area of variant larger than current one
                    result[1] = i;
                    return result;
                }
                if (compare_variants(masked_variant, known_variants[i]) == 0 && known_variants_values[i][obj_num] != Double.NaN) {   //found variant and check that the value for given objective was computed
                    result[0] = 1;
                    result[1] = i;
                    return result;
                }

            }

        }
        return result;

    }


    // a safe copy of a variant parameter is made
    //will insert at any valid position pos no additional checks are made to keep
    //known_variants list sorted or non-sparse, presumable, findVariant has been called before this method to determine correct position
    //value - value of the variant
    //mask will be used to arrange the variant in the same order of resources as used in repository
    public void insertVariant(int[] variant, int[] mask, double value, String objective, int pos) {

        ////System.out.println ("INSERTING  variant " + Arrays.toString(variant));
        //first apply the mask
        variant = apply_mask(variant, mask);

        int obj_num = findObjective(objective);

        //check if the given pos is empty
        if (known_variants[pos] != null) {


            //check that shifting can be done
            //System.out.println(known_variants.length);
            //System.out.println(last);
            if (last >= known_variants.length - 1) {//grow the array first
                System.out.println("%%%%%%% Growing");
                int[][] newArray = new int[known_variants.length + 10][];
                double[][] newValues = new double[known_variants.length + 10][objectives.length];
                //fill the newly created cells with NaNs
                for (int i = known_variants_values.length; i < newValues.length; i++)
                    Arrays.fill(newValues[i],Double.NaN);
                //copy into new array
                for (int i = 0; i < known_variants.length; i++) {
                    newArray[i] = known_variants[i];
                    newValues[i] = known_variants_values[i];
                }
                known_variants = newArray;
                known_variants_values = newValues;
            }


            //shift the variant up to free up the space at pos
            for (int j = last; j >= pos; j--) {

                known_variants[j + 1] = known_variants[j];
                known_variants_values[j + 1] = known_variants_values[j];
            }
        }

        //proceed with insertion, either slot at pos is empty or it wasn't and space was prepared above

            known_variants[pos] = Arrays.copyOf(variant, variant.length);
            known_variants_values[pos][obj_num] = value;
            //don't forget to update the last mark
            last++;

    }

    //pos - must be a non-negative integer less than size known_variants array
    //This method is inteded to be used after findVariant method
    //Returns: NaN if no value is stored for the specified objective or the value for that objective
    public double getVariantValue(int pos, String objective) {
        System.out.println(">>>> retrieving");
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