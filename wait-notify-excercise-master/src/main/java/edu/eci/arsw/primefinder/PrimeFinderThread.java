package edu.eci.arsw.primefinder;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PrimeFinderThread extends Thread {

	private int a, b;
	private static List<Integer> primes;
	private boolean paused;

    private static Lock lock = new ReentrantLock();

	public PrimeFinderThread(int a, int b) {
		super();
		PrimeFinderThread.primes = new LinkedList<>();
		this.a = a;
		this.b = b;
		this.paused = false;
	}

	public synchronized void pauseThread() {
		paused = true;
	}

	public synchronized void resumeThread() {
		paused = false;
		notify();
	}

	@Override
	public void run() {
        List<Integer> localPrimes = new LinkedList<>();
		for (int i = a; i < b; i++) {
			synchronized (this) {
				while (paused) {
					try {
						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			if (isPrime(i)) {
                localPrimes.add(i);
            }

		}

        lock.lock();
        try {
            primes.addAll(localPrimes);
        } finally {
            lock.unlock();
        }
	}

	boolean isPrime(int n) {
		boolean ans;
		if (n > 2) {
			ans = n % 2 != 0;
			for (int i = 3; ans && i * i <= n; i += 2) {
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
}