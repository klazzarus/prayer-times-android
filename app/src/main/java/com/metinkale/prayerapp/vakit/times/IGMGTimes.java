package com.metinkale.prayerapp.vakit.times;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

public class IGMGTimes extends WebTimes {

    IGMGTimes(long id) {
        super(id);
        fixIGMG();
    }

    @Override
    public Source getSource() {
        return Source.IGMG;
    }

    @Override
    protected boolean syncTimes() throws Exception {
        String path = getId().replace("nix", "-1");
        String a[] = path.split("_");
        int world = Integer.parseInt(a[1]);
        int germany = Integer.parseInt(a[2]);

        Calendar cal = Calendar.getInstance();
        int rY = cal.get(Calendar.YEAR);
        int Y = rY;
        int m = cal.get(Calendar.MONTH) + 1;

        for (int M = m; M <= m + 1 && rY == Y; M++) {
            if (M == 13) {
                M = 1;
                Y++;
            }
            String url = "http://www.igmg.org/index.php?id=201&no_cache=1"
                    + "&deutschland=" + (germany == -1 ? "nix" : germany)
                    + "&welt=" + (world == -1 ? "nix" : world)
                    + "&ganzermonat=" + M;

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");

            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            while ((line = reader.readLine()) != null)
                if (line.contains("<td class=\"green\"")) {
                    line = extractLine(line);
                    int d = Integer.parseInt(line.substring(0, line.indexOf(".")));
                    String times[] = new String[6];
                    for (int i = 0; i < 6; i++) {
                        line = extractLine(reader.readLine());
                        line = line.replace("&nbsp;", "").replace(".", ":").replace(",", ":");
                        if (line.length() == 4) line = "0" + line;
                        times[i] = line;
                    }

                    setTimes(d, M, Y, times);

                }

            reader.close();
        }
        return true;
    }


    private void fixIGMG() {
        String oid = getId();
        if (oid.split("_").length == 4) return;
        try {
            List<Cities.Item> resp = Cities.search2(0, 0, null, null, getName());
            if (resp != null)
                for (Cities.Item i : resp) {
                    if (i.source == Source.IGMG) {
                        setId(i.id);
                        if (!i.id.startsWith(oid))
                            clearTimes();
                        return;
                    }
                }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}