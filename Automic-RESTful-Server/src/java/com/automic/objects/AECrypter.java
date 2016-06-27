package com.automic.objects;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;

import com.uc4.util.HexUtil;

/**
 * 
 * @author bsp
 * @purpose provide encryption / decryption services for ini files or other (INTERNALKKEY)
 * !!! This class can only work if a binary file is present on the target system...
 *
 */
public class AECrypter {


	private final static String ENCODING = "ISO-8859-1";
	
	private final static String KEYFILEPATH = "web-app/bin.key";
	private final static String INTERNALKEY = "h1st0ri@";
	
	// check the presence of binary file
	public static boolean isBinKeyFilePresent(){
			File keyfile = new File(KEYFILEPATH);
			if(keyfile.exists() && !keyfile.isDirectory()) { 
			    return true;
			}
			return false;
	}
	
	// retrieve binary key from file
	public static byte[] getKeyFromFile() throws IOException{
		Path path = Paths.get(KEYFILEPATH);
		byte[] data = Files.readAllBytes(path);
		return data;
	}
	
	/**
	 * Decodes a password
	 * 
	 * @param value Decoded value using the default key.
	 * @return Value
	 */
	public static String deMaximWithBinFile(final String value) throws IOException {
		return AECrypter.deMaxim(value, getKeyFromFile());
	}

	/**
	 * Decrypts password for the  REST Server (with internal key stored in this class)
	 * 
	 * @param value String
	 * @return Encrypted String
	 */
	public static String deMaximWithInternalKey(final String value) throws IOException {
		byte[] key = INTERNALKEY.getBytes(ENCODING);
		return AECrypter.deMaxim(value, key);
	}
	
	/**
	 * @param value encrypted String
	 * @param keyString Key
	 * @return readable text
	 * @throws RuntimeException in case of an error
	 */
	public static String deMaxim(final String value, final String keyString) {
		try {
			return AECrypter.deMaxim(value, keyString.getBytes(AECrypter.ENCODING));
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private static String deMaxim(final String value, final byte[] key) {
		if (value.length() < 4) {
			return value;
		}
		if (value.startsWith("--10") || value.charAt(0) == 0xAD && value.charAt(1) == 0xAD && value.charAt(2) == '1' && value.charAt(3) == '0') {
			try {
				final Key secretKey = new SecretKeySpec(key, "DES");
				final byte[] input = HexUtil.hexStringToByteArray(value.substring(4));
				final Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
				cipher.init(Cipher.DECRYPT_MODE, secretKey);
				
				byte[] result = null;
				try{
					result = cipher.doFinal(input);
				}catch (IllegalBlockSizeException e){
					// returns a password that can only be wrong.. but avoids  throwing an error
					return value.replace("--10", "");
				}
				// find 0 byte
				int len = 0;
				for (int i = 0; i < result.length; i++) {
					if (result[i] == 0) {
						len = i;
						break;
					}
				}
				if (len == 0) {
					len = result.length;
				}
				final byte[] text = new byte[len];
				System.arraycopy(result, 0, text, 0, len);
				return new String(text, AECrypter.ENCODING);

			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
		return value;
	}

	/**
	 * Encrypts password for the  REST Server
	 * 
	 * @param value String
	 * @return Encrypted String
	 */
	public static String enMaximWithInternalKey(final String value) throws IOException {
		byte[] key = INTERNALKEY.getBytes(ENCODING);
		return AECrypter.enMaxim(value, key);
	}

	
	
	/**
	 * Encrypts password
	 * 
	 * @param value String
	 * @return Encrypted String
	 */
	public static String enMaximWithBinFile(final String value) throws IOException {
		return AECrypter.enMaxim(value, getKeyFromFile());
	}

	/**
	 * Encrypts a password using the specified key
	 * 
	 * @param value String
	 * @param keyString Key
	 * @return Encrypted String in upper case
	 */
	public static String enMaxim(final String value, final String keyString) {
		try {
			return AECrypter.enMaxim(value, keyString.getBytes(AECrypter.ENCODING));
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private static String enMaxim(final String value, final byte[] key) {

		try {
			final Key secretKey = new SecretKeySpec(key, "DES");
			final byte[] input = value.getBytes(AECrypter.ENCODING);
			int n = input.length;
			while (n % 8 != 0) {
				n++;
			}
			final byte[] ninput = new byte[n];
			System.arraycopy(input, 0, ninput, 0, input.length);
			final Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			final byte[] result = cipher.doFinal(ninput);
			return "--10" + HexUtil.binToHexString(result).toUpperCase();

		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
