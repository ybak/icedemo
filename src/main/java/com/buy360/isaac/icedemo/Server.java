package com.buy360.isaac.icedemo;

import Ice.Identity;

public class Server extends Ice.Application {
    public int run(String[] args) {
        if (args.length > 0) {
            System.err.println(appName() + ": too many arguments");
            return 1;
        }

        Ice.ObjectAdapter adapter = communicator().createObjectAdapterWithEndpoints("Hello",
                "default -p " + Config.SERVER_PORT);
        adapter.add(new HelloI(), new Identity("Hello", null));
        adapter.activate();
        communicator().waitForShutdown();
        return 0;
    }

    static public void main(String[] args) {
        Server app = new Server();
        int status = app.main("Server", args);
        System.exit(status);
    }
}
