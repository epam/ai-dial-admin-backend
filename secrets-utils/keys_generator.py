"""
Keys Generator Script (uses AES-GCM Encryption)
This script generates random keys, encrypts a data key using AES-GCM with a master key and IV, prints the results.

HOW TO RUN THE SCRIPT:
1. Install Python 3.x on your computer.
2. Ensure the `cryptography` library is installed:
   Run: `pip install cryptography`
3. Execute the script in a command prompt or terminal:
   Run: `python keys_generator.py`
   This will generate keys, encrypt and print them.
"""

import os
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from base64 import b64encode


AES_KEY_SIZE = 192
IV_SIZE = 12
TAG_SIZE = 16


def generate_random_key() -> bytes:
    return os.urandom(AES_KEY_SIZE // 8)


def encrypt_data_key(master_key: bytes, data_key: bytes):
    iv = os.urandom(IV_SIZE)
    cipher = Cipher(algorithms.AES(master_key), modes.GCM(iv))
    encryptor = cipher.encryptor()
    encrypted_data_key = encryptor.update(data_key) + encryptor.finalize()
    return iv, encrypted_data_key, encryptor.tag


def get_randomly_generated_base64_key():
    key = generate_random_key()
    return b64encode(key).decode()


def main():
    db_password = generate_random_key()
    db_password_base64 = b64encode(db_password).decode()
    print(f"DB Password Key (Base64): {db_password_base64}")

    master_key = generate_random_key()
    master_key_base64 = b64encode(master_key).decode()
    print(f"Master Key (Base64): {master_key_base64}")

    encryption_key = generate_random_key()
    iv, encrypted_data_key, tag = encrypt_data_key(master_key, encryption_key)
    combined_encrypted_key = b64encode(iv + encrypted_data_key + tag).decode()
    print(f"Encrypted Encryption Key (Base64): {combined_encrypted_key}")
    

if __name__ == "__main__":
    main()
