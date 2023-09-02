
package edu.eci.arsw.primefinder;

import java.util.Scanner;

public class Control extends Thread {

	private final static int NTHREADS = 3;
	private final static int MAXVALUE = 30000000;
	private final static int TMILISECONDS = 5000;

	private final int NDATA = MAXVALUE / NTHREADS;

	private PrimeFinderThread pft[];

	private Control() {
		super();
		this.pft = new PrimeFinderThread[NTHREADS];

		int i;
		for (i = 0; i < NTHREADS - 1; i++) {
			PrimeFinderThread elem = new PrimeFinderThread(i * NDATA, (i + 1) * NDATA);
			pft[i] = elem;
		}
		pft[i] = new PrimeFinderThread(i * NDATA, MAXVALUE + 1);
	}

	public static Control newControl() {
		return new Control();
	}

	public synchronized void pauseThreads() {
		for (PrimeFinderThread thread : pft) {
			thread.pauseThread();
		}
	}

	public synchronized void resumeThreads() {
		for (PrimeFinderThread thread : pft) {
			thread.resumeThread();
		}
	}

	@Override
	public void run() {
		for (int i = 0; i < NTHREADS; i++) {
			pft[i].start();
		}

		Scanner scanner = new Scanner(System.in);
        int size = 0;
        int size2 = 0;
		while (true) {
			try {
				Thread.sleep(TMILISECONDS);
				pauseThreads();

                size = pft[0].getPrimes().size();
                if(size != size2){
                    System.out.println("Cantidad de Primos encontrados por los Hilos: " + size);
                    size2 = size;
                    System.out.println("Presiona ENTER para continuar...");
                    scanner.nextLine();
                    resumeThreads();
                } else {
                    System.out.println("No hay más primos que encontrar.");
                    System.out.println("Total Primos: " + size);
                    break;
                }
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
