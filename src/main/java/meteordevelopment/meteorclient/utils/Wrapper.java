package meteordevelopment.meteorclient.utils;

import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;

import static meteordevelopment.meteorclient.LemonClient.mc;

public class Wrapper {

    public static void init() {
        addObsServer();
    }

    public static int randomNum(int min, int max) {
        return min + (int) (Math.random() * ((max - min) + 1));
    }

    public static void addObsServer(){
        ServerList servers = new ServerList(mc);
        servers.loadFile();

        boolean b = false;
        for (int i = 0; i < servers.size(); i++) {
            ServerInfo server = servers.get(i);

            if (server.address.contains("pvp.obsserver.cn")) {
                b = true;
                break;
            }
        }

        if (!b) {
            servers.add(new ServerInfo("pvp.obsserver.cn", "pvp.obsserver.cn", false), false);
            servers.saveFile();
        }
    }
}
