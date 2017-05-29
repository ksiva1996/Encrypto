package com.leagueofshadows.encrypto;

import android.content.Context;
import android.os.AsyncTask;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


class DecryptFile extends AsyncTask<String,Boolean,Boolean> {


    private File inFile;
    private File outFile;
    private String Password;
    private final SecretKeyFactory factory;
    private final Cipher cipher;
    private comm comm;
    private int id;

    DecryptFile(Context context, File inFile, File outFile, String Password, int id) throws NoSuchAlgorithmException, NoSuchPaddingException {
        comm = (comm) context;
        this.inFile = inFile;
        this.outFile = outFile;
        this.Password = Password;
        this.id=id;
        factory = SecretKeyFactory.getInstance(Util.factory);
        cipher = Cipher.getInstance(Util.cipher);
    }

    @Override
    protected void onPreExecute() {
        comm.start(2);
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean s) {

        int requestCode = 2;
        if(id == -1)
            requestCode = 4;
        if(s)
            comm.stop(requestCode,1,inFile,id);
        else
            comm.stop(requestCode,2,inFile,id);
        super.onPostExecute(s);
    }

    @Override
    protected Boolean doInBackground(String... p) {

        try
        {
            FileInputStream inputFile = new FileInputStream(inFile);
            FileOutputStream outputFile = new FileOutputStream(outFile);

            byte[] salt = new byte[8];
            int x = inputFile.read(salt);

            byte[] iv = new byte[16];
            x = inputFile.read(iv);

            SecretKey secretkey = makeKey(Password, salt);
            cipher.init(Cipher.DECRYPT_MODE, secretkey, new IvParameterSpec(iv));
            useKeyOnData(inputFile, outputFile);
            inFile.delete();
            inputFile.close();
            outputFile.flush();
            outputFile.close();

            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private SecretKey makeKey(String password, byte[] salt) throws InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt,Util.numberOfIterations, Util.keySize);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    private void useKeyOnData(FileInputStream inFile, FileOutputStream outFile)  {
        try {
            byte[] input = new byte[4096];
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

}
