package ec.ups.edu.app.g2.cooperativaUnion.modelo;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Query;

import ec.ups.edu.app.g2.cooperativaUnion.DAO.CuentaAhorroDAO;
import ec.ups.edu.app.g2.cooperativaUnion.DAO.SolicitudPolDAO;
import ec.ups.edu.app.g2.cooperativaUnion.DAO.TransaccionDAO;
import ec.ups.edu.app.g2.cooperativaUnion.EN.CuentaAhorro;
import ec.ups.edu.app.g2.cooperativaUnion.EN.SolicitudPoliza;
import ec.ups.edu.app.g2.cooperativaUnion.EN.Transaccion;
import ec.ups.edu.app.g2.cooperativaUnion.utils.SolicitudTemporal;

@Stateless
public class SolicitudPolON {
	
	@Inject
	CuentaAhorroDAO cuentaDao;
	
	@Inject
	private SolicitudPolDAO sdao;
	
	public List<SolicitudPoliza> listaSolicitudes(String numeroCuenta) {
	
		return sdao.listaTransaciones(numeroCuenta);
		
		}
	
	public List<SolicitudTemporal> listaSolicitudesEmpleado() {
		
		return sdao.listaSolictudesEmpleado();
		
		}
	
	public void nuevaSolicitud(SolicitudPoliza solicitud, CuentaAhorro cuenta) {
		solicitud.setEstado("Pendiente");
	  sdao.insertSolicitudCredito(solicitud);
	}
	
	public CuentaAhorro getCuenta(String numeroCuenta) { 
		return cuentaDao.buscarCuentaAhorro(numeroCuenta);  		
    }
	
	public SolicitudPoliza getSolicitud(int codigo) {
		return sdao.buscarSolicitudCredito(codigo);
	}
	
	
	public List<SolicitudPoliza> verSolicitudCredito() {
		return sdao.getSolicitudCreditoes();
	}
	
	public void actualizarSolisitud(SolicitudPoliza solicitud) {
		sdao.update(solicitud);
	}
	
	public void  actualizar2(SolicitudPoliza solicitud) {
		sdao.actualizar2(solicitud.getId(),solicitud.getEstado());
	}

}
