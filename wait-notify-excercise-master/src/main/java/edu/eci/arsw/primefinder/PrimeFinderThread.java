package edu.eci.arsw.primefinder;

import java.util.LinkedList;
import java.util.List;
//import java.util.concurrent.ConcurrentLinkedQueue;
//import java.util.concurrent.CopyOnWriteArrayList;

public class PrimeFinderThread extends Thread{

	
	int a,b;
    public boolean paused = false;
    public final Object pausedLock = new Object();
	
    //public CopyOnWriteArrayList<Integer> primesA;

    //public ConcurrentLinkedQueue<Integer> primesB;

	private List<Integer> primes;
	
	public PrimeFinderThread(int a, int b) {
		super();
                this.primes = new LinkedList<>();
		this.a = a;
		this.b = b;
	}

    public void pauseThread(){
        paused = true;
    }

    @Override
	public void run(){
        for (int i= a;i < b;i++){
            synchronized (pausedLock){
                while (paused){
                    try{
                        pausedLock.wait();
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
            if (isPrime(i)){
                addToList(i);
                //System.out.println(i);
            }
        }
	}
    public void resumeThread() {
        synchronized (pausedLock) {
            paused = false;
            pausedLock.notifyAll();
        }
    }
	boolean isPrime(int n) {
	    boolean ans;
            if (n > 2) { 
                ans = n%2 != 0;
                for(int i = 3;ans && i*i <= n; i+=2 ) {
                    ans = n % i != 0;
                }
            } else {
                ans = n == 2;
            }
	    return ans;
	}

	public List<Integer> getPrimes() {
		return primes;
	}

    private synchronized void addToList(int prime){
        primes.add(prime);
    }	
}
