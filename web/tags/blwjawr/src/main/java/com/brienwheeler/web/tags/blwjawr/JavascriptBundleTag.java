package com.brienwheeler.web.tags.blwjawr;

import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.renderer.BundleRenderer;

public class JavascriptBundleTag extends net.jawr.web.taglib.JavascriptBundleTag
{
    protected String otherAttrs;

    public void setOtherAttrs(String otherAttrs) {
        this.otherAttrs = otherAttrs;
    }

    @Override
    protected BundleRenderer createRenderer() {
        if(null == pageContext.getServletContext().getAttribute(JawrConstant.JS_CONTEXT_ATTRIBUTE))
            throw new IllegalStateException("ResourceBundlesHandler not present in servlet context. Initialization of Jawr either failed or never occurred.");

        ResourceBundlesHandler rsHandler = (ResourceBundlesHandler) pageContext.getServletContext().getAttribute(JawrConstant.JS_CONTEXT_ATTRIBUTE);
        return new JavascriptHTMLBundleLinkRenderer(rsHandler, useRandomParam, otherAttrs);
    }
}
