package ro.pub.cs.systems.pdsd.mypracticaltest2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import android.util.Log;

public class CommunicationThread extends Thread{
	private Socket socket;
	private ServerThread serverThread;

	public CommunicationThread(Socket socket, ServerThread serverThread) {
		this.socket = socket;
		this.serverThread = serverThread;
	}

	public void run() {
		try {
			Log.v("[CommunicationThread]", "Connection opened with " + socket.getInetAddress() + ":" + socket.getLocalPort());

			BufferedReader bufferedReader = Utilities.getReader(socket);
			PrintWriter printWriter = Utilities.getWriter(socket);
			
			if (bufferedReader != null && printWriter != null) {
				Log.i("[COMMUNICATION THREAD]", " Waiting for parameters from client (city / information type)!");
				String city            = bufferedReader.readLine();
				String informationType = bufferedReader.readLine();
				
				
				HttpClient httpClient = new DefaultHttpClient();
				
				String url = "http://www.wunderground.com/cgi-bin/findweather/getForecast?query=" + city;
				HttpGet httpGet = new HttpGet(url);
				ResponseHandler responseH = new BasicResponseHandler();
				String content = httpClient.execute(httpGet, responseH);
				
				Element htmlTag = Jsoup.parse(content);
				String general = htmlTag.getElementById("curCond").child(0).text();
				String temperature = htmlTag.getElementById("curTemp").child(0).text();
				String windSpeed = htmlTag.getElementById("windCompassSpeed").child(0).text();

				WeatherForecastInformation info = new WeatherForecastInformation();
				info.setTemperature(temperature);
				info.setWindSpeed(windSpeed);
				info.setCondition(general);
				
				String result = null;
				if (informationType.compareTo("all") == 0) {
					result = info.getCondition() + " " + info.getWindSpeed() + " " + info.getTemperature();
				}
				else if (informationType.compareTo("condition") == 0) {
					result = info.getTemperature();
				}
				else if (informationType.compareTo("temperature") == 0) {
					result = info.getCondition();
				}
				else if (informationType.compareTo("wind") == 0) {
					result = info.getWindSpeed();
				}
				
				printWriter.println(result);
				printWriter.flush();
				
			}
			
			socket.close();
			Log.v("[CommunicationThread]", "Connection closed");
		} catch (IOException ioException) {
			Log.e("[CommunicationThread]", "An exception has occurred: " + ioException.getMessage());
			ioException.printStackTrace();
		}
	}
}
