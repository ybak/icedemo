package com.buy360.isaac.icedemo;

import java.util.Scanner;

import Demo.HelloPrx;
import Demo.HelloPrxHelper;
import Ice.ObjectPrx;

public class Client extends Ice.Application {
    class ShutdownHook extends Thread {
        public void run() {
            try {
                communicator().destroy();
            } catch (Ice.LocalException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Client app = new Client();
        int status = app.main("Client", args, "config.client");
        System.exit(status);
    }

    @Override
    public int run(String[] args) {
        setInterruptHook(new ShutdownHook());

        HelloPrx hello = getHelloProxy();
        while (true) {
            sayHello(hello);
        }
    }

    private void sayHello(HelloPrx hello) {
        System.out.println("what's your name?");
        String user = new Scanner(System.in).nextLine();
        System.out.println(hello.sayHello(user));
    }

    private HelloPrx getHelloProxy() {
        HelloPrx hello = null;
        try {
            ObjectPrx stringToProxy = communicator().stringToProxy("hello");
            hello = HelloPrxHelper.checkedCast(stringToProxy);
        } catch (Exception ex) {
            IceGrid.QueryPrx query = IceGrid.QueryPrxHelper.checkedCast(communicator().stringToProxy(
                    "DemoIceGrid/Query"));
            hello = HelloPrxHelper.checkedCast(query.findObjectByType("::Demo::Hello"));
        }
        if (hello == null) {
            throw new RuntimeException("couldn't find a '::Demo::Hello' object");
        }
        return hello;
    }
}
