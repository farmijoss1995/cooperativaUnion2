package ec.ups.edu.app.g2.cooperativaUnion.Controlador;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.SessionScoped;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;

import org.primefaces.model.file.UploadedFile;

import ec.ups.edu.app.g2.cooperativaUnion.EN.PolizaPres;
import ec.ups.edu.app.g2.cooperativaUnion.EN.CuentaAhorro;
import ec.ups.edu.app.g2.cooperativaUnion.EN.Empleado;
import ec.ups.edu.app.g2.cooperativaUnion.EN.Pago;
import ec.ups.edu.app.g2.cooperativaUnion.EN.SolicitudPoliza;
import ec.ups.edu.app.g2.cooperativaUnion.modelo.PolizaPresON;
import ec.ups.edu.app.g2.cooperativaUnion.modelo.SolicitudPolON;
import ec.ups.edu.app.g2.cooperativaUnion.utils.Mail;
import ec.ups.edu.app.g2.cooperativaUnion.utils.PolizaTemp;
import ec.ups.edu.app.g2.cooperativaUnion.utils.SolicitudpPolTemp;
import ec.ups.edu.app.g2.cooperativaUnion.utils.SolicitudTemporal;
@Named
@ConversationScoped
public class SolicitudPolBean implements Serializable  {
	
	private static final long serialVersionUID = 1L;
	
	@Inject
	private SolicitudPolON son;
	
	@Inject
	private PolizaPresON creon;
	private SolicitudPoliza solicitud;
	private PolizaTemp t;
	private Pago p;
	private PolizaPres cpres;
	private CuentaAhorro cuenta;
	private List<SolicitudPoliza> solicitudes;
	private List<SolicitudTemporal> solicitudes2; 
	private int codigo;
	private SolicitudTemporal st;
	private String numcuenta;
	
	
	private UploadedFile filec;
	private UploadedFile filep;
	private UploadedFile filer;
	private String WS_SAVE_SOLICITUD = "http://127.0.0.1:8000/api/";
	
	
	
	
	@PostConstruct
	public void init() {
		solicitud = new SolicitudPoliza();
		cuenta = new CuentaAhorro();
		t = new  PolizaTemp();
		cpres = new PolizaPres();
		p = new Pago();
		st = new SolicitudTemporal();
		loadDataSolicitudesEmpleado();
	}
	public String realizarSolicitud(SolicitudpPolTemp t) {
		
		Client client = ClientBuilder.newClient();		
		WebTarget target = client.target(WS_SAVE_SOLICITUD);
		String respuesta = target.request().post(Entity.json(t), (new GenericType<String>() {}));	
		return respuesta;
	}
	
	public String consumiendoServicoPython() {
		solicitud = son.getSolicitud(codigo);
		SolicitudpPolTemp ct = new SolicitudpPolTemp();
		ct.setPlazo_meses(solicitud.getPlazoCredito());
		ct.setProposito_credito(solicitud.getPropositoCredito());
		ct.setMonto_credito((int)Math.round(solicitud.getMontoCredito()));
		ct.setTipo_empleado(solicitud.getTiempoEmpleo());
		ct.setEstado_civil(solicitud.getEstadoCivil());
		ct.setAvaluo_vivienda((int)Math.round(solicitud.getMontoCredito()));
		ct.setActivos(solicitud.getActivos());
		ct.setVivienda(solicitud.getTipoVivienda());
		ct.setEmpleo(solicitud.getTiempoEmpleo());
		ct.setTrabajador_extranjero(solicitud.getTrabajorExtranjero());
		String g = realizarSolicitud(ct);
		
		return g;
	}

	
	public String recibir(String numeroCuenta) {
		numcuenta = numeroCuenta;
		return numeroCuenta ;
	}
	
	
	public int recibir2(int codigo2) {
		codigo = codigo2;
		 solicitud = son.getSolicitud(codigo2);
		return codigo;
	}
	
	
	public String guardarSolicitud() {
		cuenta = buscarCuenta(numcuenta);
		solicitud.setCuentaAhorro(cuenta);
		uploadFile();
		son.nuevaSolicitud(solicitud, cuenta);
		return "vistaSolicitudCredito?faces-redirect=true&numcuenta="+cuenta.getNumeroCuenta();	
	}

