package restart.management.accelerator.lambda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

import com.amazonaws.greengrass.dao.DefaultEmiYieldFactDao;
import com.amazonaws.greengrass.dao.EmiYieldFactDao;
import com.amazonaws.greengrass.pojo.Component;
import com.amazonaws.greengrass.pojo.ComponentStatistic;
import com.amazonaws.greengrass.pojo.ManagementData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * 
 * @author Dinesh
 *
 */
public class FrameJSONPacketFromMachineData {

	/*- To Test as a local application
	public static void main(String[] args) throws ParseException {
		frameJson(null, new Date(), "09091600");
	}
	*/

	private final static EmiYieldFactDao yieldFactDao = DefaultEmiYieldFactDao.instance;

	public FrameJSONPacketFromMachineData() {
		super();
	}

	/**
	 * Request data from socket connection
	 * 
	 * @return
	 * @throws Exception
	 */
	public static List<String> getOutputStreamFromSocket() {
		Socket socket = null;

		String host = "10.0.3.164";
		int port = 6549;
		BufferedReader br = null;
		List<String> data = new ArrayList<>();
		try {
			// InetAddress address = InetAddress.getByName(host);
			socket = new Socket(host, port);

			DateFormat sdf = new SimpleDateFormat("MMddHHmm");

			String trpTime = sdf.format(org.apache.commons.lang3.time.DateUtils.addMinutes(new Date(), -31));
			
			
			DateFormat sdfTrp = new SimpleDateFormat("MMddHHmm");
			Date currentTrpDate = sdfTrp.parse(trpTime);

			System.out.println("Current TRP date: " + currentTrpDate);

			String dbLatestTrp = yieldFactDao.getLatestTrpTime();

			Date latestTrpDate = sdfTrp.parse(dbLatestTrp);

			System.out.println("DB Latest TRP date: " + latestTrpDate);
			System.out.println("XYZ: " + DateUtils.addMinutes(currentTrpDate, 55));

			boolean isDate = isDateInBetweenIncludingEndPoints(currentTrpDate, DateUtils.addMinutes(currentTrpDate, 50),
					latestTrpDate);

			if (isDate) {
				trpTime = dbLatestTrp;
			}

			System.out.println("Date is in between : " + isDate);

			// Send the message to the server
			PrintWriter os = new PrintWriter(socket.getOutputStream(), true);
			System.out.println("IP: " + host);
			String sendMessage = "GetData -s " + trpTime + " Management";
			os.println(sendMessage);
			System.out.println("Message sent to the server : " + sendMessage);
			System.out.println("Application was deployed");

			// Get the return message from the server
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>> Reading machine response from : " + br);
			data = frameJson(br, new Date(), trpTime);
		} catch (Exception e) {
			System.out
					.println(">>>>>>>>>>>>>>>>>>>>>>>>>> Exception occurred while Reading Machine Data. ErrorMessage: "
							+ e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (socket != null && !socket.isClosed()) {
					socket.close();
				}
			} catch (Exception e) {
				System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>> Exception occurred while closing socket. ErrorMessage: "
						+ e.getMessage());
				e.printStackTrace();
			}
		}
		return data;
	}

	/**
	 * Frame JSON Packet from BufferedReader
	 * 
	 * @param bur
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws Exception
	 */
	public static List<String> frameJson(BufferedReader br, Date date, String trp) throws ParseException {

		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

		List<String> eventStart = new ArrayList<>();
		ManagementData start = new ManagementData();
		List<Component> coList = null;
		String st = null;

		try {
			st = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error occured while reading from bufferedReader : " + e);
		}

		while (st != null && !"E".equals(st.trim())) {

			int flag = 0;
			boolean isMd05Available = true;

			if (st.startsWith(">MD00")) {
				String[] md00Arr = st.split(" ", 2);
				start.setLayoutName(md00Arr[1]);
				start.setTrpTime(trp);
				start.setDate(date);
			} else if (st.startsWith(">MD01")) {
				String[] md01Arr = st.split(" ");
				start.setMachineName(md01Arr[1]);
			} else if (st.startsWith(">MD02")) {
				String[] md02Arr = st.split(" ");
				start.setAssembledLayouts(md02Arr[1]);
			} else if (st.startsWith(">MD03")) {
				String[] md03Arr = st.split(" ");
				start.setAssembledBoards(md03Arr[1]);
			} else if (st.startsWith(">MD04")) {
				String[] md04Arr = st.split(" ");
				start.setAssemblyTotalTime(md04Arr[1]);
				start.setAssemblyDispenseTime(md04Arr[2]);
				start.setAssemblyTime(md04Arr[3]);

				try {
					st = br.readLine();
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Error occured while reading from bufferedReader : " + e);
				}

				// Check is for after to MD04 is MD05 available or not.
				if (st.startsWith(">MD00")) {
					isMd05Available = false;
				}

				String componenetName = null;
				if (st != null && (st.equals("E") || st.startsWith(">MD00") || st.startsWith(">MD05")
						|| st.startsWith(">MD06") || st.startsWith(">MD07"))) {

					if (st.startsWith(">MD00")) {
						coList = null;
					} else {
						coList = new ArrayList<>();
					}

					while ((st != null && !st.equals("E") && isMd05Available)
							&& (st.startsWith(">MD05") || st.startsWith(">MD06") || st.startsWith(">MD07"))) {

						Component co = new Component();
						ComponentStatistic cs = new ComponentStatistic();

						if (st != null && st.startsWith(">MD05")) {
							String md05Arr[] = st.split(" ", 2);
							componenetName = md05Arr[1];
						} else if (st != null && st.startsWith(">MD06")) {
							String[] md06Arr = st.split(" ");
							cs.setPlaced(md06Arr[1]);
							cs.setTime(md06Arr[2]);
							cs.setBadDimension(md06Arr[3]);
							cs.setBadElectric(md06Arr[4]);
							cs.setBadPicked(md06Arr[5]);
							cs.setBadPlaced(md06Arr[6]);
							cs.setBadOther(md06Arr[7]);
							try {
								st = br.readLine();
							} catch (IOException e) {
								e.printStackTrace();
								System.out.println("Error occured while reading from bufferedReader : " + e);
							}
							if (st != null && st.startsWith(">MD07")) {
								String[] arr3 = st.split(" ");
								co.setComponentName(componenetName);
								co.setDispensed(arr3[1]);
								co.setTime(arr3[2]);
								coList.add(co);
							}
							co.setComponentStatistic(cs);
						}
						try {
							st = br.readLine();
						} catch (IOException e) {
							e.printStackTrace();
							System.out.println("Error occured while reading from bufferedReader : " + e);
						}
						flag = 1;
					}

					if (coList != null && !coList.isEmpty()) {
						start.setComponent(coList);
					} else {
						start.setComponent(null);
					}

					String responsestart = null;
					try {
						responsestart = ow.writeValueAsString(start);
					} catch (JsonProcessingException e) {
						e.printStackTrace();
						System.out.println("Error occured while parsing into JSON : " + e);
					}
					eventStart.add(responsestart);
				}
			}

			if (flag == 0 && isMd05Available) {
				try {
					st = br.readLine();
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Error occured while reading from bufferedReader : " + e);
				}
			}
		}

		return eventStart;
	}

	/**
	 * Check the given date in the range
	 * 
	 * @param min
	 * @param max
	 * @param date
	 * @return
	 */
	public static boolean isDateInBetweenIncludingEndPoints(final Date min, final Date max, final Date date) {
		return date.after(min) && date.before(max);
	}

}