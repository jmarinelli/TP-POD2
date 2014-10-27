package ar.edu.itba.pod.mmxivii.sube.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;

import javax.annotation.Nonnull;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import ar.edu.itba.pod.mmxivii.sube.common.CardRegistry;
import ar.edu.itba.pod.mmxivii.sube.common.CardService;
import ar.edu.itba.pod.mmxivii.sube.service.exception.CardNotFoundException;
import ar.edu.itba.pod.mmxivii.sube.service.exception.InvalidRechargeException;
import ar.edu.itba.pod.mmxivii.sube.service.exception.InvalidTravelException;

public class CardServiceImpl extends UnicastRemoteObject implements CardService
{
	private static final JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");
	private static final long serialVersionUID = 2919260533266908792L;
	private static final long FLUSH_TIME = 10000l;
	
	@Nonnull
	private final CardRegistry cardRegistry;
	private final Jedis balances;

	public CardServiceImpl(@Nonnull CardRegistry cardRegistry) throws RemoteException
	{
		super(0);
		this.cardRegistry = cardRegistry;
		this.balances = pool.getResource();
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						Thread.sleep(FLUSH_TIME);
						CardServiceImpl.this.flushCache();
					} catch (InterruptedException | RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	@Override
	public double getCardBalance(@Nonnull UID id) throws RemoteException
	{
		Operation balance = Operation.fromJson(this.balances.get(getUIDAsString(id)));
		if (balance == null) {
			balance = getBalance(id);
		}
		return balance.getCurrent();
	}

	@Override
	public double travel(@Nonnull UID id, @Nonnull String description, double amount) throws RemoteException
	{
		if (amount >= 100 || amount < 1)
			throw new IllegalArgumentException();
		try {			
			return this.updateBalance(id, -amount).getCurrent();
		} catch (InvalidAmountException e) {
			throw new InvalidTravelException();
		}
	}

	@Override
	public double recharge(@Nonnull UID id, @Nonnull String description, double amount) throws RemoteException
	{
		if (amount >= 100 || amount < 1)
			throw new IllegalArgumentException();
		try {
			return this.updateBalance(id, amount).getCurrent();			
		} catch (InvalidAmountException e) {
			throw new InvalidRechargeException();
		}
	}
	
	public void flushCache() throws RemoteException {
		for (String id : this.balances.keys("*")) {
			try {
				Operation balance = Operation.fromJson(this.balances.get(id));
				Double result = this.cardRegistry.addCardOperation(this.getStringAsUID(id), "operation", balance.diff());
				
				// Si no hay errores lo borro, si no lo mantengo hasta que se solucionen
				
				if (result >= 0) {
					this.balances.del(id);
				}
			} catch (Exception e) {
				// Corrupted data in redis, deleting
				this.balances.del(id);
			}
		}
	}
	
	private Operation getBalance(UID id) throws RemoteException {
		Double originalBalance = cardRegistry.getCardBalance(id);
		if (originalBalance == CardRegistry.CARD_NOT_FOUND)
			throw new CardNotFoundException();
		Operation balance = new Operation(originalBalance, originalBalance, new Date());
		this.balances.set(getUIDAsString(id), balance.asJson());
		
		return balance;
	}
	
	private Operation updateBalance(UID id, Double amount) throws RemoteException, InvalidAmountException {
		System.out.println("updateo el cache para id: " + id + " amount: " + amount);
		Operation balance = Operation.fromJson(this.balances.get(getUIDAsString(id)));
		if (balance == null) {
			balance = getBalance(id);
		}
		synchronized (Operation.fromJson(this.balances.get(getUIDAsString(id)))) {
			balance.update(amount);
			this.balances.set(getUIDAsString(id), balance.asJson());
			return balance;
		}
	}
	
	private UID getStringAsUID(String id) {
		try {
			return UID.read(new DataInputStream(new ByteArrayInputStream(id.getBytes("ISO-8859-1"))));
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}
	
	private String getUIDAsString(UID id) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		
		DataOutput output = new DataOutputStream(stream);

		try {
			id.write(output);
		} catch (IOException e) {
			throw new RuntimeException();
		}
		
		try {
			return stream.toString("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}
}
