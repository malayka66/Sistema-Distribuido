package ui.events;

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import ui.helpers.BancoHelper;
import ui.helpers.ErroHelper;
import domain.Activus;
import domain.ApplicationException;
import domain.BancosHandler;

@WebServlet("/Internal/criarBanco")
public class CriarBancoEvent extends Event {

	@EJB private BancosHandlerRemote bancosHandler;
	
	
	@Override
	public void process() throws ServletException, IOException,
			ApplicationException {
		
		BancoHelper nbh = new BancoHelper();
		ErroHelper eh = new ErroHelper();
		request.setAttribute("helper", nbh);
		request.setAttribute("erroHelper", eh);
		//validaInput(nbh);
		
		//String designacao = request.getParameter("designacao");
		//String sigla = request.getParameter("sigla");
		
		nbh.setDesignacao(request.getParameter("designacao"));
		nbh.setSigla(request.getParameter("sigla"));
		
		
		/**if(!isFilledDesignacao(nbh,nbh.getDesignacao(),"Erro n�o preencheu a designa��o") || !isFilledSigla(nbh,nbh.getDesignacao(),"Erro n�o preencheu a sigla")){
			System.out.println("Aqui2");
			request.getRequestDispatcher("../novoBanco.jsp").forward(request, response);
		}**/
		
		try{
			bancosHandler.adicionarBanco(nbh.getSigla(),nbh.getDesignacao());	
			nbh.clearFields();
			nbh.addMessage("Banco criado com sucesso");
		}catch(ApplicationException e){
			nbh.addMessage("Erro ao criar banco"+ e.getMessage());
		}
		
		request.getRequestDispatcher("../novoBanco.jsp").forward(request, response);

	}
	
	private void validaInput(BancoHelper nbh){
		nbh.setDesignacao(request.getParameter("designacao"));
		nbh.setSigla(request.getParameter("sigla"));
	}
}
