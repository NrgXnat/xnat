/*
 * web: org.nrg.xnat.servlet.ArchiveServlet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.servlet;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.bean.CatEntryBean;
import org.nrg.xdat.bean.CatEntryMetafieldBean;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.ResourceFile;
import org.nrg.xft.utils.XftStringUtils;
import org.nrg.xft.utils.zip.ZipI;
import org.nrg.xft.utils.zip.ZipUtils;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("serial")
public class ArchiveServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ArchiveServlet.class);

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
        doGetOrPost(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
        doGetOrPost(arg0, arg1);
    }

    @SuppressWarnings("unchecked")
    protected void getCatalog(UserI user, String path, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String rootElementName = path.substring(0, path.indexOf("/"));
        path = path.substring(path.indexOf("/") + 1);

        res.setContentType("text/xml");

        if (rootElementName.equals("stored")) {
            File f = Users.getUserCacheFile(user, "catalogs/" + path);
            if (f.exists()) {
                writeFile(f, res);
                return;
            } else {
                return;
            }
        }

        int indexOfSlash = path.indexOf("/");
        final String value;
        if (indexOfSlash == -1) {
            value = path;
            path = "";
        } else {
            value = path.substring(0, path.indexOf("/"));
            path = path.substring(path.indexOf("/") + 1);
        }

        CatCatalogBean cat = new CatCatalogBean();

        String server = TurbineUtils.GetFullServerPath(req);
        if (!server.endsWith("/")) {
            server += "/";
        }

        ArrayList<String> ids = XftStringUtils.CommaDelimitedStringToArrayList(value, true);

        try {
            final GenericWrapperElement root = GenericWrapperElement.GetElement(rootElementName);
            final ItemSearch is = ItemSearch.GetItemSearch(root.getFullXMLName(), user);
            int index = 0;
            for (GenericWrapperField f : root.getAllPrimaryKeys()) {
                is.addCriteria(f.getXMLPathString(root.getFullXMLName()), ids.get(index++));
            }
            final ItemI rootO = is.exec(false).getFirst();
            final XFTItem i = (XFTItem) rootO;
            ItemI rootOM = BaseElement.GetGeneratedItem(i);


            String xmlPath = server + "archive/" + rootElementName + "/" + value;
            String uri = server + "archive/cache/";

            String rootPath = getRootPath(rootOM);
            File rootDir = new File(rootPath);

            final ArrayList<XFTItem> al;
            if (!path.equals("")) {
                al = (ArrayList<XFTItem>) i.getProperty(path, true);
                xmlPath += "/" + path.substring(0, path.indexOf("["));
            } else {
                al = new ArrayList<>();
                al.add(i);
            }

            for (XFTItem child : al) {
                final String subString;
                if (!path.equals("")) {
                    subString = xmlPath + "/" + child.getPKValueString() + "/";
                } else {
                    subString = xmlPath;
                }

                BaseElement om = (BaseElement) BaseElement.GetGeneratedItem(child);
                ArrayList<ResourceFile> rfs = om.getFileResources(rootPath);
                for (ResourceFile rf : rfs) {
                    CatEntryBean entry = new CatEntryBean();

                    String relative = rf.getAbsolutePath();

                    Object id = cacheFileLink(subString + rf.getXdatPath(), relative, i.getDBName(), user.getLogin());

                    entry.setUri(uri + id);

                    relative = relative.replace('\\', '/');
                    String cleaned = rootPath.replace('\\', '/');

                    if (relative.startsWith(cleaned)) {
                        relative = relative.substring(cleaned.length());
                    } else {
                        if (relative.contains("/" + rootDir.getName() + "/")) {
                            relative = relative.substring(relative.indexOf("/" + rootDir.getName() + "/") + 1);
                        }
                    }

                    entry.setCachepath(relative);

                    CatEntryMetafieldBean meta = new CatEntryMetafieldBean();
                    meta.setMetafield(relative);
                    meta.setName("RELATIVE_PATH");
                    entry.addMetafields_metafield(meta);

                    meta = new CatEntryMetafieldBean();
                    meta.setMetafield(rf.getSize().toString());
                    meta.setName("SIZE");
                    entry.addMetafields_metafield(meta);


                    cat.addEntries_entry(entry);
                }
            }

            ServletOutputStream out = res.getOutputStream();

            OutputStreamWriter sw = new OutputStreamWriter(out);
            cat.toXML(sw, false);
            sw.flush();
            sw.close();
        } catch (XFTInitException e) {
            logger.error("An error occurred initializing XFT", e);
        } catch (ElementNotFoundException e) {
            logger.error("Did not find the requested element on the item", e);
        } catch (Exception e) {
            logger.error("An unknown exception occurred", e);
        }
    }

    public static boolean ARCHIVE_PATH_CHECKED = false;

    public static Boolean CreatedArchivePathCache(String dbName, String login) throws Exception {
        if (!ARCHIVE_PATH_CHECKED) {
            String query = "SELECT relname FROM pg_catalog.pg_class WHERE  relname=LOWER('xs_archive_path_cache');";
            String exists = (String) PoolDBUtils.ReturnStatisticQuery(query, "relname", dbName, login);

            if (exists != null) {
                ARCHIVE_PATH_CHECKED = true;
            } else {
                query = "CREATE TABLE xs_archive_path_cache" +
                        "\n(" +
                        "\n  id serial," +
                        "\n  create_date timestamp DEFAULT now()," +
                        "\n  username VARCHAR(255)," +
                        "\n  url text," +
                        "\n  _token VARCHAR(255)," +
                        "\n  absolute_path text" +
                        "\n) " +
                        "\nWITH OIDS;";

                PoolDBUtils.ExecuteNonSelectQuery(query, dbName, login);

                ARCHIVE_PATH_CHECKED = true;
            }
        }
        return true;
    }

    public static Object cacheFileLink(String url, String absolutePath, String dbName, String login) throws Exception {
        CreatedArchivePathCache(dbName, login);
        Object o = RandomStringUtils.randomAlphanumeric(64);
        Object exists = PoolDBUtils.ReturnStatisticQuery("SELECT id FROM xs_archive_path_cache WHERE _token='" + o + "';", "id", dbName, login);
        while (exists != null) {
            o = RandomStringUtils.randomAlphanumeric(64);
            exists = PoolDBUtils.ReturnStatisticQuery("SELECT id FROM xs_archive_path_cache WHERE _token='" + o + "';", "id", dbName, login);
        }
        String query = "INSERT INTO xs_archive_path_cache (username,url,_token,absolute_path) VALUES ('" + login + "'," + DBAction.ValueParser(url, "string", true) + ",'" + o + "'," + DBAction.ValueParser(absolutePath, "string", true) + ");";
        PoolDBUtils.ExecuteNonSelectQuery(query, dbName, login);
        return o;
    }

    public Object retrieveCacheFileLink(String o, String dbName, String login) throws Exception {
        o = StringUtils.remove(o, '\'');
        return PoolDBUtils.ReturnStatisticQuery("SELECT absolute_path FROM xs_archive_path_cache WHERE _token='" + o + "';", "absolute_path", dbName, login);
    }

    protected String getRootPath(ItemI i) {
        if (i instanceof XnatProjectdata) {
            return ((XnatProjectdata) i).getRootArchivePath();
        } else if (i instanceof XnatSubjectdata) {
            return ((XnatSubjectdata) i).getPrimaryProject(false).getRootArchivePath();
        } else if (i instanceof XnatExperimentdata) {
            return ((XnatExperimentdata) i).getPrimaryProject(false).getRootArchivePath();
        }
        return null;
    }

    protected void writeFile(File _return, HttpServletResponse res) throws IOException {
        writeFile(_return, res, _return.getName());
    }

    protected void writeFile(File _return, HttpServletResponse res, String name) throws IOException {
        TurbineUtils.setContentDisposition(res, name, false);

        OutputStream os = res.getOutputStream();
        java.io.FileInputStream in = new java.io.FileInputStream(_return);
        byte[] buf = new byte[FileUtils.LARGE_DOWNLOAD];
        int len;
        while ((len = in.read(buf)) > 0) {
            os.write(buf, 0, len);
            os.flush();
        }
        os.flush();
        in.close();
    }

    protected void getDataFile(UserI user, String path, HttpServletResponse response) throws ServletException, IOException {
        String rootElementName = path.substring(0, path.indexOf("/"));
        path = path.substring(path.indexOf("/") + 1);
        String value = path.substring(0, path.indexOf("/"));
        path = path.substring(path.indexOf("/") + 1);

        final ArrayList<String> ids = XftStringUtils.CommaDelimitedStringToArrayList(value, true);

        XFTItem session = null;
        XFTItem project = null;
        try {
            final GenericWrapperElement root = GenericWrapperElement.GetElement(rootElementName);
            SchemaElementI localE = root;
            ItemSearch is = ItemSearch.GetItemSearch(root.getFullXMLName(), user);
            int index = 0;
            for (GenericWrapperField f : root.getAllPrimaryKeys()) {
                is.addCriteria(f.getXMLPathString(root.getFullXMLName()), ids.get(index++));
            }

            final ItemI rootO = is.exec(false).getFirst();
            XFTItem i = (XFTItem) rootO;

            if (i.instanceOf("xnat:projectData")) {
                project = i;
            } else if (i.instanceOf("xnat:imageSessionData")) {
                session = i;
            }

            String nextPath = null;
            GenericWrapperField lastField = null;
            while (path.contains("/")) {
                final String next = path.substring(0, path.indexOf("/"));

                try {
                    if (lastField == null) {
                        lastField = localE.getGenericXFTElement().getDirectField(next);
                    } else {
                        lastField = lastField.getDirectField(next);
                    }

                    if (nextPath == null) {
                        nextPath = next;
                    } else {
                        nextPath += "/" + next;
                    }

                    path = path.substring(path.indexOf("/") + 1);

                    if (lastField.isReference()) {
                        localE = lastField.getReferenceElement();
                        value = path.substring(0, path.indexOf("/"));
                        path = path.substring(path.indexOf("/") + 1);

                        ids.clear();
                        ids.addAll(XftStringUtils.CommaDelimitedStringToArrayList(value, true));

                        is = ItemSearch.GetItemSearch(localE.getFullXMLName(), user);
                        index = 0;
                        for (GenericWrapperField f : localE.getGenericXFTElement().getAllPrimaryKeys()) {
                            is.addCriteria(f.getXMLPathString(localE.getFullXMLName()), ids.get(index++));
                        }
                        i = (XFTItem) is.exec(false).getFirst();

                        lastField = null;
                        nextPath = null;

                        if (i.instanceOf("xnat:projectData")) {
                            project = i;
                        } else if (i.instanceOf("xnat:imageSessionData")) {
                            session = i;
                        }
                    }
                } catch (FieldNotFoundException e) {
                    break;
                }
            }

            System.out.println("ENDING:" + path);


            //identify project
            if (project == null) {
                if (session != null) {
                    XnatImagesessiondata img = (XnatImagesessiondata) BaseElement.GetGeneratedItem(session);
                    project = img.getPrimaryProject(false).getItem();
                } else {
                    ArrayList<XFTItem> parents = i.getParents("xnat:projectData");
                    project = parents.get(0);
                }
            }

            XnatProjectdata p = (XnatProjectdata) BaseElement.GetGeneratedItem(project);
            String rootPath = p.getRootArchivePath();

            BaseElement om = (BaseElement) BaseElement.GetGeneratedItem(i);

            ArrayList<ResourceFile> resources = om.getFileResources(rootPath);

            if (path.equals("*")) {
                response.setContentType("application/zip");
                TurbineUtils.setContentDisposition(response, value + ".zip", false);
                OutputStream outStream = response.getOutputStream();
                final ZipI zip = new ZipUtils();
                zip.setOutputStream(outStream, ZipOutputStream.DEFLATED);

                for (ResourceFile rf : resources) {
                    File f = rf.getF();
                    String relative = f.getAbsolutePath();
                    if (session != null) {
                        if (relative.contains(File.separator + session.getProperty("ID"))) {
                            relative = relative.substring(relative.indexOf(File.separator + session.getProperty("ID")) + 1);
                        } else if (project != null) {
                            if (relative.contains(File.separator + project.getProperty("ID"))) {
                                relative = relative.substring(relative.indexOf(File.separator + project.getProperty("ID")) + 1);
                            }
                        }
                    } else if (project != null) {
                        if (relative.contains(File.separator + project.getProperty("ID"))) {
                            relative = relative.substring(relative.indexOf(File.separator + project.getProperty("ID")) + 1);
                        }
                    }
                    zip.write(relative, f);
                }

                // Complete the ZIP file
                zip.close();
            } else {
                File _return = null;
                for (ResourceFile rf : resources) {
                    if (rf.getF().getName().equals(path)) {
                        _return = rf.getF();
                        break;
                    }
                }

                if (_return == null) {
                    int count = Integer.parseInt(path);
                    _return = resources.get(count).getF();
                }

                if (_return != null) {
                    writeFile(_return, response);
                }
            }
        } catch (XFTInitException e) {
            logger.error("An error occurred initializing XFT", e);
        } catch (ElementNotFoundException e) {
            logger.error("Did not find the requested element on the item", e);
        } catch (Exception e) {
            logger.error("An unknown exception occurred", e);
        }
    }

    protected void doGetOrPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        System.out.println("PathInfo: " + req.getPathInfo());
        String path = req.getPathInfo();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        UserI user = XDAT.getUserDetails();
        if (user == null) {
            user = (UserI) req.getSession().getAttribute("user");
        }

        if (path.startsWith("catalogs/")) {
            if (user != null)
                getCatalog(user, path.substring(9), req, res);
        } else if (path.startsWith("cache/")) {
            String o = path.substring(6);
            try {
                String dbName = GenericWrapperElement.GetElement("xdat:user").getDbName();
                String login = null;
                if (user != null) {
                    login = user.getLogin();
                }
                String filePath = (String) retrieveCacheFileLink(o, dbName, login);
                if (filePath != null) {
                    File f = new File(filePath);
                    if (f.exists()) {
                        writeFile(f, res);
                    }
                }
            } catch (Exception e) {
                logger.error("", e);
            }
        } else if (user != null) {
            getDataFile(user, path, res);
        }
    }

    /* (non-Javadoc)
     * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
     */
    @Override
    public void init(ServletConfig arg0) throws ServletException {
        super.init(arg0);
        ArcSpecManager.GetInstance();
    }
}
