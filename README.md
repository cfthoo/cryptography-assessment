# Cryptography Assessment – Java Implementation

This project contains three Java applications:

1. RSA Key Pair Generator (2048-bit)
2. SHA-256 File Hash Generator
3. File Encryption and Decryption using RSA key pair (Hybrid Encryption)

All implementations use only the standard Java library.

---

## 1. RSA Key Pair Generator

**File:** `RSAKeyGenerator.java`

This application generates a 2048-bit RSA key pair.

**Output files:**

- `public.key`
- `private.key`

### Compile and Run

```bash
javac RSAKeyGenerator.java
java RSAKeyGenerator
```

After running, the public and private keys will be generated in the same folder.

## 2. SHA-256 File Hash Generator

**File:** `SHA256Generator.java`

This application generates the SHA-256 hash of a given file and prints the hash in HEX format to standard output.

### How to Compile and Run

```bash
javac SHA256Generator.java
java SHA256Generator <filename>
```

### Example

```bash
java SHA256Generator test.txt
```

The SHA-256 hash will be printed in HEX format.

## 3. File Encryption and Decryption

**File:** `FileEncryptDecrypt.java`

This application encrypts and decrypts a file using the generated RSA key pair.

Since RSA (2048-bit) can only encrypt small data (about 245 bytes), this implementation uses hybrid encryption:

- The file content is encrypted using AES.
- The AES key is encrypted using the RSA public key.
- During decryption, the AES key is decrypted using the RSA private key.
- Then the file content is decrypted using AES.

This approach follows standard secure encryption practice.

---

### How to compile

```bash
javac FileEncryptDecrypt.java
```

### Encrypt a File

```bash
java FileEncryptDecrypt encrypt test.txt encrypted.dat
```

This will:

- Encrypt the file
- Output the encrypted result as `encrypted.dat`

### Decrypt a File

```bash
java FileEncryptDecrypt decrypt encrypted.dat decrypted.txt
```

This will:

- Decrypt the file
- Output the decrypted result as `decrypted.txt`

The decrypted file content should be identical to the original file (test.txt).

## Files Included in Submission

- `README.md`
- `RSAKeyGenerator.java`
- `SHA256Generator.java`
- `FileEncryptDecrypt.java`
- `public.key`
- `private.key`
- `test.txt`
- `encrypted.dat`

The decrypted file can also be provided for verification if needed.

---

## Verification

To verify correctness:

1. Generate SHA-256 of `test.txt`
2. Encrypt the file
3. Decrypt the file
4. Generate SHA-256 of `decrypted.txt`

The hash of `test.txt` and `decrypted.txt` should be the same.
