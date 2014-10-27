package ar.edu.itba.pod.mmxivii.sube.balancer;

import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

import ar.edu.itba.pod.mmxivii.sube.common.CardService;
import ar.edu.itba.pod.mmxivii.sube.common.CardServiceRegistry;

public class CardServiceRegistryImpl extends UnicastRemoteObject implements CardServiceRegistry
{
	private static final long serialVersionUID = 2473638728674152366L;
	private final AtomicInteger index = new AtomicInteger(0);
	private final List<CardService> serviceList = Collections.synchronizedList(new LinkedList<CardService>());

	protected CardServiceRegistryImpl() throws RemoteException {}

	@Override
	public void registerService(@Nonnull CardService service) throws RemoteException
	{
		serviceList.add(service);
	}

	@Override
	public void unRegisterService(@Nonnull CardService service) throws RemoteException
	{
		serviceList.remove(service);
	}

	@Override
	public Collection<CardService> getServices() throws RemoteException
	{
		return serviceList;
	}

	CardService getCardService()
	{
		if (this.serviceList.isEmpty())
			throw new IllegalStateException("There are no registered services");
		synchronized (index) {
			int current = index.intValue();
			if (current >= this.serviceList.size()) {
				current = 0;
				index.set(current);
			}
			index.incrementAndGet();
			CardService candidate = this.serviceList.get(current);
			try {
				candidate.getCardBalance(new UID());
			} catch (RemoteException e) {
				this.serviceList.remove(current);
				this.getCardService();
			}
			return candidate;
		}
	}
}
