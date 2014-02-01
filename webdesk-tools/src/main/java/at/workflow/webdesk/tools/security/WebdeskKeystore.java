package at.workflow.webdesk.tools.security;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;

import javax.crypto.Cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import at.workflow.webdesk.tools.WebdeskConstants;

/**
 * Loads the webdesk keystore from a hardcoded resource path ("at/workflow/webdesk/webdeskKeyStore").
 * This source was refactored from LicenceReaderImpl.
 * TODO: Not refactored was EncryptFile (sandbox/maven/encryptedLicenceDef).
 * 
 * @author fritzberger 22.10.2010
 */
@Deprecated	// TODO rework this. Private key must not be together with public key, and must not be deployed.
public class WebdeskKeystore {

	private KeyStore keyStore;
	
	public Certificate getCertificate() throws GeneralSecurityException, IOException	{
		return getKeyStore().getCertificate("encrypt");
	}
	
	public PrivateKey getPrivateKey() throws GeneralSecurityException, IOException	{
		return (PrivateKey) getKeyStore().getKey("encrypt", WebdeskConstants.SADMIN_PASSWORD.toCharArray());	// see WebdeskConstants.SADMIN_PASSWORD
	}
	
	public PublicKey getPublicKey() throws GeneralSecurityException, IOException	{
		return getCertificate().getPublicKey();
	}
	
	/**
	 * @param opmode operation mode, one of Cipher.DECRYPT_MODE or Cipher.ENCRYPT_MODE. 
	 * @throws GeneralSecurityException
	 * @throws IOException 
	 */
	public Cipher newCipherInstance(int opmode) throws GeneralSecurityException, IOException {
		assert opmode == Cipher.ENCRYPT_MODE || opmode == Cipher.DECRYPT_MODE;
		getKeyStore();	// call this for initialization of security provider
		final Cipher cipher = Cipher.getInstance("RSA", "BC");	// "BC" is bouncycastle
		cipher.init(opmode, opmode == Cipher.DECRYPT_MODE ? getPublicKey() : getPrivateKey());
		return cipher;
	}

	public KeyStore getKeyStore() throws GeneralSecurityException, IOException	{
		if (this.keyStore == null)	{
			// add "bouncycastle" RSA Provider, usable via "BC"
			Security.addProvider(new BouncyCastleProvider());
			
	        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
	        Resource keyStoreResource = new ClassPathResource("at/workflow/webdesk/webdeskKeyStore");
	        ks.load(keyStoreResource.getInputStream(), "lmksedhi".toCharArray());
	        this.keyStore = ks;
		}
		return this.keyStore;
	}
	
}
