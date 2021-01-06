package ec.ups.edu.app.g2.cooperativaUnion.modelo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.persistence.Query;

import ec.ups.edu.app.g2.cooperativaUnion.DAO.PolizaPreDAO;
import ec.ups.edu.app.g2.cooperativaUnion.DAO.CuentaAhorroDAO;
import ec.ups.edu.app.g2.cooperativaUnion.DAO.PagoDAO;
import ec.ups.edu.app.g2.cooperativaUnion.DAO.TransaccionDAO;
import ec.ups.edu.app.g2.cooperativaUnion.EN.PolizaPres;
import ec.ups.edu.app.g2.cooperativaUnion.EN.CuentaAhorro;
import ec.ups.edu.app.g2.cooperativaUnion.EN.Empleado;
import ec.ups.edu.app.g2.cooperativaUnion.EN.Pago;
import ec.ups.edu.app.g2.cooperativaUnion.EN.Transaccion;
import ec.ups.edu.app.g2.cooperativaUnion.EN.Usuario;
import ec.ups.edu.app.g2.cooperativaUnion.utils.Decimales;
import ec.ups.edu.app.g2.cooperativaUnion.utils.Mail;
import ec.ups.edu.app.g2.cooperativaUnion.utils.PolizaTemp;
import ec.ups.edu.app.g2.cooperativaUnion.utils.Respuesta;
import ec.ups.edu.app.g2.cooperativaUnion.utils.SolicitudTemporal;
import ec.ups.edu.app.g2.cooperativaUnion.utils.Trans;

@Stateless
public class TransaccionON {

	@Inject
	CuentaAhorroDAO cuentaDao;

	@Inject
	TransaccionDAO transaccionDao;

	@Inject
	PolizaPreDAO creditoDao;

	@Inject
	PagoDAO pagoDao;

	public List<Transaccion> listaTransaciones(String numeroCuenta) {

		return transaccionDao.listaTransaciones(numeroCuenta);

	}

	public void nuevaTransaccion(Transaccion transaccion, CuentaAhorro cuenta) {
		if (transaccion.getTipoTransaccion().equals("Deposito")) {
			TransDeposito(transaccion, cuenta);
		} else if (transaccion.getTipoTransaccion().equals("Retiro")) {
			TransRetiro(transaccion, cuenta);
		} else if (transaccion.getTipoTransaccion().equals("Pago Credito")) {
			System.out.println("Metodo construccion");
		}

	}

	public CuentaAhorro getCuenta(String numeroCuenta) {
		return cuentaDao.buscarCuentaAhorro(numeroCuenta);
	}

	public PolizaPres getCredito(int codigo) {
		return creditoDao.buscarCreditoPres(codigo);
	}

	public Pago getPago(int codigo) {
		return pagoDao.buscarPago(codigo);
	}

