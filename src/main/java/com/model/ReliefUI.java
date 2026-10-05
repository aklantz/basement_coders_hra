   package com.model;

   import java.util.ArrayList;

   /**
    * Text-based UI for the hurricane relief app.
    */
   public class ReliefUI {

       /**
        * Loads the shelters and displays each one.
        *
        * @param args not used
        */
       public static void main(String[] args) {
           ArrayList<Shelter> shelters = DataLoader.getCachedShelters();
           System.out.println("Shelters:");
           for (Shelter shelter : shelters) {
               System.out.println(shelter);
           }
       }
   }