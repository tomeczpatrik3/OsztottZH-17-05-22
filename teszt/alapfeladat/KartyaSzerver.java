package alapfeladat;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class KartyaSzerver {
	public static final String[] magyarKartya =
		{"P_7", "P_8", "P_9", "P_10", "P_11", "P_12", "P_13", "P_14",
		"Z_7", "Z_8", "Z_9", "Z_10", "Z_11", "Z_12", "Z_13", "Z_14",
		"M_7", "M_8", "M_9", "M_10", "M_11", "M_12", "M_13", "M_14",
		"T_7", "T_8", "T_9", "T_10", "T_11", "T_12", "T_13", "T_14"};
	
	public static final int players = 4;
	
	public static void main(String[] args) {
		//Pakli és keverés:
		List<String> deck = getShuffledDeck(magyarKartya);
		//pakli.forEach(kartya -> System.out.println(kartya));
		List<String> names = new ArrayList<>();
		List<Socket> clients = new ArrayList<>();
		Map<String, Scanner> scanners = new HashMap<>();
		Map<String, PrintWriter> printers = new HashMap<>();
		
		
		try (ServerSocket server = new ServerSocket(12345);)
		{
			for (int i =0; i<players; i++) {
				Socket client = server.accept();
				Scanner sc = new Scanner(client.getInputStream());
				PrintWriter pw = new PrintWriter(client.getOutputStream());
				
				String name = sc.nextLine();
			
				names.add(name);
				clients.add(client);
				scanners.put(name, sc);
				printers.put(name, pw);
			}
			
			//System.out.println("CLIENTS HAS CONNECTED");
			
			for (int i=0; i<deck.size(); i++){
				int playerNum = i%players;
				PrintWriter pw = printers.get(names.get(playerNum));
				pw.println(deck.get(i));
				pw.flush();
			}
			
			//System.out.println("CLIENTS HANDS ARE READY");
			
			PrintWriter pw = printers.get(names.get(0));
			pw.println("START");
			pw.flush();
					
			String receivedCard = "";
			String name = "";
			int i = 0;
			boolean gameOver = false;
			while (!gameOver) {
//				name = names.get(0);
//				Scanner scanner = scanners.get(names.get(0));
//				PrintWriter printer = printers.get(names.get(0));				
				
				name = names.get(i);
				Scanner scanner = scanners.get(names.get(i));
				PrintWriter printer = printers.get(names.get((i+1)%players));
				
				receivedCard = scanner.nextLine();
				
				if (receivedCard.equals("CSACSI")) {
					gameOver = true;
					for (String playerName: names) {
						if (!playerName.equals(name)) {
							printer = printers.get(playerName);
							printer.println("VEGE");
							printer.flush();
						}
					}
				}
				else {
					printer.println(receivedCard);
					printer.flush();
					i = (i+1)%players;				
				}
				
				System.out.println(String.format("%s: %s", name, receivedCard));	
			}
			
			for (Socket client: clients)
				client.close();
			
			//System.out.println("SZERVER: VÉGE");
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private static List<String> getShuffledDeck(String[] deck) {
		List<String> tempDeck = new ArrayList<>();
		for (String card: deck)
			tempDeck.add(card);
		
		java.util.Collections.shuffle(tempDeck, new Random(12345));
		return tempDeck;
	}
	
}
