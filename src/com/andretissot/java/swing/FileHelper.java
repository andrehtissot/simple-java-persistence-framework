package com.andretissot.java.swing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.filechooser.FileFilter;

/**
 * @author André Augusto Tissot
 */
public class FileHelper {
    public static class ExtensionFileFilter extends FileFilter {
        String description;
        String extensions[];

        public ExtensionFileFilter(String description, String extension) {
            this(description, new String[]{extension});
        }

        public ExtensionFileFilter(String description, String extensions[]) {
            if (description == null) {
                this.description = extensions[0];
            } else {
                this.description = description;
            }
            this.extensions = (String[]) extensions.clone();
            toLower(this.extensions);
        }

        private void toLower(String array[]) {
            for (int i = 0, n = array.length; i < n; i++) {
                array[i] = array[i].toLowerCase();
            }
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            } else {
                String path = file.getAbsolutePath().toLowerCase();
                for (int i = 0, n = extensions.length; i < n; i++) {
                    String extension = extensions[i];
                    if ((path.endsWith(extension) && (path.charAt(path.length()
                            - extension.length() - 1)) == '.')) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public static String generateFilePath(String dir, String fileName) {
        return (new java.io.File("").getAbsolutePath()) + File.separator
                + "files" + File.separator + dir + File.separator + fileName;
    }

    public static void copy(String fromFileName, String toFileName)
            throws IOException {
        File fromFile = new File(fromFileName);
        File toFile = new File(toFileName);

        if (!fromFile.exists()) {
            throw new IOException("File " + fromFileName + " doesn't exist");
        }
        if (!fromFile.isFile()) {
            throw new IOException("FileCopy: " + "can't copy directory: "
                    + fromFileName);
        }
        if (!fromFile.canRead()) {
            throw new IOException("File " + fromFileName + " is unreadable");
        }

        if (toFile.isDirectory()) {
            toFile = new File(toFile, fromFile.getName());
        }

        if (toFile.exists()) {
            if (!toFile.canWrite()) {
                throw new IOException("File " + toFileName +
                        " is unwriteable");
            }
        } else {
            String parent = toFile.getParent();
            if (parent == null) {
                parent = System.getProperty("user.dir");
            }
            File dir = new File(parent);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            if (dir.isFile()) {
                throw new IOException("FileCopy: "
                        + "destination is not a directory: " + parent);
            }
            if (!dir.canWrite()) {
                throw new IOException("FileCopy: "
                        + "destination directory is unwriteable: " + parent);
            }
        }

        FileInputStream from = null;
        FileOutputStream to = null;
        try {
            from = new FileInputStream(fromFile);
            to = new FileOutputStream(toFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = from.read(buffer)) != -1)
                to.write(buffer, 0, bytesRead); // write
        } finally {
            if (from != null) {
                try {
                    from.close();
                } catch (IOException e) {}
            }
            if (to != null) {
                try {
                    to.close();
                } catch (IOException e) {}
            }
        }
    }
}