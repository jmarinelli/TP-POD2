package ar.edu.itba.pod.mmxivii.sube.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

import javax.annotation.Nonnull;

import ar.edu.itba.pod.mmxivii.sube.common.BaseMain;
import ar.edu.itba.pod.mmxivii.sube.common.CardClient;
import ar.edu.itba.pod.mmxivii.sube.common.Utils;

public class Main extends BaseMain {

	private final ClientContext context;

	private Main(@Nonnull String[] args) throws NotBoundException {
		super(args, DEFAULT_CLIENT_OPTIONS);
		getRegistry();
		this.context = new ClientContext(
				(CardClient) Utils.lookupObject(Utils.CARD_CLIENT_BIND));
	}

	public static void main(@Nonnull String[] args) throws Exception {
		final Main main = new Main(args);
		main.run();
	}

	private void run() throws RemoteException {

		final Scanner scan = new Scanner(System.in);
		scan.useDelimiter("\n");
		String line;
		while (true) {
			line = scan.next();
			if (line != null) {
				String[] command = line.split(" ");
				try {
					switch (command[0]) {
					case "recargar":
						this.context.recharge(Double.valueOf(command[1]));
						break;
					case "viajar":
						this.context.travel(Double.valueOf(command[1]));
						break;
					case "balance":
						this.context.showBalance();
						break;
					case "new":
						this.context.createCard(command[1], command[2]);
						break;
					case "cards":
						this.context.showCards();
						break;
					case "use":
						this.context.selectCard(Integer.valueOf(command[1]));
						break;
					case "quit":
						scan.close();
						System.out.println("Adiós");
						return;
					default:
						System.out.println("Comando inválido");
						break;
					}
				} catch (Exception e) {
					System.out.println("Comando inválido");
				}
			}
		}
	}
}
