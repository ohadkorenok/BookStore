package bgu.spl.mics;

import java.util.concurrent.TimeUnit;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 * 
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */
//@INV: if isDone() == false  -> get() = BLOCKING
public class Future<T> {

	private volatile boolean done;
	private T value = null;
	
	/**
	 * This should be the the only public constructor in this class.
	 */
	public Future() {
		done = false;
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved.
     * This is a blocking method! It waits for the computation in case it has
     * not been completed.
     * <p>
     * @return return the result of type T if it is available, if not wait until it is available.
     * 	       
     */
	//@PRE:  none
	//@POST  trivial
	public T get() {
		synchronized (this) {
			while (!isDone()) {
				try {
					wait();
				} catch (InterruptedException e) {
					System.out.println("The thread was interrupted!!! ");
				}
			}
			return value;
		}

	}
	
	/**
     * Resolves the result of this Future object.
     */
	//@PRE: isDone() == false
	//@POST: get() is NOT BLOCKING && isDone() == true
	public void resolve (T result) {
		synchronized (this) {
			if (!isDone()) {
				value = result;
				done = true;
				notifyAll();
			} else {
				System.out.println("future object has been resolved already! ");
			}
		}

	}
	
	/**
     * @return true if this object has been resolved, false otherwise
     */
	//@PRE: none
	//@POST: none
	public boolean isDone() {
		return done;
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved,
     * This method is non-blocking, it has a limited amount of time determined
     * by {@code timeout}
     * <p>
     * @param timeout	the maximal amount of time units to wait for the result.
     * @param unit		the {@link TimeUnit} time units to wait.
     * @return return the result of type T if it is available, if not, 
     * 	       wait for {@code timeout} TimeUnits {@code unit}. If time has
     *         elapsed, return null.
     */
	//@PRE: none
	//@POST: trivial
	public T get(long timeout, TimeUnit unit) {
		synchronized (this) {
			while (!isDone()) {
				try {
					wait(unit.toSeconds(timeout));
					return value;
				} catch (InterruptedException e) {
					System.out.println("The thread was interrupted! ");
				}
			}
			return value;
		}
	}

}
