package examples

import examples.greeter.api.Greeter

import java.util.ServiceLoader

class Runner {
    static void main(String[] args) {
        Greeter greeter = ServiceLoader.load(Greeter.class).findFirst().orElseThrow { new RuntimeException('No Greeter found!') }
        println(greeter.hello())

        def resource = Runner.class.getResourceAsStream('/resourcetest.txt')
        if(!resource) {
            throw new RuntimeException("Couldn't load resource")
        }

        ModuleLayer.boot().modules().stream()
                .map{ it.name }
                .filter { it == 'java.sql' }
                .findAny().orElseThrow { new RuntimeException('Expected module java.sql not found') }
    }
}
