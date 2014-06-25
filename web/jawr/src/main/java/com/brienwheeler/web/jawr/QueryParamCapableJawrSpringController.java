package com.brienwheeler.web.jawr;

import net.jawr.web.servlet.JawrSpringController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

public class QueryParamCapableJawrSpringController extends JawrSpringController
{
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String jawrHC = request.getParameter("jawrHC");
        if (jawrHC != null) {
            request = new RewrittenURIRequest(request, "/" + jawrHC + request.getRequestURI());
        }
        return super.handleRequest(request, response);
    }

    private static class RewrittenURIRequest extends HttpServletRequestWrapper {
        private final StringBuffer requestURL;
        private final String requestURI;

        private RewrittenURIRequest(HttpServletRequest request, String requestURI) {
            super(request);
            this.requestURI = requestURI;

            // update requestURL too
            this.requestURL = request.getRequestURL();
            requestURL.replace(requestURL.lastIndexOf(request.getRequestURI()), requestURL.length(), requestURI);
        }

        @Override
        public String getRequestURI() {
            return requestURI;
        }

        @Override
        public StringBuffer getRequestURL() {
            return requestURL;
        }
    }
}
