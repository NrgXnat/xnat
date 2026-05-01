package org.nrg.dcm.xnat.utils;

import org.nrg.xdat.bean.XnatAddfieldBean;
import org.nrg.xdat.bean.XnatImagescandataBean;
import org.nrg.xdat.model.XnatAddfieldI;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DicomMappingUtils {

    private static class AddParamMethodAndBean {
        Method method;
        Class addParamBeanClass;

        public AddParamMethodAndBean(Method method, Class addParamBeanClass) {
            this.method = method;
            this.addParamBeanClass = addParamBeanClass;
        }
    }
    /**
     * Verify that scan bean has an addParam method, either the default e.g.,
     * {@link org.nrg.xdat.bean.XnatMrscandataBean#addParameters_addparam(XnatAddfieldI)}} or a customized one
     *
     * @param elementBeanClass the scan bean class
     * @param schemaElement the scan schema element
     * @throws NoSuchMethodException if it doesn't have this method
     * @throws ElementNotFoundException if schema element for custom add param bean cannot be found
     * @throws ClassNotFoundException if bean class for custom add param bean cannot be found
     * @throws XFTInitException if XNAT not initialized
     */
    public static AddParamMethodAndBean checkBeanHasAddParamMethod(Class<?> elementBeanClass, String schemaElement)
            throws NoSuchMethodException, XFTInitException, ElementNotFoundException, ClassNotFoundException {
        try {
            // validate that this element has an addParam method
            return new AddParamMethodAndBean(
                    elementBeanClass.getMethod("addParameters_addparam", XnatAddfieldBean.class),
                    XnatAddfieldBean.class);
        } catch (NoSuchMethodException e) {
            Class<?> addParamBeanClass = SchemaElement.GetElement(schemaElement + "_addParam")
                    .getCorrespondingJavaBeanClass();
            return new AddParamMethodAndBean(
                    elementBeanClass.getMethod("addParameters_addparam", addParamBeanClass),
                    addParamBeanClass);
        }
    }

    /**
     * Find addParamMethod for scan and invoke it, passing name and value to the add param bean
     * @param scanBean the scan bean
     * @param name the parameter name
     * @param value the parameter value
     * @throws NoSuchMethodException if scan bean doesn't have this method
     * @throws ElementNotFoundException if schema element for custom add param bean cannot be found
     * @throws ClassNotFoundException if bean class for custom add param bean cannot be found
     * @throws XFTInitException if XNAT not initialized
     * @throws IllegalAccessException for issues invoking the method
     * @throws InvocationTargetException for issues invoking the method
     * @throws InstantiationException for issues invoking the method
     */
    public static void findAndInvokeAddParamMethod(XnatImagescandataBean scanBean, String name, String value)
            throws NoSuchMethodException, XFTInitException, ElementNotFoundException, ClassNotFoundException,
            IllegalAccessException, InvocationTargetException, InstantiationException {

        AddParamMethodAndBean methodAndBean = checkBeanHasAddParamMethod(scanBean.getClass(),
                scanBean.getFullSchemaElementName());

        Object addParamBeanInstance;
        if (XnatAddfieldBean.class.isAssignableFrom(methodAndBean.addParamBeanClass)) {
            addParamBeanInstance = new XnatAddfieldBean();
            ((XnatAddfieldBean) addParamBeanInstance).setName(name);
            ((XnatAddfieldBean) addParamBeanInstance).setAddfield(value);
        } else {
            addParamBeanInstance = makeAndPopulateAddParamBean(methodAndBean.addParamBeanClass, name, value);
        }

        // Add addParam bean instance to scan
        methodAndBean.method.invoke(scanBean, addParamBeanInstance);
    }

    /**
     * Instantiate and populate a custom addParam bean
     * @param addParamBeanClass the bean class
     * @param name the parameter name
     * @param value the parameter value
     * @param <T> the bean type
     * @return instance of the custom bean type
     * @throws NoSuchMethodException if we can't find method to set name and/or value on this param bean
     * @throws IllegalAccessException for issues invoking the set name or value method on this param bean
     * @throws InvocationTargetException for issues invoking the set name or value method on this param bean
     * @throws InstantiationException for issues invoking the set name or value method on this param bean
     */
    private static <T> T makeAndPopulateAddParamBean(Class<T> addParamBeanClass, String name, String value)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        final T addParamBeanInstance = addParamBeanClass.getConstructor().newInstance();
        // set params on the addParam bean
        Method setNameMethod = addParamBeanClass.getMethod("setName", String.class);
        setNameMethod.invoke(addParamBeanInstance, name);
        Method setParamMethod = addParamBeanClass.getMethod("setAddparam", String.class);
        setParamMethod.invoke(addParamBeanInstance, value);
        return addParamBeanInstance;
    }
}
