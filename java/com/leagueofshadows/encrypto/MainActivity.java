package com.leagueofshadows.encrypto;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity implements comm {

    static int PERMISSION_EXTERNAL_STORAGE=1;
    String Password;
    ProgressDialog pd;
    Db db;
    Toolbar toolbar;
    ArrayList<FileItem> fileItems;
    ListView listview;
    ArrayList<String> names;
    ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listview = (ListView)findViewById(R.id.list);
        listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listview.setMultiChoiceModeListener(new CalBack());
        Password = getIntent().getStringExtra("password");
        db = new Db(this);
        File file = new File(Util.path);
        if(!file.exists())
            file.mkdir();
        int permissionCheck1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionCheck1== PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_EXTERNAL_STORAGE);
        }
       FloatingActionButton button  = (FloatingActionButton)findViewById(R.id.fab);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,FilePicker.class);
                startActivityForResult(i,1);
            }
        });
        initialize();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    private void initialize() {
        fileItems = new ArrayList<>();
        fileItems = db.getData();
        names = new ArrayList<>();
        for (FileItem o:fileItems) {
            names.add(o.getName());
        }
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_activated_1,names);
        listview.setAdapter(adapter);
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                
                return true;
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final FileItem f = fileItems.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Do you want to decrypt the file "+f.getName());
                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File inFile = new File(f.getNewpath());
                        File outFile = new File(f.getOriginalpath());
                        try
                        {
                            if(inFile.exists()) {
                                DecryptFile decryptFile = new DecryptFile(MainActivity.this, inFile, outFile, Password, f.getId());
                                decryptFile.execute();
                            }
                            else
                            {
                                dialog.dismiss();
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                                builder1.setMessage("The file selected must have been deleted from the memory would you like to delete it fromth database");
                                builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        db.deleteFile(f.getId());
                                        names.remove(f.getName());
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                                builder1.create().show();
                            }
                        }
                        catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                            e.printStackTrace();
                        }
                    }
                }).setNeutralButton("SHARE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(Intent.ACTION_SEND);
                        int x = f.getName().lastIndexOf(".");
                        String s = MimeTypeMap.getSingleton().getMimeTypeFromExtension(f.getName().substring(x+1));
                        i.setType(s);
                        i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(f.getNewpath())));
                        startActivity(Intent.createChooser(i,"Share file"));
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==PERMISSION_EXTERNAL_STORAGE)
        {
            if(grantResults.length>0)
            {
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this,"permission granted successfully",Toast.LENGTH_SHORT).show();
                else
                    finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == R.id.action_other_decrypt)
        {
            Intent i = new Intent(MainActivity.this,FilePicker.class);
            startActivityForResult(i,2);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK) {
            if (requestCode == 1)
            {
                if (data.hasExtra(FilePicker.EXTRA_FILE_PATH)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    final File file = new File(data.getStringExtra(FilePicker.EXTRA_FILE_PATH));
                    final File outFile = new File(Util.path + file.getName());
                    builder.setMessage("Would you like to encrypt the file " + file.getName());
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                EncryptFile encrypytFile = new EncryptFile(MainActivity.this, file, outFile, Password, 0);
                                encrypytFile.execute();
                            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                                e.printStackTrace();
                            }
                        }
                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
            else
            {
                if(data.hasExtra(FilePicker.EXTRA_FILE_PATH))
                {
                    File dir = new File(Util.output);
                    if(!dir.exists())
                        dir.mkdir();
                    final File file = new File(data.getStringExtra(FilePicker.EXTRA_FILE_PATH));
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Enter password to decrypt the file "+file.getName()+". Enter the correct password or the file will not be decrpted properly.");
                    View v = getLayoutInflater().inflate(R.layout.check_password,null);
                    builder.setView(v);
                    final EditText pass = (EditText)v.findViewById(R.id.pass);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String password = pass.getText().toString();
                            if(!password.equals(""))
                            {
                                try
                                {
                                    DecryptFile decryptFile = new DecryptFile(MainActivity.this,file,new File(Util.output+file.getName()),password,-1);
                                    decryptFile.execute();
                                }
                                catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                                    e.printStackTrace();
                                }
                            }
                            else
                            Toast.makeText(MainActivity.this,"please enter the password",Toast.LENGTH_SHORT).show();
                        }
                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                }
            }
        }
        else
            Toast.makeText(this,"file not selected",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void start(int type) {

        pd = new ProgressDialog(this);
        if(type==1)
            pd.setMessage("Encrypting");
        else
            pd.setMessage("Decrypting");
        pd.show();
    }

    @Override
    public void stop(int type, int result, final File inFile, int id) {

        String s;
        if(type == 1)
            s = "Encrypting";
        else
            s = "Decrypting";

        if(result==1)
        {

            if(type==1)
            {
                Toast.makeText(this,s+" successful",Toast.LENGTH_SHORT).show();
                int x = db.addFile(inFile.getName(),inFile.getAbsolutePath(), Util.path + inFile.getName(), (int) inFile.getTotalSpace());
                names.add(inFile.getName());
                FileItem fileItem = new FileItem(x,inFile.getName(),inFile.getAbsolutePath(),Util.path+inFile.getName(),Long.toString(inFile.getTotalSpace()));
                fileItems.add(fileItem);
                adapter.notifyDataSetChanged();
            }
            else if(type == 2)
            {
                Toast.makeText(this,s+" successful",Toast.LENGTH_SHORT).show();
                FileItem file=null;
                for(int i=0;i<fileItems.size();i++)
                {
                    if(fileItems.get(i).getId()==id) {
                        file = fileItems.get(i);
                    }
                }
                db.deleteFile(file.getId());
                fileItems.remove(file);
                names.remove(file.getName());
                adapter.notifyDataSetChanged();
            }
            else if(type == 3)
            {
                Toast.makeText(this,"decrption successful",Toast.LENGTH_SHORT).show();
                fileItems.clear();
                initialize();
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Decryption successful");
                builder.setPositiveButton("OPEN", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        int x = inFile.getName().lastIndexOf(".");
                        File f = new File(Util.output);
                        String s = MimeTypeMap.getSingleton().getMimeTypeFromExtension(inFile.getName().substring(x+1));
                        i.setType(s);
                        i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(f.getAbsolutePath()+"/"+inFile.getName())));
                        startActivity(Intent.createChooser(i,"Open file"));
                    }
                }).setNegativeButton("DISMISS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        }
        else
            Toast.makeText(this,s+" not successful please try again",Toast.LENGTH_SHORT).show();
        pd.dismiss();
    }

    private class CalBack implements AbsListView.MultiChoiceModeListener {
        boolean[] checked;
        int count=0;
        int previousCount=0;
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean check) {
            checked[position]=check;
            previousCount = count;
            if(check)
            count++;
            else
            count--;
            mode.setSubtitle(Integer.toString(count)+" Items selected ");
            if(previousCount == 1 && count == 2)
                mode.invalidate();
            else if(previousCount == 2 && count == 1)
                mode.invalidate();
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.multiple_select, menu);
            checked = new boolean[fileItems.size()];
            count = 0;
            for(int i =0;i<fileItems.size();i++)
            {
                checked[i]=false;
            }
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            if(count == 1 || count == 0)
            {
                MenuItem item = menu.findItem(R.id.action_single_share);
                item.setVisible(true);
            }
            else
            {
                MenuItem item = menu.findItem(R.id.action_single_share);
                item.setVisible(false);
            }
            return true;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
            int id = item.getItemId();
            if(id == R.id.action_multiple_decrypt)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Do you want to decrypt the selected file?");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<FileItem> items = new ArrayList<>();
                        for(int i=0;i<fileItems.size();i++)
                        {
                            if(checked[i])
                                items.add(fileItems.get(i));
                        }
                        try
                        {
                            DecryptMuitiple decryptMuitiple = new DecryptMuitiple(MainActivity.this,items,Password);
                            decryptMuitiple.execute();
                        }
                        catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                            e.printStackTrace();
                        }
                        mode.finish();
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return true;
            }
            else if(id == R.id.action_single_share)
            {
                int x=0;
                for(int i=0;i<fileItems.size();i++)
                {
                    if(checked[i])
                    {
                        x=i;
                        break;
                    }
                }
                FileItem f = fileItems.get(x);
                Intent i = new Intent(Intent.ACTION_SEND);
                x = f.getName().lastIndexOf(".");
                String s = MimeTypeMap.getSingleton().getMimeTypeFromExtension(f.getName().substring(x+1));
                i.setType(s);
                i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(f.getNewpath())));
                startActivity(Intent.createChooser(i,"Share file"));
                mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }
    }
}
