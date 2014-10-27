package ar.edu.itba.pod.mmxivii.sube.client;

import ar.edu.itba.pod.mmxivii.sube.common.BaseMain;
import ar.edu.itba.pod.mmxivii.sube.common.Card;
import ar.edu.itba.pod.mmxivii.sube.common.CardClient;
import ar.edu.itba.pod.mmxivii.sube.common.Utils;

import javax.annotation.Nonnull;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

import static ar.edu.itba.pod.mmxivii.sube.common.Utils.*;

public class Main extends BaseMain
{
	private CardClient cardClient = null;

	private Main(@Nonnull String[] args) throws NotBoundException
	{
		super(args, DEFAULT_CLIENT_OPTIONS);
		getRegistry();
		cardClient = Utils.lookupObject(CARD_CLIENT_BIND);
	}

	public static void main(@Nonnull String[] args ) throws Exception
	{
		final Main main = new Main(args);
		main.run();
	}

	private void run() throws RemoteException
	{
		System.out.println("Main.run");
		final Card card = cardClient.newCard("alumno", "tarjeta");
//		final double primero = cardClient.recharge(card.getId(), "primero", 100);
//		System.out.println("primero = " + primero);
//		final double bondi = cardClient.travel(card.getId(), "bondi", 3);
//		System.out.println("bondi = " + bondi);
//		final double segundo = cardClient.recharge(card.getId(), "primero", 2);
//		System.out.println("primero = " + segundo);
//		final double bondi2 = cardClient.travel(card.getId(), "bondi", 3);
//		System.out.println("bondi = " + bondi2);
		
		final Scanner scan = new Scanner(System.in);
		String line;
		do {
			line = scan.next();
			
			String[] giladitas = line.split("-");
			if ("recargar".equals(giladitas[0])) {
				cardClient.recharge(card.getId(), "recargar", Double.valueOf(giladitas[1]));
			} else if ("viajar".equals(giladitas[0])) {
				cardClient.travel(card.getId(), "recargar", Double.valueOf(giladitas[1]));
			} else if ("balance".equals(giladitas[0])) {
				System.out.println(cardClient.getCardBalance(card.getId()));
			}
		} while(!"x".equals(line));
		scan.close();
//		cardClient.newCard()
	}
}
