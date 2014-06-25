package com.brienwheeler.web.tags.blwjawr;

import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavascriptHTMLBundleLinkRenderer extends net.jawr.web.resource.bundle.renderer.JavascriptHTMLBundleLinkRenderer
{
    private static final Logger log = LoggerFactory.getLogger(JavascriptHTMLBundleLinkRenderer.class);

    private static final String HASHCODE_REGEX = "/((gzip_)?N?[0-9]+)(/.*)";
    private static final Pattern hashCodePattern = Pattern.compile(HASHCODE_REGEX);

    protected String otherAttrs;
    protected boolean useQueryParam = false;

    public void setOtherAttrs(String otherAttrs) {
        this.otherAttrs = otherAttrs;
    }

    public void setUseQueryParam(boolean useQueryParam) {
        this.useQueryParam = useQueryParam;
    }

    protected String renderLink(String fullPath) {
        if (useQueryParam) {
            Matcher matcher = hashCodePattern.matcher(fullPath);
            if (matcher.matches()) {
                if (log.isDebugEnabled())
                    log.debug("detected hash code prefix: " + matcher.group(1) + ", base bundle path: " +
                            matcher.group(3));
                fullPath = matcher.group(3) + "?jawrHC=" + matcher.group(1);
            }
        }

        StringBuffer sb = new StringBuffer("<script type=\"text/javascript\" src=\"");
        sb.append(fullPath).append("\"");
        if (StringUtils.isNotEmpty(otherAttrs)) {
            sb.append(" ").append(otherAttrs);
        }
        sb.append("></script>\n");
        return sb.toString();
    }
}
