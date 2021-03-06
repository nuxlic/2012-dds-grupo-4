package ar.edu.frba.utn.dds.operaciones;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class Itinerario {
	private List<Asiento> asientos = new ArrayList<Asiento>();
	public List<Asiento> getAsientos() {
		return asientos;
	}

	public List<Reserva> getReservas(){
		List<Reserva> reservas = new ArrayList<Reserva>();
		for(Asiento unAsiento : this.getAsientos()){
			reservas.addAll(unAsiento.getReservas());
		}
		return reservas;
	}
	
	public void setAsientos(List<Asiento> asientos) {
		this.asientos = asientos;
	}
	
	public BigDecimal precioTotal(){
		BigDecimal precioTotal=new BigDecimal(0);
		for(Asiento unAsiento: this.getAsientos()){
			precioTotal = precioTotal.add(unAsiento.getPrecio());
		}
		return precioTotal;
	}
	
	public Long tiempoDeVuelo(){
		Long tiempo=new Long(0);

		for(int i=1;i<this.getAsientos().size();i++){
			tiempo+=this.getAsientos().get(i).getFechaLlegada().getFecha().getTime()-this.getAsientos().get(i).getFechaSalida().getFecha().getTime();
			tiempo+=(this.getAsientos().get(i).getFechaSalida().getFecha().getTime()-this.getAsientos().get(i-1).getFechaLlegada().getFecha().getTime());
		}
		
		return tiempo;
	}
	
	public Integer popularidad(){
		Integer popu= new Integer(0);
			for(Asiento unAsiento: this.getAsientos()){
				popu+=unAsiento.obtenerPopularidadDelVuelo();
			}
		return popu;
	}
	
	public boolean estaReservado(){
		
		for(Asiento unAsiento: this.getAsientos()){
			if(unAsiento.getEstado()!="R"){
				return false;
			}
		}
		return true;
	}
	
}
