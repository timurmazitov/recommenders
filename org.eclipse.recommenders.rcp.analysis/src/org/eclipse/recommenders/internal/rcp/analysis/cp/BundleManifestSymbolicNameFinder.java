package org.eclipse.recommenders.internal.rcp.analysis.cp;

import static org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.eclipse.osgi.util.ManifestElement;
import org.eclipse.recommenders.commons.utils.IOUtils;
import org.osgi.framework.BundleException;

import com.google.common.collect.Maps;

public class BundleManifestSymbolicNameFinder implements INameFinder {

    private static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";
    private static final String UNKNOWN = "unknown";

    @Override
    public String find(final File file) throws IOException {
        final JarFile jarFile = new JarFile(file);
        final ZipEntry manifestEntry = jarFile.getEntry(MANIFEST_PATH);
        if (manifestEntry == null) {
            return UNKNOWN;
        }

        try {
            final Map<String, String> headers = parseManifestHeaders(jarFile, manifestEntry);
            if (!headers.containsKey(BUNDLE_SYMBOLICNAME)) {
                return UNKNOWN;
            }
            final String value = headers.get(BUNDLE_SYMBOLICNAME);
            final ManifestElement manifestElement = ManifestElement.parseHeader(BUNDLE_SYMBOLICNAME, value)[0];
            return manifestElement.getValue();
        } catch (final BundleException e) {
            return UNKNOWN;
        }
    }

    private Map<String, String> parseManifestHeaders(final JarFile jarFile, final ZipEntry manifestEntry) {
        final Map<String, String> headers = Maps.newHashMap();
        InputStream is = null;
        try {
            is = jarFile.getInputStream(manifestEntry);
            ManifestElement.parseBundleManifest(is, headers);
        } catch (final Exception e) {
            // what should to with this exception? At least print it to
            // console...
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(is);
        }
        return headers;
    }

}
