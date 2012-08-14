package ar.edu.frba.utn.dds.entrega_3;

import java.util.Comparator;

import ar.edu.frba.utn.dds.entrega_2.Asiento;

public class CriterioPrecioDescendente implements Comparator<Asiento>{
	@Override
	public int compare(Asiento unAsiento, Asiento otroAsiento){
		return otroAsiento.getPrecio().compareTo(unAsiento.getPrecio());
	}
}