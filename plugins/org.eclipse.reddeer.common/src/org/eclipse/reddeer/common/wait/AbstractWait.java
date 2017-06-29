/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.reddeer.common.wait;

import org.eclipse.reddeer.common.condition.WaitCondition;
import org.eclipse.reddeer.common.exception.WaitTimeoutExpiredException;
import org.eclipse.reddeer.common.logging.Logger;
import org.eclipse.reddeer.common.util.Display;

/**
 * Partial (abstract) implementation of {@link Wait} interface.
 * 
 * @author Vlado Pakan
 * @author Lucia Jelinkova
 * @author jkopriva@redhat.com
 * @author mlabuda@redhat.com

 */
public abstract class AbstractWait implements Wait {
	
	/**
	 * Wait logger.
	 */
	private static final Logger log = Logger.getLogger(AbstractWait.class);
	// Default wait tick period in milliseconds
	private static final long DEFAULT_TICK_PERIOD = 500;
	
	private TimePeriod timeout;

	private boolean throwTimeoutException = true;

	/**
	 * Waits till condition is met for default time period. Throws
	 * WaitTimeoutExpiredException after waiting for specified time period and
	 * wait condition is not met.
	 * 
	 * @param condition
	 *            wait condition to met
	 */
	public AbstractWait(WaitCondition condition) {
		this(condition, TimePeriod.DEFAULT);
	}

	/**
	 * Waits till condition is met for specified timeout period. Throws
	 * WaitTimeoutExpiredException after waiting for specified time period and
	 * wait condition is not met.
	 * 
	 * @param condition
	 *            wait condition to met
	 * @param timePeriod
	 *            time period to wait
	 */
	public AbstractWait(WaitCondition condition, TimePeriod timePeriod) {
		this(condition, timePeriod, true);
	}
	
	/**
	 * Waits till condition is met for specified timeout period. Throws
	 * WaitTimeoutExpiredException after waiting for specified time period and
	 * wait condition is not met.
	 * 
	 * @param condition
	 *            wait condition to met
	 * @param throwRuntimeException
	 *            whether exception should be thrown after expiration of the
	 *            period
	 */
	public AbstractWait(WaitCondition condition, boolean throwRuntimeException) {
		this(condition, TimePeriod.DEFAULT, throwRuntimeException);
	}

	/**
	 * Waits till condition is met for specified timeout period. There is a
	 * possibility to turn on/off throwing a exception.
	 *
	 * @param condition
	 *            wait condition to met
	 * @param timePeriod
	 *            time period to wait
	 * @param throwRuntimeException
	 *            whether exception should be thrown after expiration of the
	 *            period
	 * @throws WaitTimeoutExpiredException
	 *             the wait timeout expired exception
	 */
	public AbstractWait(WaitCondition condition, TimePeriod timePeriod, boolean throwRuntimeException) {
		this(condition, timePeriod, throwRuntimeException, DEFAULT_TICK_PERIOD);
	}

	/**
	 * Waits till condition is met for specified timeout period. There is a
	 * possibility to turn on/off throwing a exception. This constructor also
	 * allows to set custom test period - time elapsed before another execution
	 * of a wait condition is performed.
	 *
	 * @param condition
	 *            wait condition to met
	 * @param timePeriod
	 *            time period to wait
	 * @param throwRuntimeException
	 *            whether exception should be thrown after expiration of the
	 *            period
	 * @param testPeriod
	 *            time to wait before another testing of a wait condition is
	 *            performed in milliseconds
	 * @throws WaitTimeoutExpiredException
	 *             the wait timeout expired exception
	 */
	public AbstractWait(WaitCondition condition, TimePeriod timePeriod, boolean throwRuntimeException,
			long testPeriod) {
		if (condition == null) {
			throw new IllegalArgumentException("condition can't be null");
		}
		if (timePeriod == null) {
			throw new IllegalArgumentException("timePeriod can't be null");
		}
		if (testPeriod < 0) {
			throw new IllegalArgumentException("testPeriod can't be lesser than 0 milliseconds.");
		}
		this.timeout = timePeriod;
		this.throwTimeoutException = throwRuntimeException;
		wait(condition, testPeriod);
	}

	@Override
	public void wait(WaitCondition condition, long testPeriod) {
		log.debug(this.description() + condition.description() + "...");

		long limit;
		if ((Long.MAX_VALUE - System.currentTimeMillis()) / 1000 > getTimeout().getSeconds()) {
			limit = System.currentTimeMillis() + getTimeout().getSeconds() * 1000;
		} else {
			limit = Long.MAX_VALUE;
		}

		while (true) {
			if (stopWaiting(condition)) {
				break;
			}

			if (timeoutExceeded(condition, limit)) {
				return;
			}

			sleep(testPeriod);
		}

		log.debug(this.description() + condition.description() + " finished successfully");
	}

	/**
	 * Gets time period of timeout.
	 * 
	 * @return time period of timeout
	 */
	protected TimePeriod getTimeout() {
		return timeout;
	}

	/**
	 * Finds out whether exception should be thrown is wait condition is not met
	 * after expiration of specified time period.
	 * 
	 * @return true if exception should be thrown, false otherwise
	 */
	protected boolean throwTimeoutException() {
		return throwTimeoutException;
	}

	/**
	 * Sleeps for specified time period.
	 * 
	 * @param timePeriod
	 *            time period to sleep
	 */
	public static void sleep(TimePeriod timePeriod) {
		log.debug("Wait for " + timePeriod.getSeconds() + " seconds");
		sleep(timePeriod.getSeconds() * 1000);
	}
	
	private static void sleep(long milliseconds) {
		if (Thread.currentThread().equals(Display.getDisplay().getThread())) {
			throw new RuntimeException("Tried to execute sleep in UI thread!");
		}
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			throw new RuntimeException("Sleep interrupted", e);
		}
	}
	
	private boolean timeoutExceeded(WaitCondition condition, long limit) {
		if (System.currentTimeMillis() > limit) {
			if (throwTimeoutException()) {
				log.debug(this.description() + condition.description() + " failed, an exception will be thrown");
				throwWaitTimeOutException(timeout, condition);
			} else {
				log.debug(this.description() + condition.description() + " failed, NO exception will be thrown");
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Throws an exception, if timeout has exceeded and creates error message. Calls
	 * condtion's method description - this method is overridden with more
	 * specific error message call in WaitWhile/WaitUntil.
	 * 
	 * @param timeout
	 *            exceeded timeout
	 * @param condition
	 *            failed wait condition
	 */
	protected void throwWaitTimeOutException(TimePeriod timeout, WaitCondition condition) {
		throw new WaitTimeoutExpiredException(
				"Timeout after: " + timeout.getSeconds() + " s.: " + condition.description());
	}
}