	public void TransDeposito(Transaccion transaccion, CuentaAhorro cuenta) {
		double nuevoSaldo = cuenta.getSaldoCuenta() + transaccion.getMonto();
		transaccion.setFecha(new Date());
		cuenta.setSaldoCuenta(nuevoSaldo);
		transaccionDao.nuevaTransaccion(transaccion);
		try {
			cuentaDao.update(cuenta);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void TransRetiro(Transaccion transaccion, CuentaAhorro cuenta) {
		if (cuenta.getSaldoCuenta() > transaccion.getMonto()) {
			double nuevoSaldo = cuenta.getSaldoCuenta() - transaccion.getMonto();
			transaccion.setFecha(new Date());
			cuenta.setSaldoCuenta(nuevoSaldo);
			transaccionDao.nuevaTransaccion(transaccion);
			try {
				cuentaDao.update(cuenta);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.out.println("La cuenta no tiene fondos suficientes para realizar la transaccion");
		}
	}

	public void TransPagoCredito(Transaccion transaccion, PolizaPres credito, Pago pago) {
		Decimales d = new Decimales();
	if(transaccion.getMonto()>=pago.getSaldo()) {
		
		double nuevoSaldo =  d.redondearDecimales(credito.getSaldo() - transaccion.getMonto(),2);
		double nuevoSaldoPago = d.redondearDecimales(pago.getSaldo() - transaccion.getMonto(),2);
		
		credito.setSaldo(nuevoSaldo);
		creditoDao.update(credito);
		if (nuevoSaldoPago <= 0) {
			pago.setEstado("Pagado");
			pago.setSaldo(0.0);
			nuevoSaldoPago=(nuevoSaldoPago*-1);
			if (nuevoSaldoPago > 0) {
				Pago p = new Pago();
				p = pagoDao.buscarPago(pago.getCodigo()+1);
				nuevoSaldoPago = p.getSaldo() - nuevoSaldoPago;
				p.setSaldo(nuevoSaldoPago);
			}
		}
		pagoDao.update(pago);
		transaccion.setFecha(new Date());
		transaccion.setTipoTransaccion("Pago Credito");
		transaccion.setCuenta(credito.getCuenta());
		transaccion.setCredito(credito);
		transaccionDao.nuevaTransaccion(transaccion);
	}else {
		double nuevoSaldo =  d.redondearDecimales(credito.getSaldo() - transaccion.getMonto(),2);
		double nuevoSaldoPago = d.redondearDecimales(pago.getSaldo() - transaccion.getMonto(),2);
		credito.setSaldo(nuevoSaldo);
		creditoDao.update(credito);
		pago.setSaldo(nuevoSaldoPago);
		pagoDao.update(pago);
		transaccion.setFecha(new Date());
		transaccion.setTipoTransaccion("Pago Credito");
		transaccion.setCuenta(credito.getCuenta());
		transaccion.setCredito(credito);
		transaccionDao.nuevaTransaccion(transaccion);
	}
		
		
	}

	public Respuesta Transacciones(Trans trx) throws Exception {

		Respuesta res = null;
		CuentaAhorro origen = cuentaDao.buscarCuentaAhorro(trx.getCuentaorigen());
		CuentaAhorro destino = cuentaDao.buscarCuentaAhorro(trx.getCuentadestino());
System.out.println(origen.toString());

		if (trx.getTipo().equals("tranferencia")) {
			if (destino == null)
				throw new Exception("La cuentas no existe");

			if (trx.getCuentaorigen() != null && trx.getCuentadestino() != null) {
				Double saldoOri = origen.getSaldoCuenta() - trx.getMonto();
				Double saldoDes = destino.getSaldoCuenta() + trx.getMonto();

				origen.setSaldoCuenta(saldoOri);
				destino.setSaldoCuenta(saldoDes);

				Transaccion tOri = new Transaccion();
				tOri.setCuent(trx.getCuentaorigen());
				tOri.setFecha(new Date());
				tOri.setTipoTransaccion("tranferencia");
				tOri.setMonto(trx.getMonto());

				Transaccion tDes = new Transaccion();

				tDes.setCuent(trx.getCuentadestino());
				tDes.setFecha(new Date());
				tDes.setTipoTransaccion("tranferencia");
				tDes.setMonto(trx.getMonto());

				origen.addTransaccion(tOri);
				destino.addTransaccion(tDes);
				
				transaccionDao.nuevaTransaccion(tOri);
				transaccionDao.nuevaTransaccion(tDes);
				cuentaDao.update(origen);
				cuentaDao.update(destino);

				/*List<CuentaAhorro> cuentas = cuentaDao.getCuentas(origen.getNumeroCuenta(), destino.getNumeroCuenta());

				for (CuentaAhorro cs : cuentas) {
					System.out.println(cs.toString());
				}*/
				res = new Respuesta();
				res.setCodigo(1);
				res.setMensaje("Ok");
			//	res.setCuentasafectadas(cuentas);
			}
		}
		return res;
	}
	public Respuesta Deposito(Trans trx) throws Exception {

		Respuesta res = null;
		
		CuentaAhorro destino = cuentaDao.buscarCuentaAhorro(trx.getCuentadestino());


		if (trx.getTipo().equals("deposito")) {
			if (destino == null)
				throw new Exception("La cuentas no existe");

			if ( trx.getCuentadestino() != null) {
				
				Double saldoDes = destino.getSaldoCuenta() + trx.getMonto();

				destino.setSaldoCuenta(saldoDes);

				Transaccion tDes = new Transaccion();

				tDes.setCuent(trx.getCuentadestino());
				tDes.setFecha(new Date());
				tDes.setTipoTransaccion("deposito");
				tDes.setMonto(trx.getMonto());

			
				destino.addTransaccion(tDes);
				
				transaccionDao.nuevaTransaccion(tDes);
				
				cuentaDao.update(destino);

				res = new Respuesta();
				res.setCodigo(1);
				res.setMensaje("Ok");
			
			}
		}
		return res;
	}
	
	public Respuesta Retiro(Trans trx) throws Exception {

		Respuesta res = null;
		
		CuentaAhorro destino = cuentaDao.buscarCuentaAhorro(trx.getCuentadestino());


		if (trx.getTipo().equals("retiro")) {
			if (destino == null)
				throw new Exception("La cuentas no existe");

			if ( trx.getCuentadestino() != null) {
				
				Double saldoDes = destino.getSaldoCuenta() - trx.getMonto();

				destino.setSaldoCuenta(saldoDes);

				Transaccion tDes = new Transaccion();

				tDes.setCuent(trx.getCuentadestino());
				tDes.setFecha(new Date());
				tDes.setTipoTransaccion("retiro");
				tDes.setMonto(trx.getMonto());

			
				destino.addTransaccion(tDes);
				
				transaccionDao.nuevaTransaccion(tDes);
				
				cuentaDao.update(destino);

				res = new Respuesta();
				res.setCodigo(1);
				res.setMensaje("Ok");
			
			}
		}
		return res;
	}
	
	public List<Object[]> vencimientoPagos(){
		List<Pago> ps = new ArrayList<>();
		List<Object[]> pagos = transaccionDao.vencimientoCreditos();
		for (Object item[] : pagos) {
			Pago p = new Pago();
			CuentaAhorro cuenta = new CuentaAhorro();
			PolizaPres cp = new PolizaPres();
			Transaccion t = new Transaccion();
			
			String cue = item[1].toString();
			cuenta= cuentaDao.buscarCuentaAhorro(cue);
			Double saldoPago = Double.parseDouble(item[7].toString());
			
			int cre =Integer.parseInt(item[6].toString()) ;
			cp = creditoDao.buscarCreditoPres(cre);
			
			if (cuenta.getSaldoCuenta() >= saldoPago ) {
				
				Double nuevoSaldoCuenta = cuenta.getSaldoCuenta() - saldoPago;
				Double nuevoSaldoCredito = cp.getSaldo() - saldoPago;
				cp.setSaldo(nuevoSaldoCredito);
				
				p.setCodigo(Integer.parseInt(item[1].toString()));
				p.setSaldo(0.0);
				p.setEstado("Pagado");
				p.setCuenta(cue);
				p.setValor(Double.parseDouble(item[5].toString()));
				p.setCreditoPres(cp);
				p.setFechaPago(item [3].toString());
				
				t.setMonto(saldoPago);
				t.setFecha(new Date());
				t.setTipoTransaccion("Debito Automatico Prestamo");
				t.setCuenta(cuenta);
				t.setCredito(cp);
				
				cuenta.setSaldoCuenta(nuevoSaldoCuenta);
				
				try {
					cuentaDao.update(cuenta);
					creditoDao.update(cp);
					pagoDao.update(p);
					transaccionDao.nuevaTransaccion(t);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				//cuenta.setSaldoCuenta(nuevoSaldoCuenta);
	
			}else if(cuenta.getSaldoCuenta() > 0) {
				Double nuevoSaldoPago = saldoPago - cuenta.getSaldoCuenta() ;
				Double nuevoSaldoCredito = cp.getSaldo() - saldoPago;
				cp.setSaldo(nuevoSaldoCredito);
				
				p.setSaldo(nuevoSaldoPago);
				p.setEstado("Pendiente");
				p.setCuenta(cue);
				p.setValor(Double.parseDouble(item[5].toString()));
				p.setCreditoPres(cp);
				p.setFechaPago(item [3].toString());
				
				t.setMonto(cuenta.getSaldoCuenta());
				t.setFecha(new Date());
				t.setTipoTransaccion("Debito Automatico Prestamo");
				t.setCuenta(cuenta);
				t.setCredito(cp);
				
				cuenta.setSaldoCuenta(0.0);
				
				try {
					cuentaDao.update(cuenta);
					creditoDao.update(cp);
					pagoDao.update(p);
					transaccionDao.nuevaTransaccion(t);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
			}else if (cuenta.getSaldoCuenta() <= 0) {
					System.out.println("Saldo Insuficiente...");
			}

			ps.add(p);
		}

		return pagos;
		
	}

}
