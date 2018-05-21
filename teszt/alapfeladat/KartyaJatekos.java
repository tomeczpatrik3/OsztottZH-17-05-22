package alapfeladat;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class KartyaJatekos {
	public static void main(String[] args) {
		String playerName = args[0];
		String fileName = args[1];

		List<String> hand = new ArrayList<>();

		try (Socket client = new Socket("localhost", 12345);
				Scanner sc = new Scanner(client.getInputStream());
				PrintWriter pw = new PrintWriter(client.getOutputStream());
				Scanner fileSc = new Scanner(new File(fileName));) {
			
			pw.println(playerName);
			pw.flush();
			
			for (int i = 0; i < 8; i++) {
				hand.add(sc.nextLine());
			}
			
			//hand.forEach(card -> System.out.println(card));

			boolean gameOver = false;
			String action;
			int number;
			while (sc.hasNextLine() && !gameOver) {
				action = sc.nextLine();
				switch (action) {
				case "START":
					//System.out.println("START");
					number = Integer.parseInt(fileSc.nextLine());
					pw.println(hand.get(number-1));
					pw.flush();
					hand.remove(number-1);
					break;
				case "VEGE":
					//System.out.println("VEGE");
					gameOver = true;
					//System.out.println("Játék vége, veszítettem!");
					break;
				default:
					//System.out.println(action.toUpperCase());
					hand.add(action);
					boolean csacsi = checkCards(hand);
					if (csacsi) {
						pw.println("CSACSI");
						pw.flush();
						gameOver = true;
					} else {
						number = Integer.parseInt(fileSc.nextLine());
						pw.println(hand.get(number-1));
						pw.flush();
						hand.remove(number-1);
					}
				}
			}
			
			//System.out.println("KLIENS: VÉGE");

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static boolean checkCards(List<String> cards) {
		boolean csacsi = false;
		
		int pCounter = 0;
		int zCounter = 0;
		int mCounter = 0;
		int tCounter = 0;
		
		for (int i =0; i<cards.size(); i++) {
			switch (cards.get(i).toUpperCase().charAt(0)) {
			case 'P': pCounter++; break;
			case 'Z': zCounter++; break;
			case 'M': mCounter++; break;
			case 'T': tCounter++; break;
			}
		}
		
		if (pCounter == 8 || zCounter == 8 || mCounter==8 || tCounter == 8)
			csacsi = true;
		
		return csacsi;
	}
}
