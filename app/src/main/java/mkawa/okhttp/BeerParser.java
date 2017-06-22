package mkawa.okhttp;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by mattkawahara on 8/8/16.
 */
public class BeerParser {

    public static ArrayList<String> beerDetermine(String inputBeer){
        String delims = "[ -/]+";
        String[] tokens = inputBeer.split(delims);
        ArrayList<String> chips = new ArrayList<>();

        for (int i = 0; i < tokens.length; i++){
            String desc = tokens[i].toLowerCase().replaceAll("\"", "");

            //begin tests
            //System.out.println(tokens[i].toLowerCase());
            if (desc.equals("ale")){chips.add("ale");}                                                     //icon made
            if (desc.equals("altbier")){chips.add("altbier");}
            if (desc.equals("amber")){chips.add("amber");}                                                 //icon made
            if (desc.equals("apa")||desc.equals("american")){chips.add("american");}                       //icon made
            if (desc.equals("belgian")){chips.add("belgian");}                                             //icon made
            if (desc.equals("biere de garde")||desc.equals("garde")||desc.equals("Bière de Garde")){chips.add("bieredegarde");}
            if (desc.equals("black")){chips.add("black");}                                                 //icon made
            if (desc.equals("blonde")){chips.add("blonde");}                                               //icon made
            if (desc.equals("bock")){chips.add("bock");}                                                   //icon made
            if (desc.equals("brown")){chips.add("brown");}
            if (desc.equals("brett")){chips.add("brett");}
            if (desc.equals("chili")|desc.equals("pepper")|desc.equals("jalapeno")|desc.equals("jalapeño")){chips.add("chili");}                                                 //icon made
            if (desc.equals("chocolate")|desc.equals("choco")|desc.equals("cocoa")){chips.add("chocolate");}                                         //icon made
            if (desc.equals("cider")){chips.add("cider");}                                                 //icon made
            if (desc.equals("coffee")|desc.equals("cafe")|desc.equals("cappuccino")|desc.equals("latte")|desc.equals("espresso")){chips.add("coffee");}                                               //icon made
            if (desc.equals("dunkel")||desc.equals("dunkelweizen")){chips.add("dunkel");}                  //icon made
            if (desc.equals("esb")||desc.equals("bitter")){chips.add("esb");}                              //icon made
            if (desc.equals("experimental")){chips.add("experimental");}                                   //icon made
            if (desc.equals("english")){chips.add("english");}
            if (desc.equals("field")){chips.add("field");}
            if (desc.equals("fruit")){chips.add("fruit");}                                                 //icon made
            if (desc.equals("german")){chips.add("german");}
            if (desc.equals("ginjo")||desc.equals("sake")){chips.add("ginjo");}                            //icon made
            if (desc.equals("heavy")){chips.add("heavy");}
            if (desc.equals("hefe")||desc.equals("hefeweizen")){chips.add("hefe");}
            if (desc.equals("helles")){chips.add("helles");}
            if (desc.equals("historical")){chips.add("historical");}
            if (desc.equals("honey")){chips.add("honey");}                                                 //icon made
            if (desc.equals("ipa")||desc.equals("india")||desc.equals("i.p.a.")){chips.add("ipa");}        //icon made
            if (desc.equals("imperial")){chips.add("imperial");}
            if (desc.equals("irish")){chips.add("irish");}                                                 //icon made
            if (desc.equals("kolsch")||desc.equals("kölsch")){chips.add("kolsch");}
            if (desc.equals("lager")){chips.add("lager");}                                                 //icon made
            if (desc.equals("lambic")){chips.add("lambic");}                                               //icon made
            if (desc.equals("marzen")||desc.equals("märzen")){chips.add("marzen");}
            if (desc.equals("oktoberfest")){chips.add("oktoberfest");}                                     //icon made
            if (desc.equals("pilsener")){chips.add("pilsener");}                                           //icon made
            if (desc.equals("porter")){chips.add("porter");}                                               //icon made
            if (desc.equals("pumpkin")){chips.add("pumpkin");}                                             //icon made
            if (desc.equals("red")){chips.add("red");}
            if (desc.equals("saison")){chips.add("saison");}                                               //icon made
            if (desc.equals("sour")){chips.add("sour");}                                                   //icon made
            if (desc.equals("smoke")|desc.equals("smoked")|desc.equals("smokey")){chips.add("smoke");}
            if (desc.equals("scottish")){chips.add("scottish");}                                            //icon made
            if (desc.equals("specialty")){chips.add("specialty");}
            if (desc.equals("session")){chips.add("session");}
            if (desc.equals("strong")){chips.add("strong");}
            if (desc.equals("sweet")){chips.add("sweet");}
            if (desc.equals("spice")){chips.add("spice");}                                                 //icon made
            if (desc.equals("stout")){chips.add("stout");}                                                 //icon made
            if (desc.equals("tropical")){chips.add("tropical");}
            if (desc.equals("wheat")){chips.add("wheat");}                                                 //icon made
            if (desc.equals("white")||desc.equals("wit")||desc.equals("witbier")){chips.add("wit");}       //icon made
            if (desc.equals("wild")){chips.equals("wild");}
            if (desc.equals("wine")||desc.equals("barleywine")){chips.add("wine");}                        //icon made
            if (desc.equals("wood")||desc.equals("barrel")||desc.equals("bbl")){chips.add("wood");}        //icon made


        }
        if(chips.isEmpty()){
            chips.add("Nothing to See here");
        }

        return chips;


    }

}
