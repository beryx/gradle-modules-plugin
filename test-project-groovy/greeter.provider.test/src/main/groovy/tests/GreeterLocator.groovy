package tests

import examples.greeter.api.Greeter 

import java.util.ServiceLoader

class GreeterLocator {
    Greeter findGreeter() {
        ServiceLoader.load(Greeter).findFirst().orElseThrow { new RuntimeException('No Greeter found') }
    }
}
