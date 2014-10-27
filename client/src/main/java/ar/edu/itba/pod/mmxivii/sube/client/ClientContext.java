package ar.edu.itba.pod.mmxivii.sube.client;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ar.edu.itba.pod.mmxivii.sube.common.Card;
import ar.edu.itba.pod.mmxivii.sube.common.CardClient;
import ar.edu.itba.pod.mmxivii.sube.service.exception.InvalidRechargeException;
import ar.edu.itba.pod.mmxivii.sube.service.exception.InvalidTravelException;

public class ClientContext {

	private CardClient cardClient = null;
	private List<Card> cards = new LinkedList<Card>();
	private Card currentCard = null;

	ClientContext(CardClient cardClient) {
		this.cardClient = cardClient;
	}

	void recharge(double value) throws RemoteException {
		if (currentCard == null) {
			System.out.println("Debe seleccionar una tarjeta");
			return;
		}
		try {
			cardClient.recharge(currentCard.getId(), "recargar", value);
		} catch (InvalidRechargeException e) {
			System.out.println(e.getMessage());
			return;
		}
		System.out.println("Recargado " + value);
	}

	void travel(double value) throws RemoteException {
		if (currentCard == null) {
			System.out.println("Debe seleccionar una tarjeta");
			return;
		}
		try {
			cardClient.travel(currentCard.getId(), "viajar", value);
		} catch (InvalidTravelException e) {
			System.out.println(e.getMessage());
			return;
		}
		System.out.println("Se ha viajado por " + value);
	}

	void showBalance() throws RemoteException {
		if (currentCard == null) {
			System.out.println("Debe seleccionar una tarjeta");
			return;
		}
		cardClient.getCardBalance(currentCard.getId());
	}

	void createCard(String cardHolder, String label) throws RemoteException {
		Card newCard = cardClient.newCard(cardHolder, label);
		System.out.println("Tarjeta creada, id: " + newCard.getId());
		cards.add(newCard);
		currentCard = newCard;
	}

	void showCards() {
		for (int i = 0; i < cards.size(); i++) {
			System.out.print((i + 1) + ") ");
			System.out.print(cards.get(i).getId() + ", "
					+ cards.get(i).getCardHolder());
			System.out.println();
		}
	}

	void selectCard(int index) {
		if (index >= cards.size()) {
			System.out.println("Tarjeta inexistente");
		} else {
			currentCard = cards.get(index - 1);
			System.out.println("Tarjeta " + currentCard.getId()
					+ " seleccionada");
		}
	}

	void startSimulation() throws RemoteException {
		System.out.println("Starting simulation...");
		Executor executors = Executors.newCachedThreadPool();
		
		int control = 0;
		
		this.cards.add(this.cardClient.newCard("sim1", "simulation 1"));
		this.cards.add(this.cardClient.newCard("sim2", "simulation 2"));
		this.cards.add(this.cardClient.newCard("sim3", "simulation 3"));
		this.cards.add(this.cardClient.newCard("sim4", "simulation 4"));
		this.cards.add(this.cardClient.newCard("sim5", "simulation 5"));
		while (true) {
			executors.execute(getCommand());
			if (control++ % 4 == 0) {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					throw new RuntimeException();
				}
			}
		}
	}
	
	private Runnable getCommand() {
		return new Runnable() {

			@Override
			public void run() {
				ClientContext parent = ClientContext.this;
				Card card = parent.getCard();
				try {
					double initialBalance = parent.cardClient.getCardBalance(card.getId());
					
					if (initialBalance > 0.01)
						parent.cardClient.travel(card.getId(), "viaje", initialBalance);
					
					parent.cardClient.recharge(card.getId(), "carga", 10.9);
					parent.cardClient.recharge(card.getId(), "carga", 20.1);
					parent.cardClient.travel(card.getId(), "viaje", 10);
					parent.cardClient.recharge(card.getId(), "carga", 20.1);
					parent.cardClient.recharge(card.getId(), "carga", 20.1);
					parent.cardClient.travel(card.getId(), "viaje", 30);
					
					double currentBalance = parent.cardClient.getCardBalance(card.getId());
					
					double amount = Math.floor((Math.random() * currentBalance) * 100) / 100;
					
					parent.cardClient.travel(card.getId(), "viaje", amount);
				} catch (RemoteException e) {
					throw new RuntimeException();
				}
			}
			
		};
	}
	
	private Card getCard() {
		return this.cards.get((int) Math.round(Math.random() * 4));
	}

}