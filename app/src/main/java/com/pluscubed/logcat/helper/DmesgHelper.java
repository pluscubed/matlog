package com.pluscubed.logcat.helper;

import com.pluscubed.logcat.util.UtilLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DmesgHelper {

    private static UtilLogger log = new UtilLogger(DmesgHelper.class);

    private static List<String> getDmesgArgs() {
        List<String> args = new ArrayList<String>(Arrays.asList("dmesg"));

        return args;
    }

    public static List<CharSequence> getDmsg() {
        Process dmesgProcess = null;
        BufferedReader reader = null;
        List<CharSequence> lines = new ArrayList<CharSequence>();
        try {

            List<String> args = getDmesgArgs();

            dmesgProcess = RuntimeHelper.exec(args);
            reader = new BufferedReader(new InputStreamReader(dmesgProcess
                    .getInputStream()), 8192);

            String line = null;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            log.e(e, "unexpected exception");
        } finally {
            if (dmesgProcess != null) {
                RuntimeHelper.destroy(dmesgProcess);
                log.d("destroyed 1 dump logcat process");
            }
            // post-jellybean, we just kill the process, so there's no need
            // to close the bufferedReader.  Anyway, it just hangs.
            if (VersionHelper.getVersionSdkIntCompat() < VersionHelper.VERSION_JELLYBEAN
                    && reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.e(e, "unexpected exception");
                }
            }
        }

        return lines;
    }
}
