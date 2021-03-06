package ar.edu.frba.utn.dds.operaciones;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ar.edu.frba.utn.dds.aerolineasAdapters.Aerolinea;

import ar.edu.frba.utn.dds.exeptions.LaReservaNoCorrespondeAlUsuarioExeption;
import ar.edu.frba.utn.dds.exeptions.NoAdmiteReservaExeption;
import ar.edu.frba.utn.dds.exeptions.NoSePudoComprarExeption;
import ar.edu.frba.utn.dds.exeptions.ParametrosErroneosExeption;
import ar.edu.frba.utn.dds.exeptions.UsuarioInvalidoParaReservaExeption;
import ar.edu.frba.utn.dds.fechas.Fecha;
import ar.edu.frba.utn.dds.usuarios.Estandar;
import ar.edu.frba.utn.dds.usuarios.Usuario;
import ar.edu.frba.utn.dds.usuarios.Vip;

public class Aterrizar {

	private List<Aerolinea> aerolineas;
	private Criterio ordenador=new Criterio();
	
	public Aterrizar(List<Aerolinea> aerolineas) {
		this.setAerolineas(aerolineas);

	}

	public List<Aerolinea> getAerolineas() {
		return aerolineas;
	}

	public void setAerolineas(List<Aerolinea> aerolineas) {
		this.aerolineas = aerolineas;
	}
	
	public List<Itinerario> itinerariosDisponibles(String unOrigen, String unDestino, Fecha unaFecha, Usuario unUsuario){
		List<Itinerario> itinerarios = new ArrayList<Itinerario>();
		for(Aerolinea unaAerolinea : this.getAerolineas()){
			itinerarios.addAll(this.obtenerItinerarios(unOrigen, unDestino, unaFecha, unUsuario, unaAerolinea));
		}
		
		return itinerarios;
	}
	
	public List<Itinerario> itinerariosDisponibles(String unOrigen, String unDestino, Fecha unaFecha, Usuario unUsuario, Comparator<Itinerario> criterio){
		List<Itinerario> itinerarios = new ArrayList<Itinerario>();
		for(Aerolinea unaAerolinea : this.getAerolineas()){
			itinerarios.addAll(this.obtenerItinerarios(unOrigen, unDestino, unaFecha, unUsuario, unaAerolinea));
		}
		this.getOrdenador().setComparador(criterio);
		this.getOrdenador().ordenarSegunCriterio(itinerarios);
		return itinerarios;
	}
	
	
	public List<Asiento> asientosDisponiblesDeUnaAerolinea(String unOrigen,
			String unDestino, Fecha fecha, Usuario user, Aerolinea unaAerolinea) {
		List<Asiento> asientos = new ArrayList<Asiento>();
		List<Asiento> asientosReturn = new ArrayList<Asiento>();
		asientos.addAll(unaAerolinea.asientosDisponibles(unOrigen, unDestino,
				fecha));
		for (Asiento unAsiento : asientos) {
			unAsiento.setPrecio(this.calcularPrecioDeUnAsiento(
					unAsiento.getPrecioOriginal(), user,
					unAsiento.getAerolinea()));
			asientosReturn.add(unAsiento);
			if (unAsiento.esSuperOferta() && !(user.getTipo() instanceof Vip)) {
				asientosReturn.remove(unAsiento);
			}
		}
		return asientosReturn;
	}

	public List<Asiento> asientosDisponibles(String unOrigen, String unDestino,
			Fecha fecha, Usuario user) {
		if (unOrigen == null || unDestino == null || fecha==null) {
			throw new ParametrosErroneosExeption();
		}
		List<Asiento> asientos = new ArrayList<Asiento>();
		for (Aerolinea aerolinea : this.getAerolineas()) {
			asientos.addAll(this.asientosDisponiblesDeUnaAerolinea(unOrigen,
					unDestino, fecha, user, aerolinea));
		}
		return asientos;
	}

	/*
	 * Metodo privado que se encarga de la validacion de la primera reserva.
	 */
	protected boolean esElUsuarioQueReservoOriginalmente(Asiento unAsiento,
			Usuario unUsuario) {
		if (unAsiento.getReservaPosta() != null){
			return unAsiento.getReservaPosta().getDni().equals(unUsuario.getDni());
		}else{
			return true;
		}
	}

