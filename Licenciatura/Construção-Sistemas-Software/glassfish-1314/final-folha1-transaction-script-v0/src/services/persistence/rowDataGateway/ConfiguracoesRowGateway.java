package services.persistence.rowDataGateway;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import services.persistence.DataSource;
import services.persistence.PersistenceException;

/**
 * A representa��o de um registo da tabela de configuracoes como um
 * objeto em mem�ria.
 *	
 * Chamadas de aten��o:
 * 1. Ver classe ClientesRowGateway
 * 
 * @author fmartins
 *
 */
public class ConfiguracoesRowGateway {
	// atributos que representam os campos da tabela
	private int idConfiguracao;
	private double percentagemTotal;
	private double limiteTotal;
	private double percentagemElegivel;
	
	// Getters e setters para os atributos
	public double getPercentagemTotal() {
		return percentagemTotal;
	}

	public void setPercentagemTotal(double percentagemTotal) {
		this.percentagemTotal = percentagemTotal;
	}

	public double getLimiteTotal() {
		return limiteTotal;
	}

	public void setLimiteTotal(double limiteTotal) {
		this.limiteTotal = limiteTotal;
	}

	public double getPercentagemElegivel() {
		return percentagemElegivel;
	}

	public void setPercentagemElegivel(double percentagemElegivel) {
		this.percentagemElegivel = percentagemElegivel;
	}

	// n�o h� setIdConfigura��o, porque o seu valor � atribu�do
	// automaticamente pela base de dados.
	public int getIdConfiguracao() {
		return idConfiguracao;
	}

	// actualiza��o a base de dados
	
	// a aplica��o s� l� a configura��o. N�o h� escritas nesta tabela
	// atrav�s da aplica��o.
		
	private static final String obterConfiguracoes =
			"select idConfiguracao, percentagemTotal, limiteTotal, percentagemElegivel " +
				"from configuracoes ";
	
	/**
	 * Gets the app configuration 
	 * 
	 * @return The new object that represents the in-memory app configuration
	 * @throws PersistenceException 
	 */
	public static ConfiguracoesRowGateway getConfiguracoes () throws PersistenceException {
		PreparedStatement comando = null;
		ResultSet rs = null;
		try {
			// obter comando
			comando = DataSource.INSTANCE.prepare(obterConfiguracoes);			
			rs = comando.executeQuery();
			rs.next();
			// cria um novo cliente
			ConfiguracoesRowGateway config = new ConfiguracoesRowGateway();
			config.idConfiguracao = rs.getInt("idConfiguracao");
			config.limiteTotal = rs.getDouble("limiteTotal");
			config.percentagemTotal = rs.getDouble("percentagemTotal");
			config.percentagemElegivel = rs.getDouble("percentagemElegivel");
			return config;
		} catch (SQLException e) {
			throw new PersistenceException("Erro interno ao obter a configura��o da aplica��o", e);
		} finally {
			DataSource.INSTANCE.close(rs);
			DataSource.INSTANCE.close(comando);
		} 
	}
}
