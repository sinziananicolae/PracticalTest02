package ro.pub.cs.systems.pdsd.practicaltest02;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

class Utilities {
	
	public static BufferedReader getReader(Socket socket) throws IOException {
		return new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}
	
	public static PrintWriter getWriter(Socket socket) throws IOException {
		return new PrintWriter(socket.getOutputStream(), true);
	}

}


public class PracticalTest02MainActivity extends Activity {

	private EditText serverPortEditText;
	private Button connectButton;
	
	private EditText clientAddressEditText;
	private EditText clientPortEditText;
	private EditText cityEditText;
	private Spinner informationTypeSpinner;
	private Button getWeatherForecastButton;
	private TextView weatherForecastTextView;
	
	private ServerThread serverThread;
	private ClientThread clientThread;
	
	class Meteo {
		String condition;
		String temperature;
		String wind;
		String pressure;
		String humidity;
		
		public Meteo (String condition, String temperature, String wind, String pressure, String humidity){
			this.temperature = temperature;
			this.wind   = wind;
			this.condition   = condition;
			this.pressure    = pressure;
			this.humidity    = humidity;
		}
	}
	
	private ConnectButtonListener connectButtonListener = new ConnectButtonListener();
	private class ConnectButtonListener implements Button.OnClickListener {

		@Override
		public void onClick(View view) {
			String serverPort = serverPortEditText.getText().toString();
			Log.d("TAG", "Am apasat buton server " + serverPort);
			if(serverPort == null || serverPort.isEmpty()){
				Toast.makeText(
						getApplicationContext(),
						"Server port should be filled!",
						Toast.LENGTH_SHORT
					).show();
					return;
			}
			
			serverThread = new ServerThread(Integer.parseInt(serverPort));
			if (serverThread.getServerSocket() != null) {
				serverThread.start();
			} else {
				Log.e("TAG", "[MAIN ACTIVITY] Could not creat server thread!");
			}
		}
		
	}
	
	private GetWeatherButtonClickListener getWeatherButtonClickListener = new GetWeatherButtonClickListener();
	private class GetWeatherButtonClickListener implements Button.OnClickListener {

		@Override
		public void onClick(View view) {
			String clientAddress = clientAddressEditText.getText().toString();
			String clientPort = clientPortEditText.getText().toString();
			
			if (clientAddress == null || clientAddress.isEmpty() || clientPort == null || clientPort.isEmpty()) {
				Toast.makeText(
						getApplicationContext(),
						"Client port and address should be filled!",
						Toast.LENGTH_SHORT
					).show();
					return;
			}
			
			if (serverThread == null || !serverThread.isAlive()) {
				Log.e("TAG", "[MAIN ACTIVITY] There is no server to connect to!");
				return;
			}
			
			String city = cityEditText.getText().toString();
			String informationType = informationTypeSpinner.getSelectedItem().toString();
			
			if (city == null || city.isEmpty() || informationType == null || informationType.isEmpty()) {
					Toast.makeText(
						getApplicationContext(),
						"Parameters from client (city / information type) should be filled!",
						Toast.LENGTH_SHORT
					).show();
					return;
				}
			
			weatherForecastTextView.setText("");
			
			clientThread = new ClientThread(
					clientAddress,
					Integer.parseInt(clientPort),
					city,
					informationType,
					weatherForecastTextView);
			clientThread.start();
		}
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_practical_test02_main);
		
		serverPortEditText = (EditText)findViewById(R.id.server_port_edit_text);
		connectButton = (Button)findViewById(R.id.connect_button);
		connectButton.setOnClickListener(connectButtonListener);
		
