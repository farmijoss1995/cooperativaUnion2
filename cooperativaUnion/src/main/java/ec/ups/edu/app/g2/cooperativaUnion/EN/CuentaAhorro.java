package ec.ups.edu.app.g2.cooperativaUnion.EN;


import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;

import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class CuentaAhorro{
	
	@Id
	private String numeroCuenta;
	private Double saldoCuenta;
	

    @OneToOne
    @JoinColumn(name = "cedula_usuario", referencedColumnName = "cedula")
    private Usuario usuario;
	
	@JsonIgnore
	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_numeroCuenta")
    private List<Transaccion> listaTra;
	
	@JsonIgnore
	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_ahorro")
	 private List<SolicitudPoliza> listaSolicitud;
	
	@JsonIgnore
	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
	@JoinColumn(name = "cuenta_ahorroPres")
	private List<PolizaPres>creditos;
	
	public void agregarCredito(PolizaPres cred) {
	if (creditos == null) {
		creditos = new ArrayList<PolizaPres>();
	}
	creditos.add(cred);
	}
	
	public String getNumeroCuenta() {
		return numeroCuenta;
	}

	public void setNumeroCuenta(String numeroCuenta) {
		this.numeroCuenta = numeroCuenta;
	}

	public Double getSaldoCuenta() {
		return saldoCuenta;
	}

	public void setSaldoCuenta(Double saldoCuenta) {
		this.saldoCuenta = saldoCuenta;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public List<Transaccion> getListaTra() {
		return listaTra;
	}

	public void setListaTra(List<Transaccion> listaTra) {
		this.listaTra = listaTra;
	}
	public void addTransaccion(Transaccion t) {
		if(listaTra==null) {
			listaTra= new ArrayList<>();
	}
		this.listaTra.add(t);
	}
	
	
	public List<SolicitudPoliza> getListaSolicitud() {
		return listaSolicitud;
	}

	public void setListaSolicitud(List<SolicitudPoliza> listaSolicitud) {
		this.listaSolicitud = listaSolicitud;
	}

	public void addSolicitud(SolicitudPoliza sc) {
		if(listaSolicitud==null) {
			listaSolicitud= new ArrayList<>();
	}
		this.listaSolicitud.add(sc);
	}

	public List<PolizaPres> getCreditos() {
		return creditos;
	}

	public void setCreditos(List<PolizaPres> creditos) {
		this.creditos = creditos;
	}

	@Override
	public String toString() {
		return "CuentaAhorro [numeroCuenta=" + numeroCuenta + ", saldoCuenta=" + saldoCuenta + ", usuario=" + usuario
				+ ", listaTra=" + listaTra + ", listaSolicitud=" + listaSolicitud + ", creditos=" + creditos + "]";
	}
	
	
	
	
	
}
