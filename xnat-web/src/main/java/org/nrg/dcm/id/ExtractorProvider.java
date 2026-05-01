package org.nrg.dcm.id;

import org.nrg.dcm.Extractor;
import org.nrg.dcm.xnat.AbstractConditionalAttrDef;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

public interface ExtractorProvider {
    List<Extractor> provide(CompositeDicomObjectIdentifier.ExtractorType type);
    List<AbstractConditionalAttrDef.Rule> provideRules(CompositeDicomObjectIdentifier.ExtractorType type);
    List<Extractor> parseAsExtractors( Reader source) throws IOException;
    List<AbstractConditionalAttrDef.Rule> parseAsRules( Reader source) throws IOException;
    List<String> validate( CompositeDicomObjectIdentifier.ExtractorType type);
    List<String> validate();

}