	public void comprar(Itinerario unItinerario, Usuario unUsuario) throws NoSePudoComprarExeption, LaReservaNoCorrespondeAlUsuarioExeption{
		for( Asiento unAsiento : unItinerario.getAsientos() ){
			if (unAsiento.getEstado().equals("R") && !this.esElUsuarioQueReservoOriginalmente(unAsiento, unUsuario)) {throw new LaReservaNoCorrespondeAlUsuarioExeption();}	
		}
		for( Asiento unAsiento : unItinerario.getAsientos() ){
			this.comprar(unAsiento, unUsuario);
		}
	}
	
	public void comprar(Asiento unAsiento, Usuario user) throws NoSePudoComprarExeption, LaReservaNoCorrespondeAlUsuarioExeption {
		if (unAsiento.getEstaReservado()
				&& !this.esElUsuarioQueReservoOriginalmente(unAsiento, user)) {
			throw new LaReservaNoCorrespondeAlUsuarioExeption();
		}
		unAsiento.getAerolinea().comprar(unAsiento, user.getDni());
		unAsiento.getReservas().clear();
	}

	public void reservar(Itinerario unItinerario, Usuario unUsuario) throws UsuarioInvalidoParaReservaExeption, NoAdmiteReservaExeption{
		for( Asiento unAsiento : unItinerario.getAsientos()){
			this.reservar(unAsiento, unUsuario);
		}
		Aerolinea unaAerolinea = unItinerario.getAsientos().get(0).getAerolinea();
		unaAerolinea.getItinerariosReservados().add(unItinerario);
	}
	
	/*
	 * Este metodo reserva un asiento que se le pasa por parametro, si no tiene
	 * reservas le avisa a la aerolinea que lo reserve, de lo contrario registra
	 * una nueva sobrereserva En el asiento guardamos dichas reservas y la del
	 * index 0 corresponde a la reserva posta (la oriyinal)
	 */

	public void reservar(Asiento unAsiento, Usuario usuario) throws UsuarioInvalidoParaReservaExeption, NoAdmiteReservaExeption {
		if (!(usuario.getTipo() instanceof Vip)) {
			throw new UsuarioInvalidoParaReservaExeption("El usuario es invalido para reservar");
		}
		if (!unAsiento.getAerolinea().admiteReserva()) {
			throw new NoAdmiteReservaExeption("La compania no admite reservas");
		}
		// discutimos 2 opciones de diseño.. 1.Usando exepcion que tira el
		// metodo reserva de lanchitaPosta
		// 2. usando este if
		if (!(unAsiento.getEstado().equals("R"))) {
			unAsiento.getAerolinea().reservarAsiento(unAsiento, usuario);
		}
		unAsiento.guardarReserva(new Reserva(unAsiento, usuario.getDni()));
	}

	private float calcularPrecioDeUnAsiento(String precio, Usuario unUsuario,
			Aerolinea unaAerolinea) {
		float precioOriginal = new Float(precio);
		return precioOriginal + precioOriginal * unaAerolinea.getImpuesto()
				+ unUsuario.getTipo().getRecargo();
	}

	
	private List<Itinerario> obtenerItinerariosQueSatisfaganUnViajeDeUnaEscala(
			String unOrigen, String unDestino, Fecha fecha, Usuario user,
			Aerolinea unaAerolinea) {
		List<Itinerario> itinerarios = new ArrayList<Itinerario>();

		List<Asiento> asientosOrigenes = this
				.asientosDisponiblesDeUnaAerolinea(unOrigen, null, fecha, user,
						unaAerolinea);
		List<Asiento> asientosDestinos = this
				.asientosDisponiblesDeUnaAerolinea(null, unDestino, fecha,
						user, unaAerolinea);

		for (Asiento unAsientoOrigen : asientosOrigenes) {
			for (Asiento unAsientoDestino : asientosDestinos) {
				if (unAsientoOrigen.getDestino().equals(
						unAsientoDestino.getOrigen())&& unAsientoDestino.getFechaSalida().esPosteriorA(unAsientoOrigen.getFechaLlegada())) {
					Itinerario unItinerario = new Itinerario();
					unItinerario.getAsientos().add(unAsientoOrigen);
					unItinerario.getAsientos().add(unAsientoDestino);
					itinerarios.add(unItinerario);
				}
			}
		}


		return itinerarios;

	}

