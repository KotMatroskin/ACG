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


    //an array of pointers to arrays representing variants
    private int[][] known_variants;
    private double[] known_variants_values;
    private int last =0;
    private final ArrayList<Resource> res_list; //list of resources


    //it is more efficient to give the resource list in the order arranged for at least one objective

    public VariantRepository(ArrayList<Resource> res_list){
            this.res_list = res_list;
    }


    
    //TODO implement binary search instead
    //assuming that the known_variants list is always kept sorted
    //positive number i means that that the variant was found at position i
    //negative number i means that the variant was not found but i would be the position where it would go into should it be inserted
    //mask array contains indecies that direct how to rearrange variant to match the order of resources in repository
    //for example, if the mask is [3,2,1...] and in the repository the resources are in order [A,B,C, ...] and the variant is
    //[0,8,9...] then after the application of the mask the variant will be converted to [9,8,0,...]
    public int findVariant (int[] variant, int[] mask){
        if (last > 0) {
            for (int i = 0; i < known_variants.length; i++){
                if (compare_variants(variant, known_variants[i]) < 0){ //reached the area of variant larger than current one
                    return -i;    //now i is empty
                }
                if  (compare_variants(variant, known_variants[i]) == 0)
                    return i;

            }

        }
        return 0;

    }


    // a safe copy of a parameter is made
    //will insert at any valid position pos no additional checks are made to keep
    //known_variants list sorted or non-sparse
    public void insertVariant (int[] variant, double value, int pos){

        //shift the variant up to free up the space at pos

        //check that shifting can be done
        if (last == known_variants.length-1 ){//grow the array first
            int[] newArray = new int[known_variants.length + 10]
        }
        for (int j = last; j >= pos; j--){
            known_variants[j+1] = known_variants[j];
            known_variants_values[j+1] = known_variants_values[j];
        }
        known_variants[pos] = Arrays.copyOf(variant, variant.length);
        known_variants_values[pos] = value;
        //don't forget to update the last mark
        last++;
    }

    public double getVariantValue (int pos){
        System.out.println(">>>> retrieving");
        return known_variants_values[pos];
    }

    //returns 1 if variant1 is *lexographically* (i.e. in the order of tree branches where left is less and right is more)
    //larger and -1 if variant 2 is larger
    //and 0 if variants are the same
    public int compare_variants (int[] variant1, int[] variant2){

        for (int i = 0; i < variant1.length; i++){
            if (variant1[i] > variant2[i]) return 1;
            else if (variant1[i] < variant2[i]) return -1;
            //else keep checking
        }
        return 0;
    }

    private int[] apply_mask (int[] variant, int[] mask){
         int[] masked_variant = new int[variant.length];
        
        for (int i = 0; i < variant.length; i++){
            masked_variant[i] = variant[mask[i]];

        }
        
    }

}
