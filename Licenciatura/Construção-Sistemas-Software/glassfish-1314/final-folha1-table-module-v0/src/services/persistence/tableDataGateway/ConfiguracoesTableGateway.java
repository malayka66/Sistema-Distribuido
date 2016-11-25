 package services.persistence.tableDataGateway;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import services.persistence.DataSource;

/**
 * Representa��o da tabela de Configura��es
 * 
 * Chamadas de aten��o:
 * 1. Ver classe ClientesDataGateway
 * 
 * @author fmartins
 *
 */
public enum ConfiguracoesTableGateway {
	INSTANCE;
	
	private static String obterConfiguracoes = 
			"select percentagemTotal, limiteTotal, percentagemElegivel " +
				"from configuracoes ";
	
	/**
	 * Gets configuration settings
	 * 
	 * @return The result set of the query
	 * @throws SQLException
	 */
	public ResultSet getConfiguracoes () throws SQLException {
		// obter comando
		PreparedStatement comando = DataSource.INSTANCE.prepare(obterConfiguracoes);
		// executa SQL
		return comando.executeQuery();
	}
	
}
