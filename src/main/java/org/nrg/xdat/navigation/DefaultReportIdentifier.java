package org.nrg.xdat.navigation;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.modules.actions.DisplayItemAction;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xdat.turbine.utils.TurbineUtils;

/**
 * Created by mmckay01 on 1/16/14.
 */
public class DefaultReportIdentifier implements DefaultReportIdentifierI{
    public DefaultReportIdentifier(){

    }
    public String identifyReport(RunData data, Context context) throws Exception{
        SchemaElementI se = TurbineUtils.GetSchemaElementBySearch(data);
        if (se != null)
        {
            String templateName = DisplayItemAction.GetReportScreen(se);
            return templateName;
        }else{
            throw new Exception("No Element Found. ");
        }
    }
}