	private List<Itinerario> obtenerItinerariosQueSatisfaganUnViajeDeDosEscalas(
			String unOrigen, String unDestino, Fecha fecha, Usuario user,
			Aerolinea unaAerolinea) {
		List<Itinerario> itinerarios = new ArrayList<Itinerario>();

		List<Asiento> asientosOrigenes = this
				.asientosDisponiblesDeUnaAerolinea(unOrigen, null, fecha, user,
						unaAerolinea);
		List<Asiento> asientosDestinos = this
				.asientosDisponiblesDeUnaAerolinea(null, unDestino, fecha,
						user, unaAerolinea);

		// FIXME ¿3 for anidados? Dale que va!
		for (Asiento unAsientoOrigen : asientosOrigenes) {
			for (Asiento unAsientoDestino : asientosDestinos) {
				List<Itinerario> itinerariosIntermedios = obtenerItinerariosQueSatisfaganUnViajeDeUnaEscala(
						unAsientoOrigen.getOrigen(),
						unAsientoDestino.getOrigen(), fecha, user, unaAerolinea);
				for (Itinerario unItinerarioIntermedio : itinerariosIntermedios) {
					if (unItinerarioIntermedio.getAsientos().get(1)
							.getDestino().equals(unAsientoDestino.getOrigen())&& unAsientoDestino.getFechaSalida().esPosteriorA(unItinerarioIntermedio.getAsientos().get(1).getFechaLlegada())) {
						Itinerario unItinerario = new Itinerario();
						unItinerario.getAsientos().addAll(
								unItinerarioIntermedio.getAsientos());
						unItinerario.getAsientos().add(unAsientoDestino);
						itinerarios.add(unItinerario);
					}
				}
			}
		}
		return itinerarios;

	}
	
	private List<Itinerario> obtenerItinerariosDirectos(String unOrigen,
			String unDestino, Fecha fecha, Usuario user, Aerolinea unaAerolinea){
		List<Itinerario> itinerariosToReturn = new ArrayList<Itinerario>();
		List<Asiento> asientosDirectos = this
				.asientosDisponiblesDeUnaAerolinea(unOrigen, unDestino, fecha,
						user, unaAerolinea);
		if (asientosDirectos.size() != 0) {
			for (Asiento unAsientoDirecto : asientosDirectos) {
				Itinerario unItinerario = new Itinerario();
				unItinerario.getAsientos().add(unAsientoDirecto);
				itinerariosToReturn.add(unItinerario);
			}
		}
		return itinerariosToReturn;
	}
	
	public List<Itinerario> obtenerItinerarios(String unOrigen,
			String unDestino, Fecha fecha, Usuario user, Aerolinea unaAerolinea) {
		List<Itinerario> itinerariosToReturn = new ArrayList<Itinerario>();
		
		//Busca en los niveles de escala y se trae todos los itinerarios que encuentre
		itinerariosToReturn.addAll(this.obtenerItinerariosDirectos(unOrigen, unDestino, fecha, user, unaAerolinea));
		
		itinerariosToReturn
				.addAll(this.obtenerItinerariosQueSatisfaganUnViajeDeUnaEscala(
						unOrigen, unDestino, fecha, user, unaAerolinea));
		itinerariosToReturn
				.addAll(this.obtenerItinerariosQueSatisfaganUnViajeDeDosEscalas(
						unOrigen, unDestino, fecha, user, unaAerolinea));

		return itinerariosToReturn;
	}

	public Criterio getOrdenador() {
		return ordenador;
	}

	public void setOrdenador(Criterio ordenador) {
		this.ordenador = ordenador;
	}
	
	public void chequearExpiracionReservas(){
		for (Aerolinea unaAerolinea : this.getAerolineas()) {
			unaAerolinea.chequearExpiracionAsientos();
		}
	}

}
