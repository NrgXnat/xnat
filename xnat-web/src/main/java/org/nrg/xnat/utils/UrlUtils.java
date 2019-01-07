package org.nrg.xnat.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.services.archive.FilesystemService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

@Slf4j
public class UrlUtils {

    /**
     * Gets Url protocol (works for URLs like s3:// that aren't recognized by Java's URL class)
     *
     * @param path the URL
     * @return the protocol
     */
    public static String GetUrlProtocol(String path) {
        if (!FileUtils.IsUrl(path)) {
            return null;
        }
        return path.replaceAll("://.*", "");
    }

    /**
     * Writes URL connection to file
     *
     * @param connection    the url connection
     * @param f             the file
     * @return boolean success
     */
    public static boolean writeUrlConnection(URLConnection connection, File f) {
        boolean success;
        ReadableByteChannel rbc = null;
        FileOutputStream fos = null;
        try {
            long length = connection.getContentLength();
            rbc = Channels.newChannel(connection.getInputStream());
            fos = new FileOutputStream(f);
            long lengthSaved = fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            success = length == lengthSaved;
        } catch (IOException e) {
            log.error("Issue downloading to " + f.getAbsolutePath(), e);
            success = false;
        } finally {
            try {
                if(fos != null){
                    fos.close();
                }
                if(rbc != null) {
                    rbc.close();
                }
            } catch (IOException e) {
                log.error("Issue closing streams", e);
            }
        }
        return success;
    }

    public static File downloadUrl(String uri, String destinationPath) {
        return downloadUrl(uri, destinationPath, false);
    }

    /**
     * downloadUrl will download from the URL into destinationPath
     *
     * @param url               URL string
     * @param destinationPath   local destination
     * @param checkConflict     return null if file exists already in destinationPath
     * @return File
     */
    public static File downloadUrl(String url, String destinationPath, boolean checkConflict) {
        File f;
        if (StringUtils.isEmpty(destinationPath)) {
            // Make local path from cache path + URL
            destinationPath = org.nrg.xft.utils.FileUtils.AppendRootPath(XDAT.getSiteConfigPreferences().getCachePath(),
                    url.replaceAll("[^A-Za-z0-9.]+", "_"));
        }
        f = new File(destinationPath);
        if (f.exists()) {
            return checkConflict ? null : f;
        } else {
            f.getParentFile().mkdirs();
        }
        URLConnection connection;
        try {
            connection = openURLConnectionWithRedirects(new URL(url));
        } catch (IOException e) {
            log.error("Issue downloading from " + url + " to " + destinationPath, e);
            return null;
        }
        if (!writeUrlConnection(connection,f)) {
            f.delete();
            f = null;
        }
        return f;
    }

    public static URLConnection openURLConnectionWithRedirects(URL url) throws IOException {
        return openURLConnectionWithRedirects(url, null);
    }

    /**
     * Opens a URL connection, following redirects. Also uses a hack to imitate a CLI user agent.
     *
     * @param url           URL string
     * @param requestMethod HEAD, etc
     * @return URLConnection
     * @throws IOException for errors
     */
    public static URLConnection openURLConnectionWithRedirects(URL url, String requestMethod) throws IOException {
        int redirects = 0;
        URLConnection connection = url.openConnection();
        while (redirects <= 5) {
            redirects++;
            if (connection instanceof HttpURLConnection) {
                if (StringUtils.isNotEmpty(requestMethod)) {
                    ((HttpURLConnection) connection).setRequestMethod(requestMethod);
                }
                ((HttpURLConnection) connection).setInstanceFollowRedirects(true); //Doesn't work https<->http
                long contentLength = connection.getContentLength();
                if (contentLength == -1) {
                    connection = url.openConnection();
                    // Dropbox (maybe others?) try to give the java user agent a file preview
                    // but, it understands that cURL just wants the file, as do we.
                    connection.addRequestProperty("User-Agent", "curl/7.61.0");
                    continue;
                }
                int responseCode = ((HttpURLConnection) connection).getResponseCode();
                switch (responseCode) {
                    case HttpURLConnection.HTTP_OK:
                        break;
                    case HttpURLConnection.HTTP_MOVED_TEMP:
                    case HttpURLConnection.HTTP_MOVED_PERM:
                        String location = connection.getHeaderField("Location");
                        if (location != null) {
                            url = new URL(connection.getURL(), location);
                            connection = url.openConnection();
                            continue;
                        }
                        throw new IOException("Redirect with no location");
                    default:
                        throw new IOException("HTTP response: " + responseCode);
                }
            }
            return connection;
        }
        throw new IOException("Too many redirects");
    }

    /**
     * downloadUrlHeaders will read metadata for the uri into destinationPath
     *
     * @param uri               URL string
     * @return attrs CatalogUtils.CatalogEntryAttributes
     */
    public static CatalogUtils.CatalogEntryAttributes downloadUrlHeaders(String uri) {
       CatalogUtils.CatalogEntryAttributes attrs = null;
        try {
            URL url = new URL(uri);
            String hash_type = "MD5";
            //fall back on url path if digest fails
            String relativePath = url.getPath().replaceFirst("^/", "");
            String name = relativePath.replaceFirst(".*/","");
            try {
                MessageDigest md5 = MessageDigest.getInstance(hash_type);
                md5.update(uri.getBytes());
                relativePath = Hex.encodeHexString(md5.digest()) + "_" + name;
            } catch (NoSuchAlgorithmException e) {
                log.error("Unsupported hashing algorithm " + hash_type, e);
            }
            URLConnection connection = openURLConnectionWithRedirects(url, "HEAD");
            if (connection.getContentLength() != -1) {
                attrs = new CatalogUtils.CatalogEntryAttributes(connection.getContentLengthLong(), new Date(connection.getLastModified()),
                        null, relativePath, name);
            }
        } catch (MalformedURLException e) {
            log.error("Bad url " + uri, e);
            return null;
        } catch (IOException e) {
            log.error("Issue downloading metadata from " + uri, e);
            return null;
        }
        return attrs;
    }


    /**
     * Get URL headers (either via filesystem service or directly from URL) to populate CatalogUtils.CatalogEntryAttributes object
     * @param url the url
     * @param catPath used determine path relative to catalog parent for CatEntryBean ID field
     * @return CatalogUtils.CatalogEntryAttributes
     */
    public static CatalogUtils.CatalogEntryAttributes getCatalogEntryAttributesForUrl(String url, String catPath) {
        if (!FileUtils.IsUrl(url, true)) return null;
        CatalogUtils.CatalogEntryAttributes attrs = null;
        List<FilesystemService> fsList = CatalogUtils.getFilesystemServices();
        String protocol = GetUrlProtocol(url);
        for (FilesystemService fs : fsList) {
            if (fs != null && fs.supportedUrlProtocols().contains(protocol)) {
                attrs = fs.getMetadata(url, catPath);
                if (attrs != null) break;
            }
        }
        if (attrs == null) {
            attrs = downloadUrlHeaders(url);
        }
        return attrs;
    }
    public static CatalogUtils.CatalogEntryAttributes getCatalogEntryAttributesForUrl(String url) {
        return getCatalogEntryAttributesForUrl(url,null);
    }
}
