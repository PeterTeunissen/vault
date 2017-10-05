package com.pt.vault.hasher;

import java.io.UnsupportedEncodingException;

public class PlainHasherImpl implements HashEngine {

	@Override
	public String getSecretHash(String hash) throws IllegalStateException, UnsupportedEncodingException {
		return hash;
	}

}
