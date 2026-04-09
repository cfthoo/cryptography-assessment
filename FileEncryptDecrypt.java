import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.security.*;

public class FileEncryptDecrypt {

    private static final int AES_KEY_SIZE = 128; // bits
    private static final int IV_SIZE = 16;       // bytes
    private static final String MODE_ENCRYPT = "encrypt";
    private static final String MODE_DECRYPT = "decrypt";
    private static final String PUBLIC_KEY_FILE = "public.key";
    private static final String PRIVATE_KEY_FILE = "private.key";

    public static void main(String[] args) {
        try {
            if (args.length != 3) {
                printUsage();
                return;
            }

            String mode = args[0];

            if (mode.equalsIgnoreCase(MODE_ENCRYPT)) {
                encryptFile(args[1], args[2]);
            } else if (mode.equalsIgnoreCase(MODE_DECRYPT)) {
                decryptFile(args[1], args[2]);
            } else {
                System.err.println("Unknown mode: " + mode);
                printUsage();
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        } catch (GeneralSecurityException e) {
            System.err.println("Encryption/Decryption error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            // e.printStackTrace(); // Uncomment for debugging
        }
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("Encrypt: java FileEncryptDecrypt encrypt <inputFile> <outputFile>");
        System.out.println("Decrypt: java FileEncryptDecrypt decrypt <inputFile> <outputFile>");
    }

    private static void encryptFile(String inputFile, String outputFile) throws Exception {
        // Load RSA public key
        File publicKeyFile = new File(PUBLIC_KEY_FILE);
        if (!publicKeyFile.exists()) {
            throw new FileNotFoundException("public.key not found");
        }

        PublicKey publicKey;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(publicKeyFile))) {
            publicKey = (PublicKey) ois.readObject();
        }

        // Generate AES key
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(AES_KEY_SIZE);
        SecretKey aesKey = keyGen.generateKey();

        // Generate random IV
        byte[] ivBytes = new byte[IV_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(ivBytes);
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

        // Encrypt file using AES/CBC/PKCS5Padding
        byte[] fileBytes = readFile(inputFile);
        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
        byte[] encryptedFile = aesCipher.doFinal(fileBytes);

        // Encrypt AES key using RSA
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedAESKey = rsaCipher.doFinal(aesKey.getEncoded());

        // Write output: [4-byte AES key length][encrypted AES key][16-byte IV][encrypted file]
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputFile))) {
            dos.writeInt(encryptedAESKey.length);
            dos.write(encryptedAESKey);
            dos.write(ivBytes);
            dos.write(encryptedFile);
        }
        System.out.println("File encrypted successfully: " + outputFile);
    }

    private static void decryptFile(String inputFile, String outputFile) throws Exception {
        // Load RSA private key
        File privateKeyFile = new File(PRIVATE_KEY_FILE);
        if (!privateKeyFile.exists()) {
            throw new FileNotFoundException("private.key not found");
        }

        PrivateKey privateKey;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(privateKeyFile))) {
            privateKey = (PrivateKey) ois.readObject();
        }

        // Read encrypted file
        byte[] fileBytes = readFile(inputFile);
        EncryptedFileData data = readEncryptedFile(fileBytes);

        // Decrypt AES key using RSA
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] aesKeyBytes = rsaCipher.doFinal(data.encryptedAESKey);
        SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");

        // Decrypt file using AES/CBC/PKCS5Padding
        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(data.iv));
        byte[] decryptedFile = aesCipher.doFinal(data.encryptedFile);

        // Write decrypted file
        Files.write(new File(outputFile).toPath(), decryptedFile);
        System.out.println("File decrypted successfully: " + outputFile);
    }

    private static EncryptedFileData readEncryptedFile(byte[] fileBytes) throws IOException {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(fileBytes))) {
            int keyLength = dis.readInt();
            byte[] encryptedAESKey = new byte[keyLength];
            dis.readFully(encryptedAESKey);

            byte[] iv = new byte[IV_SIZE];
            dis.readFully(iv);

            byte[] encryptedFile = dis.readAllBytes();
            return new EncryptedFileData(encryptedAESKey, iv, encryptedFile);
        }
    }

    private static class EncryptedFileData {
        byte[] encryptedAESKey;
        byte[] iv;
        byte[] encryptedFile;

        public EncryptedFileData(byte[] encryptedAESKey, byte[] iv, byte[] encryptedFile) {
            this.encryptedAESKey = encryptedAESKey;
            this.iv = iv;
            this.encryptedFile = encryptedFile;
        }
    }

    private static byte[] readFile(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            throw new FileNotFoundException(path + " not found");
        }
        return Files.readAllBytes(file.toPath());
    }
}