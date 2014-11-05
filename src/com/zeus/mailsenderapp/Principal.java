package com.zeus.mailsenderapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.mail.MessagingException;

import org.apache.http.auth.AuthenticationException;

import ru.telepuzinator.gmaillibrary.GmailOauthSender;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.zeus.mailsenderapp.R.id;

public class Principal extends ActionBarActivity {

	Button btnEscolherConta, btnEnviarEmail, btnSalvarArquivo, btnLerArquivo;
	
	private int SOME_REQUEST_CODE = 0;
    String accountName = "";
    String mtoken = "";
    String mScope = "";
    
    private String file = "arquivo";
	private String data = "data files and others data images";

	protected String path = "";
	protected Bitmap bitmap;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.principal);
		
		BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.zeus_menor);
		bitmap = drawable.getBitmap();
		
//		bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.zeus);
		btnEscolherConta = (Button) findViewById(id.btn_escolher_conta);
		btnEnviarEmail = (Button) findViewById(id.btn_enviar_email);
		btnSalvarArquivo = (Button) findViewById(id.btn_salvar_arquivo);
		btnLerArquivo = (Button) findViewById(id.btn_ler_arquivo);
		
		btnEscolherConta.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				escolherConta();
			}
		});
		
		btnEnviarEmail.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new SendMailTask().execute("");
			}
		});
		
		btnSalvarArquivo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				saveImageExternalStorage();
				path = saveImageInternalStorage(bitmap);
//				saveFile();
//				System.out.println(path);
			}
		});
		
		btnLerArquivo.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						loadImageInternalStorage(path);
//						loadImageExtenalStorage();
//						read();
//						readFile();
					}
				});
			}

	
	private void escolherConta() {
		Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[] { GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE }, true,
				null, null, null, null);
		startActivityForResult(intent, SOME_REQUEST_CODE);

	}
	
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (requestCode == SOME_REQUEST_CODE && resultCode == RESULT_OK) {
			accountName = "";
			mtoken = "";
			accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
			new RetrieveTokenTask().execute(accountName);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.principal, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private static final String TAG = "RetrieveAccessToken";
	private static final int REQ_SIGN_IN_REQUIRED = 55664;

	private class RetrieveTokenTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String scopes = "oauth2:https://mail.google.com/";
			String token = null;
			try {
				token = GoogleAuthUtil.getToken(getApplicationContext(),
						params[0], scopes);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			} catch (UserRecoverableAuthException e) {
				startActivityForResult(e.getIntent(), REQ_SIGN_IN_REQUIRED);
			} catch (GoogleAuthException e) {
				Log.e(TAG, e.getMessage());
			}
			return token;
		}

		@Override
		protected void onPostExecute(String token) {
			super.onPostExecute(token);
			mtoken = token;
		}
	}
	
	public void saveFile() {
		try {
			FileOutputStream fOut = openFileOutput(file, MODE_PRIVATE);
			fOut.write(data.getBytes());
			fOut.close();
			Toast.makeText(getBaseContext(), "file saved", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void read() {
		try {
			FileInputStream fin = openFileInput(file);
			int c;
			String temp = "";
			while ((c = fin.read()) != -1) {
				temp = temp + Character.toString((char) c);
			}
			System.out.println(temp);
			Toast.makeText(getBaseContext(), "file read", Toast.LENGTH_SHORT).show();

		} catch (Exception e) {

		}
	}
	
	private String saveImageInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
         // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, "profile.jpg");

        FileOutputStream fos = null;
        try {           

            fos = new FileOutputStream(mypath);

       // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            File parent = directory.getParentFile();
            if (parent != null) parent.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        path = directory.getAbsolutePath();
        System.out.println(path);
        return path;
    }
	
	private void saveImageExternalStorage() {
		File sdCardDirectory = Environment.getExternalStorageDirectory();
		File image = new File(sdCardDirectory, "test.png");
		image.getParentFile().mkdirs();
		boolean success = false;

	    // Encode the file as a PNG image.
	    FileOutputStream outStream;
	    try {
	        outStream = new FileOutputStream(image);
	        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);  /* 100 to keep full quality of the image */
	        outStream.flush();
	        outStream.close();
	        success = true;
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    
	    if (success) {
	    	path = sdCardDirectory.getAbsolutePath() + "/test.png";
	    	System.out.println(sdCardDirectory.getAbsolutePath());
	        Toast.makeText(getApplicationContext(), "Image saved with success",
	                Toast.LENGTH_LONG).show();
	    } else {
	        Toast.makeText(getApplicationContext(),
	                "Error during image saving", Toast.LENGTH_LONG).show();
	    }
	}
	
	private Bitmap loadImageInternalStorage(String path) {
		
		Bitmap bitmap = null;
		try {
			File file = new File(path, "profile.jpg");
			bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
			ImageView img = (ImageView) findViewById(R.id.imgPicker);
			img.setImageBitmap(bitmap);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return bitmap;

	}
	
	private Bitmap loadImageExtenalStorage() {
		try {
			final File file = new File(Environment
					.getExternalStorageDirectory().getAbsolutePath(),
					"test.png");
			bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
			ImageView img = (ImageView) findViewById(R.id.imgPicker);
			img.setImageBitmap(bitmap);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return bitmap;
	}
	
	private class SendMailTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
	        try {
	        	GmailOauthSender sender = new GmailOauthSender();
	        	sender.sendMail("test", "<H1>Hello</H1><img src=\"cid:image\" <H1>Hello</H1> <H1>Hello</H1><H1>Hello</H1> <H1>Hello</H1><H1>Hello</H1> <H1>Hello</H1>", accountName, mtoken, accountName, path + "/profile.jpg");
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			} catch (AuthenticationException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
	        
	        return "sucesso";
		}

		@Override
		protected void onPostExecute(String resposta) {
			super.onPostExecute(resposta);
			System.out.println(resposta);
		}
	}
	
	
}
