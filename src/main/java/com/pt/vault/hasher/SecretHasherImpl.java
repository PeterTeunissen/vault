package com.pt.vault.hasher;

import java.io.UnsupportedEncodingException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.tomcat.util.codec.binary.Base64;

public class SecretHasherImpl implements HashEngine {

	private Mac hmac;

	public SecretHasherImpl(String secretKey) {
		try {
			hmac = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
			hmac.init(secret_key);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getSecretHash(String hash) throws IllegalStateException, UnsupportedEncodingException {
		return Base64.encodeBase64String(hmac.doFinal(hash.getBytes("UTF-8")));
	}
}
