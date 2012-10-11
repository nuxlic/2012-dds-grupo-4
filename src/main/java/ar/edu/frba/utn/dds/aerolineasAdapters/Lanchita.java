package ar.edu.frba.utn.dds.aerolineasAdapters;

import java.util.ArrayList;
import java.util.List;



import ar.edu.frba.utn.dds.fechas.Fecha;
import ar.edu.frba.utn.dds.operaciones.Asiento;
import ar.edu.frba.utn.dds.operaciones.Itinerario;
import ar.edu.frba.utn.dds.operaciones.Vuelo;
import ar.edu.frba.utn.dds.usuarios.Usuario;

import com.lanchita.AerolineaLanchita;

public class Lanchita implements Aerolinea {
	private static final boolean admiteReserva = true;
	private AerolineaLanchita lanchita = AerolineaLanchita.getInstance();
	private static float impuesto = 15;
	private Integer maximaDuracionDeReserva;
	private List<Vuelo> vuelos = new ArrayList<Vuelo>();
	private List<Itinerario> itinerariosReservados = new ArrayList<Itinerario>();
	private List<Asiento> asientosReservados = new ArrayList<Asiento>();
	
	public Lanchita() {
		
	}

	public List<Asiento> asientosDisponibles(String unOrigen, String unDestino,Fecha fecha){
		List<Asiento> asientosDisponibles = new ArrayList<Asiento>();
		for (String[] unStringAsiento : this.getLanchita().asientosDisponibles(
				unOrigen, unDestino, null, null, null, null)) {
			//FIXME ¿como hacer este if mas feliz?
			if ((unStringAsiento[8] == unOrigen
					&& unStringAsiento[9] == unDestino) || (unOrigen==null && unDestino==null)
					|| (unStringAsiento[8] == unOrigen && unDestino == null)
					|| (unStringAsiento[9] == unDestino && unOrigen == null)) {
				Asiento unAsiento = new Asiento(unStringAsiento[8],
						unStringAsiento[9], unStringAsiento[0],
						unStringAsiento[1], 
						unStringAsiento[2], unStringAsiento[3],
						unStringAsiento[4], unStringAsiento[10],
						unStringAsiento[6], unStringAsiento[11],
						unStringAsiento[7],this);
//				if (unAsiento.tieneFechasEntre(fecha)) {
					asientosDisponibles.add(unAsiento);
//				}
			}
		}

		return asientosDisponibles;
	}

	public AerolineaLanchita getLanchita() {
		return lanchita;
	}

	public void setLanchita(AerolineaLanchita lanchita) {
		this.lanchita = lanchita;
	}

	@Override
	public float getImpuesto() {
		return Lanchita.impuesto;
	}

	public static void setImpuesto(float impuesto) {
		Lanchita.impuesto = impuesto;
	}

	
	private void incremetarPopularidad(String nroVuelo){
		nroVuelo=nroVuelo.split("-")[0];
		Vuelo unVuelo = null;
		for(Vuelo otroVuelo:this.getVuelos()){
			if(otroVuelo.getNroDeVuelo().equals(nroVuelo)){
				unVuelo=otroVuelo;
				break;
			}
		}
		if(unVuelo!=null){
			unVuelo.setPopularidad(unVuelo.getPopularidad()+1);
			this.getVuelos().add(unVuelo);

		}else{
			Vuelo vuelo = new Vuelo();
			vuelo.setNroDeVuelo(nroVuelo);
			Integer popularidaNueva=vuelo.getPopularidad()+1;
			vuelo.setPopularidad(popularidaNueva);
			this.getVuelos().add(vuelo);
		}
	}
	
	@Override
	public void comprar(Asiento unAsiento, String unDni) {
		this.getLanchita().comprar(unAsiento.getAsiento());
		unAsiento.setEstado("C");
		this.incremetarPopularidad(unAsiento.getAsiento());
	}
	
	public void reservarAsiento(Asiento unAsiento, Usuario unUsuario){
		this.getLanchita().reservar(unAsiento.getAsiento(), unUsuario.getDni());
		unAsiento.setEstado("R");
		this.getAsientosReservados().add(unAsiento);
		//TODO ¿que es esto? ¿lo puedo romper?
		unAsiento.setEstaReservado(true);
		
	}
	
	public boolean admiteReserva(){
		return Lanchita.admiteReserva;
	}
	

	public Integer getMaximaDuracionDeReserva() {
		return maximaDuracionDeReserva;
	}

	public void setMaximaDuracionDeReserva(Integer maximaDuracionDeReserva) {
		this.maximaDuracionDeReserva = maximaDuracionDeReserva;
	}

	@Override
	public Integer popularidadDeUnVuelo(String codigoAsientoDeUnVuelo) {
		codigoAsientoDeUnVuelo=codigoAsientoDeUnVuelo.split("-")[0];
		for(Vuelo unVuelo:this.getVuelos()){
			if(unVuelo.getNroDeVuelo().equals(codigoAsientoDeUnVuelo)){
				return unVuelo.getPopularidad();
			}
		}
		return 0;
	}

	public List<Vuelo> getVuelos() {
		return vuelos;
	}

	public void setVuelos(List<Vuelo> vuelos) {
		this.vuelos = vuelos;
	}

	public List<Itinerario> getItinerariosReservados() {
		return itinerariosReservados;
	}

	public void setItinerariosReservados(List<Itinerario> itinerariosReservados) {
		this.itinerariosReservados = itinerariosReservados;
	}

	@Override
	public void chequearExpiracionAsientos() {
		List<Asiento> asientosAEliminar = new ArrayList<Asiento>();
		for (Asiento unAsiento : this.getAsientosReservados()) {
			asientosAEliminar.add(unAsiento.actualizarReservas());
		}
		this.getAsientosReservados().removeAll(asientosAEliminar);
		
	}

	public List<Asiento> getAsientosReservados() {
		return asientosReservados;
	}

	public void setAsientosReservados(List<Asiento> asientosReservados) {
		this.asientosReservados = asientosReservados;
	}
}
