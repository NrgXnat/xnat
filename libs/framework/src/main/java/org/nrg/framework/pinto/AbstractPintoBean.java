/*
 * framework: org.nrg.framework.pinto.AbstractPintoBean
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.pinto;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.nrg.framework.utilities.Reflection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.CharBuffer;
import java.util.*;

public abstract class AbstractPintoBean {

    /**
     * Processes the incoming arguments, setting the bean's print stream to {@link System#out}.
     *
     * @param parent    The parent object.
     * @param arguments Incoming parameter arguments to process.
     *
     * @throws PintoException When something goes wrong constructing the bean.
     */
    protected AbstractPintoBean(final Object parent, final String[] arguments) throws PintoException {
        this(parent, arguments, System.out);
    }

    /**
     * Processes the incoming arguments, setting the bean's print stream to the submitted parameter.
     *
     * @param parent      The parent object.
     * @param arguments   Incoming parameter arguments to process.
     * @param printStream Indicates the print stream to be used for printing output.
     *
     * @throws PintoException When something goes wrong constructing the bean.
     */
    protected AbstractPintoBean(final Object parent, final String[] arguments, final PrintStream printStream) throws PintoException {
        assert parent != null : "You must specify the parent for your pinto bean.";

        _arguments = Arrays.asList(arguments);
        _parent = parent;

        try {
            scan();
            harvest();
            prune();

            _printStream = getOutputStream();

            if (getHelp()) {
                displayHelp();
            } else if (getVersion()) {
                displayVersion();
            } else {
                validate();
            }
        } catch (PintoException exception) {
            String parameter = exception.getParameter();
            if (!StringUtils.isBlank(parameter)) {
                printStream.println("Found error with parameter: " + parameter + ":");
            }
            printStream.println("Error type: " + exception.getType());
            printStream.println(exception.getMessage());
            throw exception;
        }
    }

    /**
     * Provides an opportunity for subclasses to validate the processed parameters and their arguments.
     *
     * @throws PintoException When validation fails for some reason.
     */
    abstract public void validate() throws PintoException;

    @SuppressWarnings("unused")
    public void setPrintStream(PrintStream printStream) {
        _printStream = printStream;
    }

    public PrintStream getPrintStream() {
        return _printStream;
    }

    /**
     * The setter for the help option.
     *
     * @param help Incoming parameter.
     */
    @Parameter(value = "h", longOption = "help", help = "Displays this help text.", argCount = ArgCount.StandAlone)
    public void setHelp(boolean help) {
        _help = help;
    }

    /**
     * The getter for the help option.
     *
     * @return Gets the value for the help option.
     */
    @Value("h")
    public boolean getHelp() {
        return _help;
    }

    /**
     * The setter for the version option.
     *
     * @param version Incoming parameter.
     */
    @Parameter(value = "v", longOption = "version", argCount = ArgCount.StandAlone, help = "Displays the version of this application.")
    public void setVersion(boolean version) {
        _version = version;
    }

    /**
     * The getter for the version option.
     *
     * @return Gets the value for the version option.
     */
    @Value("v")
    public boolean getVersion() {
        return _version;
    }

    /**
     * The setter for the outputStreamAdapter option.
     *
     * @param outputStreamAdapter Incoming parameter.
     */
    @Parameter(value = "osa", longOption = "outputStreamAdapter", help = "Specifies an output stream adapter implementation to handle redirecting the output from your application.")
    public void setOutputStreamAdapter(String outputStreamAdapter) {
        _outputStreamAdapter = outputStreamAdapter;
    }

    /**
     * The getter for the outputStreamAdapter option.
     *
     * @return Gets the value for the outputStreamAdapter option.
     */
    @Value("osa")
    public String getOutputStreamAdapter() {
        return _outputStreamAdapter;
    }

    /**
     * Returns any arguments that are posted on the end of the list of arguments but aren't associated with a
     * parameter.
     *
     * @return Any arguments on the end of the list of arguments not associated with a parameter.
     */
    @SuppressWarnings("unused")
    public List<String> getTrailingArguments() {
        return _trailing;
    }

    /**
     * Indicates whether application execution should continue based on submitted parameters. Common reasons for not
     * continuing include specifying help or version parameters.
     *
     * @return Whether application execution should continue once the pinto bean has been constructed.
     */
    public boolean getShouldContinue() {
        return !_help && !_version;
    }

    /**
     * Display the help for each of the available command-line parameters supported by this bean. The help is printed to
     * the stream specified by the {@link #setPrintStream(java.io.PrintStream)} property or passed in through the {@link
     * AbstractPintoBean#AbstractPintoBean(Object, String[], PrintStream)} constructor.
     */
    public void displayHelp() {
        if (_parametersByShortOption == null || _parametersByShortOption.size() == 0) {
            getPrintStream().println("No parameters found for this application!");
        } else {
            // TODO: Add an annotation to put application name, copyright info, and introductory help text on the class level.
            String           appName, copyright, introduction;
            int              pageWidth;
            PintoApplication application = _parent.getClass().getAnnotation(PintoApplication.class);
            if (application == null) {
                appName = _parent.getClass().getSimpleName();
                copyright = introduction = null;
                pageWidth = WIDTH;
            } else {
                appName = application.value();
                copyright = application.copyright();
                introduction = application.introduction();
                pageWidth = application.pageWidth();
            }

            getPrintStream().println(WordUtils.wrap(appName, pageWidth));
            if (!StringUtils.isBlank(copyright)) {
                getPrintStream().println(WordUtils.wrap(copyright, pageWidth));
            }
            if (!StringUtils.isBlank(introduction)) {
                getPrintStream().println();
                getPrintStream().println(WordUtils.wrap(introduction, pageWidth));
            }
            getPrintStream().println();

            for (ParameterData parameter : _parametersByShortOption.values()) {
                StringBuilder parameterText = new StringBuilder(PREFIX);
                parameterText.append(SHORT_OPTION_DELIMITER).append(parameter.getShortOption());
                if (parameter.hasLongOption()) {
                    parameterText.append(", ").append(LONG_OPTION_DELIMITER).append(parameter.getLongOption());
                }

                // If our parameter text is so long that it will either run into the hanging indent text or directly up
                // to the hanging indent text (i.e., no space left between them)...
                final int length = parameterText.length();
                if (length > HANGING_INDENT - 1) {
                    // Then add a new line
                    parameterText.append(INDENT_FILLER);
                } else {
                    parameterText.append(CharBuffer.allocate(HANGING_INDENT - length).toString().replace('\0', ' '));
                }

                parameterText.append(WordUtils.wrap(parameter.getHelp(), pageWidth - HANGING_INDENT, INDENT_FILLER, true));
                getPrintStream().println(parameterText.toString());
            }
        }
    }

    /**
     * Displays the version from the {@link PintoApplication annotation} on the parent class if available. If the
     * annotation is not available, this method tries to find a <b>getVersion()</b> method on the parent class and call
     * that, along with displaying the application name as the class name.
     *
     * @throws PintoException When an error occurs retrieving the parent version.
     */
    protected void displayVersion() throws PintoException {
        String           appName, version, copyright;
        PintoApplication application = _parent.getClass().getAnnotation(PintoApplication.class);
        if (application == null) {
            appName = _parent.getClass().getSimpleName();
            version = getVersionFromParent();
            copyright = null;
        } else {
            appName = resolveAttribute(application.value());
            version = application.version();
            if (StringUtils.isBlank(version)) {
                version = getVersionFromParent();
            } else {
                version = resolveAttribute(version);
            }
            copyright = resolveAttribute(application.copyright());
        }
        getPrintStream().println(appName + ", version " + version);
        if (!StringUtils.isBlank(copyright)) {
            getPrintStream().println(copyright);
        }
    }

    /**
     * Splits the key-value argument and adds it to the parameters map. This is a handy way to split lots of "x=y"-style
     * arguments into a more easily queried and manipulated map.
     *
     * @param keyValues A list of key-value arguments, with key and value separated by the default '=' delimiter.
     *
     * @return A map with the list items separated into key-value pairs.
     */
    @SuppressWarnings("unused")
    protected Map<String, String> addKeyValueListToParameter(final List<String> keyValues) {
        return addKeyValueListToParameter(keyValues, "=");
    }

    /**
     * Splits the key-value argument and adds it to the parameters map. This is a handy way to split lots of "x=y"-style
     * arguments into a more easily queried and manipulated map.
     *
     * @param keyValues A list of key-value arguments, with key and value separated by the indicated delimiter.
     * @param delimiter The delimiter on which key-value arguments should be split.
     *
     * @return A map with the list items separated into key-value pairs.
     */
    protected Map<String, String> addKeyValueListToParameter(final List<String> keyValues, final String delimiter) {
        final Map<String, String> parameters = new HashMap<>();
        for (final String keyValue : keyValues) {
            if (!StringUtils.isBlank(keyValue)) {
                String[] atoms = keyValue.split(delimiter, 2);
                if (atoms.length == 1 && parameters.containsKey(atoms[0])) {
                    parameters.remove(atoms[0]);
                } else {
                    parameters.put(atoms[0], atoms[1]);
                }
            }
        }
        return parameters;
    }

    /**
     * Converts a string to the type indicated by the <b>type</b> parameter. The ability of a pinto bean to convert
     * strings to any arbitrary type can be extended by overriding and extending this method.
     *
     * @param type     Indicates the type to which the argument should be converted.
     * @param argument The argument to be converted.
     *
     * @return An object of the indicated type from the given value.
     *
     * @throws PintoException If the argument can't be converted to the indicated type.
     */
    protected Object convertStringToType(final Class<?> type, final String argument) throws PintoException {
        Object object;
        if (type == String.class) {
            object = argument;
        } else if (type == Integer.class || type == int.class) {
            object = Integer.parseInt(argument);
        } else if (type == Long.class || type == long.class) {
            object = Long.parseLong(argument);
        } else if (type == Float.class || type == float.class) {
            object = Float.parseFloat(argument);
        } else if (type == Double.class || type == double.class) {
            object = Double.parseDouble(argument);
        } else if (type == Character.class || type == char.class) {
            object = argument.toCharArray()[0];
        } else if (type == Byte.class || type == byte.class) {
            object = Byte.parseByte(argument);
        } else if (type == Short.class || type == short.class) {
            object = Short.parseShort(argument);
        } else if (type == Boolean.class || type == boolean.class) {
            object = Boolean.parseBoolean(argument);
        } else if (type == File.class) {
            object = new File(argument);
        } else if (type == URI.class) {
            try {
                object = new URI(argument);
            } catch (URISyntaxException exception) {
                throw new PintoException(PintoExceptionType.SyntaxFormat, "The value " + argument + " is not a valid URI.", exception);
            }
        } else {
            throw new PintoException(PintoExceptionType.UnknownParameterTypes, "I don't know how to convert to the type " + type.getName());
        }

        return object;
    }

    /**
     * This can be used by {@link #validate()} implementations to have help displayed if no arguments are specified on
     * the command line. If this is called and no arguments were passed to the application, this method will set the
     * help display flag to true.
     *
     * @return Returns true if help should be displayed. Note that this should terminate further parameter validation.
     */
    @SuppressWarnings("unused")
    protected boolean noArgsHelp() {
        if (_arguments == null || _arguments.size() == 0) {
            displayHelp();
            _help = true;
            return true;
        }
        return false;
    }

    /**
     * Scans the bean class for configured parameters. Parameters are configured by adding the {@link Parameter}
     * annotation to the setter method. Associated getter methods are marked with the {@link Value} annotation.
     */
    private void scan() throws PintoException {
        // TODO: For now this doesn't detect duplication in parameters when subclass method hides base class method. Maybe that's OK?
        Method[] methods = getClass().getMethods();
        for (Method method : methods) {
            Parameter annotation = method.getAnnotation(Parameter.class);
            if (annotation != null) {
                if (_log.isDebugEnabled()) {
                    _log.debug("Found command-line parameter annotation " + annotation.value() + " on " + getClass().getName() + "." + method.getName() + "() method");
                }
                final ParameterData parameter = new ParameterData(method, annotation);
                if (_parametersByShortOption.containsKey(parameter.getShortOption())) {
                    throw new PintoException(PintoExceptionType.DuplicateParameter, "Your application has multiple declarations of the short option " + parameter.getShortOption());
                }
                _parametersByShortOption.put(parameter.getShortOption(), parameter);
                if (StringUtils.isNotBlank(parameter.getDefaultValue())) {
                    _parametersWithDefaultValues.put(parameter.getShortOption(), parameter);
                }
                final String longOption = parameter.getLongOption();
                if (!StringUtils.isBlank(longOption)) {
                    if (_parametersByLongOption.containsKey(parameter.getLongOption())) {
                        throw new PintoException(PintoExceptionType.DuplicateParameter, "Your application has multiple declarations of the long option " + parameter.getLongOption());
                    }
                    _parametersByLongOption.put(longOption, parameter);
                }
            }
        }
    }

    /**
     * Harvests the command-line parameters and sorts them along with their arguments. Any trailing arguments are
     * assigned to the last found parameter. Trailing arguments that occur without parameters are stashed in the {@link
     * #getTrailingArguments()} property.
     *
     * @throws PintoException When an error occurs harvesting parameters.
     */
    private void harvest() throws PintoException {
        ParameterData parameter = null;
        for (String argument : _arguments) {
            // Is this argument a parameter?
            if (isParameter(argument)) {
                // If there are trailing arguments, then something is wrong.
                if (_trailing.size() > 0) {
                    throw new PintoException(PintoExceptionType.SyntaxFormat, argument, "Trailing arguments were found prior to the parameter " + argument + ". Check that you've supplied only the expected number of arguments to each parameter.");
                }

                // Try to get the data for the indicated parameter.
                ParameterData foundParameter = getParameterData(argument);

                // Before we finish with an existing parameter...
                if (parameter != null) {
                    ArgCount argCount = parameter.getArgCount();
                    int      argSize  = _parameters.get(parameter.getShortOption()).size();

                    // It's OK to have a new parameter now, so bail out.
                    if (argSize == 0 && (argCount == ArgCount.ZeroToN || argCount == ArgCount.StandAlone)) {
                        break;
                    }

                    if (argCount == ArgCount.OneArgument && argSize != 1) {
                        throw new PintoException(PintoExceptionType.SyntaxFormat, "Not enough arguments specified for parameter " + parameter.getShortOption() + ", requires exactly one");
                    } else if (argCount == ArgCount.OneToN && argSize == 0) {
                        throw new PintoException(PintoExceptionType.SyntaxFormat, "Not enough arguments specified for parameter " + parameter.getShortOption() + ", requires one or more");
                    } else if (argCount == ArgCount.SpecificCount && argSize != parameter.getExactArgCount()) {
                        throw new PintoException(PintoExceptionType.SyntaxFormat, "Not enough arguments specified for parameter " + parameter.getShortOption() + ", requires " + parameter.getExactArgCount());
                    }
                }

                parameter = foundParameter;

                // Now store the parameter option in the map of parameter data and initialize the argument cache.
                if (_parameters.containsKey(parameter.getShortOption())) {
                    if (!parameter.getMultiplesAllowed()) {
                        throw new PintoException(PintoExceptionType.SyntaxFormat, "You can only specify the " + parameter.getShortOption() + "/" + parameter.getLongOption() + " parameter once on the command line.");
                    }
                } else {
                    _parameters.put(parameter.getShortOption(), new ArrayList<String>());
                }

                // Remove explicit parameters from default values to be set.
                _parametersWithDefaultValues.remove(parameter.getShortOption());

                // If the parameter takes no args, there's no reason to keep it around.  The next tokens have to be
                // either another parameter or trailing arguments.
                if (parameter.getArgCount() == ArgCount.StandAlone) {
                    parameter = null;
                }
            } else if (parameter == null) {
                // This is a fail-safe catch for situations with no parameters, e.g., ls file1 file2 file3
                _trailing.add(argument);
            } else {
                // Add the argument to the current parameter. The last parameter will harvest all trailing data.
                // We'll handle that situation when we prune the parameter list.
                List<String> args = _parameters.get(parameter.getShortOption());
                args.add(argument);

                ArgCount argCount = parameter.getArgCount();

                // Note that we don't handle ArgCount.StandAlone here because that's cut off when the
                // stand-alone parameter is detected earlier. We also don't deal with ZeroToN and OneToN,
                // since they should be cut off by end of command or the next parameter.
                switch (argCount) {
                    case OneArgument:
                        // Really we shouldn't ever get this since we're going to cut it off after this.
                        if (args.size() > 1 && !parameter.getMultiplesAllowed()) {
                            throw new PintoException(PintoExceptionType.SyntaxFormat, "Too many arguments specified for parameter " + parameter.getShortOption());
                        }
                        parameter = null;
                        break;
                    case SpecificCount:
                        if (args.size() > parameter.getExactArgCount()) {
                            throw new PintoException(PintoExceptionType.SyntaxFormat, "Too many arguments specified for parameter " + parameter.getShortOption());
                        }
                }
            }
        }
        if (_parametersWithDefaultValues.size() > 0) {
            for (final String option : _parametersWithDefaultValues.keySet()) {
                parameter = _parametersWithDefaultValues.get(option);
                _parameters.put(option, Collections.singletonList(parameter.getDefaultValue()));
            }
        }
    }

    /**
     * Prunes the parameters and arguments. This includes validating the arguments passed in against the accepted
     * arguments for each parameter, as well as removing trailing arguments and setting default values.
     */
    private void prune() throws PintoException {
        for (String parameterId : _parameters.keySet()) {
            ParameterData parameter = _parametersByShortOption.get(parameterId);
            List<String>  arguments = _parameters.get(parameterId);

            validateArgCount(parameter, arguments);

            final Method method = parameter.getMethod();
            try {
                if (parameter.getArgCount() == ArgCount.StandAlone) {
                    method.invoke(this, true);
                } else {
                    try {
                        Object[] coercedArguments = coerceArguments(method, arguments);
                        method.invoke(this, coercedArguments);
                    } catch (PintoException exception) {
                        if (exception.getType() == PintoExceptionType.UnknownParameterTypes && StringUtils.isBlank(exception.getMessage())) {
                            throw new PintoException(PintoExceptionType.UnknownParameterTypes, "The parameter " + parameterId + " has unknown parameter types. Check your set method for compatible parameter types.");
                        }
                        if (exception.getType() == PintoExceptionType.SyntaxFormat) {
                            final StringBuilder message = new StringBuilder("The parameter " + parameterId + " has a syntax error. Check that your arguments match the parameter requirements.");
                            if (!StringUtils.isBlank(exception.getMessage())) {
                                message.append(" The specific error message is:\n\n").append(exception.getMessage());
                            }
                            throw new PintoException(PintoExceptionType.SyntaxFormat, parameterId, message.toString());
                        }
                        throw exception;
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException exception) {
                throw new PintoException(PintoExceptionType.Configuration, parameter.getShortOption(), "Unable to call the " + method.getName() + " method configured for handling parameter", exception);
            }
        }
    }

    private Object[] coerceArguments(final Method method, final List<String> arguments) throws PintoException {
        final Class<?>[] types = method.getParameterTypes();
        if (types.length == 0) {
            throw new PintoException(PintoExceptionType.UnknownParameterTypes);
        }
        final boolean isArrayParameter = types.length == 1 && types[0].isArray();
        final boolean isListParameter  = types.length == 1 && List.class.isAssignableFrom(types[0]);
        if (types.length != arguments.size() && !isArrayParameter && !isListParameter) {
            throw new PintoException(PintoExceptionType.SyntaxFormat);
        }
        Class<?>           type             = isArrayParameter ? types[0].getComponentType() : null;
        final List<Object> coercedArguments = new ArrayList<>(types.length);
        if (!isListParameter) {
            for (int index = 0; index < arguments.size(); index++) {
                if (!isArrayParameter) {
                    type = types[index];
                }
                coercedArguments.add(convertStringToType(type, arguments.get(index)));
            }
        } else {
            coercedArguments.add(arguments);
        }

        return isArrayParameter ? new Object[]{coercedArguments.toArray((Object[]) Array.newInstance(types[0].getComponentType(), coercedArguments.size()))} : coercedArguments.toArray();
    }

    private void validateArgCount(final ParameterData parameter, final List<String> arguments) throws PintoException {
        final String   parameterId = parameter.getShortOption();
        final int      argCount    = arguments.size();
        final ArgCount ArgCount    = parameter.getArgCount();

        switch (ArgCount) {
            case StandAlone:
                if (_log.isDebugEnabled()) {
                    _log.debug("Found parameter " + parameterId + " specified as StandAlone parameter, comes with " + argCount + " arguments");
                }
                if (argCount > 0) {
                    throw new PintoException(PintoExceptionType.SyntaxFormat, "The parameter " + parameterId + " does not accept any arguments.");
                }
                break;
            case OneArgument:
                if (_log.isDebugEnabled()) {
                    _log.debug("Found parameter " + parameterId + " specified as OneArgument parameter, comes with " + argCount + " arguments");
                }
                if (argCount != 1 && !parameter.getMultiplesAllowed()) {
                    throw new PintoException(PintoExceptionType.SyntaxFormat, "The parameter " + parameterId + " only accepts a single argument.");
                }
                break;
            case OneToN:
                if (_log.isDebugEnabled()) {
                    _log.debug("Found parameter " + parameterId + " specified as OneToN parameter, comes with " + argCount + " arguments");
                }
                if (argCount == 0) {
                    throw new PintoException(PintoExceptionType.SyntaxFormat, "The parameter " + parameterId + " requires one or more arguments.");
                }
                break;
            case SpecificCount:
                int acceptedArgCount = parameter.getExactArgCount();
                if (_log.isDebugEnabled()) {
                    _log.debug("Found parameter " + parameterId + " specified as SpecificCount parameter with " + acceptedArgCount + " arguments required, comes with " + argCount + " arguments");
                }
                if (argCount != acceptedArgCount) {
                    throw new PintoException(PintoExceptionType.SyntaxFormat, "The parameter " + parameterId + " requires exactly " + acceptedArgCount + " arguments.");
                }
                break;
            case ZeroToN:
                if (_log.isDebugEnabled()) {
                    _log.debug("Found parameter " + parameterId + " specified as ZeroToN parameter, comes  with " + argCount + " arguments");
                }
        }
    }

    private ParameterData getParameterData(String parameter) throws PintoException {
        if (parameter.startsWith(LONG_OPTION_DELIMITER)) {
            parameter = parameter.substring(LONG_OPTION_DELIMITER.length());
            if (_parametersByLongOption.containsKey(parameter)) {
                return _parametersByLongOption.get(parameter);
            }
        }
        if (parameter.startsWith(SHORT_OPTION_DELIMITER)) {
            parameter = parameter.substring(SHORT_OPTION_DELIMITER.length());
            if (_parametersByShortOption.containsKey(parameter)) {
                return _parametersByShortOption.get(parameter);
            }
        }
        // We didn't find it.
        throw new PintoException(PintoExceptionType.UnknownParameter, parameter, "The parameter " + parameter + " is not a valid parameter.");
    }

    private boolean isParameter(final String argument) {
        return StringUtils.startsWithAny(argument, OPTION_DELIMITERS);
    }

    private String getVersionFromParent() throws PintoException {
        Method getVersion = null;
        try {
            getVersion = _parent.getClass().getMethod("getVersion");
        } catch (NoSuchMethodException ignored) {
            // If it doesn't exist, so be it.
        }
        if (getVersion != null) {
            try {
                return (String) getVersion.invoke(_parent);
            } catch (Exception exception) {
                throw new PintoException(PintoExceptionType.Configuration, "v", "Version method was found, but throws an error", exception);
            }
        }
        return getVersionFromParentProperties();
    }

    private String getVersionFromParentProperties() throws PintoException {
        final String version = getPropertyFromParentProperties("version");
        return StringUtils.isBlank(version) ? "Unknown" : version;
    }

    private String getPropertyFromParentProperties(String property) throws PintoException {
        Properties properties    = null;
        Method     getProperties = null;

        try {
            getProperties = _parent.getClass().getMethod("getProperties");
        } catch (NoSuchMethodException ignored) {
            // If it doesn't exist, so be it.
        }

        if (getProperties != null) {
            try {
                properties = (Properties) getProperties.invoke(_parent);
            } catch (Exception exception) {
                throw new PintoException(PintoExceptionType.Configuration, "v", "Properties method was found, but throws an error", exception);
            }
        }

        if (properties == null) {
            properties = Reflection.getPropertiesForClass(_parent.getClass());
        }

        return properties == null ? null : properties.getProperty(property);
    }

    private String resolveAttribute(final String value) {
        if (value.startsWith(PROPERTY_INDICATOR)) {
            try {
                return getPropertyFromParentProperties(value.substring(PROPERTY_INDICATOR.length()));
            } catch (PintoException ignored) {
            }
        }
        return value;
    }

    private PrintStream getOutputStream() throws PintoException {
        final String outputStreamAdapter = getOutputStreamAdapter();
        if (StringUtils.isBlank(outputStreamAdapter)) {
            return System.out;
        }
        try {
            Class<?>           clazz   = getClass().getClassLoader().loadClass(outputStreamAdapter);
            PintoStreamAdapter adapter = (PintoStreamAdapter) clazz.newInstance();
            return adapter.getOutputStream();
        } catch (ClassNotFoundException e) {
            throw new PintoException(PintoExceptionType.InvalidOutputStreamAdapter, "Couldn't find output stream adapter class: " + outputStreamAdapter);
        } catch (InstantiationException e) {
            throw new PintoException(PintoExceptionType.InvalidOutputStreamAdapter, "Couldn't create new instance of output stream adapter class: " + outputStreamAdapter);
        } catch (IllegalAccessException e) {
            throw new PintoException(PintoExceptionType.InvalidOutputStreamAdapter, "Can't access constructor of output stream adapter class: " + outputStreamAdapter);
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(AbstractPintoBean.class);

    /**
     * This is the text to place before each option in the help text.
     */
    private static final String PREFIX = " ";

    /**
     * This is the width of the hanging indent in the help text. Illustration ('|' is the left margin):
     * <p/>
     * <div style='font-family: "Courier New", Courier, monospace'> | -h, --help         Show this help text.
     * |12345678901234567890 </div>
     * <p/>
     * Above shows a 20-character hanging indent.
     */
    private static final int    HANGING_INDENT = 20;
    /**
     * Provides the indent filler to format the hanging indent space.
     */
    private static final String INDENT_FILLER  = "\n" + CharBuffer.allocate(HANGING_INDENT).toString().replace('\0', ' ');
    /**
     * The overall format width for the help text.
     */
    private static final int    WIDTH          = 80;

    private static final String   SHORT_OPTION_DELIMITER = "-";
    private static final String   LONG_OPTION_DELIMITER  = "--";
    private static final String[] OPTION_DELIMITERS      = new String[]{LONG_OPTION_DELIMITER, SHORT_OPTION_DELIMITER};
    public static final  String   PROPERTY_INDICATOR     = "property:";

    /**
     * The print stream for help text and user messages.
     */
    private PrintStream _printStream = System.out;
    private final Object _parent;

    private boolean _help;
    private boolean _version = false;
    private String _outputStreamAdapter;

    private List<String> _arguments;
    private Map<String, List<String>>  _parameters                  = new LinkedHashMap<>();
    private List<String>               _trailing                    = new ArrayList<>();
    private Map<String, ParameterData> _parametersByShortOption     = new HashMap<>();
    private Map<String, ParameterData> _parametersByLongOption      = new HashMap<>();
    private Map<String, ParameterData> _parametersWithDefaultValues = new HashMap<>();
}
