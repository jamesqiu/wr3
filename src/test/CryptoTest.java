package test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * ���ܣ�����
 * @author jamesqiu 2009-9-9
 *
 */
public class CryptoTest {

	Cipher ecipher;
	Cipher dcipher;
	
	// 8-byte Salt
    private final byte[] salt = {
        (byte)0xA9, (byte)0x9B, (byte)0xC8, (byte)0x32,
        (byte)0x56, (byte)0x35, (byte)0xE3, (byte)0x03
    };    
    // Iteration count
    int iterationCount = 19;

    /**
     * @param password �Զ�������
     */
	public CryptoTest(String password) {
		
		// Create the key
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), 
        		salt, iterationCount);
        SecretKey key;
		try {
			key = SecretKeyFactory.getInstance(
			    "PBEWithMD5AndDES").generateSecret(keySpec);
			ecipher = Cipher.getInstance(key.getAlgorithm());
			dcipher = Cipher.getInstance(key.getAlgorithm());
	         // Prepare the parameter to the ciphers
            AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);
            // Create the ciphers
            ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
            dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ���ؼ��ܺ���ַ�����base64��
	 * @param s
	 * @return
	 */
	public String enc(String s) {
		
		byte[] bytes0 = toBytes(s);
		byte[] bytes1 = enc(bytes0);
        return new sun.misc.BASE64Encoder().encode(bytes1);		
	}
	
	/**
	 * ���ؼ��ܺ��byte[]
	 * @param bytes
	 * @return
	 */
	public byte[] enc(byte[] bytes) {
		
		try {
			return ecipher.doFinal(bytes);
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * ����
	 * @param s ���ܺ���ַ�����base64��
	 * @return
	 */
	public String dec(String s) {
		
		try {
			// base64 -> ����byte[]
			byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(s);
			// ����byte[] -> ԭbyte[]
			byte[] bytes = dcipher.doFinal(dec);
			String s1 = toString(bytes);
			return s1;
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}		
		return null;
	}
	
	/**
	 * ����byte[]
	 * @param bytes ���ܵ�byte[]
	 * @return ԭbyte[]
	 */
	public byte[] dec(byte[] bytes) {
		
		try {
			return dcipher.doFinal(bytes);
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private byte[] toBytes(String s) {
		try {
			return s.getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private String toString(byte[] bytes) {
		try {
			return new String(bytes, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		
		CryptoTest o;
		
		if (args.length!=3) {
			System.out.println("usage:\n" + 
					"  ���ܣ�\n" + 
					"  java test.CryptoTest -enc \"ԭ�ַ���\" mypassword\n" + 
					"  ���ܣ�\n" + 
					"  java test.CryptoTest -dec \"�����ַ���\" mypassword\n");
			// ����
			String s0 = "cn���ģ�ԭ�ַ���";
			o = new CryptoTest("pass3"); // ����
			System.out.println(o.enc(s0));
			System.out.println(o.dec(o.enc(s0)));
			return;
		}
		
		String flag = args[0];
		String s = args[1];
		String pass = args[2];
		
		o = new CryptoTest(pass);
		
		if (flag.equals("-enc")) {
			System.out.println(o.enc(s)+ ".");
		} else if (flag.equals("-dec")) {
			System.out.println(o.dec(s));
		}
	}
}
