package services.persistence.rowDataGateway;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import services.persistence.DataSource;
import services.persistence.PersistenceException;
import services.persistence.RecordNotFoundException;

/**
 * A representa��o de um registo da tabela de clientes como um
 * objeto em mem�ria.
 *	
 * Chamadas de aten��o:
 * 1. As string de SQL n�o deveriam estar nesta classe porque assim
 * limita a portabilidade em rela��o �s bases de dados
 * 
 * 2. Esta classe n�o lan�a exce��es SQL, mas sim exce��es pr�prias 
 * deste pacote. No entanto, as exce��es SQL (de mais baixo n�vel) s�o
 * "embrulhadas" na exce��o enviada e podem ser obtidas utilizando o m�todo
 * getCause da classe exception.
 * 
 * 3. No m�todo para inserir um novo cliente usa-se o m�todo GetGeneratedKeys
 * para obter o idCliente que � um campo gerado automaticamente (auto-incrementado)
 * pela base de dados.
 * 
 * 4. Preparar o comando para obter as chaves geradas leva um argumento extra. Veja
 * DataSource.prepareGetGenKey(...).
 * 
 * 5. Os recursos de acesso � base de dados devem ser fechados o mais rapidamente 
 * poss�vel. Inclu� um bloco finally que trata de fechar os ResultSets e Statements
 * em cada m�todo. Recorde-se que o bloco finally � sempre executado quer tenha sido
 * lan�ada uma exce��o quer n�o tenha. Mesmo que o bloco try contenha um return, como
 * acontece no m�todo getClientePorId, o bloco finally � executado antes da fun��o 
 * terminar. Podem consultar mais informa��o sobre exce��es em 
 * http://download.oracle.com/javase/tutorial/essential/exceptions/index.html
 * A partir da vers�o 7 do Java existe um novo construtor try-with-resources 
 * que facilita esta tarefa. Pode ver mais informa��o em 
 * http://download.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html.
 * 
 * 6. Quando se fecha, por exemplo, um result set, o m�todo close pode mandar uma
 * exce��o SQL. N�o queremos que esta exce��o seja lan�ada no bloco finally sen�o
 * outra qualquer exce��o lan�ada anteriormente (no bloco try ou catch) � simplemente
 * ignorada e � lan�ada esta do finally. De qualquer forma n�o h� nada que
 * possamos fazer caso o close lance uma exce��o. Da� ignoramos esta exce��o. 
 * Ver DataSource.close. Este problema do lan�amento de exce��es no bloco finally
 * � tratado, por exemplo, no livro Thinking in Java (http://www.mindview.net/Books/TIJ/)
 * sob o nome "Pitfall: the lost exception". Podem encontrar o texto no cap�tulo 9
 * em (http://www.smart2help.com/e-books/tij-3rd-edition/).
 * 
 * 7. A utiliza��o de Singletons (como � o caso do DataSource) dificulta os testes.
 * Programei assim para ser mais simples de perceberem, mas na vers�o 2 resolvo 
 * este problema, bem como o problema das constantes de SQL. A solu��o involve
 * a utiliza��o de f�bricas de objectos e � um pouco mais complicada de seguir 
 * de in�cio.
 * 
 * 8. A utiliza��o de m�todos est�ticos para se obter informa��o da base
 * de dados � s� uma forma de resolver o problema sobre "quem cria os objectos
 * desta classe". Outra forma � ter uma classe espec�fica para obter a informa��o
 * da base de dados e criar estes objectos. Isto encontra-se exemplificado na
 * vers�o "sofisticada" deste exemplo.
 * 
 * @author fmartins
 *
 */
public class ClientesRowGateway {
	// atributos que representam os campos da tabela
	private int idCliente;
	private int npc;
	private String designacao;
	private int telefone;
	private int idDesconto;
	
	// Getters e setters para os atributos
	public int getNpc() {
		return npc;
	}

	public void setNpc(int npc) {
		this.npc = npc;
	}

