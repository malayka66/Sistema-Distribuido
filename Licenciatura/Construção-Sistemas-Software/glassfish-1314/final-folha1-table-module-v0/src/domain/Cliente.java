package domain;

import java.sql.ResultSet;
import java.sql.SQLException;

import services.persistence.DataSource;
import services.persistence.tableDataGateway.ClientesTableGateway;

public enum Cliente {
	INSTANCE;
	
	public void adicionarCliente (int npc, String designacao, 
			int telefone, int tipoDesconto) throws ApplicationException {
		try {
			// verificar que o n�mero de pessoa colectiva � �nico 
			ResultSet rs = ClientesTableGateway.INSTANCE.getClientePorNPC(npc);
			boolean existe = rs.next();
			rs.close();
	
			if (existe)
				throw new ApplicationException ("Cliente com NPC:" + npc + " j� existe!");
		
			// verifica se o cliente tem o contribu�nte v�lido
			if (!NPCValido(npc))
				throw new ApplicationException ("N�mero de pessoa colectiva inv�lido!");
	
			// guardar a informa��o na base de dados
			ClientesTableGateway.INSTANCE.adicionarCliente(npc, designacao, telefone, tipoDesconto);
		} catch (SQLException e) {
			// notem que a excep��o de SQL � passada como causa para a excep��o lan�ada.
			throw new ApplicationException ("Erro interno ao adicionar cliente", e);
		}
	}
	
	
	public int getClienteId (int npc) throws ApplicationException {
		ResultSet rs = null;
		try {
			rs = ClientesTableGateway.INSTANCE.getClientePorNPC (npc);
			int idCliente = 0;
			if (rs.next()) 
				idCliente = rs.getInt("idCliente");
			else 
				throw new ApplicationException("N�o existe cliente com NPC " + npc);
			return idCliente;
		} catch (SQLException e) {
			throw new ApplicationException ("Erro interno ao obter cliente com NPC " + npc, e);
		} finally {
			DataSource.INSTANCE.close(rs);			
		}
	}
	
	public int getTipoDesconto (int idCliente) throws ApplicationException {
		ResultSet rs = null;
		try {
			rs = getCliente (idCliente);
			rs.next(); 
			return rs.getInt("idDesconto");
		} catch (SQLException e) {
			throw new ApplicationException("Erro interno ao aceder ao cliente " + idCliente, e);
		} finally {
			DataSource.INSTANCE.close(rs);
		}
	}
	
	private ResultSet getCliente (int idCliente) throws ApplicationException {
		try {
			ResultSet rs = ClientesTableGateway.INSTANCE.getClientePorId(idCliente);
			if (rs.getFetchSize() == 0) 
				throw new ApplicationException("O cliente com id: " + idCliente + " n�o existe.");
			return rs;
		} catch (SQLException e) {
			throw new ApplicationException("Erro interno ao aceder ao cliente " + idCliente, e);
		} 
	}

	
	/**
	 * Checks if a NPC number is valid.
	 * 
	 * @param npc The number to check.
	 * @return Whether the NPC is valid. 
	 */
	private boolean NPCValido(int npc) {
		// se n�o tem 9 d�gitos, erro!
		if (npc < 100000000 || npc > 999999999)
			return false;
		
		// se o primeiro n�mero n�o � 1, 2, 5, 6, 8, 9, erro!
		int primeiroDigito = npc / 100000000;
		if (primeiroDigito != 1 && primeiroDigito != 2 && 
			primeiroDigito != 5 && primeiroDigito != 6 &&
			primeiroDigito != 8 && primeiroDigito != 9)
			return false;
		
		// validar a congru�ncia modulo 11.
		int soma = 0;
		int checkDigit = npc % 10;
		npc /= 10;
		
		for (int i = 2; i < 10 && npc != 0; i++) {
			soma += npc % 10 * i;
			npc /= 10;
		}
		
		int checkDigitCalc = 11 - soma % 11;
		if (checkDigitCalc == 10)
			checkDigitCalc = 0;
		return checkDigit == checkDigitCalc;
	}

}