		clientAddressEditText = (EditText)findViewById(R.id.client_address_edit_text);
		clientPortEditText = (EditText)findViewById(R.id.client_port_edit_text);
		cityEditText = (EditText)findViewById(R.id.city_edit_text);
		informationTypeSpinner = (Spinner)findViewById(R.id.information_type_spinner);
		getWeatherForecastButton = (Button)findViewById(R.id.get_weather_forecast_button);
		getWeatherForecastButton.setOnClickListener(getWeatherButtonClickListener);
		weatherForecastTextView = (TextView)findViewById(R.id.weather_forecast_text_view);
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.practical_test02_main, menu);
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
	
	protected void onDestroy() {
	      if (serverThread != null) {
	        serverThread.stopServer();
	      }
	      super.onDestroy();
	}
	
	private class ServerThread extends Thread {
		private int port;
		private ServerSocket serverSocket;
		private HashMap<String, Meteo> data = null;
		
		public ServerThread(int port) {
			this.port = port;
			try {
				this.serverSocket = new ServerSocket(port);
			} catch (IOException ioException) {
				Log.e("TAG", "An exception has occurred: " + ioException.getMessage());
					ioException.printStackTrace();
			}
			this.data = new HashMap<String, Meteo>();
		}
		
		public ServerSocket getServerSocket() {
			return serverSocket;
		}
		
		public synchronized HashMap<String, Meteo> getData() {
			return data;
		}
		
		public synchronized void setData(String city, Meteo meteo) {
			this.data.put(city, meteo);
		}
		
		public void stopServer() {
			if (serverSocket != null) {
				interrupt();
				try {
					serverSocket.close();
				} catch (IOException e) {
					Log.e("TAG", "An exception has occurred: " + e.getMessage());	
					e.printStackTrace();
				}
			}
		}
		
		@Override
		public void run(){
			try {
				while (!Thread.currentThread().isInterrupted()) {
					Log.i("[SERVER]", "Waiting for a connection...");
					Socket socket = serverSocket.accept();
					Log.i("[SERVER]", "A connection request was received from " + socket.getInetAddress() + ":" + socket.getLocalPort());
					CommunicationThread communicationThread = new CommunicationThread(this, socket);
					communicationThread.start();
				}	
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class ClientThread extends Thread {
		private String address;
		private int port;
		private String city;
		private String informationType;
		private TextView weatherForecastTextView;
		
		private Socket socket;
		
		public ClientThread(
				String address,
				int port,
				String city,
				String informationType,
				TextView weatherForecastTextView) {
			this.address                 = address;
			this.port                    = port;
			this.city                    = city;
			this.informationType         = informationType;
			this.weatherForecastTextView = weatherForecastTextView;
		}
		
		@Override
		public void run() {
			try {
				socket = new Socket(address, port);
				if (socket == null) {
					Log.e("[CLIENT THREAD]", "Could not create socket!");
				}
				Log.d("[CLIENT THREAD]", "Client is running on address and port: " + address + " " + port);
				
				BufferedReader bufferedReader = Utilities.getReader(socket);
				PrintWriter printWriter = Utilities.getWriter(socket);
				if (bufferedReader != null && printWriter != null) {
					printWriter.println(city);
					printWriter.flush();
					printWriter.println(informationType);
					printWriter.flush();
					String weatherInformation;
					while ((weatherInformation = bufferedReader.readLine()) != null) {
						final String finalizedWeatherInformation = weatherInformation;
						weatherForecastTextView.post(new Runnable() {
							@Override
							public void run() {
								weatherForecastTextView.append(finalizedWeatherInformation + "\n");
							}
						});
					}
				} else {
					Log.e("[CLIENT THREAD]", "BufferedReader / PrintWriter are null!");
				}
				
				socket.close();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	private class CommunicationThread extends Thread {
		private ServerThread serverThread;
		private Socket socket;
		
		public CommunicationThread(ServerThread serverThread, Socket socket) {
			this.serverThread = serverThread;
			this.socket = socket;
		}
		
		@Override
		public void run() {
			if (socket != null) {
				try {
					BufferedReader bufferedReader = Utilities.getReader(socket);
					PrintWriter printWriter    = Utilities.getWriter(socket);
					if (bufferedReader != null && printWriter != null) {
						Log.i("[COMMUNICATION THREAD]", "Waiting for parameters from client (city / information type)!");
						String city = bufferedReader.readLine();
						String informationType = bufferedReader.readLine();
						HashMap<String, Meteo> data = serverThread.getData();
						Meteo meteo = null;
						
						if (city != null && !city.isEmpty() && informationType != null && !informationType.isEmpty()) {
							if (data.containsKey(city)) {
								Log.i("[COMMUNICATION THREAD]", "Getting the information from the cache...");
								meteo = data.get(city);
							} else {
								Log.i("[COMMUNICATION THREAD]", "Getting the information from the webservice...");
								
								HttpClient httpClient = new DefaultHttpClient();
								
								String url = "http://www.wunderground.com/cgi-bin/findweather/getForecast?query=" + city;
							
								HttpGet httpGet = new HttpGet(url);
								ResponseHandler handler = new BasicResponseHandler();
							
								String content = httpClient.execute(httpGet, handler);
								JSONObject content2 = new JSONObject(content);
								
								JSONObject currentObservation = content2.getJSONObject("current_observation");
								String temperature = currentObservation.getString("temperature");
								String windSpeed = currentObservation.getString("wind_speed");
								String condition = currentObservation.getString("condition");
								String pressure = currentObservation.getString("pressure");
								String humidity = currentObservation.getString("humidity");
								
								meteo = new Meteo(
										temperature,
										windSpeed,
										condition,
										pressure,
										humidity);

								serverThread.setData(city, meteo);
							}
							
							if (informationType.compareTo("all") == 0) {
								printWriter.println(meteo.condition);
								printWriter.println(meteo.temperature);
								printWriter.println(meteo.wind);
								printWriter.println(meteo.pressure);
								printWriter.println(meteo.humidity);
							}
							if (informationType.compareTo("condition") == 0) {
								printWriter.println(meteo.condition);
							}
							if (informationType.compareTo("temperature") == 0) {
								printWriter.println(meteo.temperature);
							}
							if (informationType.compareTo("wind") == 0) {
								printWriter.println(meteo.wind);
							}
							if (informationType.compareTo("pressure") == 0) {
								printWriter.println(meteo.pressure);
							}
							if (informationType.compareTo("humidity") == 0) {
								printWriter.println(meteo.humidity);
							}
							printWriter.println("end");
							socket.close();
						}
						
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	}
}
