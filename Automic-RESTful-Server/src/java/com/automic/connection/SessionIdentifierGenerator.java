package com.automic.connection;

/**
 * 
 * @author bsp
 * @purpose simply generates a random 32 char as a String (used as a token)
 *
 */

import java.math.BigInteger;
import java.security.SecureRandom;

public final class SessionIdentifierGenerator {
	  private SecureRandom random = new SecureRandom();

	  public String nextSessionId() {
	    return new BigInteger(130, random).toString(32);
	  }
	}
