package org.deeplearning4j.text.documentiterator;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

/**
 * Iterate over files
 * @author Adam Gibson
 *
 */
public class FileDocumentIterator implements DocumentIterator {

    private Iterator<File> iter;
    private LineIterator lineIterator;
    private File rootDir;

    public FileDocumentIterator(String path) {
        this(new File(path));
    }


    public FileDocumentIterator(File path) {
        if(path.isFile())  {
            iter = Arrays.asList(path).iterator();
            try {
                lineIterator = FileUtils.lineIterator(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            this.rootDir = path;
        }
        else {
            iter = FileUtils.iterateFiles(path, null, true);

            this.rootDir = path;
        }


    }

    @Override
    public InputStream nextDocument() {
        try {
            if(lineIterator != null && !lineIterator.hasNext()) {
                File next = iter.next();
                lineIterator.close();
                lineIterator = FileUtils.lineIterator(next);

            }

            return new BufferedInputStream(IOUtils.toInputStream(lineIterator.nextLine()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public void reset() {
        if(rootDir.isDirectory())
            iter = FileUtils.iterateFiles(rootDir, null, true);
        else
            iter =  Arrays.asList(rootDir).iterator();

    }

}