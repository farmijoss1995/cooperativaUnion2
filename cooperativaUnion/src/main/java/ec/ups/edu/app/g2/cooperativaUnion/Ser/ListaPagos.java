package ec.ups.edu.app.g2.cooperativaUnion.Ser;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import ec.ups.edu.app.g2.cooperativaUnion.EN.Pago;
import ec.ups.edu.app.g2.cooperativaUnion.modelo.PolizaPresON;

@Path("/ListaPagos")
public class ListaPagos {

	@Inject
	private PolizaPresON credon;

	@GET
	@Produces("application/json")
	public List<Pago>listPagos(@QueryParam("codigo_pre") String cuenta1){
		
		return credon.listarMovil(cuenta1);
	}
	
	
}
