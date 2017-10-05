package com.pt.vault.hasher;

import java.io.UnsupportedEncodingException;

public interface HashEngine {

	public String getSecretHash(String hash) throws IllegalStateException, UnsupportedEncodingException;
}
