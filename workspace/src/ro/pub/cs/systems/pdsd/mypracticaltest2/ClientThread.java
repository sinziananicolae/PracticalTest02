package ro.pub.cs.systems.pdsd.mypracticaltest2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;
import android.widget.TextView;

public class ClientThread extends Thread {
	private String address;
	private int port;
	private Socket socket;
	private String city;
	private String informationType;
	private TextView showWeatherTextView;
	
	public ClientThread(String address, int port, String city, String informationType, TextView showWeatherTextView) {
		this.address = address;
		this.port  = port;
		this.city = city;
		this.informationType = informationType;
		this.showWeatherTextView = showWeatherTextView;
	}
	
	@Override
	public void run() {
		try {
			socket = new Socket(address, port);
			
			if (socket == null) {
				Log.e("[ClientThread]", "Could not create socket!");
			}
			Log.d("[ClientThread]", "Client is running on address and port: " + address + " " + port);
			
			BufferedReader bufferedReader = Utilities.getReader(socket);
			PrintWriter printWriter = Utilities.getWriter(socket);
			
			printWriter.println(city);
			printWriter.flush();
			printWriter.println(informationType);
			printWriter.flush();
			String value;
			while((value = bufferedReader.readLine()) != null) {
				final String ceva = value;
				showWeatherTextView.post(new Runnable() {
					
					@Override
					public void run() {
						showWeatherTextView.setText(ceva);
						
					}
				});
			}
			
			
			
			socket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
