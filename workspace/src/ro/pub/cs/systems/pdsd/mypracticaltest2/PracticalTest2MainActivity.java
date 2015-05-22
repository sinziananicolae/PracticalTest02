package ro.pub.cs.systems.pdsd.mypracticaltest2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

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
		return new BufferedReader(
				new InputStreamReader(socket.getInputStream()));
	}

	public static PrintWriter getWriter(Socket socket) throws IOException {
		return new PrintWriter(socket.getOutputStream(), true);
	}

}

public class PracticalTest2MainActivity extends Activity {

	private EditText serverPortEditText;
	private Button serverConnectButton;
	private EditText clientAddressEditText;
	private EditText clientPortEditText;
	private EditText cityEditText;
	private Spinner infoTypeSpinner;
	private Button getWeatherInfoButton;
	private TextView showWeatherTextView;

	private ServerThread serverThread;
	private ClientThread clientThread;

	private ServerConnectButtonListener serverConnectButtonListener = new ServerConnectButtonListener();

	private class ServerConnectButtonListener implements Button.OnClickListener {

		@Override
		public void onClick(View view) {
			String serverPort = serverPortEditText.getText().toString();
			Log.d("TAG", "Am apasat buton server " + serverPort);
			if (serverPort == null || serverPort.isEmpty()) {
				Toast.makeText(getApplicationContext(),
						"Server port should be filled!", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			
			serverThread = new ServerThread();
			serverThread.startServer(Integer.parseInt(serverPort));
		}
	}

	private GetWeatherButtonClickListener getWeatherButtonClickListener = new GetWeatherButtonClickListener();

	private class GetWeatherButtonClickListener implements
			Button.OnClickListener {

		@Override
		public void onClick(View view) {
			String clientAddress = clientAddressEditText.getText().toString();
			String clientPort = clientPortEditText.getText().toString();

			if (clientAddress == null || clientAddress.isEmpty()
					|| clientPort == null || clientPort.isEmpty()) {
				Toast.makeText(getApplicationContext(),
						"Client port and address should be filled!",
						Toast.LENGTH_SHORT).show();
				return;
			}

			String city = cityEditText.getText().toString();
			String informationType = infoTypeSpinner.getSelectedItem()
					.toString();

			if (city == null || city.isEmpty() || informationType == null
					|| informationType.isEmpty()) {
				Toast.makeText(
						getApplicationContext(),
						"Parameters from client (city / information type) should be filled!",
						Toast.LENGTH_SHORT).show();
				return;
			}
			
			clientThread = new ClientThread(clientAddress, Integer.parseInt(clientPort), city, informationType, showWeatherTextView);
			clientThread.start();
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_practical_test2_main);

		serverPortEditText = (EditText) findViewById(R.id.server_port_edit_text);
		serverConnectButton = (Button) findViewById(R.id.connect_button);
		clientAddressEditText = (EditText) findViewById(R.id.client_address_edit_text);
		clientPortEditText = (EditText) findViewById(R.id.client_port_edit_text);
		cityEditText = (EditText) findViewById(R.id.city_edit_text);
		infoTypeSpinner = (Spinner) findViewById(R.id.information_type_spinner);
		getWeatherInfoButton = (Button) findViewById(R.id.get_weather_forecast_button);
		showWeatherTextView = (TextView) findViewById(R.id.weather_forecast_text_view);

		serverConnectButton.setOnClickListener(serverConnectButtonListener);
		getWeatherInfoButton.setOnClickListener(getWeatherButtonClickListener);
	}
	
	@Override
	protected void onDestroy() {
		if (serverThread != null) {
			serverThread.stopServer();
		}
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.practical_test2_main, menu);
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
}
