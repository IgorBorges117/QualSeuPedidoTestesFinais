package br.com.qualseupedido.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class SenhaUtil {

    private SenhaUtil() {
    }

    public static String hashSha256(String texto) {
        if (texto == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 indisponivel.", e);
        }
    }

    public static boolean confereSenha(String senhaInformada, String senhaArmazenada) {
        if (senhaInformada == null || senhaArmazenada == null) {
            return false;
        }
        String hashInformada = hashSha256(senhaInformada);
        if (senhaArmazenada.equals(hashInformada)) {
            return true;
        }
        // Compatibilidade com usuarios antigos cadastrados antes do hash.
        return senhaArmazenada.equals(senhaInformada);
    }
}
