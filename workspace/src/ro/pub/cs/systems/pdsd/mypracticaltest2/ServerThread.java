package ro.pub.cs.systems.pdsd.mypracticaltest2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;

public class ServerThread extends Thread{
	private boolean isRunning;
	private ServerSocket serverSocket;
	private int port;
	public WeatherForecastInformation weatherInfo;

	public void startServer(int port) {
		this.isRunning = true;
		this.port = port;
		this.weatherInfo = new WeatherForecastInformation();
		start();
	}

	public void stopServer() {
		isRunning = false;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (serverSocket != null) {
						serverSocket.close();
					}
					Log.v("[ServerThread]", "stopServer() method invoked "
							+ serverSocket);
				} catch (IOException ioException) {
					Log.e("[ServerThread]", "An exception has occurred: "
							+ ioException.getMessage());
					ioException.printStackTrace();
				}
			}
		}).start();
	}

	public void run() {
		try {
			serverSocket = new ServerSocket(port);
			while (isRunning) {
				Log.i("[ServerThread]", "Waiting for a connection...");
				
				Socket socket = serverSocket.accept();
				
				Log.i("[ServerThread]", "A connection request was received from " + socket.getInetAddress() + ":" + socket.getLocalPort());
				
				new CommunicationThread(socket, ServerThread.this).start();
			}
		} catch (IOException ioException) {
			Log.e("[ServerThread]",
					"An exception has occurred: "
							+ ioException.getMessage());
			ioException.printStackTrace();
		}
	}
}
