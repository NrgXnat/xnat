package org.nrg.dcm.xnat;

import org.dcm4che3.data.Tag;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;
import org.nrg.attr.ConversionFailureException;
import org.nrg.attr.ExtAttrValue;
import org.nrg.dcm.AttrDefs;
import org.nrg.dcm.MutableAttrDefs;
import org.nrg.session.BeanBuilder;
import org.nrg.xdat.bean.XnatContrastbolusv2Bean;
import org.nrg.xdat.bean.base.BaseElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.nrg.dcm.DicomAttributes.*;

public class XAScanAttributes {
    private XAScanAttributes() {} // no instantiation

    static public AttrDefs get() { return s; }

    static final private MutableAttrDefs s = new MutableAttrDefs(ImageScanAttributes.get());

    static {
        s.add(new OrientationAttribute("parameters/orientation"));
        s.add("parameters/imageType", Tag.ImageType);
        s.add(new ImageFOVAttribute("parameters/fov"));
        s.add("parameters/derivation", Tag.DerivationDescription);
        s.add("parameters/options", Tag.ScanOptions);
        s.add("parameters/derivation", Tag.DerivationDescription);
        s.add("contrasts/contrastBolusV2", Tag.ContrastBolusAgent);
        s.add("contrasts/contrastBolusV2", CONTRAST_BOLUS_SEQUENCE);
    }

    private final static Set<String> nullValues = ImmutableSet.of("", "null");

    private final static String CONTRAST_BOLUS = "contrasts/contrastBolusV2";
    private final static BeanBuilder contrastBolusBeanBuilder = new BeanBuilder() {
        public Collection<BaseElement> buildBeans(final ExtAttrValue contrastBolusValue) throws ConversionFailureException {
            final Collection<BaseElement> beans = new ArrayList<>();
            String contrastBolus = contrastBolusValue.getText();
            if(StringUtils.endsWith(contrastBolus, "-text")){
                contrastBolus = StringUtils.stripEnd(contrastBolus,"-text");
            }
            if (null == contrastBolus || nullValues.contains(contrastBolus)) {
                return beans;
            }
            final XnatContrastbolusv2Bean contrastBolusBean = new XnatContrastbolusv2Bean();
            contrastBolusBean.setAgent(contrastBolus);
            beans.add(contrastBolusBean);
            return beans;
        }
    };
    private final static Map<String,BeanBuilder> beanBuilders = ImmutableMap.of(CONTRAST_BOLUS, contrastBolusBeanBuilder);

    public final static Map<String,BeanBuilder> getBeanBuilders() { return beanBuilders; }
}
