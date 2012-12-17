import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: irina
 * Date: 04/12/12
 * Time: 3:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class Test2 {

    public static void main(String[] args) {

        //define objectives
        Objective[] objectives = new Objective[1];
        ArrayList<Resource> res_list = new ArrayList<Resource>();
        String[] obj = {"area"};

        ResourceCopies A = new ResourceCopies("A", 1, obj);
        A.setObjectiveValues(obj[0], 1);

        ResourceCopies B = new ResourceCopies("B", 1, obj);
        B.setObjectiveValues(obj[0], 7);

        ResourceCopies C = new ResourceCopies("C", 1, obj);
        C.setObjectiveValues(obj[0], 4);

        ResourceCopies D = new ResourceCopies("D", 1, obj);
        D.setObjectiveValues(obj[0], 5);

        ResourceCopies E = new ResourceCopies("E", 1, obj);
        E.setObjectiveValues(obj[0], 3);

        res_list.add(A);
        res_list.add(B);
        res_list.add(C);
        res_list.add(D);
        res_list.add(E);

        objectives[0] = new Area("area", "min", res_list, 11);

        objectives[0].sortResources();
        System.out.println(objectives[0].toString());

        objectives[0].findTightBorderVar();
        System.out.println(Arrays.toString(objectives[0].getTight_border_var()));
        System.out.println ();

    }
}
