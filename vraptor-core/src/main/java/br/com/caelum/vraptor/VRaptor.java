/***
 *
 * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package br.com.caelum.vraptor;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.config.BasicConfiguration;
import br.com.caelum.vraptor.core.RequestExecution;
import br.com.caelum.vraptor.core.StaticContentHandler;
import br.com.caelum.vraptor.core.VRaptorRequest;
import br.com.caelum.vraptor.ioc.ContainerProvider;

/**
 * VRaptor entry point. Provider configuration through init parameter
 * 
 * @author Guilherme Silveira
 * @author Fabio Kung
 */
public class VRaptor implements Filter {
    private ContainerProvider provider;
    private ServletContext servletContext;
    
    private StaticContentHandler staticHandler;

    private static final Logger logger = LoggerFactory.getLogger(VRaptor.class);

    public void destroy() {
        provider.stop();
        provider = null;
        servletContext = null;
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
            ServletException {

        if (!(req instanceof HttpServletRequest) || !(res instanceof HttpServletResponse)) {
            throw new ServletException(
                    "VRaptor must be run inside a Servlet environment. Portlets and others aren't supported.");
        }

        HttpServletRequest webRequest = (HttpServletRequest) req;
        HttpServletResponse webResponse = (HttpServletResponse) res;

        if (staticHandler.requestingStaticFile(webRequest)) {
            staticHandler.deferProcessingToContainer(chain, webRequest, webResponse);
            return;
        }

        VRaptorRequest request = new VRaptorRequest(servletContext, webRequest, webResponse);
        try {
            provider.provide(request).instanceFor(RequestExecution.class).execute();
        } catch (VRaptorException e) {
            throw new ServletException(e);
        }
    }

    public void init(FilterConfig cfg) throws ServletException {
        servletContext = cfg.getServletContext();
        BasicConfiguration config = new BasicConfiguration(servletContext);
        this.provider = config.getProvider();
        this.provider.start(servletContext);
        this.staticHandler = new StaticContentHandler(servletContext);
        logger.info("VRaptor 3 successfuly initialized");
    }

}
