package com.leagueofshadows.encrypto;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.AlgorithmParameters;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by siva
 */

class Crypt {
    private final String password;
    private final int keySize;
    private final SecretKeyFactory factory;
    private final Cipher cipher;
    private Context context;

    Crypt(String password, int keySize, Context context) throws Exception {
        this.password = password;
        this.context=context;
        this.keySize = keySize;
        factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    }



    void encrypt(File inputFile, File outputFile)  {
        try {
            FileInputStream inFile = new FileInputStream(inputFile);
            FileOutputStream outFile = new FileOutputStream(outputFile);

            byte[] salt = new byte[8];
            Random rnd = new Random();
            rnd.nextBytes(salt);
            SecretKey secretkey = makeKey(password, salt, keySize);
            cipher.init(Cipher.ENCRYPT_MODE, secretkey);
            outFile.write(salt);
            AlgorithmParameters params = cipher.getParameters();
            byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
            outFile.write(iv);

            useKeyOnData(inFile, outFile);

            inFile.close();
            outFile.flush();
            outFile.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    void decrypt(File inputFile, File outputFile) {
        try {
            FileInputStream inFile = new FileInputStream(inputFile);
            FileOutputStream outFile = new FileOutputStream(outputFile);

            byte[] salt = new byte[8];
            int x = inFile.read(salt);
            byte[] iv = new byte[16];
            x = inFile.read(iv);
            SecretKey secretkey = makeKey(password, salt, keySize);
            cipher.init(Cipher.DECRYPT_MODE, secretkey, new IvParameterSpec(iv));
            useKeyOnData(inFile, outFile);

            inFile.close();
            outFile.flush();
            outFile.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private SecretKey makeKey(String password, byte[] salt, int keySize) throws InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt,65536, keySize);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    private void useKeyOnData(FileInputStream inFile, FileOutputStream outFile)  {
        try {
            byte[] input = new byte[64];
            int bytesRead;
            while ((bytesRead = inFile.read(input)) != -1) {
                byte[] output = cipher.update(input, 0, bytesRead);
                if (output != null) {
                    outFile.write(output);
                }
            }
            byte[] output = cipher.doFinal();
            if (output != null) {
                outFile.write(output);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    class worker extends AsyncTask<String,Integer,SecretKey>
    {
        @Override
        protected void onPostExecute(SecretKey secretKey) {

        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(Integer... values) {

        }

        @Override
        protected SecretKey doInBackground(String[] params) {
            return null;
        }
    }

}