	public String getDesignacao() {
		return designacao;
	}

	public void setDesignacao(String designacao) {
		this.designacao = designacao;
	}

	public int getTelefone() {
		return telefone;
	}

	public void setTelefone(int telefone) {
		this.telefone = telefone;
	}

	public int getIdDesconto() {
		return idDesconto;
	}

	public void setIdDesconto(int idDesconto) {
		this.idDesconto = idDesconto;
	}

	// n�o necessitamos de um setter porque o Id � gerado automaticamente.
	public int getIdCliente() {
		return idCliente;
	}


	// actualizar a base de dados
	private static final String inserirClienteSQL = 
			"insert into clientes (npc, designacao, telefone, idDesconto) " +
			"values (?, ?, ?, ?)";

	public void insert () throws PersistenceException {
		PreparedStatement comando = null;
		try {
			// obter comando
			comando = DataSource.INSTANCE.prepareGetGenKey(inserirClienteSQL);			
			// set statement arguments
			comando.setInt(1, npc);
			comando.setString(2, designacao);
			comando.setInt(3, telefone);
			comando.setInt(4, idDesconto);
			// executa SQL
			comando.executeUpdate();
			// obter o idCliente gerado automaticamente
			ResultSet rs = comando.getGeneratedKeys();
			rs.next(); 
			idCliente = rs.getInt(1);
			rs.close();
		} catch (SQLException e) {
			throw new PersistenceException ("Erro interno!", e);
		} finally {
			DataSource.INSTANCE.close(comando);
		}
	}
	
	private static final String obterClienteNPC = 
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
	public static ClientesRowGateway getClientePorNPC (int npc) throws PersistenceException {
		PreparedStatement comando = null;
		ResultSet rs = null;
		try {
			// obter comando
			comando = DataSource.INSTANCE.prepare(obterClienteNPC);			
			// set statement arguments
			comando.setInt(1, npc);
			// executa SQL
			rs = comando.executeQuery();
			// cria um novo cliente
			return carregarCliente(rs);
		} catch (SQLException e) {
			throw new PersistenceException("Erro interno ao adicionar cliente", e);
		} finally {
			DataSource.INSTANCE.close(rs);
			DataSource.INSTANCE.close(comando);
		}
	}
	
	private static final String	obterClienteId =
				"select idCliente, npc, designacao, telefone, idDesconto " +
					"from clientes " +
					"where idCliente = ?";

	/**
	 * Gets a client by its Id number
	 * 
	 * @param idCliente The id of the client to search for
	 * @return The new object that represents an in-memory client
	 * @throws PersistenceException
	 */
	public static ClientesRowGateway getClientePorId(int idCliente) throws PersistenceException {
		PreparedStatement comando = null;
		ResultSet rs = null;
		try {
			// obter comando
			comando = DataSource.INSTANCE.prepare(obterClienteId);			
			// set statement arguments
			comando.setInt(1, idCliente);
			// executa SQL
			rs = comando.executeQuery();
			// cria um novo cliente
			return carregarCliente(rs);
		} catch (SQLException e) {
			throw new PersistenceException("Erro interno ao adicionar cliente", e);
		} finally {
			DataSource.INSTANCE.close(rs);
			DataSource.INSTANCE.close(comando);
		}
	}
	
	private static ClientesRowGateway carregarCliente(ResultSet rs) throws RecordNotFoundException {
		try {
			rs.next();
			ClientesRowGateway novoCliente = new ClientesRowGateway();
			novoCliente.idCliente = rs.getInt("idCliente");
			novoCliente.npc = rs.getInt("npc");
			novoCliente.designacao = rs.getString("designacao");
			novoCliente.telefone = rs.getInt("telefone");
			novoCliente.idDesconto = rs.getInt("idDesconto");
			return novoCliente;
		} catch (SQLException e) {
			throw new RecordNotFoundException ("Cliente n�o existe", e);
		}
	}
}
