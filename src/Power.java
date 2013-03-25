import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: irina
 * Date: 21/12/12
 * Time: 4:42 PM
 * To change this template use File | Settings | File Templates.
 */

//This class implements the power objective that is calculated via basic formula of area * power (of particular clock frequency)
//It's assumed that clock resource will store the value of power directly (not the frequency)
public class Power extends Objective {

    private Clock clck = null; //a pointer to the clock resource, that controls the power requirement
    private int clck_pos = -1; //the position of clock resource in the resource list, -1 - not checked yet
    private Area areaObjective = null; //this a pointer to the area objective as its reference in repository
    //if Power objective is constructed without this string, which is possible, if area objective is
    //is not something that is being optimized, then the area will be computed, otherwise it will be
    //looked-up in repository first, if not found it would be computed and saved there.


    public Power(Clock clck, String units, String goal, ArrayList<Resource> res_list) {
        super("Power", units, goal, res_list);
        this.clck = clck;
    }

    //If this system does include Area objective, then it should be passed to this Power objective
    //since power is directly affected by area.
    public Power(Clock clck, String units, String goal, ArrayList<Resource> res_list, Area areaObjective) {
        super("Power", units, goal, res_list);
        this.clck = clck;
        this.areaObjective = areaObjective;

    }

    public Power(Clock clck, String units, String goal, ArrayList<Resource> res_list, double constraint) {
        super("Power", units, goal, res_list);
    }

    public Power(Clock clck, String units, String goal, ArrayList<Resource> res_list, double constraint, Area areaObjective) {
        super("Power", units, goal, res_list);
        this.clck = clck;
        this.areaObjective = areaObjective;

    }

    public Power(Clock clck, String units, String goal, ArrayList<Resource> res_list, VariantRepository rep) {
        super("Power", units, goal, res_list, rep);
        this.clck = clck;
    }

    public Power(Clock clck, String units, String goal, ArrayList<Resource> res_list, VariantRepository rep, Area areaObjective) {
        super("Power", units, goal, res_list, rep);
        this.clck = clck;
        this.areaObjective = areaObjective;
    }

    public Power(Clock clck, String units, String goal, ArrayList<Resource> res_list, VariantRepository rep, double constraint) {
        super("Power", units, goal, res_list, rep, constraint);
        this.clck = clck;
    }

    public Power(Clock clck, String units, String goal, ArrayList<Resource> res_list, VariantRepository rep, double constraint, Area areaObjective) {
        super("Power", units, goal, res_list, rep, constraint);
        this.clck = clck;
        this.areaObjective = areaObjective;
    }

    //to evaluate power we need to know the area and the wattage of the clock
    public double evaluate(int[] variant) {
        System.out.println("****** Calling power evaluate with variant" + Arrays.toString(variant));


        
        int[] result = rep.findVariant(variant, mask);    //search in repository
        int pos = result[1];
        if (result[0] == 1 && rep.checkObjectiveValue(pos, super.getName())) {  //the variant is in repository

            //variant already has been computed before and value filed
            System.out.println("Found variant " + Arrays.toString(variant) + " at position " + pos);
            return rep.getVariantValue(pos, super.getName());

        } else { //either the variant was not computed before at all or was not computed for power objective,
            //either way, compute and insert (the insert will take care of the details).


            //need to find out where is the clck in the arrangement,
            // it is done every time until the space is fully arranged
            if (super.getArranged() == false || (super.getArranged() && clck_pos == -1)) {
                clck_pos = super.getResourceList().indexOf(clck);
                System.out.println("the clock is right now at " + pos);
            }
            //get the power/area value
            Double power = (super.getResourceList()).get(clck_pos).getValue("Power", variant[clck_pos]);
            System.out.println("here and power is now " + power);

            //check if the area for this variant has been computed before and is in repository
            //first check if area is an objective at all in this system
            if (areaObjective == null) { //we need to compute the area ourselves
                power = power * evaluate_area(variant);

            } else { //area is an objective in this system so call evaluate method (it will/should take care of all the
                //details of checking repository and inserting the value if needed
                int[] tmp_mask = super.makeMaskforObjective(rep.getResourceList(), areaObjective.getMask());
                System.out.println("Mask that converts power to area is " + Arrays.toString(tmp_mask));


                System.out.println("Masked variant is" + Arrays.toString(convertToAnotherObjective(variant, tmp_mask)));
                System.out.println("Area is : " + areaObjective.evaluate(super.convertToAnotherObjective(variant, tmp_mask)));
                power = power * areaObjective.evaluate(super.convertToAnotherObjective(variant, tmp_mask));

            }



            System.out.println("^^^ Now inserting and returning power: " + power + "at position " + pos);
            rep.insertVariant(variant, mask, power, super.getName(), pos);

            //reset the position value if the space hasen't been arranged yet (that way on the next time around
            //we know to check clock location again)
            /*if (!super.getArranged())
                clck_pos = -1; //this means evaluate is being called durign arrangement part*/

            return power;
        }
    }


    private double evaluate_area(int[] variant) {

        ArrayList<Resource> resources = super.getResourceList();

        if (resources.size() != variant.length) {
            throw (new Error("the variant of resources must have the same length as number of *types* of resources available"));
        }

        Double area = 0.0;

        for (int i = 0; i < resources.size(); i++) {
            if (variant[i] != 0) {
                area += resources.get(i).getValue(super.getName(), variant[i]);
            }

        }
        System.out.println("Area for this variant is: " + area);

        return area;
    }
}


