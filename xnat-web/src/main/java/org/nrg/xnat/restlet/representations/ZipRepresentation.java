/*
 * org.nrg.xnat.restlet.representations.ZipRepresentation
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/17/13 4:45 PM
 */
package org.nrg.xnat.restlet.representations;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.nrg.xft.utils.zip.TarUtils;
import org.nrg.xft.utils.zip.ZipI;
import org.nrg.xft.utils.zip.ZipUtils;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.data.MediaType;
import org.restlet.resource.OutputRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class ZipRepresentation extends OutputRepresentation {
    private final Logger logger = LoggerFactory.getLogger(ZipRepresentation.class);
    
    private ArrayList<ZipEntry> _entries=new ArrayList<ZipEntry>();
    private ArrayList<String> _tokens=new ArrayList<String>();
    private MediaType mt=null;
    private final int compression;
    private List<Runnable> afterWrite = Lists.newArrayList();

    public ZipRepresentation(MediaType mt,String token, Integer compression) {
        super(mt);
        this.mt=mt;
        _tokens.add(token);
        this.compression=deriveCompression(compression);
    }

    public ZipRepresentation(MediaType mt,ArrayList<String> token, Integer compression) {
        super(mt);
        this.mt=mt;
        _tokens=token;
        this.compression=deriveCompression(compression);
    }

    public int deriveCompression(Integer compression){
        if(compression==null){
            return ZipUtils.DEFAULT_COMPRESSION;
        }else{
            return compression;
        }
    }

    public void addEntry(String p, File f){
        _entries.add(new ZipFileEntry(p,f));
    }

    public void addEntry(String p, InputStream is){
        _entries.add(new ZipStreamEntry(p,is));
    }

    public void addFolder(String p, File dir){
        if(dir.isDirectory()){
            for(final File f:dir.listFiles()){
                if(f.isDirectory()){
                    this.addFolder(append(p,f.getName()),f);
                }else{
                    this.addEntry(append(p,f.getName()),f);
                }
            }
        }else{
            this.addEntry(append(p,dir.getName()),dir);
        }
    }

    public String append(String pre, String post){
        return (pre.endsWith("/"))?pre+post:pre+"/"+post;
    }

    public void addEntry(File f){
        String p = f.getAbsolutePath().replace('\\','/');
        int i=-1;
        String _token=null;

        for(String token:_tokens){
            _token=token;
            i=p.indexOf('/'+ _token + '/');
            if(i==-1){
                i=p.indexOf('/'+ _token);

                if(i==-1){
                    i=p.indexOf(_token+'/');
                    if(i>-1){
                        i=(p.substring(0, i)).lastIndexOf('/') +1;
                        break;
                    }
                }else{
                    i++;
                    break;
                }
            }else{
                i++;
                break;
            }
        }

        if(i==-1){
            if(p.indexOf(":")>-1){
                p=p.substring(p.indexOf(":"));
                p=p.substring(p.indexOf("/"));
                p=_token+p;
            }else{
                p=_token+p;
            }

            _entries.add(new ZipFileEntry(p,f));
        }else{

            _entries.add(new ZipFileEntry(p.substring(i),f));
        }
    }

    public void addAll(List<File> fs){
        for(File f: fs){
            this.addEntry(f);
        }
    }

    public void addAllAtRelativeDirectory(String ins, ArrayList<File> fs) {
        ins=ins.replace('\\','/');
        for(File f: fs){
            String pathS = f.getAbsolutePath().replace('\\','/');
            int pos=pathS.indexOf(ins);
            if (pos>=0) {
                this.addEntry(pathS.substring(pos+ins.length()+1),f);
            } else {
                this.addEntry(f);
            }
        }
    }

    public String getTokenName(){
        if(this._tokens.size()>1){
            return "various";
        }else{
            return this._tokens.get(0);
        }
    }

    public int getEntryCount(){
        return this._entries.size();
    }

    @Override
    public String getDownloadName() {
        if (this.mt.equals(MediaType.APPLICATION_GNU_TAR))
        {
            return getTokenName()+".tar.gz";
        }else if (this.mt.equals(MediaType.APPLICATION_TAR))
        {
            return getTokenName()+".tar";
        }else if (this.mt.equals(SecureResource.APPLICATION_XAR))
        {
            return getTokenName()+".xar";
        }else{
            return getTokenName() +".zip";
        }
    }

    /**
     * Adds a task that should be performed asynchronously after this ZipRepresentation
     * completes a write.
     * @param runnable
     * @return this
     */
    public ZipRepresentation afterWrite(final Runnable runnable) {
        afterWrite.add(runnable);
        return this;
    }
    
    /**
     * After this ZipRepresentation completes a write, remove the named file.
     * @param f
     * @return this
     */
    public ZipRepresentation deleteDirectoryAfterWrite(final File f) {
        return afterWrite(new Runnable() {
            public void run() {
                try {
                    FileUtils.deleteDirectory(f);
                } catch (IOException e) {
                    logger.error("unable to remove working directory " + f, e);
                }
            }
        });
    }

    @Override
    public void write(OutputStream os) throws IOException {
        try {
            ZipI zip = null;
            if (this.mt.equals(MediaType.APPLICATION_GNU_TAR))
            {
                zip = new TarUtils();
                zip.setOutputStream(os,ZipOutputStream.DEFLATED);
                this.setDownloadName(getTokenName()+".tar.gz");
                this.setDownloadable(true);
            }else if (this.mt.equals(MediaType.APPLICATION_TAR)){
                zip = new TarUtils();
                zip.setOutputStream(os,ZipOutputStream.STORED);
                this.setDownloadName(getTokenName()+".tar");
                this.setDownloadable(true);
            }else{
                zip = new ZipUtils();
                zip.setOutputStream(os,compression);
                this.setDownloadName(getTokenName() +".zip");
                this.setDownloadable(true);
            }

            for (final ZipEntry ze: this._entries) {
                if (ze instanceof ZipFileEntry) {
                    final ZipFileEntry zfe = (ZipFileEntry)ze;
                    final File f = zfe.getF();
                    if (!f.isDirectory()) {
                        zip.write(ze.getPath(),f);
                    }
                } else {
                    zip.write(ze.getPath(), ((ZipStreamEntry)ze).getInputStream());
                }
            }

            // Complete the ZIP file
            zip.close();
        } finally {
            if (!afterWrite.isEmpty()) {
                final Executor executor = Executors.newSingleThreadExecutor();
                for (final Runnable r : afterWrite) {
                    executor.execute(r);
                }
            }
        }
    }

    public abstract class ZipEntry{
        String path=null;

        public String getPath() {
            return path;
        }
        public void setPath(String path) {
            this.path = path;
        }
    }

    public class ZipFileEntry extends ZipEntry {
        public ZipFileEntry(String p,File f){
            path=p;
            file=f;
        }

        File file=null;

        public File getF() {
            return file;
        }
        public void setF(File f) {
            this.file = f;
        }
    }


    public class ZipStreamEntry extends ZipEntry {
        public ZipStreamEntry(String p,InputStream _is){
            path=p;
            is=_is;
        }

        private InputStream is=null;

        public InputStream getInputStream() {
            return is;
        }
        public void setInputStream(InputStream _is) {
            this.is = _is;
        }
    }
}


