package com.brienwheeler.web.tags.blwjawr;

import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.util.StringUtils;

public class JavascriptHTMLBundleLinkRenderer extends net.jawr.web.resource.bundle.renderer.JavascriptHTMLBundleLinkRenderer
{
    protected String otherAttrs;

    public void setOtherAttrs(String otherAttrs) {
        this.otherAttrs = otherAttrs;
    }

    protected String renderLink(String fullPath) {
        StringBuffer sb = new StringBuffer("<script type=\"text/javascript\" src=\"");
        sb.append(fullPath).append("\"");
        if (StringUtils.isNotEmpty(otherAttrs)) {
            sb.append(" ").append(otherAttrs);
        }
        sb.append("></script>\n");
        return sb.toString();
    }
}
