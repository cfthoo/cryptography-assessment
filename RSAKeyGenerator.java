import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.*;

public class RSAKeyGenerator {
    private static final String PUBLIC_KEY_FILE = "public.key";
    private static final String PRIVATE_KEY_FILE = "private.key";
    public static void main(String[] args) {
        try {
            // Generate RSA KeyPair (2048 bits)
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            // Save Public Key
            try (FileOutputStream fos = new FileOutputStream(PUBLIC_KEY_FILE);
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(publicKey);
            }

            // Save Private Key
            try (FileOutputStream fos = new FileOutputStream(PRIVATE_KEY_FILE);
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(privateKey);
            }

            System.out.println("RSA Key Pair generated successfully.");

        } catch (NoSuchAlgorithmException e) {
            System.err.println("RSA algorithm not available: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error saving key files: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error occurred: " + e.getMessage());
        }
    }
}