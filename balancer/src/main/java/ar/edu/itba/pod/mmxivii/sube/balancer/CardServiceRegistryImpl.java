package ar.edu.itba.pod.mmxivii.sube.balancer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

import ar.edu.itba.pod.mmxivii.sube.common.CardService;
import ar.edu.itba.pod.mmxivii.sube.common.CardServiceRegistry;

public class CardServiceRegistryImpl extends UnicastRemoteObject implements CardServiceRegistry
{
	private static final long serialVersionUID = 2473638728674152366L;
	private final List<CardService> serviceList = Collections.synchronizedList(new ArrayList<CardService>());

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
		double random = Math.random();
		if (random > 0.5)
			return serviceList.get(1);
		else
			return serviceList.get(0);
	}
}
