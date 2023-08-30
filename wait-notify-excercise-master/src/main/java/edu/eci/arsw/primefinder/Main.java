package edu.eci.arsw.primefinder;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Control control = Control.newControl();
        
        control.start();

        int time = Control.getTmiliseconds();

        Scanner line = new Scanner(System.in);
        String enter = "";

        while(enter == ""){
            try {
                Control.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            control.stopThreads();
            control.printNumPrimes();

            enter = line.nextLine();
            if(enter == ""){
                control.resumeThreads();
            }
        }

        line.close();
    }
	
}
