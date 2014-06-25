package com.brienwheeler.web.tags.blwjawr;

import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.renderer.BundleRenderer;
import net.jawr.web.util.StringUtils;

public class JavascriptBundleTag extends net.jawr.web.taglib.JavascriptBundleTag
{
    protected String otherAttrs;
    protected String useQueryParam;

    public void setOtherAttrs(String otherAttrs) {
        this.otherAttrs = otherAttrs;
    }

    public void setUseQueryParam(String useQueryParam) {
        this.useQueryParam = useQueryParam;
    }

    @Override
    protected BundleRenderer createRenderer(ResourceBundlesHandler rsHandler, Boolean useRandomParam) {
        Boolean asyncFlag = null;
        if(StringUtils.isNotEmpty(async)){
            asyncFlag = Boolean.valueOf(async);
        }
        Boolean deferFlag = null;
        if(StringUtils.isNotEmpty(defer)){
            deferFlag = Boolean.valueOf(defer);
        }
        boolean queryParamFlag = false;
        if(StringUtils.isNotEmpty(useQueryParam)){
            queryParamFlag = Boolean.valueOf(useQueryParam);
        }

        JavascriptHTMLBundleLinkRenderer renderer = new JavascriptHTMLBundleLinkRenderer();
        renderer.setOtherAttrs(otherAttrs);
        renderer.setUseQueryParam(queryParamFlag);
        renderer.init(rsHandler, useRandomParam, asyncFlag, deferFlag);
        return renderer;
    }
}
