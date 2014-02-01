package at.workflow.webdesk.tools.security;

import java.util.Arrays;

import junit.framework.TestCase;

/**
 * @author fritzberger 22.10.2010
 */
public class WTestCryptUtil extends TestCase {

	public void testEncryptEmptyString() throws Exception	{
		encryptDecryptAndCheck("");
	}
	
	public void testEncryptDecryptAscii() throws Exception	{
		encryptDecryptAndCheck("encryptme");
		encryptDecryptAndCheck("encryptmeencryptmeencryptmeencryptmeencryptmeencryptme");
	}
	
	public void testEncryptDecryptSonderzeichen() throws Exception	{
		encryptDecryptAndCheck("12345-encryptme-67890");
		encryptDecryptAndCheck("\\+-*/,;.:\"'`?!~#@$%&|<>{}[]()");
	}
	
	public void testEncryptDecryptUmlaute() throws Exception	{
		encryptDecryptAndCheck("äüöÄÖÜß");
		encryptDecryptAndCheck("encryptmeäüöÄÖÜß\\+-*/,;.:äüöÄÖÜß\"'`?!~#@$%&|<>{}[]()äüöÄÖÜßencryptme");
	}
	
	public void testEncryptDecryptWithSameCryptUtil() throws Exception	{
		CryptUtil crypt = new CryptUtil();
		encryptDecryptAndCheck("encryptmeencryptmeencryptmeencryptmeencryptmeencryptme", crypt);
		encryptDecryptAndCheck("encryptmeäüöÄÖÜß\\+-*/,;.:äüöÄÖÜß\"'`?!~#@$%&|<>{}[]()äüöÄÖÜßencryptme", crypt);
	}

	/* Test if double encryption is possible 
	public void testEncryptDecryptTwice() throws Exception	{
		final String password = "encryptmeäüöÄÖÜß\\+/-*,;.:äüöÄÖÜß\"'`?!~#@$%&|<>{}[]()äüöÄÖÜßencryptme";
		final byte [] clearText = password.getBytes();
		byte [] encrypted = encrypt(password, null);
		byte [] encrypted2 = encrypt(new String(encrypted), null);
		byte [] decrypted2 = decrypt(encrypted2, null);
		byte [] decrypted = decrypt(decrypted2, null);
		assertTrue(Arrays.equals(clearText, decrypted));
	}
	*/
	
	private void encryptDecryptAndCheck(final String password) throws Exception {
		encryptDecryptAndCheck(password, null);
	}
	
	private void encryptDecryptAndCheck(final String password, CryptUtil crypt) throws Exception {
		final byte [] clearText = password.getBytes();
		
		byte [] encrypted = encrypt(password, crypt);
		assertFalse(Arrays.equals(clearText, encrypted));
		
		byte [] decrypted = decrypt(encrypted, crypt);
		assertTrue(Arrays.equals(clearText, decrypted));
		assertEquals(password, new String(decrypted));
		
		System.err.println("encrypted >"+password+"< to [ "+byteString(encrypted)+" ] and back to >"+new String(decrypted)+"<");
	}

	private byte [] encrypt(final String password, CryptUtil crypt) throws Exception {
		if (crypt == null)
			crypt = new CryptUtil();
		
		final byte [] clearText = password.getBytes();
		return crypt.encrypt(clearText);
	}

	private byte [] decrypt(final byte[] encrypted, CryptUtil crypt) throws Exception {
		if (crypt == null)
			crypt = new CryptUtil();
		
		return crypt.decrypt(encrypted);
	}

	private String byteString(byte [] bytes)	{
		StringBuffer sb = new StringBuffer();
		for (byte b : bytes)
			sb.append(" "+Byte.toString(b));
		return sb.toString();
	}
}