	public void uploadFile() {
		if (filec != null && filep != null && filer != null ) {
			byte[] contenido = filec.getContent();
			String nombre = filec.getFileName();
			solicitud.setCedulaImagen(contenido);
		
			byte[] contenido1 = filep.getContent();
			String nombre1 = filep.getFileName();
			solicitud.setPlanillaImagen(contenido1);
			
			byte[] contenido2 = filer.getContent();
			String nombre2 = filer.getFileName();
			solicitud.setRolPagosImagen(contenido2);		
		}
	}
	
	public CuentaAhorro buscarCuenta(String numeroCuenta) {
		
		return son.getCuenta(numeroCuenta);
	}
	public SolicitudPoliza buscarSolicitud(int codigo) {
		
		return son.getSolicitud(codigo);
	}
	
	public List<SolicitudTemporal> loadDataSolicitudesEmpleado() {
		solicitudes2 = son.listaSolicitudesEmpleado();
		return 	solicitudes2;
	}
	public String revisarSolicitud(int codigo) {
	
		solicitud = son.getSolicitud(codigo);
		this.numcuenta= solicitud.getCuentaAhorro().getNumeroCuenta();
		this.codigo = codigo;
		 return "vistaRevisionSolicitudCredito?faces-redirect=true&codigo="+codigo;
	}
	
	public List<SolicitudPoliza> loadDataSolicitudes(String numcuenta) {
		solicitudes = son.listaSolicitudes(numcuenta);
		return 	solicitudes;
	}
	
	public List<SolicitudPoliza> loadDataTransaccion(String numcuenta) {
		solicitudes = son.listaSolicitudes(numcuenta);
		return solicitudes;
	}
	
	
	public String estadoSolicitud() {
		son.actualizar2(solicitud);	
		solicitud = son.getSolicitud(solicitud.getId());
		
		if (solicitud.getEstado().equals("Aprobado")) {
			PolizaTemp tem = new PolizaTemp();
			tem.setNumcuenta(solicitud.getCuentaAhorro().getNumeroCuenta());
			tem.setTipo(solicitud.getPropositoCredito());
			tem.setMonto(solicitud.getMontoCredito());
			tem.setCuotas(solicitud.getPlazoCredito());
			creon.Credito(tem);

		}else if(solicitud.getEstado().equals("Denegado")) {
			Mail mail = new Mail();
			try {
				mail.enviarMail(solicitud.getCuentaAhorro().getUsuario().getCorreo(), "Se ha denegado su prestamo por falta de requisitos acercarse a las oficinas", ""+cuenta.toString());
			} catch (AddressException e) {
				e.printStackTrace();
			} catch (MessagingException e) {

				e.printStackTrace();
			}
			
		}
		return"VistaJefeCredito?faces-redirect=true";
	}
	

	
	public SolicitudPoliza getSolicitud() {
		return solicitud;
	}

	public void setSolicitud(SolicitudPoliza solicitud) {
		this.solicitud = solicitud;
	}

	public CuentaAhorro getCuenta() {
		return cuenta;
	}

	public void setCuenta(CuentaAhorro cuenta) {
		this.cuenta = cuenta;
	}

	public List<SolicitudPoliza> getSolicitudes() {
		return solicitudes;
	}

	public void setSolicitudes(List<SolicitudPoliza> solicitudes) {
		this.solicitudes = solicitudes;
	}

	public String getnumcuenta() {
		return numcuenta;
	}

	public void setnumcuenta(String numcuenta) {
		this.numcuenta = numcuenta;
	}

	public String getNumcuenta() {
		return numcuenta;
	}

	public void setNumcuenta(String numcuenta) {
		this.numcuenta = numcuenta;
	}

	public SolicitudTemporal getSt() {
		return st;
	}

	public void setSt(SolicitudTemporal st) {
		this.st = st;
	}

	public int getCodigo() {
		return codigo;
	}

	public void setCodigo(int codigo) {
		this.codigo = codigo;
	}

	public List<SolicitudTemporal> getSolicitudes2() {
		return solicitudes2;
	}

	public void setSolicitudes2(List<SolicitudTemporal> solicitudes2) {
		this.solicitudes2 = solicitudes2;
	}


	public UploadedFile getFilec() {
		return filec;
	}


	public void setFilec(UploadedFile filec) {
		this.filec = filec;
	}


	public UploadedFile getFilep() {
		return filep;
	}


	public void setFilep(UploadedFile filep) {
		this.filep = filep;
	}


	public UploadedFile getFiler() {
		return filer;
	}


	public void setFiler(UploadedFile filer) {
		this.filer = filer;
	}


	
	
}
