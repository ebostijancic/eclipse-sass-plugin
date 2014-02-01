package at.workflow.webdesk.tools.hibernate;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.orm.hibernate3.support.OpenSessionInViewInterceptor;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.WebRequestHandlerInterceptorAdapter;


/**
 * This is a filter which calls OSIV Interceptors declared as beans inside the applicationcontext. 
 * Its main purpose is to decouple other webdesk modules from the process to put elements into web.xml.
 * 
 * @author ggruber
 *
 */
public class MultipleOpenSessionInViewFilter extends OncePerRequestFilter {

	Map<String,OpenSessionInViewInterceptor> osivViewInterceptors;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		if (osivViewInterceptors==null) {
			WebApplicationContext wac =
				WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
			this.osivViewInterceptors = wac.getBeansOfType(OpenSessionInViewInterceptor.class);
		}
		
		
		if (osivViewInterceptors==null || osivViewInterceptors.size()==0) {
			filterChain.doFilter(request, response);
		} else {
			Exception ex = null;
			try {
				osivPreHandle(request, response);
				
				filterChain.doFilter(request, response);
				
				osivPostHandle(request, response);
			} catch (Exception e) {
				ex = e;
			} finally {
				osivAfterCompletion(request,response, ex);
			}
		}
		
	}

	private void osivAfterCompletion(HttpServletRequest request, HttpServletResponse response, Exception ex) throws ServletException {
		
		for(Iterator<String> itr = this.osivViewInterceptors.keySet().iterator(); itr.hasNext();) {
			OpenSessionInViewInterceptor osivInterceptor = osivViewInterceptors.get(itr.next());
			HandlerInterceptor osiv = new WebRequestHandlerInterceptorAdapter(osivInterceptor);
			try {
				osiv.afterCompletion(request, response, null, ex);
			} catch (Exception e) {
				this.logger.error("problems with osiv: afterCompletion..", e);
			}
		}
	}

	private void osivPostHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {

		for(Iterator<String> itr = this.osivViewInterceptors.keySet().iterator(); itr.hasNext();) {
			OpenSessionInViewInterceptor osivInterceptor = osivViewInterceptors.get(itr.next());
			HandlerInterceptor osiv = new WebRequestHandlerInterceptorAdapter(osivInterceptor);
			osiv.postHandle(request, response, null, null);
		}
	}

	private void osivPreHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		for(Iterator<String> itr = this.osivViewInterceptors.keySet().iterator(); itr.hasNext();) {
			OpenSessionInViewInterceptor osivInterceptor = osivViewInterceptors.get(itr.next());
			HandlerInterceptor osiv = new WebRequestHandlerInterceptorAdapter(osivInterceptor);
			osiv.preHandle(request, response, null);
		}
	}

}
