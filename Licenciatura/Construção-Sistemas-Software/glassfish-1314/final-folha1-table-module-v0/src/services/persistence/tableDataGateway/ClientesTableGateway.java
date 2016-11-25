package services.persistence.tableDataGateway;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import services.persistence.DataSource;

/**
 * Table Data Gateway para a tabela de clientes
 *
 * Chamadas de aten��o:
 * 1. Esta classe est� implementada como um singleton. � uma das abordagens
 * poss�veis � sua implementa��o. Outra alternativa �, por exemplo, implementar
 * todos os m�todos de forma est�tica. Nenhuma das abordagens � genuinamente orientada
 * a objetos e traz complica��es no futuro. Na vers�o 2.0 deste pacote irei implementar
 * as classes resolvendo este problema.
 * 
 * 2. As strings de SQL tamb�m n�o deveriam fazer parte desta classe. Ver a discuss�o
 * em services.persistence.rowGateway.ClientesRowGateway sobre o assunto e a vers�o
 * 2.0 para uma eventual solu��o do problema.
 * 
 * 3. Nesta abordagem ao acesso aos dados os m�todos devolvem conjuntos resultado
 * com a informa��o obtida das sele��es � base de dados. Estou a utilizar a 
 * classe ResultSet da API JDBC. Este abordagem tem alguns problemas, em particular,
 * o de n�o se fechar a ResultSet nem o Statement. Isto provoca a utiliza��o 
 * de recursos na base de dados de forma n�o �tima. S� quando os objetos 
 * correspondentes aos ResultSets e Statements forem reclamados (garbage collected)
 * � que estes recursos ser�o libertados.
 * 
 * 4. Os m�todos desta classe n�o deveriam lan�ar a exce��o SQLException. A
 * camada de neg�cio n�o deveria ter conhecimento sobre a forma como a persist�ncia
 * � implementada. Imagine-se que se resolve substituir o acesso a base de dados
 * por um acesso a ficheiros; ent�o, a exce��o lan�ada passaria a ser IOException,
 * em vez da atual, o que traria impacto na camada de neg�cio. Utilizar uma exce��o
 * pr�pria da camada e "embrulhar" as exce��es de mais baixo n�vel como 
 * descritivas da causa (real) do problema � uma melhor solu��o. Veja, como exemplo,
 * as classes do pacote services.persistence.rowGateway.
 * 
 * 5. Todos estas decis�es quando ao desenho das classes deste pocote 
 * foram tomadas de forma a se perceber a ess�ncia do Table Data Gateway. Claro, 
 * que introduzem outros problemas, alguns dos quais discutidos acima, que 
 * ser�o resolvidos na vers�o seguinte.
 * 
 * @author fmartins
 *
 */
public enum ClientesTableGateway {
	INSTANCE;
	
	private static String obterClienteNPCSQL =
			   "select idCliente, npc, designacao, telefone, idDesconto " +
					"from clientes " +
					"where npc = ?";
	/**
	 * Gets a client by its NPC 
	 * 
	 * @param npc The NPC number of the client to search for
	 * @return The result set of the query
	 * @throws SQLException 
	 */
	public ResultSet getClientePorNPC (int npc) throws SQLException {
		// obter comando
		PreparedStatement comando = DataSource.INSTANCE.prepare(obterClienteNPCSQL);
		// set statement arguments
		comando.setInt(1, npc);
		// executa SQL
		return comando.executeQuery();
	}

	
	private static String obterClienteIdSQL = 
			"select idCliente, npc, designacao, telefone, idDesconto " +
				"from clientes " +
				"where idCliente = ?";
	
	
	/**
	 * Gets a client by its Id number
	 * 
	 * @param idCliente The id of the client to search for
	 * @return The result set of the query
	 * @throws SQLException
	 */
	public ResultSet getClientePorId(int idCliente) throws SQLException {
		// obter comando
		PreparedStatement comando = DataSource.INSTANCE.prepare(obterClienteIdSQL);			
		// set statement arguments
		comando.setInt(1, idCliente);
		// executa SQL
		return comando.executeQuery();
	}
	
	
	private static String inserirClienteSQL = 
			"insert into clientes (npc, designacao, telefone, idDesconto) " +
				"values (?, ?, ?, ?)";
	
	/**
	 * Add a new client to provided that its NPC is not in the database
	 * 
	 * @param npc The NPC of the customer
	 * @param designacao The customer's name
	 * @param telefone The customer's phone number
	 * @param desconto The discount type for the customer
	 * @throws SQLException
	 */
	public void adicionarCliente (int npc, String designacao, int telefone, int desconto) throws SQLException {
		// obter comando
		PreparedStatement comando = DataSource.INSTANCE.prepare(inserirClienteSQL);			
		// set statement arguments
		comando.setInt(1, npc);
		comando.setString(2, designacao);
		comando.setInt(3, telefone);
		comando.setInt(4, desconto);
		// executa SQL
		comando.executeUpdate();
	}
}
